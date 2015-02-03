package nz.dcoder.inthezone.data_model.factories;

import java.io.IOException;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

class RecordValidator {
	private final String[] headerNames;

	public RecordValidator(String[] headerNames) {
		this.headerNames = headerNames;
	}

	public void validate(CSVRecord record) throws IOException {
		for (String header : headerNames) {
			if (!record.isMapped(header)) {
				throw new IOException("Missing expected header " + header);
			}
		}
	}

	public static boolean parseBoolean(String s) throws NumberFormatException {
		String u = s.toUpperCase().trim();
		if (u.equals("TRUE") || u.equals("YES") || u.equals("1")) {
			return true;
		} else if (u.equals("FALSE") || u.equals("NO") || u.equals("0")) {
			return false;
		} else {
			throw new NumberFormatException("Expected a boolean value, saw " + s);
		}
	}
}

