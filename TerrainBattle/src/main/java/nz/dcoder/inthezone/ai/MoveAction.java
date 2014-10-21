/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import aima.core.agent.Action;

/**
 *
 * @author informatics-palmerson
 */
public class MoveAction implements Action, Comparable<MoveAction> {

	static enum Dir {NORTH, EAST, SOUTH, WEST};
	Dir direction = Dir.NORTH;
	public MoveAction(Dir direction) {
		this.direction = direction;
	}
	public Dir getDir() {
		return direction;
	}
	@Override
	public boolean isNoOp() {
		return false;
	}

	@Override
	public String toString() {
		switch (direction) {
			case NORTH:
				return "north";
			case EAST:
				return "east";
			case SOUTH:
				return "south";
			case WEST:
				return "west";
		}
		return null;
	}
	
	@Override
	public int compareTo(MoveAction o) {
		return this.direction == o.direction ? 0 : 1;
	}

}
