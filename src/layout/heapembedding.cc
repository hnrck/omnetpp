//=========================================================================
//  HEAPEMBEDDING.CC - part of
//                  OMNeT++/OMNEST
//           Discrete System Simulation in C++
//
//=========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2006 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include "heapembedding.h"

HeapEmbedding::HeapEmbedding(GraphComponent *graphComponent, double nodeSpacing)
{
    this->graphComponent = graphComponent;
    this->nodeSpacing = nodeSpacing;
}

void HeapEmbedding::embed()
{
    std::vector<Rc> rcs; // rectangles of vertices already positioned
    std::vector<Pt> pts; // candidate points where vertices may be positioned
    pts.push_back(Pt::getZero());

    // delete all vertex positions
    for (int i = 0; i < graphComponent->getVertexCount(); i++)
        graphComponent->getVertex(i)->rc.pt = Pt::getNil();

    // go through vertices in spanning tree order
    for (int i = 0; i < graphComponent->spanningTreeVertices.size(); i++) {
        Vertex *vertex = graphComponent->spanningTreeVertices[i];
        Rs rs = vertex->rc.rs;

        // find the best minimizing the distance cost function
        double bestDistance = POSITIVE_INFINITY;
        Rc bestRc;

        // for all candidate points
        for (int j = 0; j < pts.size(); j++) {
            Pt pt = pts[j];

            // align vertex to candidate points with its various points
            for (int k = 0; k < 4; k++) {
                Rc candidateRc;
                Pt ptCopy(pt);

                switch (k) {
                    case 0:
                        // candidate point is center top
                        candidateRc = Rc(ptCopy.subtract(rs.width / 2, 0, 0), rs);
                        break;
                    case 1:
                        // candidate point is left center
                        candidateRc = Rc(ptCopy.subtract(0, rs.height / 2, 0), rs);
                        break;
                    case 2:
                        // candidate point is right center
                        candidateRc = Rc(ptCopy.subtract(rs.width, rs.height / 2, 0), rs);
                        break;
                    case 3:
                        // candidate point is center bottom
                        candidateRc = Rc(ptCopy.subtract(rs.width / 2, rs.height, 0), rs);
                        break;
                }

                // find an already positioned vertex which would intersect the candidate rectangle
                bool intersects = false;
                for (int l = 0; l < rcs.size(); l++) {
                    Rc rc = rcs[l];
                    if (candidateRc.basePlaneProjectionIntersects(rc, true)) {
                        intersects = true;
                        break;
                    }
                }
                // if found one then this is a wrong candidate
                if (intersects)
                    continue;

                // calculate the distance sum to neighbours already positioned
                double distance = 0;
                for (int k = 0; k < vertex->neighbours.size(); k++) {
                    Vertex *neighbour = vertex->neighbours[k];

                    if (!neighbour->rc.pt.isNil())
                        distance += candidateRc.getCenterCenter().getDistance(neighbour->rc.getCenterCenter());
                }

                // if better the current best
                if (distance < bestDistance) {
                    bestRc = candidateRc;
                    bestDistance = distance;
                }
            }
        }

        // store position and rectangle
        vertex->rc.pt = bestRc.pt;

        // grow rectangle
        bestRc.pt.x -= nodeSpacing;
        bestRc.pt.y -= nodeSpacing;
        bestRc.rs.width += 2 * nodeSpacing;
        bestRc.rs.height += 2 * nodeSpacing;

        // delete candidate points covered by best rc
        for (int j = 0; j < pts.size(); j++) {
            Pt pt = pts[j];

            if (bestRc.basePlaneProjectionContains(pt, true))
                pts.erase(pts.begin() + j--);
        }

        // push new candidates
        pushPtUnlessRcsContains(pts, rcs, bestRc.getCenterTop());
        pushPtUnlessRcsContains(pts, rcs, bestRc.getCenterBottom());
        pushPtUnlessRcsContains(pts, rcs, bestRc.getLeftCenter());
        pushPtUnlessRcsContains(pts, rcs, bestRc.getRightCenter());

        rcs.push_back(bestRc);
    }
}

void HeapEmbedding::pushPtUnlessRcsContains(std::vector<Pt>& pts, std::vector<Rc>& rcs, Pt& pt)
{
    for (int j = 0; j < rcs.size(); j++) {
        Rc rc = rcs[j];

        if (rc.basePlaneProjectionContains(pt, true))
            return;
    }

    pts.push_back(pt);
}
