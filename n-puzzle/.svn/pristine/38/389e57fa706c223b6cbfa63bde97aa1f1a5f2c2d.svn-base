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

import java.util.Set;

/**
 *
 * @author thiago
 */
public abstract class Node implements Comparable<Node> {

    protected float cost;
    protected float heuristic;
    protected int depth;
    protected Node parent;

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public float getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(float heuristic) {
        this.heuristic = heuristic;
    }

    public Node getParent() {
        return parent;
    }

    public int setParent(Node parent) {
        if (parent != null) {
            depth = parent.depth + 1;
            this.parent = parent;
            return depth;
        } else {
            parent = null;
            return -1;
        }
    }

    public abstract Set<Node> getNeighbours();

    public abstract float costDifference(Node other);
}
