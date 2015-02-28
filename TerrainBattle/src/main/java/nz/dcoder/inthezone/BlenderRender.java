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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author denz
 */
public class BlenderRender extends SimpleApplication {
    private String csvName;
	public static void main(String args[]) {
		BlenderRender app = new BlenderRender();
        System.out.println("number of args = "+ args.length);
        if (args.length < 2) {
            args = new String[]{args[0], "scene.csv"};
        }
        if (args.length != 2) {
            throw new RuntimeException("usage: blender <csvName>");
        }
        app.setCsvName(args[1]);
		app.start();
	}
    public void setCsvName(String name) {
        csvName = name;
    }
	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets", FileLocator.class);
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        try {
            loadCsv(csvName);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private void loadCsv(String name) throws IOException {
        //InputStream is = (new FileInputStream(name));
        //CSVParser parser = CSVParser.parse(new File(name), new CSVFormat(),
        System.out.println("Reading file: "+ name);
        Reader in = new FileReader(name);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        for (CSVRecord record : records) {
            String filename = record.get(0);
            String xs = record.get(1);
            String ys = record.get(2);
            String zs = record.get(3);
            //String  = record.get("X");
            System.out.println(filename +" | "+ xs +" | "+ ys +" | "+ zs);
            addAndAttachModel(filename, Float.parseFloat(xs), Float.parseFloat(ys), Float.parseFloat(zs));
        }
    }

    private void addAndAttachModel(String path, float x, float y, float z) {
        Spatial spatial = assetManager.loadModel(path);
        rootNode.attachChild(spatial);
        spatial.setLocalTranslation(x, y, z);
    }
}
