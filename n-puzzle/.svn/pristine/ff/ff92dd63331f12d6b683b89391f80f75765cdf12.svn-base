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

package com.googlecode.npuzzle.controller;

import com.googlecode.npuzzle.logic.Command;
import com.googlecode.npuzzle.logic.EigthPuzzleState;
import com.googlecode.npuzzle.logic.PuzzleState;
import com.googlecode.npuzzle.logic.astar.AStarTree;
import com.googlecode.npuzzle.logic.astar.NodeImpl;
import com.googlecode.npuzzle.logic.astar.Path;
import com.googlecode.npuzzle.logic.astar.PathFinder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thiago
 */
public class AStarController extends BaseController {

    private PuzzleState idealState = new EigthPuzzleState();

    public AStarController(PuzzleState initialState) {
        super(initialState);
    }

    public AStarController(PuzzleState initialState, PuzzleState idealState) {
        super(initialState);
        this.idealState = idealState;
    }

    @Override
    public void startSolving() {
        solving = true;
        List<Command> commands = generateCommands(initialState);
        for (Command command : commands) {
            bufferCommand.add(command);
        }
        solving = false;
    }

    private List<Command> generateCommands(PuzzleState initialState) {
        PathFinder aStar = new AStarTree();
        Path path = aStar.findPath(new NodeImpl(initialState),
                new NodeImpl(idealState));
        return pathToCommands(path);
    }

    private List<Command> pathToCommands(Path path) {
        List<Command> commands = new ArrayList<Command>();
        if (path != null) {
            for (int i = 1; i < path.getLength(); i++) {
                PuzzleState stateA = ((NodeImpl) path.getNode(i - 1)).getState();
                PuzzleState stateB = ((NodeImpl) path.getNode(i)).getState();
                commands.add(stateB.commandDiff(stateA));
            }
        }
        return commands;
    }
}
