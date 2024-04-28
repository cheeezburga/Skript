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

@Name("Armor Stand - Has Behaviour")
@Description("Allows users to check the behaviour of an armor stand (i.e. whether it is ticking or moving).")
@Examples({
	"if {_armorstands::*} are not able to tick:",
	"if {_armorstand} is able to move:"
})
@Since("INSERT VERSION")
public class CondArmorStandBehaviour extends Condition {

	static {
		Skript.registerCondition(CondArmorStandBehaviour.class,
			"%livingentities% (is|are) able to (:tick|move)",
			"%livingentities% (isn't|is not|aren't|are not) able to (:tick|move)"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	private boolean tick;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		tick = parseResult.hasTag("tick");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (tick) {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).canTick(), isNegated());
		} else {
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).canMove(), isNegated());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entities.toString(event, debug) + " is " + (isNegated() ? "not " : "") + "able to " + (tick ? "tick" : "move");
	}
}
