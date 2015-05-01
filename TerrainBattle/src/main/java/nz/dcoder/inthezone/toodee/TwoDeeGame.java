package nz.dcoder.inthezone.toodee;

import java.util.Date;

public abstract class TwoDeeGame {
	public abstract void simpleInit();
	public abstract void simpleUpdate(float tpf);

	public void start() {
		simpleInit();
		float tpf = 0f;
		while (true) {
			long start = new Date().getTime();
			simpleUpdate(tpf);
			try {
				Thread.sleep((long) 1000/120);
			} catch (InterruptedException ex) {
			}
			tpf = (new Date().getTime() - start) / 1000f;
		}
	}
}
