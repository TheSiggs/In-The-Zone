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

package com.googlecode.npuzzle.controller;

import com.googlecode.npuzzle.logic.PuzzleState;

/**
 *
 * @author thiago
 */
public class ControllerFactory {

    public static Controller createController(String name, PuzzleState state) {
        if ("human".equalsIgnoreCase(name)) {
            return new HumanController(state);
        } else if ("a*".equalsIgnoreCase(name)) {
            return new AStarController(state);
        } else if ("genetic".equalsIgnoreCase(name)) {
            return new GAEncogController(state);
        } else if ("annealing".equalsIgnoreCase(name)) {
            return new SimulatedController(state);
        } else {
            throw new IllegalArgumentException("Invalid Controller.");
        }
    }
}
