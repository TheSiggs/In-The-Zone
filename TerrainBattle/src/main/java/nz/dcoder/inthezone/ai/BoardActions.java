/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.agent.Action;
import aima.core.search.framework.ActionsFunction;
import java.util.Set;
import java.util.TreeSet;


/**
 *
 * @author informatics-palmerson
 */
public class BoardActions implements ActionsFunction {

	@Override
	public Set<Action> actions(Object s) {
		BoardState state = (BoardState) s;
		Set<Action> actions = new TreeSet<>();
		int x = state.position.x;
		int y = state.position.y;
		int board[][] = state.board;
		
		int newY = y+1;
		if (board[x].length > newY && state.board[x][newY] == 0) {
			actions.add(new MoveAction(MoveAction.Dir.NORTH));
		}
		int newX = x+1;
		if (board.length > newX && board[newX][y] == 0) {
			actions.add(new MoveAction(MoveAction.Dir.EAST));
		}
		newY = y-1;
		if (newY >= 0 && board[x][newY] == 0) {
			actions.add(new MoveAction(MoveAction.Dir.SOUTH));
		}
		newX = x-1;
		if (newX >= 0 && board[newX][y] == 0) {
			actions.add(new MoveAction(MoveAction.Dir.WEST));
		}
		return actions;
	}
	
}
