/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.search.framework.HeuristicFunction;
/**
 *
 * @author informatics-palmerson
 */
public class BoardHeuristicFunction implements HeuristicFunction {

	@Override
	public double h(Object boardState) {
		BoardState state = (BoardState) boardState;
		return state.distanceToEndHeuristic();
	}
	
}
