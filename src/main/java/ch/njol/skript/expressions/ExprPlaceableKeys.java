package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Name("Placeable Keys Of Item")
@Description("Get or modify the placeable keys of an item.")
@Examples({"add dirt to place items of player's tool",
	"add stone and diamond ore to place on blocks of {_item}",
	"clear placeable items of {_item}",
	"remove sand from place keys of {_item}"})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class ExprPlaceableKeys extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getPlaceableKeys")) {
			register(ExprPlaceableKeys.class, ItemType.class, "place[able| on] (keys|items|blocks)", "itemtypes");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		return true;
	}

	@SuppressWarnings("")
	@Override
	protected ItemType @NotNull [] get(Event event, ItemType [] source) {
		Set<ItemType> onItem = new HashSet<>();
		for (ItemType item : source) {
			if (item.getRandom().hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasPlaceableKeys()) {
					for (Namespaced key : meta.getPlaceableKeys()) {
						Material material = BukkitUnsafe.getMaterialFromMinecraftId(key.toString());
						if (material != null) {
							onItem.add(new ItemType(material));
						}
					}
				}
			}
		}
		return onItem.toArray(new ItemType[0]);
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

	@SuppressWarnings("ConstantValue")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ItemType[] source = getExpr().getArray(event);

		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			for (ItemType item : source) {
				ItemMeta meta = item.getItemMeta();
				Collection<Namespaced> empty = new ArrayList<>();
				meta.setPlaceableKeys(empty);
				item.setItemMeta(meta);
			}
		} else if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {

			Set<Namespaced> keys = new HashSet<>();

			if (delta != null) {
				for (Object o : delta) {
					if (o instanceof ItemType item) {
						keys.add(item.getMaterial().getKey());
					}
				}
			}

			if (mode == ChangeMode.SET) {
				for (ItemType item : source) {
					ItemMeta meta = item.getItemMeta();
					meta.setPlaceableKeys(keys);
					item.setItemMeta(meta);
				}
			} else if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
				for (ItemType item : source) {
					ItemMeta meta = item.getItemMeta();
					if (meta.hasPlaceableKeys()) {
						Set<Namespaced> alreadyOn = meta.getPlaceableKeys();
						if (mode == ChangeMode.ADD) {
							alreadyOn.addAll(keys);
						} else if (mode == ChangeMode.REMOVE) {
							alreadyOn.removeAll(keys);
						}
						meta.setPlaceableKeys(alreadyOn);
					}

					item.setItemMeta(meta);
				}
			}
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
	public @NotNull String toString(@Nullable Event e, boolean b) {
		return "placeable keys of " + getExpr().toString(e, b);
	}

}
