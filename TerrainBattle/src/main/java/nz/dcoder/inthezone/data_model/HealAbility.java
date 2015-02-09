package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class HealAbility extends Ability {
	public static EffectName effectName = new EffectName("heal");

	public HealAbility(AbilityInfo info) {
		super(effectName, info);
	}

	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// NOTE: this is where the healing formula goes.
            
                // TODO:
                // - set turnCharacter as selected character
                // - figure out path rules
            
                TurnCharacter turnCharacter = battle.turn.turnCharacters.iterator().next();
                //Character user = battle.getCharacterAt(pos);
                ArrayList<Position> targetArea = null;
                ArrayList<Character> targets = null;
                targetArea.add(pos);
                
                if(canApplyEffect(agent, pos, battle)){
                // 1) determine the affected squares using areaOfEffect and piercing
                    if(info.isPiercing){
                        // add all in path
                    }else{
                        // move target to first Obstacle
                    }
                    // target now set so we can 
                    if(info.areaOfEffect>0){
                        // add dimond of size aoe reletive to pos
                    }

                // 2) find the targets (i.e. the characters on the affected squares)
                    for(int i=0; i > targetArea.size(); i++){
                        if(battle.getCharacterAt(targetArea.get(i)) != null){
                            targets.add(battle.getCharacterAt(targetArea.get(i)));
                        }
                    }
                    
                // 3) gather the parameters from the agent doing the ability and
                    double amount = 0;
                    
                // 4) apply the formula to each target.
                    for(int i=0;i > targets.size(); i++){
                        if((targets.get(i).hp+amount)<=targets.get(i).getMaxHP()){
                            targets.get(i).hp += amount;
                        }else{
                            targets.get(i).hp = targets.get(i).getMaxHP();
                        }
                    }
                    turnCharacter.ap = turnCharacter.ap - info.cost;
                }
		return;
	}
		
	public boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
                // TODO:
                // - set turnCharacter as selected character
                // - search for line of sight

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
                // check obsticles
                
                    // check destination is clear
                    if(battle.getObstacles().contains(pos)){
                        return false;
                    }
                
		return true;
	}
}

