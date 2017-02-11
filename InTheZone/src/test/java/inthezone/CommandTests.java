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
import inthezone.battle.Trap;
import inthezone.battle.Zone;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
		final List<Command> cmds = crq.makeCommand(b.battleState);
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
}

