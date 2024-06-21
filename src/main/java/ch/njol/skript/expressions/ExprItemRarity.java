package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ExprItemRarity extends SimplePropertyExpression<ItemType, ItemRarity> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getRarity"))
			register(ExprItemRarity.class, ItemRarity.class, "[item] rarity", "itemtypes");
	}

	@Override
	public ItemRarity convert(ItemType item) {
		if (item.getItemMeta().hasRarity())
			return item.getItemMeta().getRarity();
		Material material = item.getItem().getMaterial();
		return material.isItem() ? asBukkitRarity(material.getItemRarity()) : ItemRarity.COMMON;
		// TODO: remove the material stuff when its removed
	}

	@Override
	public Class<? extends ItemRarity> getReturnType() {
		return ItemRarity.class;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(ItemRarity.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ItemRarity rarity = delta == null ? ItemRarity.COMMON : (ItemRarity) delta[0];
		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			if (mode == ChangeMode.SET)
				meta.setRarity(rarity);
			if (mode == ChangeMode.RESET) {
				Material material = item.getItem().getMaterial();
				if (material.isItem())
					meta.setRarity(asBukkitRarity(material.getItemRarity()));
			}
		}
	}

	@Override
	public String getPropertyName() {
		return "item rarity";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "item rarity of " + getExpr().toString(event, debug);
	}

	private ItemRarity asBukkitRarity(io.papermc.paper.inventory.ItemRarity rarity) {
		switch (rarity) {
			case COMMON:
				return ItemRarity.COMMON;
			case UNCOMMON:
				return ItemRarity.UNCOMMON;
			case RARE:
				return ItemRarity.RARE;
			case EPIC:
				return ItemRarity.EPIC;
			default:
				return ItemRarity.COMMON;
        }
	}
}
