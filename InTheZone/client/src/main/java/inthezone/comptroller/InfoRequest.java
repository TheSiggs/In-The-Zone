package inthezone.comptroller;

import inthezone.battle.Battle;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

public abstract class InfoRequest<T> extends Action {
	public final CompletableFuture<T> complete;

	protected InfoRequest() {
		super(Optional.empty());
		this.complete = new CompletableFuture<>();
	}

	void cancel() {
		complete.cancel(true);
	}
}

