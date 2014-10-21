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

import com.googlecode.npuzzle.controller.Controller;
import com.googlecode.npuzzle.controller.ControllerFactory;
import com.googlecode.npuzzle.logic.Command;
import com.googlecode.npuzzle.logic.EigthPuzzleState;
import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.npuzzleui.effects.HoaxTransition;
import com.googlecode.npuzzle.npuzzleui.effects.RefreshTransition;
import com.googlecode.npuzzle.npuzzleui.effects.SolveTask;
import java.util.Arrays;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;

/**
 *
 * @author thiago
 */
public class Board extends StackPane {

    private final int height;
    private final int width;
    private final PuzzleState<Integer> puzzleState = new EigthPuzzleState();
    private Controller controller = ControllerFactory.createController("human", puzzleState);
    private final StatusBar statusBar;
    private final Transition refreshTransition = new RefreshTransition(this);
    private Window window;
    private Menu menu;
    private Task solveTask = new SolveTask(this);

    public Board(StatusBar status, int width, int height) {
        this.width = width;
        this.height = height;
        this.statusBar = status;
        controller.randomize();
        drawBoard();
    }

    public void stop() {
        solveTask.abort();
        menu.stopedStatus();
    }

    public void solve() {
        TaskListener<Boolean> taskListener = new TaskListener<Boolean>() {

            @Override
            public void taskExecuted(Task<Boolean> task) {
                menu.movingStatus();
            }

            @Override
            public void executeFailed(Task<Boolean> task) {
                Alert.alert(MessageType.ERROR, "Problem found: " + task.getFault()
                        + "\n" + task.getFault().getMessage(), window);
            }
        };
        menu.solvingStatus();
        solveTask.execute(new TaskAdapter<Boolean>(taskListener));
    }

    public void refreshCommands() {
        if (!refreshTransition.isRunning()) {
            refreshTransition.start();
        }
    }

    public void moveAnimation() {
        PuzzleState<Integer> cp = puzzleState.copyState();
        controller.execCommand();
        Command cmd = cp.commandDiff(puzzleState);
        if (cmd != null) {
            Transition transition = new HoaxTransition(movingLayer(puzzleState), cmd);
            transition.start(new TransitionListener() {

                @Override
                public void transitionCompleted(Transition transition) {
                    drawBoard();
                }
            });
        }
    }

    private void drawBoard() {
        setSize(new Dimensions(width, height));
        Integer[] rep = puzzleState.representation();
        for (int i = 0; i < rep.length; i++) {
            BoardLayer aux = new BoardLayer(this, i, rep[i]);
            add(aux);
        }
        if (getLength() > rep.length) {
            remove(0, rep.length);
        }
    }

    private BoardLayer movingLayer(PuzzleState<Integer> state) {
        int idx = Arrays.asList(state.representation()).indexOf(0);
        return (BoardLayer) get(idx);
    }

    public Controller getController() {
        return controller;
    }

    public void setController(String name) {
        this.controller = ControllerFactory.createController(name, puzzleState);
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }
}
