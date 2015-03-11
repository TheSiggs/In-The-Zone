package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.stream.Collectors;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Information about a turn
 * */
public class Turn {
	final boolean isPlayerTurn;
	final private Collection<TurnCharacter> turnCharacters;
	final private Battle battle;
	final private int turnNumber;
	final private ItemBag items;

	public Turn (
		boolean isPlayerTurn,
		Collection<TurnCharacter> turnCharacters,
		ItemBag items,
		Battle battle,
		int turnNumber
	) {
		this.isPlayerTurn = isPlayerTurn;
		this.turnCharacters = turnCharacters;
		this.items = items;
		this.battle = battle;
		this.turnNumber = turnNumber;
	}

	public ItemBag getItems() {
		return items;
	}

	/**
	 * May return null
	 * */
	public TurnCharacter turnCharacterAt(Position pos) {
		return turnCharacters.stream()
			.filter(t -> t.getPos().equals(pos)).findFirst().orElse(null);
	}

	public Collection<CharacterInfo> getPlayerInfo() {
		return turnCharacters.stream()
			.map(c -> c.getCharacterInfo()).collect(Collectors.toList());
	}

	public Collection<CharacterInfo> getNPCInfo() {
		return battle.aiPlayers.stream()
			.map(c -> c.getCharacterInfo(battle.terrain.isManaZone(c.position)))
			.collect(Collectors.toList());
	}

	public CharacterInfo getCharacterAt(Position pos) {
		Character c = battle.getCharacterAt(pos);
		if (c == null) return null;
		return c.getCharacterInfo(battle.terrain.isManaZone(c.position));
	}

	/**
	 * Determine if there is anything more that can be done on this turn
	 * @param items Consumable items available to the player
	 * @return true if there is nothing more that can be done on this turn
	 * */
	public boolean isTurnOver(Collection<Item> items) {
		return !turnCharacters.stream().anyMatch(t -> t.hasOptions(items));
	}

	/**
	 * Determine if the battle is over.  If the battle is over, then a
	 * onBattleEnd event will be generated the next time endTurn is called.  The
	 * presentation layer should call endTurn to properly end the battle.
	 * */
	public boolean isBattleOver() {
		return battle.isBattleOver();
	}

	/**
	 * End this turn.  When the presentation layer calls this method, the AI
	 * player will take a turn.  Then, if the battle is not over, the
	 * presentation layer will be sent an onPlayerTurnStart event with a new turn
	 * object.  If the battle is over, this method causes an onBattleEnd event to
	 * be generated instead.
	 * */
	public void endTurn() {
		battle.changeTurn();
	}
}

