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

import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.PushButton;

/**
 *
 * @author thiago
 */
public class Menu extends FlowPane {

    private PushButton randomize;
    private PushButton start;
    private ListButton controllers;
    private Board board;

    public Menu(Board board) {
        this.board = board;
        this.board.setMenu(this);
        add(new Label("Controller: "));
        controllers = new ListButton();
        controllers.setListData("['Human', 'A*', 'Genetic', 'Annealing']");
        controllers.setSelectedIndex(0);
        add(controllers);
        randomize = new PushButton();
        randomize.setButtonData("Randomize");
        add(randomize);
        start = new PushButton();
        start.setButtonData("Start");
        add(start);
    }

    public void addRandomize(ButtonPressListener buttonPressListener) {
        randomize.getButtonPressListeners().add(buttonPressListener);
    }

    public void addStart(ButtonPressListener buttonPressListener) {
        start.getButtonPressListeners().add(buttonPressListener);
    }

    public void addControllers(ListButtonSelectionListener listButtonSelectionListener) {
        controllers.getListButtonSelectionListeners().add(listButtonSelectionListener);
    }

    public void setItem(Integer index) {
        controllers.setSelectedIndex(index);
    }

    public void solvingStatus() {
        controllers.setEnabled(false);
        start.setEnabled(false);
        randomize.setEnabled(false);
        board.getStatusBar().setStatus(StatusBar.Status.SOLVING);
    }

    public void movingStatus() {
        controllers.setEnabled(false);
        start.setEnabled(false);
        randomize.setEnabled(false);
        board.getStatusBar().setStatus(StatusBar.Status.MOVING);
    }

    public void stopedStatus() {
        controllers.setEnabled(true);
        start.setEnabled(true);
        randomize.setEnabled(true);
        board.getStatusBar().setStatus(StatusBar.Status.STOPED);
        start.requestFocus();
    }
}
