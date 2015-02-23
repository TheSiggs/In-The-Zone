package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.dcoder.ai.astar.AStarSearch;
import nz.dcoder.ai.astar.BoardNode;
import nz.dcoder.ai.astar.Node;
import nz.dcoder.ai.astar.Tile;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Combines a character with turns specific information about the character
 * */
public class TurnCharacter {
	public int mp;
	public int ap;
	public int maxMP;
	public int maxAP;
	private final Character character;
	private final int turnNumber;
	private final Battle battle;

	public TurnCharacter(
		Character character,
		int turnNumber,
		Battle battle
	) {
		this.character = character;
		this.turnNumber = turnNumber;
		this.battle = battle;
		this.maxMP = character.getBaseStats().baseMP;
		this.maxAP = character.getBaseStats().baseAP;
		mp = maxMP;
		ap = maxAP;
	}

	/**
	 * Get a path on behalf of the presentation layer.  This method is designed
	 * to permit waypoints.  The soFar parameter contains the path up until now.
	 *
	 * Note: This method does not check that the destination is unoccupied.  That
	 * check is to be done by the GUI and repeated by "doMotion".  This method
	 * does, however, check that the destination is not an obstacle.
	 *
	 * @param soFar The path taken so far.  Set to null if this is the first
	 * waypoint
	 * @param destination The destination.  This may be a waypoint in which case a
	 * subsequent call to getMove gets the rest of the path
	 *
	 * @return null if there is no path for this character to the destination, or
	 * if this character does not have enough MP to reach the destination.
	 * Returns a new list every time, containing the complete path including the
	 * contents of the "soFar" parameter.
	 * */
	public List<Position> getMove(List<Position> soFar, Position destination) {
		Set<Position> obstacles = new HashSet<Position>(battle.getObstacles());
		obstacles.addAll(battle.terrain.getObstacles());

		Position startPos;
		if (soFar != null && soFar.size() > 0) {
			startPos = soFar.get(soFar.size() - 1);
		} else {
			startPos = character.position;
		}

		Node<Position> start = new AStarPositionNode(null,
			obstacles,
			battle.terrain.getWidth(),
			battle.terrain.getHeight(),
			startPos,
			destination);

		AStarSearch search = new AStarSearch(start);
		List<Node<Position>> npath = search.search();
		List<Position> path = npath.stream()
			.map(n -> n.getPosition()).collect(Collectors.toList());

		if (npath.size() + soFar.size() > mp) {
			return null;
		} else {
			return Stream.concat(
				soFar.stream(),
				npath.stream().map(n -> n.getPosition())
			).collect(Collectors.toList());
		}
	}

	/**
	 * Cause the character to move along the specified path.
	 *
	 * May cause zero or more onMove events.  May also cause zero of more
	 * onAbility events if the character triggers certain objects such as mines
	 *
	 * @param path The path (generated by getMove) along which the character is
	 * to walk
	 * */
	public void doMotion(List<Position> path) {
		battle.checkTurn(turnNumber);

		if (path == null || path.size() > mp || path.size() < 1) {
			// WARNING: we really should validate the path here, just to be sure
			throw new RuntimeException("Invalid path " + path.toString());
		}

		List<Position> pathSoFar = new ArrayList<Position>();
		while (path.size() > 0) {
			Position p = path.get(0);
			path.remove(0);
			pathSoFar.add(p);

			Collection<BattleObject> os = battle.getTriggeredObjects(p);
			if (os.size() > 0) {
				doMotion0(pathSoFar);
				for (BattleObject o : os) {
					// trigger object abilities
					Ability ability = o.ability.ability;
					ability.applyEffect(o.ability, p, battle);

					Collection<Position> targets = ability.getTargets(
						ability.getAffectedArea(p, p, battle), battle);

					battle.controller.callOnAbility(
						character.position, targets, ability.info);
				}

				pathSoFar = new ArrayList<Position>();
			}
		}

		if (pathSoFar.size() > 0) {
			doMotion0(pathSoFar);
		}
	}

	private void doMotion0(List<Position> path) {
		mp -= path.size();
		Position p0 = character.position;
		character.position = path.get(path.size() - 1);
		battle.controller.callOnMove(p0, path);
	}

