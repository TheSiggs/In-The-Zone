/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author denz
 */
public class Scene extends SimpleApplication {

	/*
	public Scene() {
		super((AppState) null);
	}
	*/

	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets", FileLocator.class);
		assetManager.registerLocator("assets/town.zip", ZipLocator.class);
		flyCam.setMoveSpeed(70f);
		getCamera().setLocation(new Vector3f(-111.59849f,
				67.81453f, -65.62508f));

		getCamera().lookAtDirection(new Vector3f(0.7044869f,
				-0.6950954f, 0.1433202f), Vector3f.UNIT_Y);
		Spatial scene = assetManager.loadModel("Scenes/sample.j3o");
		rootNode.attachChild(scene);
		initGrid();
	}

	public static void main(String[] args) {
		Scene app = new Scene();
		app.start();
	}
	float val = 0;

	@Override
	public void simpleUpdate(float tpf) {
		val += tpf;
		if ((int) Math.round(val) % 10 == 0) {
			System.out.println("Cam: " + getCamera().getLocation());
			System.out.println("Cam dir: " + getCamera().getDirection());
			System.out.println("Cam Up: " + getCamera().getUp());
		}

	}
	Node reference = new Node();

	private void initGrid() {
		int width = 10;
		int height = 10;
		Vector3f location = (new Vector3f(-111.59849f,
				67.81453f, -65.62508f));
		reference.setLocalTranslation(location);
		for (int i = -1; ++i < width;) {
			for (int j = -1; ++j < height;) {
				drawGridCell(i, j);
			}
		}
		rootNode.attachChild(reference);
	}

	private void drawGridCell(int i, int j) {
		/*
		Mesh m = new Mesh();
		m.setMode(Mesh.Mode.Lines);

// Line from 0,0,0 to 0,1,0
		m.setBuffer(VertexBuffer.Type.Position, 3, new float[]{0, 0, 0, 0, 1, 0});
		m.setBuffer(VertexBuffer.Type.Index, 2, new short[]{0, 1});
		*/
		Quad mesh = new Quad(1f,1f);
		Geometry geom = new Geometry("A shape", mesh); // wrap shape into geometry
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/ShowNormals.j3md");   // create material
		//mat.setColor("Color", ColorRGBA.Green);
		geom.setMaterial(mat);                         // assign material to geometry
// if you want, transform (move, rotate, scale) the geometry.
		geom.setLocalTranslation(i * 2.0f, 0f, -j * 2.0f);
		reference.attachChild(geom);
		
	}
}
