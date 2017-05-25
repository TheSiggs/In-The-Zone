package inthezone.test;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.RoadBlock;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.StatusEffectFactory;
import inthezone.battle.Trap;
import inthezone.battle.Zone;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandTests {
	final BattleTest bt;

	public CommandTests() throws Exception {
		bt = new BattleTest();
	}

	private static <A> Collection<A> single(A a) {
		final Collection<A> r = new ArrayList<>();
		r.add(a);
		return r;
	}

	@Test
	public void placeTrap() throws Exception {
		final Battle b = bt.simpleBattle();

		final Ability trap1 = getAbility(b, bt.danPos, "Trap1");
		assertNotNull(trap1);

		final MapPoint trapPos = new MapPoint(2, 4);

		final CommandRequest requestTrap = new UseAbilityCommandRequest(
			bt.danPos, AbilityAgentType.CHARACTER, trap1, single(new Casting(bt.danPos, trapPos)));

		final List<ExecutedCommand> rs = doCommand(requestTrap, b);
		assertFalse(rs.isEmpty());

		final Trap trap = b.battleState.getTrapAt(trapPos).get();
		assertNotNull(trap);
		assertEquals(trapPos, trap.getPos());

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		assertNotNull(dan);
		assertEquals(1, dan.getAP());
	}

	public List<ExecutedCommand> doCommand(CommandRequest crq, Battle b) throws Exception {
		return doCommands(crq.makeCommand(b.battleState), b);
	}

	public List<ExecutedCommand> doCommands(List<Command> cmds, Battle b) throws Exception {
		final List<ExecutedCommand> r = new ArrayList<>();

		for (Command cmd : cmds) {
			r.addAll(cmd.doCmdComputingTriggers(b));
		}

		return r;
	}

	public Ability getAbility(Battle b, MapPoint p, String name) {
		return b.battleState.getCharacterAt(p).flatMap(c -> c.abilities.stream()
			.filter(a -> a.info.name.equals(name)).findFirst()).get();
	}

	@Test
	public void placeZone() throws Exception {
		final Battle b = bt.simpleBattle();

		final Ability zone1 = getAbility(b, bt.danPos, "Zone1");
		assertNotNull(zone1);

		final MapPoint zonePos = new MapPoint(4, 4);

		final CommandRequest requestZone = new UseAbilityCommandRequest(
			bt.danPos, AbilityAgentType.CHARACTER, zone1, single(new Casting(bt.danPos, zonePos)));

		final List<ExecutedCommand> rs = doCommand(requestZone, b);
		assertFalse(rs.isEmpty());

		final Zone zone = b.battleState.getZoneAt(zonePos).get();
		final Zone zoneX1 = b.battleState.getZoneAt(zonePos.add(new MapPoint( 0, -1))).get();
		final Zone zoneX2 = b.battleState.getZoneAt(zonePos.add(new MapPoint(-1,  0))).get();
		final Zone zoneX3 = b.battleState.getZoneAt(zonePos.add(new MapPoint( 1,  0))).get();
		final Zone zoneX4 = b.battleState.getZoneAt(zonePos.add(new MapPoint( 0,  1))).get();
		assertNotNull(zone);
		assertNotNull(zoneX1);
		assertNotNull(zoneX2);
		assertNotNull(zoneX3);
		assertNotNull(zoneX4);
		assertEquals(zonePos, zone.getPos());
		assertEquals(zonePos, zoneX1.getPos());
		assertEquals(zonePos, zoneX2.getPos());
		assertEquals(zonePos, zoneX3.getPos());
		assertEquals(zonePos, zoneX4.getPos());

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		assertNotNull(dan);
		assertEquals(1, dan.getAP());
	}

	@Test
	public void defusingZone() throws Exception {
		// set up a battle
		final Battle b = bt.simpleBattle();

		// create a trap
		final Ability trap1 = getAbility(b, bt.danPos, "Trap1");
		assertNotNull(trap1);

		final MapPoint trapPos = new MapPoint(2, 4);

		final CommandRequest requestTrap = new UseAbilityCommandRequest(
			bt.danPos, AbilityAgentType.CHARACTER, trap1, single(new Casting(bt.danPos, trapPos)));

		List<ExecutedCommand> trapRS = doCommand(requestTrap, b);
		assertFalse(trapRS.isEmpty());

		// confirm that the trap is there
		Trap trap = b.battleState.getTrapAt(trapPos).get();
		assertNotNull(trap);
		assertEquals(trapPos, trap.getPos());

		// create a safe passage zone (which defuses all traps)
		final Ability zone1 = getAbility(b, bt.danPos, "Safepassage");
		assertNotNull(zone1);

		final MapPoint zonePos = new MapPoint(2, 4);

		final CommandRequest requestZone = new UseAbilityCommandRequest(
			bt.danPos, AbilityAgentType.CHARACTER, zone1, single(new Casting(bt.danPos, zonePos)));

		final List<ExecutedCommand> zoneRS = doCommand(requestZone, b);
		assertFalse(zoneRS.isEmpty());

		// confirm that the trap is defused
		trap = b.battleState.getTrapAt(trapPos).orElse(null);
		assertNull(trap);

		// attempt to create another trap
		final CommandRequest requestTrap2 = new UseAbilityCommandRequest(
			bt.danPos, AbilityAgentType.CHARACTER, trap1, single(new Casting(bt.danPos, trapPos)));

		trapRS = doCommand(requestTrap2, b);
		assertFalse(trapRS.isEmpty());

		// confirm that it is defused
		// TODO: confirm what the appropriate behaviour is.
		// trap = b.battleState.getTrapAt(trapPos).orElse(null);
		// assertNull(trap);
	}

	@Test
	public void pullOverDeadCharacter() throws Exception {
		final Battle b = bt.simpleBattle();

		final Ability pull4 = getAbility(b, bt.danPos, "pull4");
		assertNotNull(pull4);

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		final Character zan2 = b.battleState.getCharacterAt(bt.zan2Pos).get();
		final Character dan2 = b.battleState.getCharacterAt(bt.dan2Pos).get();
		assertNotNull(dan);
		assertNotNull(zan2);
		assertNotNull(dan2);

		final MapPoint deadPos = bt.danPos.add(new MapPoint(4, 0));
		final MapPoint targetPos = bt.danPos.add(new MapPoint(6, 0));

		zan2.teleport(deadPos, false);
		dan2.teleport(targetPos, false);
		zan2.dealDamage(1000);

		final CommandRequest requestPull = new UseAbilityCommandRequest(bt.danPos,
			AbilityAgentType.CHARACTER, pull4, single(new Casting(bt.danPos, targetPos)));

		final List<ExecutedCommand> rs = doCommand(requestPull, b);

		assertEquals(targetPos.add(new MapPoint(-4, 0)), dan2.getPos());
		assertNotNull(b.battleState.getCharacterAt(deadPos).get());
		assertEquals(1, dan.getAP());
	}

	@Test
	public void shortenedPull() throws Exception {
		final Battle b = bt.simpleBattle();

		final Ability pull4 = getAbility(b, bt.danPos, "pull4");
		assertNotNull(pull4);

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		final Character zan2 = b.battleState.getCharacterAt(bt.zan2Pos).get();
		final Character dan2 = b.battleState.getCharacterAt(bt.dan2Pos).get();
		assertNotNull(dan);
		assertNotNull(zan2);
		assertNotNull(dan2);

		final MapPoint obstaclePos = bt.danPos.add(new MapPoint(2, 0));
		final MapPoint deadPos = bt.danPos.add(new MapPoint(3, 0));
		final MapPoint targetPos = bt.danPos.add(new MapPoint(6, 0));

		zan2.teleport(deadPos, false);
		dan2.teleport(targetPos, false);
		zan2.dealDamage(1000);

		final RoadBlock obstacle = b.battleState.placeObstacle(
			obstaclePos, bt.testData.getStandardSprites());

		assertNotNull(obstacle);
		assertEquals(obstaclePos, obstacle.getPos());

		final CommandRequest requestPull = new UseAbilityCommandRequest(bt.danPos,
			AbilityAgentType.CHARACTER, pull4, single(new Casting(bt.danPos, targetPos)));

		final List<ExecutedCommand> rs = doCommand(requestPull, b);

		assertEquals(targetPos.add(new MapPoint(-2, 0)), dan2.getPos());
		assertNotNull(b.battleState.getCharacterAt(deadPos).get());
		assertEquals(1, dan.getAP());
	}

	@Test
	public void multiplePull() throws Exception {
		final Battle b = bt.simpleBattle();

		final Ability pull4 = getAbility(b, bt.danPos, "pull4");
		assertNotNull(pull4);

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		final Character zan = b.battleState.getCharacterAt(bt.zanPos).get();
		final Character dan2 = b.battleState.getCharacterAt(bt.dan2Pos).get();
		assertNotNull(dan);
		assertNotNull(zan);
		assertNotNull(dan2);

		final MapPoint targetPos = bt.danPos.add(new MapPoint(5, 0));
		final MapPoint target2Pos = bt.danPos.add(new MapPoint(6, 0));

		zan.teleport(targetPos, false);
		dan2.teleport(target2Pos, false);

		final CommandRequest requestPull = new UseAbilityCommandRequest(bt.danPos,
			AbilityAgentType.CHARACTER, pull4, single(new Casting(bt.danPos, targetPos)));

		final List<ExecutedCommand> rs = doCommand(requestPull, b);

		assertEquals(targetPos.add(new MapPoint(-4, 0)), zan.getPos());
		assertEquals(target2Pos.add(new MapPoint(-4, 0)), dan2.getPos());
		assertEquals(1, dan.getAP());
	}

	@Test
	public void fearedBasic() throws Exception {
		final Battle b = bt.simpleBattle();

		final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
		final Character dan2 = b.battleState.getCharacterAt(bt.dan2Pos).get();
		assertNotNull(dan);
		assertNotNull(dan2);

		final MapPoint targetPos = bt.danPos.add(new MapPoint(3, 0));
		dan2.teleport(targetPos, false);

		final StatusEffect feared = StatusEffectFactory.getEffect(
			new StatusEffectInfo("feared 3"), 0, Optional.of(dan));
		dan2.applyStatus(b, feared);

		assertTrue(b.doTurnStart(Player.PLAYER_B).isEmpty());
		List<ExecutedCommand> rs = doCommands(b.getTurnStart(Player.PLAYER_B), b);

		assertEquals(targetPos.add(new MapPoint(3, 0)), dan2.getPos());
		assertEquals(6, dan2.getMP());
	}

	@Test
	public void panicTraps() throws Exception {
		// Panic is stochastic, so we have to repeat the test many times.
		StandardSprites stdSprites = bt.testData.getStandardSprites();
		for (int i = 0; i < 100; i++) {
			final Battle b = bt.simpleBattle();

			final Character dan = b.battleState.getCharacterAt(bt.danPos).get();
			final Character dan2 = b.battleState.getCharacterAt(bt.dan2Pos).get();
			assertNotNull(dan);
			assertNotNull(dan2);

			final Ability trap1 = getAbility(b, bt.danPos, "Trap1");
			assertNotNull(trap1);

			final MapPoint targetPos = new MapPoint(5, 5);
			dan.teleport(targetPos, false);

			b.battleState.placeTrap(new MapPoint(4, 5), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(5, 4), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(6, 5), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(5, 6), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(4, 4), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(4, 6), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(6, 4), trap1, dan2, stdSprites);
			b.battleState.placeTrap(new MapPoint(6, 6), trap1, dan2, stdSprites);

			final StatusEffect panicked = StatusEffectFactory.getEffect(
				new StatusEffectInfo("panicked"), 0, Optional.empty());
			dan.applyStatus(b, panicked);

			assertTrue(b.doTurnStart(Player.PLAYER_A).isEmpty());
			final List<Command> cmds = b.getTurnStart(Player.PLAYER_A);
			assertFalse(cmds.isEmpty());
			List<ExecutedCommand> rs = doCommands(cmds, b);

			assertEquals(0, dan.getMP());
		}
	}
}

