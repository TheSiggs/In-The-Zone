package nz.dcoder.inthezone;

import java.util.Date;
import nz.dcoder.inthezone.toodee.TwoDeeGame;
import nz.dcoder.ai.astar.BoardState;
import nz.dcoder.inthezone.data_model.pure.Position;

public class TwoDeeExperiment extends TwoDeeGame {

	BoardState boardState = null;

	public static void main(String args[]) {
		TwoDeeExperiment app = new TwoDeeExperiment();
		app.start();
	}

	private int count = 0;
	@Override
	public void simpleUpdate(float tpf) {
		if (count++ % 1000 == 0) {
			System.out.println("tpf = "+ tpf);
		}
	}

	@Override
	public void simpleInit() {
		boardState = new BoardState().load("board.map");
		Position position = new Position(0, 0);
	}
	
}
