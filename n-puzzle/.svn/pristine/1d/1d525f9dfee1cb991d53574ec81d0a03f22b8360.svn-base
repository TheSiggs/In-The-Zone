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

import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.logic.heuristic.ManhattanHeuristic;
import com.googlecode.npuzzle.logic.heuristic.Heuristic;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author thiago
 */
public class NodeImpl extends Node {

    private Heuristic heuristicCost;
    private PuzzleState state;

    public NodeImpl(PuzzleState state) {
        this.state = state;
        this.heuristicCost = new ManhattanHeuristic();
    }

    public NodeImpl(PuzzleState state, Heuristic heuristicCost) {
        this.state = state;
        this.heuristicCost = heuristicCost;
    }

    @Override
    public Set<Node> getNeighbours() {
        Set<Node> set = new HashSet<Node>();
        int pos = state.emptyBlock();
        PuzzleState aux = state.copyState();
        aux.moveUp(pos);
        addToSet(aux, set);
        aux = state.copyState();
        aux.moveDown(pos);
        addToSet(aux, set);
        aux = state.copyState();
        aux.moveLeft(pos);
        addToSet(aux, set);
        aux = state.copyState();
        aux.moveRight(pos);
        addToSet(aux, set);

        return set;
    }

    private void addToSet(PuzzleState aux, Set<Node> set) {
        if (aux != null) {
            set.add(new NodeImpl(aux));
        }
    }

    @Override
    public int compareTo(Node o) {
        float f = heuristic + cost;
        float of = o.heuristic + o.cost;

        if (f < of) {
            return -1;
        } else if (f > of) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        final NodeImpl other = (NodeImpl) obj;

        return state.equals(other.state);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.state != null ? this.state.hashCode() : 0);
        return hash;
    }

    @Override
    public float costDifference(Node other) {
        return manhattanDistance(state, ((NodeImpl) other).state);
    }

    private Integer manhattanDistance(PuzzleState first, PuzzleState second) {
        return (int) heuristicCost.getCost(first, second);
    }

    @Override
    public String toString() {
        return state.toString();
    }

    public PuzzleState getState() {
        return state.copyState();
    }
}
