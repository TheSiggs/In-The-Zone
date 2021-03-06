package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;



public class Blend2J3o extends SimpleApplication {

	protected final BinaryExporter binaryExporter;
	protected String sourceBlend;
	protected String targetJ3o;

	protected final Map<String, String> files = new HashMap<>(); // files to convert source -> target

	public Blend2J3o() {
		this.binaryExporter = BinaryExporter.getInstance();
	}

	public void add(String blendFilename, String j3oFilename) {
		files.put(blendFilename, j3oFilename);
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
		processAll();
		this.stop();
	}

	public static void main(String args[]) {
		if (args.length == 0) {
			args = new String[]{"scene.csv"};
		}
		Blend2J3o main = new Blend2J3o();
		if (args.length == 1) {
			String sourceCsvFilename = args[0];
			try {
				main.addAll(sourceCsvFilename);
			} catch (IOException ex) {
				Logger.getLogger(Blend2J3o.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		AppSettings settings = new AppSettings(true);
		main.setSettings(settings);
		main.setShowSettings(false);
		main.start();
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

	private void addAll(String sourceCsvFilename) throws FileNotFoundException, IOException {
		Reader in = new FileReader(sourceCsvFilename);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			String source = record.get(0);
			String target = "assets/"+ source.replace(".blend", ".j3o");
			System.out.println("Source: "+ source +", Target: "+ target);
			this.add(source, target);
		}
	}

	private void processAll() {
		for (Map.Entry<String, String> entry : this.files.entrySet()) {
			Spatial spatial = assetManager.loadModel(entry.getKey());
			rootNode.attachChild(spatial);
			File file = new File(entry.getValue());
			try {
				getBinaryExporter().save(spatial, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			spatial.removeFromParent();
		}
	}
}
