package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class TeleportAbility extends Ability {
	public static EffectName effectName = new EffectName("teleport");

	public TeleportAbility(AbilityInfo info) {
		super(effectName, info);
	}

        // @override
	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
                // TODO:
                // - set turnCharacter as selected character
                
                TurnCharacter turnCharacter = battle.turn.turnCharacters.iterator().next();
                Character targetCharacter = battle.getCharacterAt(pos);
                
                if(canApplyEffect(agent, pos, battle)){
                    targetCharacter.position = pos;
                    turnCharacter.ap = turnCharacter.ap - info.cost;
                }
                
                return;
	}
		
	public boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
                // TODO:
                // - set turnCharacter as selected character

                TurnCharacter turnCharacter = battle.turn.turnCharacters.iterator().next();
                Position turnCharacterPos = turnCharacter.getPos();
                
                // check cost
                if(turnCharacter.ap<info.cost){
                    return false;
                }
                // check range
                if(Math.abs(turnCharacterPos.x - pos.x) + Math.abs(turnCharacterPos.y - pos.y) > info.range ){
                    return false;
                }
                // check destination is clear
                if(battle.getObstacles().contains(pos)){
                    return false;
                }
                
		return true;
	}
}

