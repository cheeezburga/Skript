package ch.njol.skript.bukkitutil;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

public class BlockDataUtils {

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

}
