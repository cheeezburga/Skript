package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class EffPlaceableKeys extends Effect {

	static {
		Skript.registerEffect(EffPlaceableKeys.class,
			"allow %~itemtypes% to be placed on %itemtypes/namespacedkeys%",
			"prevent %~itemtypes% from being placed on %itemtypes/namespacedkeys%");
		// again, could these patterns be combined with a parse tag?
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> modifyWith;
	private boolean remove;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		this.modifyWith = exprs[1];
		this.remove = matchedPattern != 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length == 0) {
			return;
		}

		Set<Namespaced> keys = new HashSet<>();

		for (Object o : modifyWith.getArray(event)) {
			if (o instanceof ItemType item) {
				keys.add(item.getMaterial().getKey());
			} else if (o instanceof NamespacedKey key) {
				keys.add(key);
			}
		}

		for (ItemType item : items) {
			ItemMeta meta = item.getItemMeta();

			if (this.remove) {
				if (meta.hasPlaceableKeys()) {
					Set<Namespaced> alreadyOn = meta.getPlaceableKeys();
					alreadyOn.removeAll(keys);
					meta.setPlaceableKeys(alreadyOn);
				}
			} else {
				if (meta.hasPlaceableKeys()) {
					keys.addAll(meta.getPlaceableKeys());
				}
				meta.setPlaceableKeys(keys);
			}

			item.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return remove
			? "allow " + items.toString(event, debug) + " to be placed on " + modifyWith.toString(event, debug)
			: "prevent " + items.toString(event, debug) + " from being placed on " + modifyWith.toString(event, debug);
	}
}
