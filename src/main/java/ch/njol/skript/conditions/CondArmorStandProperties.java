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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Has Properties")
@Description("Allows users to check the properties of an armor stand (i.e. whether it's small or a marker).")
@Examples({
	"send true if {_armorstand} is small",
	"if {_armorstands::*} are not markers:",
})
@Since("INSERT VERSION")
public class CondArmorStandProperties extends Condition {

	static {
		Skript.registerCondition(CondArmorStandProperties.class,
			"%livingentities% (is|are) (:small|[a] marker[s])",
			"%livingentities% (isn't|is not|aren't|are not) (:small|[a] marker[s])"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	private boolean small;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		small = parseResult.hasTag("small");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (small)
			return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).isSmall(), isNegated());
		return entities.check(event, stand -> stand instanceof ArmorStand && ((ArmorStand) stand).isMarker(), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entities.toString(event, debug) + " is " + (isNegated() ? "not " : "") + (small ? "small" : "a marker");
	}

}
