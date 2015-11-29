//==========================================================================
//  OUTPUTVECTORINSPECTOR.CC - part of
//
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2015 Andras Varga
  Copyright (C) 2006-2015 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include <cstring>
#include <cmath>
#include "omnetpp/coutvector.h"
#include "qtenv.h"
#include "inspectorfactory.h"
#include "outputvectorinspector.h"

namespace omnetpp {
namespace qtenv {

void _dummy_for_outputvectorinspector() {}

class OutputVectorInspectorFactory : public InspectorFactory
{
  public:
    OutputVectorInspectorFactory(const char *name) : InspectorFactory(name) {}

    bool supportsObject(cObject *obj) override { return dynamic_cast<cOutVector *>(obj) != nullptr; }
    int getInspectorType() override { return INSP_GRAPHICAL; }
    double getQualityAsDefault(cObject *object) override { return 3.0; }
    Inspector *createInspector(QWidget *parent, bool isTopLevel) override { return new OutputVectorInspector(parent, isTopLevel, this); }
};

Register_InspectorFactory(OutputVectorInspectorFactory);

CircBuffer::CircBuffer(int size)
{
    siz = size;
    buf = new CBEntry[siz];
    n = 0;
    head = 0;
}

CircBuffer::~CircBuffer()
{
    delete[] buf;
}

void CircBuffer::add(simtime_t t, double value1, double value2)
{
    head = (head+1)%siz;
    CBEntry& p = buf[head];
    p.t = t;
    p.value1 = value1;
    p.value2 = value2;
    if (n < siz)
        n++;
}

static void record_in_insp(void *data, simtime_t t, double val1, double val2)
{
    OutputVectorInspector *insp = (OutputVectorInspector *)data;
    insp->circbuf.add(t, val1, val2);
}

OutputVectorInspector::OutputVectorInspector(QWidget *parent, bool isTopLevel, InspectorFactory *f) : Inspector(parent, isTopLevel, f), circbuf(100)
{
    autoscale = true;
    drawingMode = DRAW_LINES;
    miny = 0;
    maxy = 10;
    timeScale = 1;  // x scaling
    hairlineTime = 0;
}

OutputVectorInspector::~OutputVectorInspector()
{
    // cancel installed callback in inspected outvector
    cOutVector *ov = static_cast<cOutVector *>(object);
    ov->setCallback(nullptr, nullptr);
}

void OutputVectorInspector::doSetObject(cObject *obj)
{
    if (object) {
        cOutVector *ov = static_cast<cOutVector *>(object);
        ov->setCallback(nullptr, nullptr);
    }

    Inspector::doSetObject(obj);

    // make inspected output vector to call us back when it gets data to write out
    cOutVector *ov = static_cast<cOutVector *>(object);
    ov->setCallback(record_in_insp, (void *)this);
}

void OutputVectorInspector::refresh()
{
    Inspector::refresh();

    if (!object) {
        //TCLKILL CHK(Tcl_VarEval(interp, canvas, " delete all", TCL_NULL));
        //TODO setLabel(".bot.info", "");
        return;
    }

    char buf[80];
    generalInfo(buf);
    //TODO setLabel(".bot.info", buf);

    if (circbuf.items() == 0)
        return;



    int canvasWidth = 1;
    int canvasHeight = 1;
    // get canvas size
    /*TCLKILL CHK(Tcl_VarEval(interp, "winfo width ", canvas, TCL_NULL));
    int canvasWidth = atoi(Tcl_GetStringResult(interp));
    if (!canvasWidth)
        canvasWidth = 1;
    CHK(Tcl_VarEval(interp, "winfo height ", canvas, TCL_NULL));
    int canvasHeight = atoi(Tcl_GetStringResult(interp));
    if (!canvasHeight)
        canvasHeight = 1;
*/
    simtime_t tbase = getSimulation()->getSimTime();
    // simtime_t tbase = circbuf.entry[circbuf.headPos()].t;

    if (autoscale) {
        // adjust time_factor if it's too far from the optimal value
        simtime_t firstt = circbuf.entry(circbuf.tailPos()).t;
        double dt = SIMTIME_DBL(tbase - firstt);
        if (dt > 0) {
            double opt_tf = dt/canvasWidth;

            // some rounding: keep 2 significant digits
            double order = pow(10.0, (int)floor(log10(opt_tf)));
            opt_tf = floor(opt_tf/order+.5)*order;

            // adjust only if it differs >=20% from its optimal value
            if (fabs(timeScale-opt_tf) > opt_tf*0.20)
                timeScale = opt_tf;
        }

        // determine miny and maxy
        int pos = circbuf.headPos();
        miny = maxy = circbuf.entry(circbuf.headPos()).value1;
        for (int i = 0; i < circbuf.items(); i++) {
            CircBuffer::CBEntry& p = circbuf.entry(pos);
            if (p.value1 < miny)
                miny = p.value1;
            if (p.value1 > maxy)
                maxy = p.value1;
            pos = (pos-1+circbuf.size())%circbuf.size();
        }
        if (miny == maxy) {
            if (miny != 0) {
                miny -= fabs(miny/3);
                maxy += fabs(maxy/3);
            }
            else {
                miny = -0.5;
                maxy = 0.5;
            }
        }
    }
    double rangey = maxy-miny;

    simtime_t tf = timeScale;

    // temporarily define X() and Y() coordinate translation macros
#define X(t)    (int)(canvasWidth-10-(tbase-(t))/tf)
#define Y(y)    (int)(canvasHeight-20-((y)-miny)*((long)canvasHeight-30)/rangey)

    // printf("cw=%d  ch=%d  tbase=%f trange=%f  miny=%f  maxy=%f rangey=%f\n",
    //        canvaswidth, canvasheight,tbase,trange, miny, maxy, rangey);

    // delete previous drawing
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " delete all", TCL_NULL));

