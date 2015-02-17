/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import nz.dcoder.inthezone.data_model.Turn;

/**
 *
 * @author denz
 */
public class Presentation {

	final Main game;

	Presentation(Main game) {
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

		this.game = game;

		// fill battle controller callback slots

			game.getBattleController().onPlayerTurnStart = this::playerTurnStart;

		// init geometry


		// start first turn

	}

	void simpleUpdate(float tpf) {
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

		// handle input
		// update HUD
		// update geometry tree
		// perform next animation step
	}

	/**
	 * callback methods to be called by reference by the battle controller
	 * object.
	 *
	 * Turn onPlayerTurnStart BattleEnd onBattleEnd DoMoveInfo onMove
	 * DoAbilityInfo onAbility DoCharacterDeath onDeath DoObjectDestruction
	 * onDestruction
	 */
	private Turn playerTurnStart(Turn turn) {

		System.out.println(turn.toString());
		
		return turn;
	}
}
