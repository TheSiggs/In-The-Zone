package inthezone.battle.data;

import java.util.Optional;

/**
 * A partially or fully autogenerated ability description.
 * */
public class AbilityDescription {
	private final AbilityInfo ability;
	private String description;

	public AbilityDescription(final AbilityInfo ability) {
		this.ability = ability;
		this.description = generateDescription(ability);
	}

	@Override public String toString() { return description; }

	public String getTitle() {
		return ability.name;
	}

	public String getInfoLine() {
		final StringBuilder s = new StringBuilder();
		generateInfoLine(s, ability);
		return s.toString();
	}

	public String getLoadoutInfoLine() {
		final StringBuilder s = new StringBuilder();
		s.append(ability.ap).append(" AP");
		if (ability.mp > 0) s.append(", ").append(ability.mp).append(" MP");
		if (ability.range.range > 1) s.append(", Range ").append(ability.range.range);
		return s.toString();
	}

	public String getDescription() {
		StringBuilder s = new StringBuilder();
		generateBannedNotice(s, ability);
		final String p1 = s.toString();

		s = new StringBuilder();
		generateAbilityDescription(s, ability);
		final String p2 = s.toString();

		return p1 + p2.substring(0, 1).toUpperCase() + p2.substring(1);
	}

	private static String generateDescription(final AbilityInfo a) {
		StringBuilder s = new StringBuilder();
		s.append(a.name).append(" ");
		generateInfoLine(s, a);
		generateBannedNotice(s, a);
		s.append("\n");
		final String p1 = s.toString();
		s = new StringBuilder();
		generateAbilityDescription(s, a);
		final String p2 = s.toString();
		return p1 + p2.substring(0, 1).toUpperCase() + p2.substring(1);
	}

	private static void generateInfoLine(
		final StringBuilder s, final AbilityInfo a
	) {
		s.append("(").append(a.type.toString());
		if (a.isMana) s.append(" / mana");
		s.append(")");

		s.append(" -- ").append(a.ap).append(" AP");
		if (a.mp > 0) {
			s.append(", ").append(a.mp).append(" MP");
		}
		if (a.range.range > 1) {
			s.append(", Range ").append(a.range.range);
		}
	}

	private static void generateBannedNotice(
		final StringBuilder s, final AbilityInfo a
	) {
		if (a.banned) {
			s.append("\n");
			s.append("=== NOTICE ===");
			s.append("\"").append(a.name).append("\" is no longer permitted for tournament play.");
			s.append("Please remove \"").append(a.name).append("\" from all your characters.");
			s.append("\n");
		}
	}

	private static void generateAbilityDescription(
		final StringBuilder s, final AbilityInfo a
	) {
		if (a.isMana) s.append("when standing on a mana zone, ");
		generateTopLevelAbility(s, true, 0, a);
	}

	private static void generateTopLevelAbility(
		final StringBuilder s, final boolean doItOnce,
		final int maxDistanceFromSelf, final AbilityInfo a
	) {
		if (a.trap) {
			generateTrap(s, a);
		} else if (a.zone != AbilityZoneType.NONE) {
			generateZone(s, a);
		} else {
			generateAbility(s, maxDistanceFromSelf, a);
		}

		Optional<AbilityInfo> next = a.subsequent;
		if (next.isPresent()) {
			AbilityInfo n = next.get();
			final boolean nextDoItOnce = doItOnce &&
				((n.range.nTargets == 1 && n.range.radius == 0) ||
					n.range.targetMode.equals(selfOnly));
			final int nextMaxDistanceFromSelf = maxDistanceFromSelf + a.range.range;

			s.append(" Next, ");
			if (a.range.range > 0 && next.get().range.range > 0) {
				if (doItOnce && n.range.targetMode.equals(selfOnly)) {
					/* append nothing */
				} else if (doItOnce) {
					s.append("from each of the previous targets, ");
				} else {
					s.append("from the previous target, ");
				}
			}

			generateTopLevelAbility(s, nextDoItOnce, nextMaxDistanceFromSelf, next.get());
		}
	}

