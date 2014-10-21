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
import com.googlecode.npuzzle.logic.genetic.PuzzleAlgorithm;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author thiago
 */
public class GAEncogController extends BaseController {

    public static final int GENE_SIZE = 30;
    public static final int POPULATION_SIZE = 1500;
    public static final double MUTATION_PERCENT = 0.08;
    public static final double PERCENT_TO_MATE = 0.25;
    public static final double MATING_POPULATION_PERCENT = 0.8;
    public static final int MAX_SAME_SOLUTION = 25;
    public static final int CUT_LENGTH = GENE_SIZE / 5;

    public GAEncogController(PuzzleState initialState) {
        super(initialState);
    }

    @Override
    public void startSolving() {
        solving = true;
        List<Command> commands = geneticCommands(initialState);
        for (Command command : commands) {
            bufferCommand.add(command);
        }
        solving = false;
    }

    private List<Command> geneticCommands(PuzzleState initialState) {
        PuzzleAlgorithm genetic = new PuzzleAlgorithm(
                initialState,
                POPULATION_SIZE,
                MUTATION_PERCENT,
                PERCENT_TO_MATE,
                MATING_POPULATION_PERCENT,
                CUT_LENGTH);

        int sameSolutionCount = 0;
        double lastSolution = Double.MAX_VALUE;
        while (sameSolutionCount < MAX_SAME_SOLUTION) {
            genetic.iteration();

            double thisSolution = genetic.getChromosome(0).getScore();
            if (Math.abs(lastSolution - thisSolution) < 1.0) {
                sameSolutionCount++;
            } else {
                sameSolutionCount = 0;
            }

            lastSolution = thisSolution;
        }

        Integer[] vals = genetic.getChromosome(0).getGenes();
        return integerToCommand(vals);
    }

    private List<Command> integerToCommand(Integer[] vals) {
        List<Command> cmds = new ArrayList<Command>();
        for (Integer val : vals) {
            cmds.add(Command.values()[val]);
        }
        return cmds;
    }

    List<Integer> randomChromosome() {
        List<Integer> representation = new LinkedList<Integer>();
        Random random = new Random();
        for (int i = 0; i < GENE_SIZE; i++) {
            representation.add(random.nextInt(4));
        }
        return representation;
    }
}
