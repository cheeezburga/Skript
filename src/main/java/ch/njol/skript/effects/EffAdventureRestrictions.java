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
import com.destroystokyo.paper.Namespaced;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Name("Apply Adventure Restrictions")
@Description("Allow or prevent an item to destroy or be placed on certain types of blocks while in /gamemode adventure.")
@Examples({
	"allow player's tool to destroy (stone, oak wood planks) in adventure mode",
	"prevent {_item} from being placed on (diamond ore, diamond block) in adventure"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffAdventureRestrictions extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setDestroyableKeys", Collection.class)) {
			Skript.registerEffect(EffAdventureRestrictions.class,
				"allow %~itemtypes% to (destroy|break|mine|place:be placed on) %itemtypes% [in adventure [mode]]",
				"(disallow|prevent) %~itemtypes% from (destroying|breaking|mining|place:being placed on) %itemtypes% [in adventure [mode]]");
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> deltaKeys;
	private boolean allow;
	private boolean destroy;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		this.deltaKeys = (Expression<ItemType>) exprs[1];
		this.allow = matchedPattern == 0;
		this.destroy = !parseResult.hasTag("place");
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length == 0)
			return;

		Set<Namespaced> keys = new HashSet<>();

		for (ItemType itemType : deltaKeys.getArray(event)) {
			Iterator<ItemStack> iter = itemType.containerIterator();
            while (iter.hasNext()) {
                ItemStack stack = iter.next();
				keys.add(stack.getType().getKey());
            }
		}

		if (!keys.isEmpty()) {
			for (ItemType item : items) {
				ItemMeta meta = item.getItemMeta();
				Set<Namespaced> existingKeys = new HashSet<>(destroy ? meta.getDestroyableKeys() : meta.getPlaceableKeys());
				if (allow) {
					existingKeys.addAll(keys);
				} else {
					existingKeys.removeAll(keys);
				}
				if (destroy) { meta.setDestroyableKeys(existingKeys); } else { meta.setPlaceableKeys(existingKeys); }
				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allow)
			return "allow " + items.toString(event, debug) + " to " + (destroy ? "destroy " : "be placed on ") + deltaKeys.toString(event, debug);
		return "prevent " + items.toString(event, debug) + " from " + (destroy ? "destroying " : "being placed on ") + deltaKeys.toString(event, debug);
	}
}