    // now scan through the whole buffer in time-increasing order
    // and draw in the meanwhile

    int y_zero = Y(0);  // so that we don't have to calculate it each time
    int next_x = -1;
    int next_y2 = 0;  // values won't be used (they're there just to prevent warning)
    int next_y1 = next_y2;  // next_y2 is just used to prevent unused variable warning.
    int x, y1;
    int pos;
    int nextPos = (circbuf.headPos()-circbuf.items()+circbuf.size())%circbuf.size();
    for (int i = 0; i <= circbuf.items(); i++) {
        x = next_x;
        y1 = next_y1;
        pos = nextPos;

        if (i == circbuf.items()) {
            next_x = X(getSimulation()->getSimTime());
        }
        else {
            nextPos = (nextPos+1)%circbuf.size();
            CircBuffer::CBEntry& p = circbuf.entry(nextPos);
            next_x = X(p.t);
            next_y1 = Y(p.value1);
            next_y2 = Y(p.value2);
        }

        if (x >= 0) {
            char tag[16];
            sprintf(tag, "value%d", pos);

            const int d = 2;
            char coords[64];
            switch (drawingMode) {
                case DRAW_DOTS:
                    // draw rectangle 1
                    sprintf(coords, "%d %d %d %d", x-d, y1-d, x+d, y1+d);
                    /*TCLKILL CHK(Tcl_VarEval(interp, canvas, " create rect ", coords,
                                " -tag ", tag, " -outline red -fill red", TCL_NULL));*/
                    break;

                case DRAW_PINS:
                    // draw rectangle 1
                    sprintf(coords, "%d %d %d %d", x, y_zero, x, y1);
                    /*TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords,
                                " -tag ", tag, " -fill red", TCL_NULL));*/
                    break;

                case DRAW_LINES:
                    // draw rectangle 1
                    sprintf(coords, "%d %d %d %d", x, y1, next_x, next_y1);
                    /*TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords,
                                " -tag ", tag, " -fill red", TCL_NULL));*/
                    break;

                case DRAW_SAMPLEHOLD:
                    // draw rectangle 1
                    sprintf(coords, "%d %d %d %d", x, y1, next_x, y1);
                    /*TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords,
                                " -tag ", tag, " -fill red", TCL_NULL));*/
                    break;

                case DRAW_BARS:
                    // draw rectangle 1
                    sprintf(coords, "%d %d %d %d", x, y_zero, next_x, y1);
                    /*TCLKILL CHK(Tcl_VarEval(interp, canvas, " create rect ", coords,
                                " -tag ", tag, " -outline red -fill red", TCL_NULL));*/
                    break;
            }
        }
    }

