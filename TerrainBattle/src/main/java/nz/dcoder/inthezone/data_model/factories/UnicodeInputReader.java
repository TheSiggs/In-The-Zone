package nz.dcoder.inthezone.data_model.factories;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

class UnicodeInputReader extends InputStreamReader {
	public UnicodeInputReader(InputStream in) throws IOException {
		this(new BOMInputStream(in, 
			ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE));
	}

	public UnicodeInputReader(BOMInputStream bomIn) throws IOException {
		super(bomIn, bomIn.hasBOM()? bomIn.getBOMCharsetName() : "UTF-8");
	}
}

