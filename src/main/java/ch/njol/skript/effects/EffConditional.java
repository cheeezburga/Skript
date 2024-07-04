package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EffConditional extends Effect {

	static {
		Skript.registerEffect(EffConditional.class, "make %blocks% [not:(un|not )]conditional");
	}

	private Expression<Block> blocks;
	private boolean conditional;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		conditional = !parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block.getBlockData() instanceof CommandBlock) {
				CommandBlock data = (CommandBlock) block.getBlockData();
				data.setConditional(conditional);
				block.setBlockData(data);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + blocks.toString(event, debug) + (conditional ? " " : " un") + "conditional";
	}

}
