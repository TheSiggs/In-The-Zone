package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import inthezone.battle.commands.CommandRequest;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;

/**
 * A class to encode all the kinds of things the battle controller can do.
 * */
class Action {
	public final Optional<CommandRequest> crq;

	// the subject of move range and targeting information requests
	public final Character subject;
	public final MapPoint castFrom;
	public final Ability ability;
	public final MapPoint target;
	public final Optional<CompletableFuture<Collection<MapPoint>>> moveRange;
	public final Optional<CompletableFuture<Collection<MapPoint>>> targeting;
	public final Optional<CompletableFuture<List<MapPoint>>> path;
	public final Optional<CompletableFuture<Collection<MapPoint>>> attackArea;

	public Action(CommandRequest crq) {
		this.crq = Optional.of(crq);
		this.subject = null;
		this.castFrom = null;
		this.ability = null;
		this.target = null;
		this.moveRange = Optional.empty();
		this.targeting = Optional.empty();
		this.path = Optional.empty();
		this.attackArea = Optional.empty();
	}

	public static Action moveRange(
		Character subject,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, null, null, null, r, null, null, null);
	}

	public static Action path(
		Character subject,
		MapPoint target,
		CompletableFuture<List<MapPoint>> r
	) {
		return new Action(subject, null, null, target, null, null, r, null);
	}

	public static Action targeting(
		Character subject,
		MapPoint castFrom,
		Ability a,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, castFrom, a, null, null, r, null, null);
	}

	public static Action attackArea(
		Character subject,
		MapPoint castFrom,
		MapPoint target,
		Ability a,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, castFrom, a, target, null, null, null, r);
	}

	private Action(
		Character subject,
		MapPoint castFrom,
		Ability ability,
		MapPoint target,
		CompletableFuture<Collection<MapPoint>> moveRange,
		CompletableFuture<Collection<MapPoint>> targeting,
		CompletableFuture<List<MapPoint>> path,
		CompletableFuture<Collection<MapPoint>> attackArea
	) {
		this.crq = Optional.empty();
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
		this.target = target;
		this.moveRange = Optional.ofNullable(moveRange);
		this.targeting = Optional.ofNullable(targeting);
		this.path = Optional.ofNullable(path);
		this.attackArea = Optional.ofNullable(attackArea);
	}

	public void cancel() {
		moveRange.ifPresent(f -> f.cancel(true));
		targeting.ifPresent(f -> f.cancel(true));
		path.ifPresent(f -> f.cancel(true));
		attackArea.ifPresent(f -> f.cancel(true));
	}
}

