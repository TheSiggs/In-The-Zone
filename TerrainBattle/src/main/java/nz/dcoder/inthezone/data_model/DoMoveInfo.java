package nz.dcoder.inthezone.data_model;

import java.util.List;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoMoveInfo {
	public final Position start;
	public final List<Position> path;

	/**
	 * @param start The position at which the character to move is currently
	 * standing
	 * @param path The complete path to move along, including the start position
	 * */
	public DoMoveInfo(Position start, List<Position> path) {
		this.start = start;
		this.path = path;
	}
}

