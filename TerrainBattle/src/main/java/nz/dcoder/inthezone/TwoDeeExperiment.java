package nz.dcoder.inthezone;

import java.util.Date;
import nz.dcoder.ai.astar.BoardState;
import nz.dcoder.inthezone.data_model.pure.Position;

public class TwoDeeExperiment {

	BoardState boardState = null;

	public void simpleInitApp() {
		boardState = new BoardState().load("board.map");
		Position position = new Position(0, 0);
		
	}

	public static void main(String args[]) {
		TwoDeeExperiment app = new TwoDeeExperiment();
		app.simpleInitApp();
		app.start();
	}

	public void start() {
		float tpf = 0f;
		while (true) {
			long start = new Date().getTime();
			simpleUpdate(tpf);
			long stop = new Date().getTime();
			long timeInMillist = stop - start;
			tpf = timeInMillist / 1000f;
		}
	}

	private int count = 0;
	private void simpleUpdate(float tpf) {
		if (count++ % 1000 == 0) {
			System.out.println("tpf = "+ tpf);
		}
	}
	
}
