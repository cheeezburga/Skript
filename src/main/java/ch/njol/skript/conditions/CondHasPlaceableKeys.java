package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Name("Has Placeable Keys")
@Description("Check if an item has any placeable keys.")
@Examples("player's tool has placeable keys")
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class CondHasPlaceableKeys extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "hasPlaceableKeys")) {
			register(CondHasPlaceableKeys.class, PropertyType.HAVE, "place[able| on] (key|item|block)[s]", "itemtypes");
		}
	}

	@Override
	public boolean check(ItemType item) {
		return item.getRandom().hasItemMeta() && item.getItemMeta().hasPlaceableKeys();
	}

	@Override
	protected @NotNull String getPropertyName() {
		return "placeable keys";
	}
}
