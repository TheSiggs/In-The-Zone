/*
 *  Copyright 2010 thiago.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.googlecode.npuzzle.logic.astar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author thiago
 */
public class AStarTree implements PathFinder {

    private List<Node> open = new BinaryList<Node>();
    private List<Node> closed = new ArrayList<Node>();
    private int maxSearchDistance;

    public AStarTree(int maxSearchDistance) {
        this.maxSearchDistance = maxSearchDistance;
    }

    public AStarTree() {
        this(Integer.MAX_VALUE);
    }

    @Override
    public Path findPath(Node source, Node target) {
        source.setCost(0);
        source.setDepth(0);
        closed.clear();
        open.clear();
        open.add(source);
        target.setParent(null);

        int maxDepth = 0;
        while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
            Node current = open.get(0);
            if (current.equals(target)) {
                target.setParent(current.getParent());
                break;
            }
            open.remove(current);
            closed.add(current);

            Set<Node> neighbours = current.getNeighbours();
            for (Node neighbour : neighbours) {
                float nextStepCost = current.getCost() + getMovementCost(current, neighbour);
                if (nextStepCost < neighbour.getCost()) {
                    if (open.contains(neighbour)) {
                        open.remove(neighbour);
                    }
                    if (closed.contains(neighbour)) {
                        closed.remove(neighbour);
                    }
                }

                if (!open.contains(neighbour) && !(closed.contains(neighbour))) {
                    neighbour.setCost(nextStepCost);
                    neighbour.setHeuristic(getHeuristicCost(neighbour, target));
                    maxDepth = Math.max(maxDepth, neighbour.setParent(current));
                    open.add(neighbour);
                }
            }
        }
        if (target.getParent() == null) {
            return new Path();
        }

        Path path = new Path();
        Node aux = target;
        while (aux != source) {
            path.prependStep(aux);
            aux = aux.getParent();
        }
        path.prependStep(aux);
        return path;
    }

    private float getMovementCost(Node source, Node target) {
        return 1.0f;
    }

    private float getHeuristicCost(Node source, Node target) {
        return source.costDifference(target);
    }
}
