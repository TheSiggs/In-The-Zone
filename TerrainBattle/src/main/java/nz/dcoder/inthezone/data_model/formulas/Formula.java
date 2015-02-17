package nz.dcoder.inthezone.data_model.formulas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import nz.dcoder.inthezone.data_model.utils.UnicodeInputReader;

public abstract class Formula {

	protected static Object parseExpression(
		String filename, Map<String, Class> varTypes
	) {
		try {
			InputStream in = Formula.class.getResourceAsStream(filename);
			if (in == null)
				throw new FileNotFoundException("File not found " + filename);
			BufferedReader reader =
				new BufferedReader(new UnicodeInputReader(in));
			StringBuilder rawExpression = new StringBuilder();
			reader.lines().forEach(l -> rawExpression.append(l).append("\n"));
			in.close();
			
			ParserContext ctx = new ParserContext();
			ctx.setStrongTyping(true);
			ctx.setInputs(varTypes);

			return MVEL.compileExpression(rawExpression.toString(), ctx);

		} catch (Exception e) {
			throw new RuntimeException(
				"Error parsing expression in " + filename + ": " + e.getMessage(), e);
		}
	}

	protected abstract Object getExpression();
	protected abstract Map<String, Class> getVarTypes();

	private String name;
	protected final Map<String, Object> vars;

	public Formula(String name) {
		this.name = name;
		vars = new HashMap<String, Object>();
	}

	public void setVariable(String name, Object value) {
		Map<String, Class> varTypes = getVarTypes();
		if (!varTypes.containsKey(name)
			|| varTypes.get(name).isInstance(value)
		) {
			throw new RuntimeException(
				"Error invoking damage formula with parameter " + name);
		}
		vars.put(name, value);
	}

	public double evaluate() throws FormulaException {
		try {
			double r = (double) MVEL.executeExpression(
				getExpression(), vars, Double.class);
			return r;
		} catch (Exception e) {
			throw new FormulaException(
				"Error evaluating formula " + name + ": " + e.getMessage(), e);
		}
	}
}

