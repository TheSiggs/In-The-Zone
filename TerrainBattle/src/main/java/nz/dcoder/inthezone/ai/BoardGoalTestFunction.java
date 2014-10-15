/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.search.framework.GoalTest;

/**
 *
 * @author Tim-Hinnerk Heuer
 */
public class BoardGoalTestFunction implements GoalTest {

	@Override
	public boolean isGoalState(Object state) {
		BoardState boardState = (BoardState) state;
		return boardState.position.equals(boardState.end);
	}
	
}
