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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Name("Adventure Restrictions Of Item")
@Description("Get or modify the adventure restrictions of an item.")
@Examples({
	"add dirt to destroyable restrictions of player's tool",
	"add (stone, diamond ore) to breakable keys of {_item}",
	"clear breakable restrictions of {_item}",
	"remove sand from destroyable keys of {_item}"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class ExprAdventureRestrictions extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getDestroyableKeys")) {
			Skript.registerExpression(ExprAdventureRestrictions.class, ItemType.class, ExpressionType.PROPERTY,
				"[the] (:break|:destroy|:place|:build)able blocks of %itemtypes% [in adventure [mode]]",
				"[the] (:break|:destroy|:place|:build)[able] restrictions of %itemtypes%",
				"%itemtypes%'[s] (:break|:destroy|:place|:build)able blocks [in adventure [mode]]",
				"%itemtypes%'[s] (:break|:destroy|:place|:build)[able] restrictions");
		}
	}

	private boolean destroy;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		this.destroy = parseResult.hasTag("break") || parseResult.hasTag("destroy");
		return true;
	}

	@Override
	protected ItemType @NotNull [] get(Event event, ItemType [] source) {
		Set<ItemType> existingKeys = new HashSet<>();
		for (ItemType item : source) {
			ItemMeta meta = item.getItemMeta();
			if (destroy ? meta.hasDestroyableKeys() : meta.hasPlaceableKeys()) {
				for (Namespaced key : destroy ? meta.getDestroyableKeys() : meta.getPlaceableKeys()) {
					Material material = BukkitUnsafe.getMaterialFromMinecraftId(key.toString());
					if (material != null) {
						existingKeys.add(new ItemType(material));
					}
				}
			}
		}
		return existingKeys.toArray(new ItemType[0]);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			return CollectionUtils.array(ItemType[].class);
		} else if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array();
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ItemType[] source = getExpr().getArray(event);

		Set<Namespaced> deltaKeys = new HashSet<>();
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			for (Object o : delta) {
				if (o instanceof ItemType) {
					for (ItemStack stack : ((ItemType) o).getAll())
						deltaKeys.add(stack.getType().getKey());
				}
			}
		}

		for (ItemType item : source) {

			ItemMeta meta = item.getItemMeta();
			Collection<Namespaced> newKeys = new ArrayList<>();

			switch (mode) {
				case RESET:
				case DELETE:
                    break;
				case SET:
					newKeys = deltaKeys;
					break;
				case ADD:
				case REMOVE:
					newKeys = new HashSet<>(destroy ? meta.getDestroyableKeys() : meta.getPlaceableKeys());
					if (mode == ChangeMode.ADD) {
						newKeys.addAll(deltaKeys);
					} else {
						newKeys.removeAll(deltaKeys);
					}
					break;
				case REMOVE_ALL:
					assert false;
			}

			if (destroy) {
				meta.setDestroyableKeys(newKeys);
			} else {
				meta.setPlaceableKeys(newKeys);
			}

			item.setItemMeta(meta);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public @NotNull Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		if (destroy) {
			return "destroyable restrictions of " + getExpr().toString(event, debug);
		} else {
			return "placeable restrictions of " + getExpr().toString(event, debug);
		}
	}

}
