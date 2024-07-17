package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;

@Name("Is Conditional")
@Description(
	"Checks whether a command block is conditional or not."
)
@Examples({
	"if {_block} is conditional:",
		"\tmake {_block} unconditional"
})
@Since("INSERT VERSION")
public class CondIsConditional extends PropertyCondition<Block> {

	static {
		register(CondIsConditional.class, "[:un]conditional", "blocks");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("un"));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Block block) {
		if (block.getBlockData() instanceof CommandBlock)
			return ((CommandBlock) block.getBlockData()).isConditional() ^ isNegated();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "conditional";
	}

}
