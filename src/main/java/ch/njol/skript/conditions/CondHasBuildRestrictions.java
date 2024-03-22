package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Has Build Restrictions")
@Description("Check if an item has any build restrictions.")
@Examples({"player's tool has destroyable blocks",
	"{_item} doesn't have placeable keys",
	"{_item} is able to break stone and dirt",
	"{_item} can not be placed on diamond ore"})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class CondHasBuildRestrictions extends Condition {
	static {
		if (Skript.methodExists(ItemMeta.class, "hasDestroyableKeys")) {
			Skript.registerCondition(CondHasBuildRestrictions.class,
				"%itemtypes% (can|is able to) (break|destroy|mine|place:be placed on) %itemtypes%",
				"%itemtypes% (can't|can[ ]not|is unable to) (break|destroy|mine|place:be placed on) %itemtypes%",
				"%itemtypes% (has|have) [a|any] (destroy[able]|place:place[able| on]) (key|item|block)[s]",
				"%itemtypes% (doesn't|does not|do not|don't) have [a|any] (destroy[able]|place:place[able| on]) (key|item|block)[s]",
				"");
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> itemTypes;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> keys;
	private boolean place;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.itemTypes = (Expression<ItemType>) exprs[0];
		if (matchedPattern == 0 || matchedPattern == 1)
			this.keys = (Expression<ItemType>) exprs[1];
		this.place = parseResult.hasTag("place");
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@SuppressWarnings("ConstantValue")
	@Override
	public boolean check(Event e) {
		if (keys != null) {
			Set<Namespaced> keysKeys = new HashSet<>();
			for (ItemType c : keys.getArray(e)) {
				for (ItemStack s : c.getAll()) {
					keysKeys.add(s.getType().getKey());
				}
			}

			for (ItemType item : itemTypes.getArray(e)) {
				ItemStack s = item.getRandom();
				if (s.hasItemMeta()) {
					ItemMeta meta = s.getItemMeta();
					if (place ? meta.hasPlaceableKeys() : meta.hasDestroyableKeys()) {
						Set<Namespaced> alreadyOn = place ? meta.getPlaceableKeys() : meta.getDestroyableKeys();
						for (Namespaced key : keysKeys) {
							if (alreadyOn.contains(key))
								return true;
						}
					}
				}
			}
			return false;
		} else {
			return itemTypes.check(e, item -> place ? item.getItemMeta().hasPlaceableKeys() : item.getItemMeta().hasDestroyableKeys(), isNegated());
		}
    }

	@SuppressWarnings("ConstantValue")
	@Override
	public String toString(@Nullable Event e, boolean b) {
		return itemTypes.toString(e, b) + " has" + (isNegated() ? " no" : "") + (place ? " placeable" : " destroyable") + " keys" +
			(keys == null ? "" : " of " + keys.toString(e, b));
	}
}
