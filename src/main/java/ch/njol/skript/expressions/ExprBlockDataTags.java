package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.BlockDataUtils;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
			String[] dataTags = BlockDataUtils.getTags(data);
			if (dataTags != null)
				Collections.addAll(tags, dataTags);
		}
		return tags.toArray(String[]::new);
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
