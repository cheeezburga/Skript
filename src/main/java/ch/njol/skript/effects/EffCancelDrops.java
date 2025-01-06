package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Nullable;

@Name("Cancel Drops")
@Description({
	"Cancels drops of items or experiences in a death or block break event.",
	"Please note that this doesn't keep items or experiences of a dead player. If you want to do that, " +
	"use the <a href='effects.html#EffKeepInventory'>Keep Inventory / Experience</a> effect."
})
@Examples({
	"on death of a zombie:",
		"\tif name of the entity is \"&cSpecial\":",
			"\t\tcancel drops of items",
	"",
	"on break of a coal ore:",
		"\tcancel the experience drops"
})
@Since("2.4")
@RequiredPlugins("1.12.2+ (cancelling item drops of blocks)")
@Events({"death", "break / mine"})
public class EffCancelDrops extends Effect {

	static {
		Skript.registerEffect(EffCancelDrops.class,
			"(cancel|clear|delete) [the] drops [of items:items|xp:[e]xp[erience][s])]",
			"(cancel|clear|delete) [the] (items:item|xp:[e]xp[erience]) drops");
	}

	private boolean cancelItems, cancelExps;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		cancelItems = parseResult.hasTag("items");
		cancelExps = parseResult.hasTag("xp");
		if (!(getParser().isCurrentEvent(EntityDeathEvent.class) || getParser().isCurrentEvent(BlockBreakEvent.class))) {
			Skript.error("The cancel drops effect can't be used outside of a death or block break event");
			return false;
		}
		if (isDelayed.isTrue()) {
			Skript.error("Can't cancel the drops anymore after the event has already passed");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof EntityDeathEvent deathEvent) {
			if (cancelItems)
				deathEvent.getDrops().clear();
			if (cancelExps)
				deathEvent.setDroppedExp(0);
		} else if (event instanceof BlockBreakEvent breakEvent) {
			if (cancelItems)
				breakEvent.setDropItems(false);
			if (cancelExps)
				breakEvent.setExpToDrop(0);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (cancelItems && !cancelExps)
			return "cancel the drops of items";
		else if (cancelExps && !cancelItems)
			return "cancel the drops of experiences";
		return "cancel the drops";
	}

}
