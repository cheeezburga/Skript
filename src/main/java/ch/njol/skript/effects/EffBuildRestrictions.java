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
import java.util.List;
import java.util.Set;

@Name("Apply Destroyable Keys")
@Description("Allow or prevent an item to destroy certain types of blocks while in /gamemode adventure.")
@Examples("allow player's tool to destroy (stone and oak wood planks)")
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffBuildRestrictions extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setDestroyableKeys")) {
			Skript.registerEffect(EffBuildRestrictions.class,
				"allow %~itemtypes% to (:(destroy|break|mine|be placed on)) %itemtypes%",
				"(disallow|prevent) %~itemtypes% from (:(destroying|breaking|mining|being placed on)) %itemtypes%");
			// should this require notnull? and can the patterns here be made any better?
		}
	}

	private boolean allow;
	private boolean destroy;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> modifyWith;
	private static final List<String> destroyTags = List.of("destroy", "destroying", "break", "breaking", "mine", "mining");

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		this.modifyWith = (Expression<ItemType>) exprs[1];
		this.allow = matchedPattern == 0;
		this.destroy = destroyTags.stream().anyMatch(parseResult::hasTag);
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
				if (destroy ? meta.hasDestroyableKeys() : meta.hasPlaceableKeys()) {
					Set<Namespaced> alreadyOn = destroy ? meta.getDestroyableKeys() : meta.getPlaceableKeys();
					if (allow) {
						alreadyOn.addAll(keys);
					} else {
						alreadyOn.removeAll(keys);
					}
                    if (destroy) { meta.setDestroyableKeys(alreadyOn); } else { meta.setPlaceableKeys(alreadyOn); }
                }

				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allow) {
			return "allow " + items.toString(event, debug) + " to " + (destroy ? "destroy " : "be placed on ") + modifyWith.toString(event, debug);
		} else {
			return "prevent " + items.toString(event, debug) + " from " + (destroy ? "destroying " : "being placed on ") + modifyWith.toString(event, debug);
		}
	}
}
