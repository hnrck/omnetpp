//
// This file is part of an OMNeT++/OMNEST simulation example.
//
// Copyright (C) 2010 OpenSim Ltd.
//
// This file is distributed WITHOUT ANY WARRANTY. See the file
// `license' for details on this and other legal matters.
//

#include <osg/Node>
#include <osg/PositionAttitudeTransform>
#include <osgEarth/Capabilities>
#include <osgEarthAnnotation/LabelNode>
#include <osgEarthSymbology/Geometry>
#include <osgEarthFeatures/Feature>

#include "MobileNode.h"
#include "OsgEarthScene.h"
#include "ChannelController.h"

USING_NAMESPACE

using namespace osgEarth;
using namespace osgEarth::Annotation;
using namespace osgEarth::Features;

MobileNode::MobileNode()
{
}

MobileNode::~MobileNode()
{
}

void MobileNode::initialize(int stage)
{
    switch (stage) {
    case 0:
        timeStep = par("timeStep");
        trailLength = par("trailLength");
        modelURL = par("modelURL").stringValue();
        showTxRange = par("showTxRange");
        txRange = par("txRange");
        labelColor = par("labelColor").stringValue();
        rangeColor = par("rangeColor").stringValue();
        trailColor = par("trailColor").stringValue();
        break;

    case 1:
        ChannelController::getInstance()->addMobileNode(this);

        auto scene = OsgEarthScene::getInstance()->getScene(); // scene is initialized in stage 0 so we have to do our init in stage 1
        mapNode = osgEarth::MapNode::findMapNode(scene);

        // build up the node representing this module
        // an ObjectLocatorNode allows positioning a model using world coordinates
        locatorNode = new osgEarth::Util::ObjectLocatorNode(mapNode->getMap());
        auto modelNode = osgDB::readNodeFile(modelURL);
        if (!modelNode)
            throw cRuntimeError("Model file \"%s\" not found", modelURL.c_str());

        // disable shader and lighting on the model so textures are correctly shown
        modelNode->getOrCreateStateSet()->setAttributeAndModes(
                            new osg::Program(),
                            osg::StateAttribute::OFF | osg::StateAttribute::OVERRIDE);
        modelNode->getOrCreateStateSet()->setMode(GL_LIGHTING, osg::StateAttribute::OFF);

        auto objectNode = cOsgCanvas::createOmnetppObjectNode(this);  // make the node selectable in QTEnv
        objectNode->addChild(modelNode);
        locatorNode->addChild(objectNode);

        // set the name label if the color is specified
        if (!labelColor.empty()) {
            Style labelStyle;
            labelStyle.getOrCreate<TextSymbol>()->alignment() = TextSymbol::ALIGN_CENTER_TOP;
            labelStyle.getOrCreate<TextSymbol>()->declutter() = true;
            labelStyle.getOrCreate<TextSymbol>()->pixelOffset() = osg::Vec2s(0,50);
            labelStyle.getOrCreate<TextSymbol>()->fill()->color() = osgEarth::Color(labelColor);
            locatorNode->addChild(new LabelNode(getFullName(), labelStyle));
        }

        // create a node showing the transmission range
        if (showTxRange) {
            Style rangeStyle;
            rangeStyle.getOrCreate<PolygonSymbol>()->fill()->color() = osgEarth::Color(rangeColor);
            rangeStyle.getOrCreate<AltitudeSymbol>()->clamping() = AltitudeSymbol::CLAMP_TO_TERRAIN;
            rangeStyle.getOrCreate<AltitudeSymbol>()->technique() = AltitudeSymbol::TECHNIQUE_DRAPE;
            rangeNode = new CircleNode(mapNode.get(), GeoPoint::INVALID, Linear(txRange, Units::METERS), rangeStyle);
            locatorNode->addChild(rangeNode);
        }

        // create a node containing a track showing the past trail of the model
        if (trailLength > 0) {
            trailStyle.getOrCreate<LineSymbol>()->stroke()->color() = osgEarth::Color(trailColor);
            trailStyle.getOrCreate<LineSymbol>()->stroke()->width() = 50.0f;
            trailStyle.getOrCreate<AltitudeSymbol>()->clamping() = AltitudeSymbol::CLAMP_RELATIVE_TO_TERRAIN;
            trailStyle.getOrCreate<AltitudeSymbol>()->technique() = AltitudeSymbol::TECHNIQUE_DRAPE;
            trailNode = new FeatureNode(mapNode.get(), nullptr);
            locatorNode->addChild(trailNode);
        }

        // add the locator node to the scene
        mapNode->getModelLayerGroup()->addChild(locatorNode);

        // position the nodes, so we will see them at correct position right after initialization
        refreshVisuals();

        // schedule first move
        cMessage *timer = new cMessage("move");
        scheduleAt(par("startTime"), timer);
        break;
    }
}

void MobileNode::refreshVisuals()
{
    auto geoSRS = mapNode->getMapSRS()->getGeographicSRS();
    double modelheading = fmod((360 + 90 + heading), 360) - 180;
    double longitude = getLongitude();
    double latitude = getLatitude();

    // update the 3D position of the model node
    locatorNode->getLocator()->setPosition(osg::Vec3d(longitude, latitude, 1.5));  // set altitude mode instead of fixed altitude
    locatorNode->getLocator()->setOrientation(osg::Vec3d(modelheading, 0, 0));

    // re-position the range indicator node
    if (showTxRange)
        rangeNode->setPosition(GeoPoint(geoSRS, longitude, latitude));

    // if we are showing the model's track, update geometry in the trackNode
    if (trailNode) {
        // store the new position to be able to create a track later
        trail.push_back(osg::Vec3d(longitude, latitude, 0));

        // if trail is at max length, remove the oldest point to keep it at "trailLength"
        if (trail.size() > trailLength)
            trail.erase(trail.begin());

        // create and assign a new feature containing the updated geometry
        // representing the movement trail as continuous line segments
        auto trailFeature = new Feature(new LineString(&trail), geoSRS, trailStyle);
        trailFeature->geoInterp() = GEOINTERP_GREAT_CIRCLE;
        trailNode->setFeature(trailFeature);
    }

    // as we have updated our position, update the connection graph too.
    // FIXME make this optional if instance returns NULL
    ChannelController::getInstance()->updateConnectionGraph();

    // update the position on the 2D canvas, too
    getDisplayString().setTagArg("p", 0, x);
    getDisplayString().setTagArg("p", 1, y);
}

void MobileNode::handleMessage(cMessage *msg)
{
    // update the node position
    move();

    // synchronize the OSG node positions to the module position
    refreshVisuals();

    // schedule next movement
    scheduleAt(simTime() + timeStep, msg);
}
