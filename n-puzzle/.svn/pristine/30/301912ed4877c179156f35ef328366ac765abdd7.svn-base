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

package com.googlecode.npuzzle.npuzzleui;

import com.googlecode.npuzzle.logic.Command;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

/**
 *
 * @author thiago
 */
public class KeyBoardAdapter implements ComponentKeyListener {

    private Board board;

    public KeyBoardAdapter(Board board) {
        this.board = board;
    }

    @Override
    public boolean keyTyped(Component cmpnt, char character) {
        return true;
    }

    @Override
    public boolean keyPressed(Component cmpnt, int keyCode, KeyLocation kl) {
        switch (keyCode) {
            case Keyboard.KeyCode.UP:
                board.getController().addCommand(Command.DOWN);
                break;
            case Keyboard.KeyCode.DOWN:
                board.getController().addCommand(Command.UP);
                break;
            case Keyboard.KeyCode.LEFT:
                board.getController().addCommand(Command.RIGHT);
                break;
            case Keyboard.KeyCode.RIGHT:
                board.getController().addCommand(Command.LEFT);
                break;
        }
        return true;
    }

    @Override
    public boolean keyReleased(Component cmpnt, int keyCode, KeyLocation kl) {
        return true;
    }
}
