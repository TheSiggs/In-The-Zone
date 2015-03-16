package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.blender.BlenderModelLoader;

import java.io.File;
import java.io.IOException;

/**
 * Created by denz on 15/03/15.
 */
public class Blend2J3o extends SimpleApplication {
    protected final BinaryExporter binaryExporter;
    protected String sourceBlend;
    protected String targetJ3o;

    public Blend2J3o() {
        this.binaryExporter = BinaryExporter.getInstance();
    }

    public void init() {
        assetManager.registerLocator("assets", FileLocator.class);
        assetManager.registerLocator("", FileLocator.class);
    }

    public void loadAndSave() {
        Spatial spatial = assetManager.loadModel(sourceBlend);
        rootNode.attachChild(spatial);
        File file = new File(targetJ3o);
        try {
            getBinaryExporter().save(spatial, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void simpleInitApp() {
        init();
        loadAndSave();
        this.stop();
    }

    public static void main(String args[]) {
        Blend2J3o main = new Blend2J3o();
        if (args.length == 1) {
            String sourceCsvFilename = args[0];
        } else if (args.length != 2) {
            args = new String[]{"Models/zan/zan_texturing.blend", "zan.j3o"};
        }
        if (args.length == 2) {
            main.convert(args[0], args[1]);
        } else {
            // csv file load

        }
    }

    public void convert(String blend, String toJ3o) {
        this.setSourceBlend(blend);
        this.setTargetJ3o(toJ3o);
        this.setShowSettings(false);
        this.start();
    }
    public BinaryExporter getBinaryExporter() {
        return binaryExporter;
    }

    public String getSourceBlend() {
        return sourceBlend;
    }

    public void setSourceBlend(String sourceBlend) {
        this.sourceBlend = sourceBlend;
    }

    public String getTargetJ3o() {
        return targetJ3o;
    }

    public void setTargetJ3o(String targetJ3o) {
        this.targetJ3o = targetJ3o;
    }
}
