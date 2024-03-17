package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Name("Has Build Restrictions")
@Description("Check if an item has any build restrictions.")
@Examples({"player's tool has destroyable blocks", "{_item} doesn't have placeable keys"})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class CondHasBuildRestrictions extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "hasDestroyableKeys")) {
			register(CondHasBuildRestrictions.class, "(destroy[able]|place:place[able]) (key|item|block)[s]","itemtypes");
		}
	}

	private boolean destroy;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		destroy = !parseResult.hasTag("place");
		return true;
	}

	@Override
	public boolean check(ItemType item) {
		return item.getRandom().hasItemMeta() && (destroy ? item.getItemMeta().hasDestroyableKeys() : item.getItemMeta().hasPlaceableKeys());
    }

	@Override
	protected @NotNull String getPropertyName() {
		return (destroy ? "destroyable" : "placeable") + "keys";
	}
}
