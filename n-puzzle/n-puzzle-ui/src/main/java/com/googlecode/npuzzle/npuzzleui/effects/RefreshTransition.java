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
import com.googlecode.npuzzle.npuzzleui.StatusBar.Status;
import org.apache.pivot.wtk.effects.Transition;

/**
 *
 * @author thiago
 */
public class RefreshTransition extends Transition {

    private final Board board;
    public static final int RATE = 1;
    public static final int DURATION = 1;

    public RefreshTransition(Board board) {
        super(DURATION, RATE, true);
        this.board = board;
    }

    @Override
    protected void update() {
        int bufferSize = board.getController().commandBufferSize();
        if (bufferSize > 0) {
            board.moveAnimation();
            board.repaint();
        } else if (board.getStatusBar().getStatus() == Status.MOVING && bufferSize == 0) {
            board.getMenu().stopedStatus();
        }
        board.getStatusBar().setBufferSize(board.getController().commandBufferSize());
    }
}
