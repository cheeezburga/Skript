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

@Name("Apply Destroyable Keys")
@Description("Allow or prevent an item to destroy certain types of blocks while in /gamemode adventure.")
@Examples("allow player's tool to destroy (stone and oak wood planks)")
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffDestroyableKeys extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setDestroyableKeys")) {
			Skript.registerEffect(EffDestroyableKeys.class,
				"allow %~itemtypes% to (destroy|break|mine) %itemtypes%",
				"prevent %~itemtypes% from (destroy|break|mine)[ing] %itemtypes%");
			// might add [:dis]allow as an option, but seems like it could be a bit messy
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
			// still not quite sure how to extend this to work with things like `any log`, etc.
			// would it just involve a like, isAll() check and then getAll() if that was true?
			// or maybe just the getAll() call, like fuse suggested?
		}

		for (ItemType item : items) {

			if (item.getRandom().hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasDestroyableKeys()) {
					Set<Namespaced> alreadyOn = meta.getDestroyableKeys();
					if (this.remove) {
						alreadyOn.removeAll(keys);
					} else {
						alreadyOn.addAll(keys);
					}
					meta.setDestroyableKeys(alreadyOn);
				}

				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (remove) {
			return "allow " + items.toString(event, debug) + " to destroy " + modifyWith.toString(event, debug);
		} else {
			return "prevent " + items.toString(event, debug) + " from destroying " + modifyWith.toString(event, debug);
		}
	}
}
