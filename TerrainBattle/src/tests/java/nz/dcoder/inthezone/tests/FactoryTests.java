package nz.dcoder.inthezone.tests;

import nz.dcoder.inthezone.data_model.*;
import nz.dcoder.inthezone.data_model.factories.*;
import nz.dcoder.inthezone.data_model.pure.*;

import java.util.Collection;
import org.junit.Test;
import static	org.junit.Assert.*;

public class FactoryTests {
	@Test public void testAbilities() {
		try {
			AbilityFactory factory = new AbilityFactory();
			Ability test = factory.newAbility(new AbilityName("test"));
			assertNotNull("Ability not null", test);

			assertEquals("Ability name", new AbilityName("test"), test.info.name);
			assertEquals("Ability cost", 5, test.info.cost);
			assertEquals("Ability amount", 10, test.info.amount);
			assertEquals("Ability range", 1, test.info.range);
			assertEquals("Ability areaOfEffect", 0, test.info.areaOfEffect);
			assertEquals("Ability isPiercing", false, test.info.isPiercing);
			assertEquals("Ability canPassObstacles", false, test.info.canPassObstacles);
			assertEquals("Ability requiresMana", false, test.info.requiresMana);
			assertEquals("Ability class", "physical", test.info.aClass);
			assertEquals("Ability effect", new EffectName("damage"), test.info.effect);
			assertEquals("Ability effect2", new EffectName("damage"), test.name);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test public void testBattleObjects() {
		try {
			AbilityFactory af = new AbilityFactory();
			BattleObjectFactory factory = new BattleObjectFactory(af);
			BattleObject test = factory.newBattleObject(
				new BattleObjectName("testObj"), new Position(0, 0));
			assertNotNull("Object not null", test);

			assertEquals("BattleObject name", new BattleObjectName("testObj"), test.name);
			assertEquals("BattleObject blocksSpace", true, test.blocksSpace);
			assertEquals("BattleObject blocksPath", false, test.blocksPath);
			assertEquals("BattleObject isAttackable", false, test.isAttackable);
			assertEquals("BattleObject isPushable", false, test.isPushable);
			assertNotNull("BattleObjectAbility not null", test.ability);
			assertEquals("BattleObject position.x", 0, test.position.x);
			assertEquals("BattleObject position.y", 0, test.position.y);
			assertEquals("BattleObject hits", 0, test.hitsRemaining);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test public void testEquipment1() {
		try {
			AbilityFactory af = new AbilityFactory();
			EquipmentFactory factory = new EquipmentFactory(af);
			Equipment test = factory.newEquipment(new EquipmentName("testEq"));
			assertNotNull("Object not null", test);

			assertEquals("Equipment name", new EquipmentName("testEq"), test.name);
			assertEquals("Equipment isHidden", true, test.isHidden);
			assertEquals("Equipment amount", 8, test.amount);
			assertEquals("Equipment baseAP", 9, test.buffs.baseAP);
			assertEquals("Equipment baseMP", 10, test.buffs.baseMP);
			assertEquals("Equipment strength", 11, test.buffs.strength);
			assertEquals("Equipment intelligence", 12, test.buffs.intelligence);
			assertEquals("Equipment dexterity", 13, test.buffs.dexterity);
			assertEquals("Equipment guard", 14, test.buffs.guard);
			assertEquals("Equipment spirit", 15, test.buffs.spirit);
			assertEquals("Equipment vitality", 16, test.buffs.vitality);
			assertEquals("Equipment class", EquipmentClass.ARMOUR, test.eClass);
			assertEquals("Equipment category", new EquipmentCategory("none"), test.category);

			Collection<AbilityInfo> abilities = test.getAbilities();
			assertTrue("Equipment abilities", abilities.isEmpty());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test public void testEquipment2() {
		try {
			AbilityFactory af = new AbilityFactory();
			EquipmentFactory factory = new EquipmentFactory(af);
			Equipment test = factory.newEquipment(new EquipmentName("testEq2"));
			assertNotNull("Object not null", test);

			assertEquals("Equipment name", new EquipmentName("testEq2"), test.name);
			assertEquals("Equipment isHidden", true, test.isHidden);
			assertEquals("Equipment amount", 9, test.amount);
			assertEquals("Equipment baseAP", 10, test.buffs.baseAP);
			assertEquals("Equipment baseMP", 11, test.buffs.baseMP);
			assertEquals("Equipment strength", 12, test.buffs.strength);
			assertEquals("Equipment intelligence", 13, test.buffs.intelligence);
			assertEquals("Equipment dexterity", 14, test.buffs.dexterity);
			assertEquals("Equipment guard", 15, test.buffs.guard);
			assertEquals("Equipment spirit", 16, test.buffs.spirit);
			assertEquals("Equipment vitality", 17, test.buffs.vitality);
			assertEquals("Equipment class", EquipmentClass.WEAPON, test.eClass);
			assertEquals("Equipment category", new EquipmentCategory("none"), test.category);

			Collection<AbilityInfo> abilities = test.getAbilities();
			int count = 0;
			for(AbilityInfo i : abilities) {
				count += 1;
				assertEquals("Equipment ability", new AbilityName("test"), i.name);
			}
			assertEquals("Equipment ability count", 1, count);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test public void testItem() {
		try {
			AbilityFactory af = new AbilityFactory();
			ItemFactory factory = new ItemFactory(af);
			Item test = factory.newItem(new ItemName("testItem")); 
			assertNotNull("Object not null", test);

			assertEquals("Item name", new ItemName("testItem"), test.name);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test public void testCharacter() {
		try {
			AbilityFactory af = new AbilityFactory();
			CharacterFactory factory = new CharacterFactory(af);
			nz.dcoder.inthezone.data_model.Character test =
				factory.newCharacter(new CharacterName("testChar"), 2); 
			assertNotNull("Object not null", test);

			assertEquals("Character name ", new CharacterName("testChar"), test.name);
			BaseStats stats = test.getBaseStats();
	
			assertEquals("Character baseAP", 10, stats.baseAP);
			assertEquals("Character baseMP", 11, stats.baseMP);
			assertEquals("Character strength", 12, stats.strength);
			assertEquals("Character intelligence", 13, stats.intelligence);
			assertEquals("Character dexterity", 14, stats.dexterity);
			assertEquals("Character guard", 15, stats.guard);
			assertEquals("Character spirit", 16, stats.spirit);
			assertEquals("Character vitality", 17, stats.vitality);

			// TODO: add checks for level, HP, and abilities when those classes are
			// implemented
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}
}

