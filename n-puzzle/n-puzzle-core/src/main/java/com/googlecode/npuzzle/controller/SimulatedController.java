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

import com.googlecode.npuzzle.logic.Command;
import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.logic.annealing.PuzzleSimulatedAnnealing;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thiago
 */
public class SimulatedController extends BaseController {

    public static final double START_TEMP = 50.0;
    public static final double STOP_TEMP = 2.0;
    public static final int CYCLES = 10;
    public static final int COMMANDS = 30;
    public static final int MAX_SAME_SOLUTION = 30;
    private boolean solving = false;

    public SimulatedController(PuzzleState initialState) {
        super(initialState);
    }

    @Override
    public void startSolving() {
        solving = true;
        List<Command> commands = simulatedCommands(initialState);
        for (Command command : commands) {
            bufferCommand.add(command);
        }
        solving = false;
    }

    private List<Command> simulatedCommands(PuzzleState initialState) {
        PuzzleSimulatedAnnealing anneal = new PuzzleSimulatedAnnealing(initialState,
                START_TEMP, STOP_TEMP, CYCLES, COMMANDS);

        int sameSolutionCount = 0;
        double lastSolution = Double.MAX_VALUE;

        while (sameSolutionCount < MAX_SAME_SOLUTION) {
            anneal.iteration();

            double thisSolution = anneal.getScore();

            if (Math.abs(lastSolution - thisSolution) < 1.0) {
                sameSolutionCount++;
            } else {
                sameSolutionCount = 0;
            }

            lastSolution = thisSolution;
        }

        return integerToCommand(anneal.getArray());
    }

    private List<Command> integerToCommand(Integer[] vals) {
        List<Command> cmds = new ArrayList<Command>();
        for (Integer val : vals) {
            cmds.add(Command.values()[val]);
        }
        return cmds;
    }
}
