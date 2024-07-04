package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;

public class CondIsConditional extends PropertyCondition<Block> {

	static {
		register(CondIsConditional.class, "[:un]conditional", "blocks");
	}

	private boolean un;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		un = parseResult.hasTag("un");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Block block) {
		if (block.getBlockData() instanceof CommandBlock)
			return ((CommandBlock) block.getBlockData()).isConditional() ^ un;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "conditional";
	}

}
