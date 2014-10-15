/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.agent.Action;
import aima.core.search.framework.ResultFunction;

/**
 *
 * @author informatics-palmerson
 */
public class BoardResultFunction implements ResultFunction {

	@Override
	public Object result(Object s, Action a) {
		BoardState state = (BoardState) s;
		MoveAction action = (MoveAction) a;
		switch (action.direction) {
			default:
			case NORTH:
				state.position.y++;
				break;
			case EAST:
				state.position.x++;
				break;
			case SOUTH:
				state.position.y--;
				break;
			case WEST:
				state.position.x--;
				break;
		}
		//state.move(action.direction);
		return state;
	}
	
}
