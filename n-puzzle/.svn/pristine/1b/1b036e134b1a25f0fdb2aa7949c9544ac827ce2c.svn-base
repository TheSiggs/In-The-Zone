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

package com.googlecode.npuzzle.logic.heuristic;

import com.googlecode.npuzzle.logic.PuzzleState;

/**
 *
 * @author thiago
 */
public class ManhattanHeuristic implements Heuristic {

    @Override
    public double getCost(PuzzleState first, PuzzleState second) {
        Integer[] aux1 = ((PuzzleState<Integer>) first).representation();
        Integer[] aux2 = ((PuzzleState<Integer>) second).representation();
        Integer result = 0;
        for (int i = 0; i < aux1.length; i++) {
            result += Math.abs(aux1[i] - aux2[i]);
        }
        return result;
    }
}
