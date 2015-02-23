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

	ColorRGBA colors[] = {
		new ColorRGBA(0.3f, 0.3f, 0.3f, 0f),
		new ColorRGBA(0.9f, 0.9f, 0.9f, 0f)
	};

	public BoardGraphics(Terrain terrain, AssetManager assetManager) {
		this.width = terrain.getWidth();
		this.height = terrain.getHeight();
		this.assetManager = assetManager;

		float x, y, z;
		z = 0;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				x = i * Graphics.scale;
				y = -j * Graphics.scale;
				String name = i + "," + j;
				addBox(name, new Vector3f(x, y, z),
						colors[j % 2 == 0 ? i % 2 : (i + 1) % 2],
						terrain.getBoardState(i, j));
			}
		}
	}

	private void addBox(
		String name,
		Vector3f translation,
		ColorRGBA color,
		int boardValue
	) {
		float offsetZ = boardValue * 0.5f;

		Box b = new Box(
			Graphics.scale / 2, Graphics.scale / 2,
			0.1f * Graphics.scale + offsetZ);

		Geometry geom = new Geometry(name, b);
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

		boardNode.attachChild(geom);
	}

	public Node getBoardNode() {
		return boardNode;
	}
}

