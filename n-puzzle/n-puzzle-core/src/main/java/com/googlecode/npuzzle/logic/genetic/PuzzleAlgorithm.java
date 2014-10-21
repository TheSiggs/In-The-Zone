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

import com.googlecode.npuzzle.logic.PuzzleState;
import org.encog.neural.NeuralNetworkError;
import org.encog.solve.genetic.GeneticAlgorithm;

/**
 *
 * @author thiago
 */
public class PuzzleAlgorithm extends GeneticAlgorithm<Integer> {

    public PuzzleAlgorithm(final PuzzleState state, final int populationSize,
            final double mutationPercent, final double percentToMate,
            final double matingPopulationPercent, final int cutLength)
            throws NeuralNetworkError {
        setMutationPercent(mutationPercent);
        setMatingPopulation(matingPopulationPercent);
        setPopulationSize(populationSize);
        setPercentToMate(percentToMate);
        setCutLength(cutLength);
        setPreventRepeat(false);

        setChromosomes(new PuzzleChromosome[getPopulationSize()]);
        for (int i = 0; i < getChromosomes().length; i++) {
            final PuzzleChromosome c = new PuzzleChromosome(this, state);
            setChromosome(i, c);
        }
        sortChromosomes();
    }
}
