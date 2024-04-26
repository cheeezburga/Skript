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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Properties")
@Description("Allows users to modify properties of an armor stand (i.e. whether its small, a marker, ticking, or moving).")
@Examples({
	"make {_armorstand} small",
	"make {_armorstand} a marker",
	"make {_armorstands::*} stop ticking",
	"make {_armorstand} stop moving"
})
@Since("INSERT VERSION")
public class EffArmorStandProperties extends Effect {

	static {
		Skript.registerEffect(EffArmorStandProperties.class,
			"make %livingentities% [not:(not|stop)] (0:small|1:a marker|2:tick[ing]|3:mov[e|ing])"
		);
	}

	private static final int SMALL = 0;
	private static final int MARKER = 1;
	private static final int TICKING = 2;
	private static final int MOVING = 3;

	private boolean not;
	private int property;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		not = parseResult.hasTag("not");
		property = parseResult.mark;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand) {
				if (property == SMALL) {
					((ArmorStand) entity).setSmall(!not);
				} else if (property == MARKER) {
					((ArmorStand) entity).setMarker(!not);
				} else if (property == TICKING) {
					((ArmorStand) entity).setCanTick(!not);
				} else if (property == MOVING) {
					((ArmorStand) entity).setCanMove(!not);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String prop = "unknown property";
		if (property == SMALL) {
			prop = "small";
		} else if (property == MARKER) {
			prop = "a marker";
		} else if (property == TICKING) {
			prop = "tick";
		} else if (property == MOVING) {
			prop = "move";
		}
		return "make " + entities.toString(event, debug) + (not ? " not " : " ") + prop;
	}
}
