package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Name("Has Destroyable Keys")
@Description("Check if an item has any destroyable keys.")
@Examples("player's tool has destroyable keys")
@Since("x.x.x") // 2.9.0?
public class CondHasDestroyableKeys extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "hasDestroyableKeys")) {
			register(CondHasDestroyableKeys.class, PropertyType.HAVE, "destroy[able] (key|item|block)[s]", "itemtypes");
		}
	}

	@Override
	public boolean check(ItemType item) {
		return item.getItemMeta().hasDestroyableKeys();
	}

	@Override
	protected @NotNull String getPropertyName() {
		return "destroyable keys";
	}
}