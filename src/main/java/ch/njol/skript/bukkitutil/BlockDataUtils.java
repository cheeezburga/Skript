package ch.njol.skript.bukkitutil;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BlockDataUtils {

	public static Material toBlock(ItemStack item) {
		Material material = item.getType();
		if (material.isBlock()) // already a block
			return material;

		return switch (material) {
			case WHEAT_SEEDS -> Material.WHEAT;
			case POTATO -> Material.POTATOES;
			case CARROT -> Material.CARROTS;
			case BEETROOT_SEEDS -> Material.BEETROOTS;
			case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
			case MELON_SEEDS -> Material.MELON_STEM;
			case SWEET_BERRIES -> Material.SWEET_BERRY_BUSH;
			default -> material;
		};
	}

	public static @Nullable Material toValidBlock(ItemStack item) {
		Material asBlock = toBlock(item);
		return asBlock.isBlock() ? asBlock : null;
	}

	public static String @Nullable [] getTagsAndValues(BlockData data) {
		String string = data.getAsString();
		int start = string.indexOf("[");
		int end = string.indexOf("]");

		if (start == -1 || end == -1 || start >= end)
			return null;
		return string.substring(start + 1, end).split(",");
	}

	public static String @Nullable [] getTags(BlockData data) {
		String[] tagsAndValues = getTagsAndValues(data);
		if (tagsAndValues == null)
			return null;

		String[] tags = new String[tagsAndValues.length];
		for (int i = 0; i < tagsAndValues.length; i++) {
			tags[i] = tagsAndValues[i].split("=")[0];
		}
		return tags;
	}

	public static @Nullable Object getValue(BlockData data, String tag) {
		String[] tagsAndValues = getTagsAndValues(data);
		if (tagsAndValues == null)
			return null;

		for (String tagAndValue : tagsAndValues) {
			String[] split = tagAndValue.split("=");
			if (split.length == 2 && split[0].equals(tag)) {
				String value = split[1];
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
					return Boolean.parseBoolean(value);

				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException ignored) {}

				return value;
			}
		}

		return null;
	}

}
