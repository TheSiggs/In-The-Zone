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

package com.googlecode.npuzzle.npuzzleui.effects;

import com.googlecode.npuzzle.npuzzleui.Board;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;

/**
 *
 * @author thiago
 */
public class SolveTask extends Task<Boolean> {

    private final Board board;

    public SolveTask(Board board) {
        this.board = board;
    }

    @Override
    public Boolean execute() throws TaskExecutionException {
        board.getController().startSolving();
        return true;
    }
}
