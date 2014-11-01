/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import aima.core.search.framework.GraphSearch;
import aima.core.search.framework.Problem;
import aima.core.search.framework.SearchAgent;
import aima.core.search.framework.TreeSearch;
import aima.core.search.informed.AStarSearch;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point2i;
import nz.dcoder.inthezone.ai.BoardActions;
import nz.dcoder.inthezone.ai.BoardGoalTestFunction;
import nz.dcoder.inthezone.ai.BoardHeuristicFunction;
import nz.dcoder.inthezone.ai.BoardResultFunction;
import nz.dcoder.inthezone.ai.BoardState;

/**
 *
 * @author informatics-palmerson
 */
public class HighlightedRoute {
	List<Spatial> highlightedSquares = new ArrayList<>();
	public HighlightedRoute(int fromX, int fromY, int toX, int toY) {
		List<Point2i> points = this.getRoute(fromX, fromY, toX, toY);
		
	}

	private List<Point2i> getRoute(int fromX, int fromY, int toX, int toY) {
		List<Point2i> points = new ArrayList<>();
		
		return points;
	}
	public static void main(String args[]) {
		//BoardState boardState = new BoardState(7, 6, 1, 2, 5, 1);
		BoardState boardState = new BoardState(5, 5, 0, 0, 4, 4);
		int x = 1;
		int y = 1;
		//boardState.addObstacle(x, y, 1);
		Problem aStarPath = new Problem(boardState, new BoardActions(), 
				new BoardResultFunction(), new BoardGoalTestFunction());
		GraphSearch graphSearch = new GraphSearch();
		//graphSearch.setCheckGoalBeforeAddingToFrontier(true);
		TreeSearch treeSearch = new TreeSearch();
		
		AStarSearch search = new AStarSearch(treeSearch, 
				new BoardHeuristicFunction());
		SearchAgent agent = null;
		try {
			agent = new SearchAgent(aStarPath, search);
		} catch (Exception ex) {
			Logger.getLogger(HighlightedRoute.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (agent != null) {
			System.out.println("actions: "+ agent.getActions());
		}
	}
}
