/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.agent.Action;
import aima.core.search.framework.ResultFunction;

/**
 *
 * @author Tim-Hinnerk Heuer
 */
public class BoardResultFunction implements ResultFunction {

	@Override
	public Object result(Object s, Action a) {
		BoardState state = (BoardState) s;
		MoveAction action = (MoveAction) a;
		state.move(action.direction);
		return state;
	}
	
}
