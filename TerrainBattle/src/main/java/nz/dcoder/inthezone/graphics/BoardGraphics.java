/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.graphics;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Terrain;

/**
 *
 * @author denz
 */
public class BoardGraphics {
	private final Node boardNode = new Node("board");

	private final int width;
	private final int height;
	private final AssetManager assetManager;

	private Map<Position, Geometry> tiles = new HashMap<>();
	private Collection<Position> highlighted = new ArrayList<>();

	public static final ColorRGBA PATH_COLOR = new ColorRGBA(0.0f, 1.0f, 0.0f, 0.0f);
	public static final ColorRGBA TARGET_COLOR = new ColorRGBA(1.0f, 0.0f, 0.0f, 0.0f);

	ColorRGBA colors[] = {
		new ColorRGBA(0.3f, 0.3f, 0.3f, 0f),
		new ColorRGBA(0.9f, 0.9f, 0.9f, 0f)
	};

	public BoardGraphics(Terrain terrain, AssetManager assetManager) {
		this.width = terrain.getWidth();
		this.height = terrain.getHeight();
		this.assetManager = assetManager;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				addBox(new Position(i, j),
					colors[j % 2 == 0 ? i % 2 : (i + 1) % 2],
					terrain.getBoardState(i, j));
			}
		}
	}

	private void addBox(
		Position p,
		ColorRGBA color,
		int boardValue
	) {
		float offsetZ = boardValue * 0.5f;

		float x = p.x * Graphics.scale;
		float y = -p.y * Graphics.scale;
		Vector3f translation = new Vector3f(x, y, 0);

		Box b = new Box(
			Graphics.scale / 2, Graphics.scale / 2,
			0.1f * Graphics.scale + offsetZ);

		Geometry geom = new Geometry("board", b);
		Material mat =
			new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

		ColorRGBA real = color.clone();
		if (boardValue != 0) {
			real.r = 1f;
		}

		mat.setColor("Color", real);
		Texture cube1Tex = assetManager.loadTexture(
			"Textures/random_grey_variations.png");
		mat.setTexture("ColorMap", cube1Tex);
		geom.setMaterial(mat);
		geom.setLocalTranslation(translation);
		geom.setUserData("defaultColor", real);

		geom.setUserData("kind", "board");
		geom.setUserData("p", new SaveablePosition(p));
		boardNode.attachChild(geom);

		tiles.put(p, geom);
	}

	public Node getBoardNode() {
		return boardNode;
	}

	/**
	 * Highlight tiles for pathfinding, targeting etc.
	 * */
	public void highlightTiles(Collection<Position> highlight, ColorRGBA h) {
		clearHighlighting();
		if (highlight == null) return;

		highlighted.addAll(highlight);
		for (Position p : highlighted) {
			Geometry g = tiles.get(p);
			if (g != null) g.getMaterial().setColor("Color", h);
		}
	}

	/**
	 * Remove highlighting
	 * */
	public void clearHighlighting() {
		for (Position p : highlighted) {
			Geometry g = tiles.get(p);
			if (g != null) {
				g.getMaterial().setColor("Color",
					(ColorRGBA) g.getUserData("defaultColor"));
			}
		}
		highlighted.clear();
	}
}

