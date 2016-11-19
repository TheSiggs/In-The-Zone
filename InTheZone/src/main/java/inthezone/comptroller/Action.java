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

	public final Optional<List<MapPoint>> completion;

	// the subject of move range and targeting information requests
	public final Character subject;
	public final MapPoint castFrom;
	public final Ability ability;
	public final MapPoint target;
	public final int range;
	public final Optional<CompletableFuture<Collection<MapPoint>>> moveRange;
	public final Optional<CompletableFuture<Collection<MapPoint>>> targeting;
	public final Optional<CompletableFuture<List<MapPoint>>> path;
	public final Optional<CompletableFuture<Collection<MapPoint>>> attackArea;
	public final Optional<CompletableFuture<Collection<MapPoint>>> teleportRange;
	public final Optional<CompletableFuture<Collection<MapPoint>>> itemTargeting;

	public Action(List<MapPoint> completion) {
		this.crq = Optional.empty();
		this.completion = Optional.of(completion);
		this.subject = null;
		this.castFrom = null;
		this.ability = null;
		this.target = null;
		this.range = 0;
		this.moveRange = Optional.empty();
		this.targeting = Optional.empty();
		this.path = Optional.empty();
		this.attackArea = Optional.empty();
		this.teleportRange = Optional.empty();
		this.itemTargeting = Optional.empty();
	}

	public Action(CommandRequest crq) {
		this.crq = Optional.of(crq);
		this.completion = Optional.empty();
		this.subject = null;
		this.castFrom = null;
		this.ability = null;
		this.target = null;
		this.range = 0;
		this.moveRange = Optional.empty();
		this.targeting = Optional.empty();
		this.path = Optional.empty();
		this.attackArea = Optional.empty();
		this.teleportRange = Optional.empty();
		this.itemTargeting = Optional.empty();
	}

	public static Action moveRange(
		Character subject,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, null, null, null, 0, r, null, null, null, null, null);
	}

	public static Action teleportRange(
		Character subject, int range,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, null, null, null, range, null, null, null, null, r, null);
	}

	public static Action path(
		Character subject,
		MapPoint target,
		CompletableFuture<List<MapPoint>> r
	) {
		return new Action(subject, null, null, target, 0, null, null, r, null, null, null);
	}

	public static Action targeting(
		Character subject,
		MapPoint castFrom,
		Ability a,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, castFrom, a, null, 0, null, r, null, null, null, null);
	}

	public static Action itemTargeting(
		Character subject,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, null, null, null, 0, null, null, null, null, null, r);
	}

	public static Action attackArea(
		Character subject,
		MapPoint castFrom,
		MapPoint target,
		Ability a,
		CompletableFuture<Collection<MapPoint>> r
	) {
		return new Action(subject, castFrom, a, target, 0, null, null, null, r, null, null);
	}

	private Action(
		Character subject,
		MapPoint castFrom,
		Ability ability,
		MapPoint target,
		int range,
		CompletableFuture<Collection<MapPoint>> moveRange,
		CompletableFuture<Collection<MapPoint>> targeting,
		CompletableFuture<List<MapPoint>> path,
		CompletableFuture<Collection<MapPoint>> attackArea,
		CompletableFuture<Collection<MapPoint>> teleportRange,
		CompletableFuture<Collection<MapPoint>> itemTargeting
	) {
		this.crq = Optional.empty();
		this.completion = Optional.empty();
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
		this.target = target;
		this.range = range;
		this.moveRange = Optional.ofNullable(moveRange);
		this.targeting = Optional.ofNullable(targeting);
		this.path = Optional.ofNullable(path);
		this.attackArea = Optional.ofNullable(attackArea);
		this.teleportRange = Optional.ofNullable(teleportRange);
		this.itemTargeting = Optional.ofNullable(itemTargeting);
	}

	public void cancel() {
		moveRange.ifPresent(f -> f.cancel(true));
		targeting.ifPresent(f -> f.cancel(true));
		path.ifPresent(f -> f.cancel(true));
		attackArea.ifPresent(f -> f.cancel(true));
	}
}

