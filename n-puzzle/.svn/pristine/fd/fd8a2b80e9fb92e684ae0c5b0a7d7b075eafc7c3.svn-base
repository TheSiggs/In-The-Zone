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

import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;

/**
 *
 * @author thiago
 */
public class StatusBar extends TablePane {

    private Label label = new Label("Stoped");
    private Label commandBuffer = new Label("Command buffer: ");

    public static enum Status {

        SOLVING, MOVING, STOPED
    };
    private Status status;

    public StatusBar(int width) {
        getColumns().add(new Column(width / 2));
        getColumns().add(new Column(width / 2));
        FlowPane flowA = new FlowPane();
        FlowPane flowB = new FlowPane();
        flowA.add(label);
        flowB.add(commandBuffer);
        Row row = new Row();
        row.add(flowA);
        row.add(flowB);
        getRows().add(row);
    }

    public void setBufferSize(int size) {
        commandBuffer.setText("Command buffer: " + size);
    }

    public void setStatus(Status desc) {
        status = desc;
        String text = "Stoped";
        switch (desc) {
            case MOVING:
                text = "Moving";
                break;
            case SOLVING:
                text = "Solving...";
                break;
            case STOPED:
                text = "Stoped";
                break;
        }
        label.setText(text);
    }

    public Status getStatus() {
        return status;
    }
}