    simtime_t tmin = tbase - tf * (canvasWidth-20);

    if (hairlineTime < tmin)
        hairlineTime = tbase;

    double midy = (miny+maxy)/2;

    // add some basic labeling
    char coords[64], value[64];
    sprintf(coords, "%d %d %d %d", X(tmin)-2, Y(miny), X(tbase)+2, Y(miny));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d %d %d", X(tmin)-2, Y(maxy), X(tbase)+2, Y(maxy));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d %d %d", X(tbase)-2, Y(midy), X(tbase)+2, Y(midy));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d %d %d", X(tbase), Y(maxy)-2, X(tbase), Y(miny)+2);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d %d %d", X(tmin), Y(maxy)-2, X(tmin), Y(miny)+2);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d %d %d", X(hairlineTime), Y(maxy)-2, X(hairlineTime), Y(miny)+2);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create line ", coords, " -fill black", TCL_NULL));

    sprintf(coords, "%d %d", X(tbase)-3, Y(miny));
    sprintf(value, "%.9g", miny);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor se", TCL_NULL));

    sprintf(coords, "%d %d", X(tbase)-3, Y(maxy));
    sprintf(value, "%.9g", maxy);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor ne", TCL_NULL));

    sprintf(coords, "%d %d", X(tbase)-3, Y(midy));
    sprintf(value, "%.9g", midy);
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor e", TCL_NULL));

    sprintf(coords, "%d %d", X(tbase)+3, Y(miny));
    sprintf(value, "%s", SIMTIME_STR(tbase));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor ne", TCL_NULL));

    sprintf(coords, "%d %d", X(tmin)-3, Y(miny));
    sprintf(value, "%s", SIMTIME_STR(tmin));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor nw", TCL_NULL));

    sprintf(coords, "%d %d", X(hairlineTime)-3, Y(miny));
    sprintf(value, "%s", SIMTIME_STR(hairlineTime));
    //TCLKILL CHK(Tcl_VarEval(interp, canvas, " create text ", coords, " -text ", value, " -anchor n", TCL_NULL));

#undef X
#undef Y
}

static const char *drawingmodes[] = {
    "dots", "bars", "pins", "sample-hold", "lines", nullptr
};

void OutputVectorInspector::generalInfo(char *buf)
{
    if (circbuf.items() == 0) {
        strcpy(buf, "(no write since opening window)");
        return;
    }

    CircBuffer::CBEntry& p = circbuf.entry(circbuf.headPos());
    sprintf(buf, "Last value: t=%s  value=%g", SIMTIME_STR(p.t), p.value1);
}

void OutputVectorInspector::valueInfo(char *buf, int valueindex)
{
    CircBuffer::CBEntry& p = circbuf.entry(valueindex);
    sprintf(buf, "t=%s  value=%g", SIMTIME_STR(p.t), p.value1);

    hairlineTime = SIMTIME_DBL(p.t);
}

void OutputVectorInspector::getConfig(char *buf)
{
    sprintf(buf, "%d %g %g %g %s", autoscale, timeScale, miny, maxy, drawingmodes[drawingMode]);
}

void OutputVectorInspector::setConfig(bool autosc, double timefac, double min_y, double max_y, const char *mode)
{
    // store new parameters
    autoscale = autosc;
    timeScale = timefac;
    miny = min_y;
    maxy = max_y;

    // corrections on y range
    if (miny > maxy)
        maxy = miny;
    if (miny == maxy) {
        if (miny != 0) {
            miny -= fabs(miny/3);
            maxy += fabs(maxy/3);
        }
        else {
            miny = -0.5;
            maxy = 0.5;
        }
    }

    // identify drawing style
    int i;
    for (i = 0; i < NUM_DRAWINGMODES; i++)
        if (0 == strcmp(mode, drawingmodes[i]))
            break;

    if (i != NUM_DRAWINGMODES)
        drawingMode = i;
}

} // namespace qtenv
} // namespace omnetpp

