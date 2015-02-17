package nz.dcoder.inthezone.tests;

import nz.dcoder.inthezone.data_model.*;
import nz.dcoder.inthezone.data_model.formulas.*;
import nz.dcoder.inthezone.data_model.pure.*;

import org.junit.Test;
import static	org.junit.Assert.*;

public class FormulaTests {
	@Test public void testHealing() {
		new HealingFormula();
	}

	@Test public void testHP() {
		new HPFormula();
	}

	@Test public void testMagicalDamage() {
		new MagicalDamage();
	}

	@Test public void testPhysicalDamage() {
		new PhysicalDamage();
	}
}


