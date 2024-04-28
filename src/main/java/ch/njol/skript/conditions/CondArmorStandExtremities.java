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

@Name("Armor Stand - Has Extremities")
@Description("Allows users to check the extremities of an armor stand (i.e. whether it has arms or a base plate).")
@Examples({
	"send true if {_armorstand} has arms",
	"if {_armorstand} has no base plate"
})
@Since("INSERT VERSION")
public class CondArmorStandExtremities extends Condition {

	static {
		Skript.registerCondition(CondArmorStandExtremities.class,
			"%livingentities% (has|have) (:arms|[a] base[ ]plate)",
			"%livingentities% (doesn't|does not|do not|don't) have (:arms|[a] base[ ]plate)"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	private boolean arms;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		arms = parseResult.hasTag("arms");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (arms)
			return entities.check(event,
				stand -> stand instanceof ArmorStand && ((ArmorStand) stand).hasArms(), isNegated());
		return entities.check(event,
			stand -> stand instanceof ArmorStand && ((ArmorStand) stand).hasBasePlate(), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entities.toString(event, debug) + " has " + (isNegated() ? "no " : "") + (arms ? "arms" : "base plate");
	}
}
