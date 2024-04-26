/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Has Properties")
@Description("Allows users to check the properties of an armor stand (i.e. whether its small, a marker, ticking, or moving).")
@Examples({
	"send true if {_armorstand} is small else false",
	"if {_armorstand} isn't a marker:",
	"if {_armorstands::*} is not ticking:",
	"if {_armorstand} is moving:"
})
@Since("INSERT VERSION")
public class CondArmorStandProperties extends Condition {

	static {
		Skript.registerCondition(CondArmorStandProperties.class,
			"%livingentities% is[not: not|'nt] (0:small|1:a marker|2:ticking|3:moving)"
		);
	}

	private static final int SMALL = 0;
	private static final int MARKER = 1;
	private static final int TICKING = 2;
	private static final int MOVING = 3;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	private int property;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		property = parseResult.mark;
		setNegated(parseResult.hasTag("not"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (property == SMALL) {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).isSmall(), isNegated());
		} else if (property == MARKER) {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).isMarker(), isNegated());
		} else if (property == TICKING) {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).canTick(), isNegated());
		} else if (property == MOVING) {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).canMove(), isNegated());
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String prop = "unknown property";
		if (property == SMALL) {
			prop = "small";
		} else if (property == MARKER) {
			prop = "a marker";
		} else if (property == TICKING) {
			prop = "ticking";
		} else if (property == MOVING) {
			prop = "moving";
		}
		return entities.toString(event, debug) + " is " + (isNegated() ? "not " : "") + prop;
	}
}
