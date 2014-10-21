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

package com.googlecode.npuzzle.logic.annealing;

import com.googlecode.npuzzle.controller.Controller;
import com.googlecode.npuzzle.controller.HumanController;
import com.googlecode.npuzzle.logic.Command;
import com.googlecode.npuzzle.logic.EigthPuzzleState;
import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.logic.heuristic.Heuristic;
import com.googlecode.npuzzle.logic.heuristic.ManhattanHeuristic;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.encog.solve.anneal.SimulatedAnnealing;

/**
 *
 * @author thiago
 */
public class PuzzleSimulatedAnnealing extends SimulatedAnnealing<Integer> {

    private PuzzleState state;
    private Integer[] commands;
    private int totalCommands;
    private Heuristic heuristicCost;
    private PuzzleState idealState;
    private Random random = new Random();

    public PuzzleSimulatedAnnealing(PuzzleState state, final double startTemp,
            final double stopTemp, final int cycles, final int totalCommands) {
        this.state = state;
        this.heuristicCost = new ManhattanHeuristic();
        this.idealState = new EigthPuzzleState();
        this.totalCommands = totalCommands;
        setStartTemperature(startTemp);
        setStopTemperature(stopTemp);
        setCycles(cycles);
        initCommands();
    }

    private void initCommands() {
        Integer[] cmds = new Integer[totalCommands];
        for (int i = 0; i < cmds.length; i++) {
            cmds[i] = random.nextInt(4);
        }
        putArray(cmds);
    }

    @Override
    public double calculateScore() {
        return distance(state, Arrays.asList(getArrayCopy()));
    }

    private double distance(PuzzleState state, List<Integer> cmds) {
        PuzzleState cp = state.copyState();
        Controller controller = new HumanController(cp);
        for (Integer val : cmds) {
            controller.addCommand(Command.values()[val]);
        }
        controller.execCommandsBuffer();
        Integer distance = (int) heuristicCost.getCost(cp, idealState);
        return distance;
    }

    @Override
    public Integer[] getArray() {
        return commands;
    }

    @Override
    public Integer[] getArrayCopy() {
        Integer result[] = new Integer[this.commands.length];
        System.arraycopy(commands, 0, result, 0, commands.length);
        return result;
    }

    @Override
    public void putArray(Integer[] array) {
        this.commands = array;
    }

    @Override
    public void randomize() {
        int limit = 20;
        final double prob = 0.05;
        for (int i = 0; i < this.getTemperature(); i++) {
            List<Integer> cpc = Arrays.asList(getArrayCopy());
            Collections.shuffle(cpc);

            double d = distance(state, cpc);
            if (Math.random() >= prob && d <= limit) {
                Integer[] aux = new Integer[cpc.size()];
                aux = cpc.toArray(aux);
                putArray(aux);
                limit = (int) d;
            }
        }
    }
}
