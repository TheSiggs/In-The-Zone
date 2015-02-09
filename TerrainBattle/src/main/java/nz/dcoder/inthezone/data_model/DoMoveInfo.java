package nz.dcoder.inthezone.data_model;

import java.util.List;
import nz.dcoder.ai.astar.Node;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoMoveInfo {
	public final Position start;
	public final List<Node> path;

	public DoMoveInfo(Position start, List<Node> path) {
		this.start = start;
		this.path = path;
	}
}

