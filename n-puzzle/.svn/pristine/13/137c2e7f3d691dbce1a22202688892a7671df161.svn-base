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
package com.googlecode.npuzzle.logic.genetic;

import com.googlecode.npuzzle.logic.Command;
import com.googlecode.npuzzle.controller.Controller;
import com.googlecode.npuzzle.controller.GAEncogController;
import com.googlecode.npuzzle.controller.HumanController;
import com.googlecode.npuzzle.logic.EigthPuzzleState;
import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.logic.heuristic.ManhattanHeuristic;
import com.googlecode.npuzzle.logic.heuristic.Heuristic;
import java.util.Random;
import org.encog.solve.genetic.Chromosome;

/**
 *
 * @author thiago
 */
public class PuzzleChromosome extends Chromosome<Integer> {

    private final PuzzleState state;
    private final Random random = new Random();
    private Heuristic heuristicCost;
    private PuzzleState idealState;

    public PuzzleChromosome(final PuzzleAlgorithm ga, PuzzleState state) {
        heuristicCost = new ManhattanHeuristic();
        idealState = new EigthPuzzleState();
        setGeneticAlgorithm(ga);
        this.state = state.copyState();
        Integer[] gene = new Integer[GAEncogController.GENE_SIZE];
        for (int i = 0; i < gene.length; i++) {
            gene[i] = random.nextInt(4);
        }
        setGenes(gene);
    }

    @Override
    public void mutate() {
        Integer[] rep = getGenes();
        int size = (int) (rep.length * 0.2);
        int rand = random.nextInt(rep.length - size);
        for (int i = 0; i < size; i++) {
            rep[rand + i] = random.nextInt(4);
        }
        setGenes(rep);
    }

    @Override
    public void calculateScore() {
        PuzzleState cp = state.copyState();
        Controller controller = new HumanController(cp);
        for (Integer val : getGenes()) {
            controller.addCommand(Command.values()[val]);
        }
        controller.execCommandsBuffer();
        Integer distance = (int) heuristicCost.getCost(cp, idealState);
        setScore(1.0 / (distance + 1));
    }
}
