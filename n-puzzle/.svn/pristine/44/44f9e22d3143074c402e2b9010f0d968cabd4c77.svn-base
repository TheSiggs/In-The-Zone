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

/**
 *
 * @author thiago
 */
public class Path {

    private ArrayList nodes = new ArrayList();

    public Path() {
    }

    public int getLength() {
        return nodes.size();
    }

    public Node getNode(int index) {
        return (Node) nodes.get(index);
    }

    public void appendStep(Node node) {
        nodes.add(node);
    }

    public void prependStep(Node node) {
        nodes.add(0, node);
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object node : nodes) {
            sb.append(node);
            sb.append("\n");
        }

        return sb.toString();
    }
}
