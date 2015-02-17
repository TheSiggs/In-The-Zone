package nz.dcoder.inthezone.data_model.formulas;

import java.util.HashMap;
import java.util.Map;

public class HealingFormula extends Formula {
	private static final Map<String, Class> varTypes;
	private static Object expression = null;

	static {
		varTypes = new HashMap<String, Class>();
		varTypes.put("c", Double.class);
		varTypes.put("agent_strength", Integer.class);
		varTypes.put("agent_intelligence", Integer.class);
		varTypes.put("agent_level", Integer.class);
		varTypes.put("agent_physicalWeapon", Integer.class);
		varTypes.put("agent_magicalWeapon", Integer.class);
		varTypes.put("target_guard", Integer.class);
		varTypes.put("target_spirit", Integer.class);
		varTypes.put("target_level", Integer.class);
		varTypes.put("target_physicalArmour", Integer.class);
		varTypes.put("target_magicalArmour", Integer.class);
		varTypes.put("rnd", Double.class);

		expression = Formula.parseExpression(
			"/nz/dcoder/inthezone/data/formulas/healing.txt", varTypes);
	}


	@Override
	protected Object getExpression() {
		return expression;
	}

	@Override
	protected Map<String, Class> getVarTypes() {
		return varTypes;
	}

	public HealingFormula() {
		super("healing");
		super.vars.put("rnd", Math.random());
	}
}

