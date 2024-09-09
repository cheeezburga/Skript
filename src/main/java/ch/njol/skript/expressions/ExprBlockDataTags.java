package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ExprBlockDataTags extends SimpleExpression<String> {

	static {
		PropertyExpression.register(ExprBlockDataTags.class, String.class, "[block[ ]data] tags", "blockdatas");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<BlockData> datas;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		datas = (Expression<BlockData>) exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		Set<String> tags = new HashSet<>();
		for (BlockData data : datas.getArray(event)) {
			String[] dataTags = getTags(data);
			if (dataTags != null)
				tags.addAll(Arrays.asList(dataTags));
		}
		return tags.toArray(String[]::new);
	}

	private static String @Nullable [] getTags(BlockData data) {
		String[] tagsAndValues = getTagsAndValues(data);
		if (tagsAndValues == null)
			return null;

		String[] tags = new String[tagsAndValues.length];
		for (int i = 0; i < tagsAndValues.length; i++) {
			tags[i] = tagsAndValues[i].split("=")[0];
		}
		return tags;
	}

	private static String @Nullable [] getTagsAndValues(BlockData data) {
		String string = data.getAsString();
		int start = string.indexOf("[");
		int end = string.indexOf("]");

		if (start == -1 || end == -1 || start >= end)
			return null;
		return string.substring(start + 1, end).split(",");
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "block data tags of " + datas.toString(event, debug);
	}

}
