/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author denz
 */
public class BlenderRender extends SimpleApplication {
	public static void main(String args[]) {
		BlenderRender app = new BlenderRender();
		app.start();
	}
	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets", FileLocator.class);
		//assetManager.registerLocator("assets/town.zip", ZipLocator.class);
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);

		//load model with packed images
		//Spatial ogre = assetManager.loadModel("Blender/2.4x/Sinbad.blend");
		//rootNode.attachChild(ogre);

		//load model with referenced images
		Spatial track = assetManager.loadModel("Scenes/zan-house.blend");
		rootNode.attachChild(track);

		// sunset light
		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.1f, -0.7f, 1).normalizeLocal());
		dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
		rootNode.addLight(dl);

		// skylight
		dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.6f, -1, -0.6f).normalizeLocal());
		dl.setColor(new ColorRGBA(0.10f, 0.22f, 0.44f, 1.0f));
		rootNode.addLight(dl);

		// white ambient light
		dl = new DirectionalLight();
		dl.setDirection(new Vector3f(1, -0.5f, -0.1f).normalizeLocal());
		dl.setColor(new ColorRGBA(0.80f, 0.70f, 0.80f, 1.0f));
		rootNode.addLight(dl);
	}

	@Override
	public void simpleUpdate(float tpf) {
	}
}
