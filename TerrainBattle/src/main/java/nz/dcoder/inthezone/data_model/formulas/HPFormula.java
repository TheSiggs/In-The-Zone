package nz.dcoder.inthezone.data_model.formulas;

import java.util.HashMap;
import java.util.Map;

public class HPFormula extends Formula {
	private static final Map<String, Class> varTypes;
	private static Object expression = null;

	static {
		varTypes = new HashMap<String, Class>();
		varTypes.put("vitality", Integer.class);
		varTypes.put("hpMod", Integer.class);

		expression = Formula.parseExpression(
			"/nz/dcoder/inthezone/data/formulas/hp.txt", varTypes);
	}

	@Override
	protected Object getExpression() {
		return expression;
	}

	@Override
	protected Map<String, Class> getVarTypes() {
		return varTypes;
	}

	public HPFormula() {
		super("hp");
		super.vars.put("rnd", Math.random());
	}
}


