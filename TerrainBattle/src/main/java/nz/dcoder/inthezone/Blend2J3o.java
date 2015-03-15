package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.UrlAssetInfo;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.blender.BlenderModelLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by denz on 15/03/15.
 */
public class Blend2J3o extends SimpleApplication {
    private final BinaryExporter binaryExporter;

    public Blend2J3o() {
        this.binaryExporter = new BinaryExporter();
    }

    @Override
    public void simpleInitApp() {
        String filename = "file:///var/projects/dcoder.nz/in-the-zone/TerrainBattle/assets/Models/zan/zan_texturing.blend";
        String assetFilename = "Models/zan/zan_texturing.blend";
        BlenderModelLoader loader = new BlenderModelLoader();
        ModelKey assetKey = new ModelKey("");
        URL url = null;
        try {
            url = new URL(filename);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        UrlAssetInfo info = null;
        try {
            info = UrlAssetInfo.create(assetManager, assetKey, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Spatial spatial = null;
        try {
            spatial = loader.load(info);
            spatial = assetManager.loadModel(assetFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rootNode.attachChild(spatial);
        File file = new File("zan_texturing.j3o");
        try {
            getBinaryExporter().save(spatial, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Blend2J3o main = new Blend2J3o();
        main.start();
    }

    public BinaryExporter getBinaryExporter() {
        return binaryExporter;
    }
}
