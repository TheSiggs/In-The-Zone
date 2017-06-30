package inthezone.game.battle;

import java.util.Collection;

import inthezone.battle.BattleOutcome;
import inthezone.battle.data.StandardSprites;

public class ReplayHUD extends HUD {
	public ReplayHUD(final StandardSprites sprites) {
		super(sprites);
	}

	@Override public void doEndMode(final BattleOutcome outcome) {
		view.modalDialog.doCancel();
		disableUI.set(true);
	}

	@Override public void doReconnectMode(final boolean thisClientReconnecting) {
		view.modalDialog.doCancel();
		disableUI.set(true);
	}

	@Override public void endReconnectMode() {
		disableUI.set(false);
	}
}

