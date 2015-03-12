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
import java.util.HashSet;
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
	private Collection<Position> movingHighlight = new ArrayList<>();
	private Collection<Position> staticHighlight = new HashSet<>();
	private ColorRGBA staticHighlightColor = null;

	public static final ColorRGBA PATH_COLOR = new ColorRGBA(0.0f, 1.0f, 0.0f, 0.8f);
	public static final ColorRGBA TARGET_COLOR = new ColorRGBA(1.0f, 0.0f, 0.0f, 0.8f);
	public static final ColorRGBA RANGE_COLOR = new ColorRGBA(1.0f, 1.0f, 0.0f, 0.8f);

	/**
	 * Utility function to alpha blend two colors.  I'm surprised this doesn't
	 * come with JME.
	 * */
	private static ColorRGBA alphaBlend(ColorRGBA src, ColorRGBA dst) {
		ColorRGBA msrc = src.mult(src.a);
		ColorRGBA mdst = dst.mult(dst.a);
		float outA = src.a + (dst.a * (1 - src.a));
		if (outA == 0) {
			return ColorRGBA.Black;
		} else {
			msrc.addLocal(mdst.multLocal(1 - src.a)).multLocal(1 / outA);
			msrc.a = outA;
			return msrc;
		}
	}

	ColorRGBA colors[] = {
		new ColorRGBA(0.3f, 0.3f, 0.3f, 1f),
		new ColorRGBA(0.9f, 0.9f, 0.9f, 1f),
		new ColorRGBA(0.5f, 0.0f, 0.8f, 1f)
	};

	public BoardGraphics(Terrain terrain, AssetManager assetManager) {
		this.width = terrain.getWidth();
		this.height = terrain.getHeight();
		this.assetManager = assetManager;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				ColorRGBA color;
				if (terrain.getBoardState(i, j) == 2) {
					color = colors[2];
				} else {
					color = colors[j % 2 == 0 ? i % 2 : (i + 1) % 2];
				}
				addBox(new Position(i, j), color, terrain.getBoardState(i, j));
			}
		}
	}

	private void addBox(
		Position p,
		ColorRGBA color,
		int boardValue
	) {
		float offsetZ = boardValue == 1 ? 0.5f : 0.0f;

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
	 * Set the moving highlight (which indicates the path for movement, or the
	 * area of effect for abilities).
	 * */
	public void setMovingHighlight(Collection<Position> highlight, ColorRGBA h) {
		clearMovingHighlight();

		if (highlight == null) return;

		movingHighlight.addAll(highlight);
		for (Position p : movingHighlight) {
			Geometry g = tiles.get(p);
			if (g != null) {
				ColorRGBA c = alphaBlend(h, (ColorRGBA) g.getUserData("defaultColor"));
				g.getMaterial().setColor("Color", c);
			}
		}
	}

	/**
	 * Clear the moving highlight.
	 * */
	public void clearMovingHighlight() {
		for (Position p : movingHighlight) {
			Geometry g = tiles.get(p);
			if (g != null) {
				ColorRGBA restoreColor = (ColorRGBA) g.getUserData("defaultColor");

				if (staticHighlight.contains(p)) {
					restoreColor = alphaBlend(staticHighlightColor, restoreColor);
				}

				g.getMaterial().setColor("Color", restoreColor);
			}
		}
		movingHighlight.clear();
	}

	/**
	 * Set the static highlight, which generally indicates range of motion or
	 * range of abilities.
	 * */
	public void setStaticHighlight(Collection<Position> highlight, ColorRGBA h) {
		staticHighlightColor = h;
		clearAllHighlighting();

		staticHighlight.addAll(highlight);
		for (Position p : staticHighlight) {
			Geometry g = tiles.get(p);
			if (g != null) {
				ColorRGBA c = alphaBlend(staticHighlightColor,
					(ColorRGBA) g.getUserData("defaultColor"));
				g.getMaterial().setColor("Color", c);
			}
		}
	}

	/**
	 * remove all highlighting.
	 * */
	public void clearAllHighlighting() {
		clearMovingHighlight();
		for (Position p : staticHighlight) {
			Geometry g = tiles.get(p);
			if (g != null) {
				g.getMaterial().setColor("Color",
					(ColorRGBA) g.getUserData("defaultColor"));
			}
		}

		staticHighlight.clear();
	}
}

