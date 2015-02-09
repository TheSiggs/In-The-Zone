package nz.dcoder.inthezone.data_model;

import java.util.function.Consumer;

/**
 * The interface through which the model layer communicates to the presentation layer
 * */
public class BattleController {
	// TODO: fill this class in
	public Consumer<Turn> onPlayerTurnStart = null;
	public Consumer<Void> onBattleEnd = null;
	public Consumer<DoMoveInfo> onMove = null;
}