	private Ability getAbility(AbilityName name) {
		return character.getAbilities().stream()
			.filter(a -> a.info.name.equals(name)).findFirst().orElse(null);
	}

	/**
	 * Determine if this character can perform an ability with a given target
	 * */
	public boolean canDoAbility(AbilityName name, Position target) {
		Ability ability = getAbility(name);
		if (ability == null || ability.info.cost > ap) return false;
		return ability.canApplyEffect(character, target, battle);
	}

	/**
	 * Get the area of effect of an ability
	 * */
	public Collection<Position> getAffectedArea(
		AbilityName name, Position target
	) {
		Ability ability = getAbility(name);
		if (ability == null) {
			return new ArrayList<Position>();
		} else {
			return ability.getAffectedArea(character.position, target, battle);
		}
	}

	/**
	 * Get the targets of an ability.  These are the squares that contain
	 * characters or objects that will be affected by the ability, assuming it is
	 * possible to use the ability
	 * */
	public Collection<Position> getTargets(
		AbilityName name, Position target
	) {
		Ability ability = getAbility(name);
		if (ability == null) {
			return new ArrayList<Position>();
		} else {
			return ability.getTargets(
				ability.getAffectedArea(character.position, target, battle), battle);
		}
	}

	/**
	 * Perform an ability.  This will an onAbility event which carries the
	 * squares on which targeted characters and objects are located.
	 * */
	public void doAbility(AbilityName name, Position target) {
		battle.checkTurn(turnNumber);

		Ability ability = getAbility(name);
		if (ability == null) return;
		ap -= ability.info.cost;
		ability.applyEffect(character, target, battle);


		Collection<Position> targets = ability.getTargets(
			ability.getAffectedArea(character.position, target, battle), battle);

		battle.controller.callOnAbility(character.position, targets, ability.info);
	}

	/**
	 * Determine if this character is able to use the specified item targeting
	 * the specified square.  In the case of items that are used on oneself, the
	 * target parameter will be set to the position of this character.
	 * */
	public boolean canUseItem(Item item, Position target) {
		if (item.ability.info.cost > ap) return false;
		return item.ability.canApplyEffect(character, target, battle);
	}

	/**
	 * Get the area of affect from using an item.  Some items such as grenades
	 * might have an area of effect
	 * */
	public Collection<Position> getItemAffectedArea(Item item, Position target) {
		return item.ability.getAffectedArea(character.position, target, battle);
	}

	/**
	 * Get the targets from using an item.  Essentially the same as getTargets
	 * but for items rather than character abilities.
	 * */
	public Collection<Position> getItemTargets(Item item, Position target) {
		return item.ability.getTargets(
			item.ability.getAffectedArea(
				character.position, target, battle), battle);
	}

	/**
	 * Use an item targeting the specified square.  In the case of items that are
	 * used on oneself, the target parameter will be set to the position of this
	 * character.
	 * */
	public void useItem(Item item, Position target) {
		battle.checkTurn(turnNumber);

		ap -= item.ability.info.cost;
		item.ability.applyEffect(character, target, battle);

		Collection<Position> targets = item.ability.getTargets(
			item.ability.getAffectedArea(
				character.position, target, battle), battle);

		battle.controller.callOnAbility(
			character.position, targets, item.ability.info);
	}

	public CharacterName getName() {
		return character.name;
	}

	public Position getPos() {
		return character.position;
	}

	public int getHP() {
		return character.hp;
	}

	public int getMaxHP() {
		return character.getMaxHP();
	}

	public Collection<Equipment> getVisibleEquipment() {
		return character.getVisibleEquipment();
	}

	public Collection<AbilityInfo> getAbilities() {
		return character.getAbilities()
			.stream().map(a -> a.info).collect(Collectors.toList());
	}

	public boolean isOnManaZone() {
		return battle.terrain.isManaZone(character.position);
	}

	/**
	 * Determine if this character has enough action points to carry out another
	 * action on this turn
	 * */
	public boolean hasOptions(Collection<Item> items) {
		boolean canUseItem = items.stream()
			.anyMatch(i -> i.ability.info.cost <= ap);
		boolean canUseAbility = character.getAbilities().stream()
			.anyMatch(a -> a.info.cost <= ap);
		return canUseItem || canUseAbility;
	}
}

