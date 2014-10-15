/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.search.framework.Node;
import aima.core.search.framework.Problem;
import aima.core.search.framework.QueueSearch;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author informatics-palmerson
 */
public class BoardQueueSearch extends QueueSearch {

	@Override
	public List<Node> getResultingNodesToAddToFrontier(Node nodeToExpand, Problem p) {
		BoardState state = (BoardState) p.getInitialState();
		int x = state.position.x;
		int y = state.position.y;
		List<Node> nodeList = new ArrayList<>(4);
		
		nodeList.add(new Node(state, nodeToExpand, null, y));
		
		
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
