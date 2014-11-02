/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.astar;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author denz
 */
public class BoardSearch {
	public static void main(String args[]) {
		SortedSet<Tile> tiles = new TreeSet<>();
		tiles.add(new Tile(0,0));
		tiles.add(new Tile(1,0));
		tiles.add(new Tile(2,0));
		tiles.add(new Tile(3,0));
		tiles.add(new Tile(0,1));
		tiles.add(new Tile(1,1));
		tiles.add(new Tile(3,1));
		tiles.add(new Tile(0,2));
		tiles.add(new Tile(1,2));
		tiles.add(new Tile(3,2));
		tiles.add(new Tile(0,3));
		tiles.add(new Tile(1,3));
		tiles.add(new Tile(2,3));
		tiles.add(new Tile(3,3));
		BoardNode.tiles = tiles;
		Node start = new BoardNode(0, 2, null);
		Node goal = new BoardNode(3, 2, null);
		AStarSearch search = new AStarSearch(start, goal);
		List<Node> path = search.search();
		System.out.println(path);
	}
}
