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

package com.googlecode.npuzzle.logic;

/**
 *
 * @author thiago
 */
public interface PuzzleState<E> {

    public void swap(int pos, int newPos);

    public int emptyBlock();

    public void command(Command command);

    public boolean canMove(Command command);

    public boolean isGoalReached();

    public void moveUp(int pos);

    public void moveDown(int pos);

    public void moveLeft(int pos);

    public void moveRight(int pos);

    public Command commandDiff(PuzzleState state);

    public PuzzleState copyState();

    public E[] representation();
}
