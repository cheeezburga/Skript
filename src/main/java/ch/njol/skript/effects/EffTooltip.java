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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

@Name("Item Tooltips")
@Description({
	"Show or hide the tooltip of an item.",
	"If changing the 'entire' tooltip of an item, nothing will show up when a player hovers over it."
})
@Examples({
	"hide the entire tooltip of player's tool",
	"hide {_item}'s additional tool tip"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.20.5+")
public class EffTooltip extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "isHideTooltip")) // this method was added in the same version as the additional tooltip item flag
			Skript.registerEffect(EffTooltip.class,
				"(:show|:hide) %itemtypes%'[s] (0:entire|1:additional) tool[ ]tip",
				"(:show|:hide) [the] (0:entire|1:additional) tool[ ]tip of %itemtypes%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean show, entire;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		show = parseResult.hasTag("show");
		entire = parseResult.mark == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType item : items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			if (entire) {
				meta.setHideTooltip(!show);
			} else {
				if (show) {
					meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				} else {
					meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				}
			}
			item.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (show ? "show" : "hide") + " the " + (entire ? "entire" : "additional") + " tooltip of " + items.toString(event, debug);
	}

}
