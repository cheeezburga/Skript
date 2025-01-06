package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Break Block")
@Description({
	"Breaks the block and spawns items as if a player had mined it.",
	"You can add a tool, which will spawn items based on how that tool would break the block " +
	"(i.e. using a hand to break stone drops nothing, whereas using a pickaxe drops cobblestone)"
})
@Examples({
	"on right click:",
		"\tbreak clicked block naturally",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block using player's tool",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block naturally using diamond pickaxe"
})
@Since("2.4")
public class EffBreakNaturally extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
	}

	private Node node;
	private Expression<Block> blocks;
	private @Nullable Expression<ItemType> tool;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		this.node = getParser().getNode();
		blocks = (Expression<Block>) exprs[0];
		tool = (Expression<ItemType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType tool = null;
		if (this.tool != null) {
			tool = this.tool.getSingle(event);
			if (tool == null)
				warning("The tool to be used to break the block was null.");
		}

		for (Block block : this.blocks.getArray(event)) {
			if (tool != null) {
				ItemStack itemStack = tool.getRandom();
				if (itemStack != null) {
					block.breakNaturally(itemStack);
				} else {
					block.breakNaturally();
				}
			} else {
				block.breakNaturally();
			}
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return this.tool == null ? null : this.tool.toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("break", blocks, "naturally")
			.append(tool == null ? "" : "using " + tool.toString(event, debug))
			.toString();
	}

}
