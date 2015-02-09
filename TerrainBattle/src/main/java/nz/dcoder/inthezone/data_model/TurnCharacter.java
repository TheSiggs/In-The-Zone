package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
	public List<Node> getMove(List<Node> soFar, Node destination) {
		// WARNING: unsafe casting
		BoardNode d = (BoardNode) destination;

		// WARNING: global variable BoardNode.tiles
		BoardNode.tiles.clear();
		BoardNode.tiles.addAll(battle.terrain.defaultBoardTiles);

		// WARNING: if paths are being routed through obstacles, this line
		// is probably to blame.
		battle.getObstacles().forEach(o -> BoardNode.tiles.remove(new Tile(o.x, o.y)));

		Node start;
		if (soFar != null && soFar.size() > 0) {
			start = soFar.get(soFar.size() - 1);
		} else {
			start = new BoardNode(character.position.x, character.position.y, null);
		}

		List<Node> path = new AStarSearch(start, destination).search();
		if (path.size() + soFar.size() > mp) {
			return null;
		} else {
			path.addAll(0, soFar);
			return path;
		}
	}

	public void doMotion(List<Node> path) {
		if (path == null || path.size() > mp || path.size() < 1) {
			// WARNING: we really should validate the path here, just to be sure
			throw new RuntimeException("Invalid path " + path.toString());
		}

		List<Position> ps = path.stream()
			.map(TurnCharacter::nodeToPosition).collect(Collectors.toList());

		// trigger object abilities (such as trip mines)
		for (Position p : ps) {
			// TODO: complete this section
			battle.objects.stream().filter(o -> o.mayTriggerAbilityAt(p))
				.forEach(o -> System.err.println("Object ability activated"));
		}

		Position destination = ps.get(ps.size() - 1);

		// update the character for this move operation
		mp -= ps.size();
		Position p0 = character.position;
		character.position = destination;
		battle.controller.onMove.accept(new DoMoveInfo(p0, path));
	}

	private static Position nodeToPosition(Node n) {
		// WARNING: unsafe casting
		BoardNode bn = (BoardNode) n;
		return new Position(bn.getX(), bn.getY());
	}

	private Ability getAbility(AbilityName name) {
		return character.getAbilities().stream()
			.filter(a -> a.info.name.equals(name)).findFirst().orElse(null);
	}

	public boolean canDoAbility(AbilityName name, Position target) {
		Ability ability = getAbility(name);
		if (ability == null || ability.info.cost > ap) return false;
		return ability.canApplyEffect(character, target, battle);
	}

	public void doAbility(AbilityName name, Position target) {
		Ability ability = getAbility(name);
		if (ability == null) return;
		ap -= ability.info.cost;
		ability.applyEffect(character, target, battle);
		battle.controller.onAbility.accept(new DoAbilityInfo(
			character.position, target, ability.info));
	}

	public boolean canUseItem(Item item, Position target) {
		if (item.ability.info.cost > ap) return false;
		return item.ability.canApplyEffect(character, target, battle);
	}

	public void useItem(Item item, Position target) {
		ap -= item.ability.info.cost;
		item.ability.applyEffect(character, target, battle);
		battle.controller.onAbility.accept(new DoAbilityInfo(
			character.position, target, item.ability.info));
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

	public boolean hasOptions(Collection<Item> items) {
		boolean canUseItem = items.stream()
			.anyMatch(i -> i.ability.info.cost <= ap);
		boolean canUseAbility = character.getAbilities().stream()
			.anyMatch(a -> a.info.cost <= ap);
		return canUseItem || canUseAbility;
	}
}

