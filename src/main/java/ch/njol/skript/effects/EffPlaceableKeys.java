package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Apply Placeable Keys")
@Description("Allow or prevent an item to be place on other itemtypes while in /gamemode adventure.")
@Examples("allow player's tool to be placed on (stone and oak wood planks)")
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffPlaceableKeys extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setDestroyableKeys")) {
			Skript.registerEffect(EffPlaceableKeys.class,
				"allow %~itemtypes% to be placed on %itemtypes%",
				"prevent %~itemtypes% from being placed on %itemtypes%");
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> modifyWith;
	private boolean remove;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		this.modifyWith = (Expression<ItemType>) exprs[1];
		this.remove = matchedPattern != 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length == 0)
			return;

		Set<Namespaced> keys = new HashSet<>();

		for (ItemType item : modifyWith.getArray(event)) {
			keys.add(item.getRandom().getType().getKey());
		}

		for (ItemType item : items) {

			if (item.getRandom().hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasPlaceableKeys()) {
					Set<Namespaced> alreadyOn = meta.getPlaceableKeys();
					if (this.remove) {
						alreadyOn.removeAll(keys);
					} else {
						alreadyOn.addAll(keys);
					}
					meta.setPlaceableKeys(keys);
				}

				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (remove) {
			return "allow " + items.toString(event, debug) + " to be placed on " + modifyWith.toString(event, debug);
		} else {
			return "prevent " + items.toString(event, debug) + " from being placed on " + modifyWith.toString(event, debug);
		}
	}
}
