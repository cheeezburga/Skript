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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Item Tooltips")
@Description("Whether or not the entire or additional tooltip of an item is shown or hidden.")
@Examples({
	"send true if entire tooltip of player's tool is shown",
	"if additional tooltip of {_item} is hidden:"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.20.5+")
public class CondTooltip extends Condition {

	static {
		if (Skript.methodExists(ItemMeta.class, "isHideTooltip")) // this method was added in the same version as the additional tooltip item flag
			Skript.registerCondition(CondTooltip.class,
				"[the] (:entire|additional) tool[ ]tip of %itemtypes% is (:shown|hidden)",
				"%itemtypes%'[s] (:entire|additional) tool[ ]tip is (:shown|hidden)");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean show;
	private boolean entire;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		show = parseResult.hasTag("shown");
		entire = parseResult.hasTag("entire");
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entire)
			return items.check(event, item -> item.getItemMeta().isHideTooltip(), show);
		else
			return items.check(event, item -> item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP), show);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (entire ? "entire " : "additional ") + "tooltip of " + items.toString(event, debug) + (show ? " shown" : " hidden");
	}

}
