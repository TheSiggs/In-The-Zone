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
import java.util.List;
import java.util.Random;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Window;

/**
 *
 * @author thiago
 */
public class Main implements Application {

    private StatusBar status = new StatusBar(600);
    private Board board = new Board(status, 600, 400);
    private Menu menu = new Menu(board);
    private Window window;

    @Override
    public void startup(Display dspl, Map<String, String> map) throws Exception {
        window = new Window();
        window.setTitle("N Puzzle Game");
        board.setWindow(window);
        board.refreshCommands();
        menu.addRandomize(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                menu.setItem(0);
                List<Command> cmds = board.getController().randomizeCommands();
                while(!cmds.isEmpty()){
                    board.getController().addCommand(cmds.remove(0));
                }
                board.getMenu().movingStatus();
            }
        });
        menu.addStart(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                board.solve();
            }
        });
        menu.addControllers(new ListButtonSelectionListener() {

            @Override
            public void selectedIndexChanged(ListButton lb, int i) {
                board.setController(lb.getSelectedItem().toString());
            }
        });
        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        boxPane.add(menu);
        boxPane.add(board);
        boxPane.add(status);
        window.getComponentKeyListeners().add(new KeyBoardAdapter(board));
        window.setContent(boxPane);
        window.setWidth(boxPane.getWidth());
        window.setHeight(boxPane.getHeight());
        window.open(dspl);
    }

    @Override
    public boolean shutdown(boolean bln) throws Exception {
        if (window != null) {
            window.close();
        }
        return false;
    }

    @Override
    public void suspend() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resume() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Main.class, args);
    }
}
