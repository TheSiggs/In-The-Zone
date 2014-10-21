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

import org.apache.pivot.wtk.GridPane;

/**
 *
 * @author thiago
 */
public class BoardLayer extends GridPane {

    private int rows = 3;
    private int columns = 3;
    private final Board board;
    private final boolean target;

    public BoardLayer(Board parent, int point, int value) {
        setColumnCount(columns);
        this.board = parent;
        target = (value == 0) ? true : false;
        drawLayer(point, value);
    }

    private void drawLayer(int point, int value) {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            GridPane.Row row = new GridPane.Row();
            for (int j = 0; j < columns; j++) {
                int h = getHoaxHeight();
                int w = getHoaxWidth();
                if (count == point) {
                    if (value == 0) {
                        row.add(new GridPane.Filler());
                    } else {
                        row.add(new Hoax(w, h, value));
                    }
                } else {
                    row.add(new GridPane.Filler());
                }
                count++;
            }
            getRows().add(row);
        }
    }

    public int getHoaxHeight() {
        return board.getHeight() / rows;
    }

    public int getHoaxWidth() {
        return board.getWidth() / columns;
    }

    public boolean isTargetLayer() {
        return target;
    }

}
