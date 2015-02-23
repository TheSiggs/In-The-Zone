package nz.dcoder.inthezone.graphics;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import nz.dcoder.inthezone.data_model.pure.Position;

public class SaveablePosition implements Savable {
	private Position p = null;

	public SaveablePosition() {
	}

	public SaveablePosition(Position p) {
		this.p = p;
	}

	public void setPosition(Position p) {
		this.p = p;
	}

	public Position getPosition() {
		return p;
	}

	@Override public void write(JmeExporter ex) throws IOException {
		OutputCapsule capsule = ex.getCapsule(this);
		capsule.write(p.x, "x", 0);
		capsule.write(p.y, "y", 0);
	}

	@Override public void read(JmeImporter im) throws IOException {
		InputCapsule capsule = im.getCapsule(this);
		int x = capsule.readInt("x", 0);
		int y = capsule.readInt("y", 0);
		this.p = new Position(x, y);
	}
}

