/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 *
 * @author denz
 */
public class BlenderRender extends SimpleApplication {
    private String csvName;
	public static void main(String args[]) {
		BlenderRender app = new BlenderRender();
		app.start();
	}
    public void setCsvName(String name) {
        csvName = name;
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
		//Spatial track = assetManager.loadModel("Scenes/zan-house.blend");
		//Spatial track = assetManager.loadModel("Models/black-canary/black canary hero185.blend");
        //Spatial track = assetManager.loadModel("Models/clock/clock.blend");
        /*
        Spatial track = assetManager.loadModel("Models/basement/Basement_02.blend");
        Spatial player = assetManager.loadModel("Models/zan/zan_texturing.blend");
		rootNode.attachChild(track);
        rootNode.attachChild(player);
        */
        loadCsv(csvName);

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

    private void loadCsv(String name) {
        //InputStream is = (new FileInputStream(name));
        addAndAttachModel("Models/basement/Basement_02.blend", 0f, 0f, 0f);
        addAndAttachModel("Models/zan/zan_texturing.blend", 0f, 0f, 0f);
    }

    private void addAndAttachModel(String path, float x, float y, float z) {
        Spatial spatial = assetManager.loadModel(path);
        rootNode.attachChild(spatial);
        spatial.setLocalTranslation(x, y, z);
    }
}