	private static void generateZone(final StringBuilder s, final AbilityInfo a) {
		final boolean isBound = a.zone == AbilityZoneType.BOUND_ZONE;
		final InstantEffectInfo obstacles;

		try {
			obstacles = new InstantEffectInfo("obstacles");
		} catch (Exception e) {
			throw new RuntimeException("obstacles type is invalid.  This cannot happen");
		}

		if (isBound && !a.instantBefore.equals(Optional.of(obstacles))) {
			s.append("ERROR: This ability attempts to make a bound zone, but it doesn't have obstacles as it's pre-effect");
		} else {
			s.append("create");
			if (a.range.nTargets == 1) {
				s.append(" a");
				if (isBound) s.append(" bound");
				s.append(" zone");
			} else {
				s.append(" ").append(a.range.nTargets);
				if (isBound) s.append(" bound");
				s.append(" zones");
			}
			s.append(" with radius ").append(a.range.radius);
			if (a.range.piercing) s.append(" including the attack path");
			if (a.range.nTargets > 1) {
				s.append(". The zones will ");
			} else {
				s.append(". The zone will ");
			}
			generateAbility(s, 0, a);
		}
	}

	private static void generateTrap(final StringBuilder s, final AbilityInfo a) {
		s.append("Place a trap which is activated when stepped on by ");
		if (a.range.targetMode.enemies && a.range.targetMode.allies && a.range.targetMode.self) {
			s.append("anyone");
		} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
			s.append("any character");
			if (!a.range.targetMode.self) s.append(" (but not you)");
		} else if (a.range.targetMode.enemies) {
			s.append("enemies");
			if (a.range.targetMode.self) s.append(" (also activated when you step on it)");
		} else if (a.range.targetMode.allies) {
			s.append("allies");
			if (!a.range.targetMode.self) s.append(" (but not you)");
		} else {
			s.append("yourself only");
		}

		s.append(". When activated, the trap will ");
		generateAbility(s, 0, a);
	}

	private static void generateAbility(
		final StringBuilder s, final int maxDistanceFromSelf, final AbilityInfo a
	) {
		final int nextMaxDistanceFromSelf = maxDistanceFromSelf + a.range.range;

		final InstantEffectInfo obstacles;
		try {
			obstacles = new InstantEffectInfo("obstacles");
		} catch (Exception e) {
			throw new RuntimeException("obstacles type is invalid.  This cannot happen");
		}

		boolean isSecondary = false;
		if (a.instantBefore.isPresent()) {
			if (!(a.zone == AbilityZoneType.BOUND_ZONE && a.instantBefore.get().equals(obstacles))) {
				generateInstantEffect(s, a, nextMaxDistanceFromSelf,
					a.instantBefore.get(), isSecondary);
				isSecondary = true;
			}
		}

		boolean noDamage = false;
		if (a.eff == 0) {
			noDamage = true;

		} else if (a.heal) {
			if (isSecondary) s.append(", then ");

			s.append("heal");
			generateTargetMode(s, a, nextMaxDistanceFromSelf, isSecondary, false);
			s.append(" for ").append(formatDouble(a.eff, 2)).append("x heal");
			isSecondary = true;

		} else {
			if (isSecondary) s.append(", then ");

			s.append("attack");
			generateTargetMode(s, a, nextMaxDistanceFromSelf, isSecondary, false);
			s.append(" for ").append(formatDouble(a.eff, 2)).append("x damage");
			isSecondary = true;
		}

		if (a.statusEffect.isPresent()) {
			if (noDamage) {
				if (isSecondary) s.append(", then ");

				if (a.statusEffect.get().kind == StatusEffectKind.BUFF) {
					s.append("give ");
					s.append(a.statusEffect.get().toNiceString());
					s.append(" to");
				} else {
					s.append("inflict ");
					s.append(a.statusEffect.get().toNiceString());
					s.append(" on");
				}

				generateTargetMode(s, a, nextMaxDistanceFromSelf, false, false);

				if (a.chance < 1f) {
					s.append(" with a ").append(formatPercent(a.chance)).append(" chance of success");
				}

			} else if (a.chance < 1f) {
				s.append(" with a ").append(formatPercent(a.chance)).append(" chance to");
				if (a.statusEffect.get().kind == StatusEffectKind.BUFF) {
					s.append(" give ");
				} else {
					s.append(" inflict ");
				}
				s.append(a.statusEffect.get().toNiceString());

			} else {
				if (a.statusEffect.get().kind == StatusEffectKind.BUFF) {
					s.append(", giving ");
				} else {
					s.append(", inflicting ");
				}
				s.append(a.statusEffect.get().toNiceString());
			}

			isSecondary = true;
		}


		if (a.instantAfter.isPresent()) {
			if (isSecondary) s.append(", then ");
			generateInstantEffect(s, a, nextMaxDistanceFromSelf,
				a.instantAfter.get(), isSecondary);
		}

		if (noDamage && !a.instantBefore.isPresent() &&
			!a.instantAfter.isPresent() && !a.statusEffect.isPresent())
		{
			s.append("Do nothing");
		}

		s.append(".");

		if (!a.range.los) {
			s.append(" Does not require line of sight to the target(s).");
		}

		if (a.recursion == 1) {
			s.append(" Rebounds once.");
		} else if (a.recursion == 2) {
			s.append(" Rebounds twice.");
		} else if (a.recursion > 2) {
			s.append(" Rebounds ").append(a.recursion).append(" times.");
		}
	}

	private static String formatDouble(final double d, final int decimalPlaces) {
		String r = "" + Math.round(d * (Math.pow(10, decimalPlaces)));
		while (r.length() < (decimalPlaces + 1)) r = "0" + r;
		while (r.endsWith("0")) r = r.substring(0, r.length() - 1);
		r = r.substring(0, 1) + "." + r.substring(1);
		if (r.endsWith(".")) return r.substring(0, r.length() - 1); else return r;
	}

	private static String formatPercent(final double d) {
		return "" + Math.round(d * 100) + "%";
	}

	private static void generateInstantEffect(
		final StringBuilder s, final AbilityInfo a,
		final int maxDistanceFromSelf, final InstantEffectInfo e,
		final boolean isSecondaryEffect
	) {
		switch (e.type) {
			case CLEANSE:
				s.append("cleanse");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				break;
			case DEFUSE:
				s.append("defuse");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, true);
				break;
			case PURGE:
				s.append("purge");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, true);
				break;
			case PUSH:
				s.append("push");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				if (e.param == 1) {
					s.append(" one square");
				} else {
					s.append(" ").append(e.param).append(" squares");
				}
				break;
			case PULL:
				s.append("pull");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				if (e.param == 1) {
					s.append(" one square");
				} else {
					s.append(" ").append(e.param).append(" squares");
				}
				break;
			case SCAN:
				s.append("scan");
				break;
			case TELEPORT:
				s.append("Teleport");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				if (e.param == 1) {
					s.append(" to an adjacent square");
				} else {
					s.append(" up to ").append(e.param).append(" squares");
				}
				break;
			case OBSTACLES:
				s.append("place");
				if (a.range.nTargets == 1) {
					s.append(" one obstacle which persists until destroyed");
				} else {
					s.append(a.range.nTargets).append(" obstacles which persist until destroyed");
				}
				break;
			case MOVE:
				s.append("move");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				if (e.param == 1) {
					s.append(" to an adjacent square");
				} else {
					s.append(" up to ").append(e.param).append(" squares");
				}
				break;
			case REVIVE:
				s.append("revive");
				generateTargetMode(s, a, maxDistanceFromSelf, isSecondaryEffect, false);
				break;
		}
	}

	private static void generateSecondaryTargetMode(
		final StringBuilder s, final AbilityInfo a, final boolean noTargetCharacters
	) {
		boolean trapZone = a.trap || a.zone == AbilityZoneType.BOUND_ZONE;

		if (noTargetCharacters) {
			s.append(" all targeted squares");
		} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
			s.append(" all targeted characters");
		} else if (a.range.targetMode.enemies) {
			s.append(" all targeted enemies");
		} else if (a.range.targetMode.allies) {
			s.append(" all targeted allies");
		} else {
			s.append(" yourself");
			if (!trapZone) {
				s.append(" (only if you targeted yourself)");
			}
		}
	}

	private static final TargetMode selfOnly = new TargetMode("S");

	private static void generateTargetMode(
		final StringBuilder s,
		final AbilityInfo a,
		final int maxDistanceFromSelf,
		final boolean isSecondary,
		final boolean noTargetCharacters
	) {
		if (isSecondary) {
			generateSecondaryTargetMode(s, a, noTargetCharacters);
			return;
		}

		boolean trapZone = a.trap ||
			a.zone == AbilityZoneType.BOUND_ZONE || a.zone == AbilityZoneType.ZONE;

		if (a.range.range == 0 || trapZone) {
			if (a.range.radius > 0 && a.zone == AbilityZoneType.NONE) {
				if (a.trap) s.append(" the target and");
				
				if (!(a.range.targetMode.equals(selfOnly))) {
					s.append(" all");
					if (a.range.radius == 1) s.append(" adjacent");
				}

				final String cont;
				if (noTargetCharacters) {
					s.append(" squares");
					cont = "squares";
				} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
					s.append(" characters");
					if (!a.range.targetMode.self) s.append(" (but not you)");
					cont = "characters";
				} else if (a.range.targetMode.enemies) {
					s.append(" enemies");
					if (a.range.targetMode.self) s.append(" (also including yourself)");
					cont = "enemies";
				} else if (a.range.targetMode.allies) {
					s.append(" allies");
					if (!a.range.targetMode.self) s.append(" (but not you)");
					cont = "allies";
				} else {
					cont = "";
					s.append(" yourself");
				}

				if (a.range.targetMode.equals(selfOnly) && a.range.radius >= maxDistanceFromSelf) {
					/* append nothing */
				} else if (a.range.radius > 1 && !trapZone) {
					if (a.range.targetMode.equals(selfOnly)) {
						s.append(" if you are");
					}
					s.append(" within range ").append(a.range.radius);
				}

				if (a.range.piercing && !trapZone && !a.range.targetMode.equals(selfOnly)) {
					s.append(", including all ").append(cont).append(" on the attack path,");
				}

			} else if (a.range.targetMode.equals(selfOnly)) {
				if (noTargetCharacters) s.append(" your square"); else s.append(" yourself");

			} else {
				if (a.zone != AbilityZoneType.NONE) {
					if (noTargetCharacters) {
						s.append(" every square in the zone");
					} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
						s.append(" any character");
						if (!a.range.targetMode.self) s.append(" (but not you)");
					} else if (a.range.targetMode.enemies) {
						s.append(" any enemy");
						if (a.range.targetMode.self) s.append(" (also including yourself)");
					} else if (a.range.targetMode.allies) {
						s.append(" any ally");
						if (!a.range.targetMode.self) s.append(" (but not you)");
					} else {
						s.append(" yourself");
					}
				} else if (a.trap || a.isSubsequent) {
					s.append(" the target");
				} else if (noTargetCharacters) {
					s.append(" your square");
				} else {
					s.append(a.range.targetMode.toString())
						.append(" (but this is range 0 so enemies and allies cannot be affected)");
				}
			}
		} else if (a.range.radius == 0) {
			if (!a.range.targetMode.equals(selfOnly)) {
				if (a.range.nTargets == 0) {
					s.append(" NO");
				} else if (a.range.nTargets == 1) {
					s.append(" one");
				} else s.append(" ").append(a.range.nTargets);

				if (a.range.range == 1) s.append(" adjacent");
			}

			final String cont;
			if (noTargetCharacters) {
				s.append(a.range.nTargets == 1? " square" : " squares");
				cont = "squares";
			} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
				s.append(a.range.nTargets == 1? " character" : " characters");
				cont = "characters";
				if (!a.range.targetMode.self) s.append(" (but not you)");
			} else if (a.range.targetMode.enemies) {
				s.append(a.range.nTargets == 1? " enemy" : " enemies");
				cont = "enemies";
				if (a.range.targetMode.self) s.append(" (also including yourself)");
			} else if (a.range.targetMode.allies) {
				s.append(a.range.nTargets == 1? " ally" : " allies");
				cont = "allies";
				if (!a.range.targetMode.self) s.append(" (but not you)");
			} else {
				s.append(" yourself");
				cont = "<THIS IS SURELY AN ERROR>";
			}

			if (a.range.piercing && !trapZone && !a.range.targetMode.equals(selfOnly)) {
				s.append(", including all ").append(cont).append(" on the attack path,");
			}

		} else {
			if (!a.range.targetMode.equals(selfOnly)) s.append(" all");

			final String cont;
			if (noTargetCharacters) {
				s.append(" squares");
				cont = "squares";
			} else if (a.range.targetMode.enemies && a.range.targetMode.allies) {
				s.append(" characters");
				cont = "characters";
				if (!a.range.targetMode.self) s.append(" (but not you)");
			} else if (a.range.targetMode.enemies) {
				s.append(" enemies");
				cont = "enemies";
				if (a.range.targetMode.self) s.append(" (also including yourself)");
			} else if (a.range.targetMode.allies) {
				s.append(" allies");
				cont = "allies";
				if (!a.range.targetMode.self) s.append(" (but not you)");
			} else {
				s.append(" yourself if you are");
				cont = "<THIS IS SURELY AN ERROR>";
			}

			s.append(" within");

			if (a.range.radius == 1) {
				s.append(" one square");
			} else {
				s.append(" ").append(a.range.radius).append(" squares");
			}

			if (a.range.nTargets == 1) {
				s.append(" of the targeted square");
			} else {
				s.append(" of any of the ").append(a.range.nTargets).append(" targeted squares");
			}

			if (a.range.piercing && !trapZone && !a.range.targetMode.equals(selfOnly)) {
				s.append(", including all ").append(cont).append(" on the attack path,");
			}
		}
	}
}

