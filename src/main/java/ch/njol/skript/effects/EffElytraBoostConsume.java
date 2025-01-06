package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Consume Boosting Firework")
@Description("Prevents the firework used in an 'elytra boost' event from being consumed.")
@Examples({
	"on elytra boost:",
		"\tif the used firework will be consumed:",
			"\t\tprevent the used firework from being consumed"
})
@RequiredPlugins("Paper")
@Since("2.10")
public class EffElytraBoostConsume extends Effect {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			Skript.registerEffect(EffElytraBoostConsume.class,
				"(prevent|disallow) [the] (boosting|used) firework from being consumed",
				"allow [the] (boosting|used) firework to be consumed");
		}
	}

	private boolean consume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("This effect can only be used in an 'elytra boost' event.");
			return false;
		}
		consume = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof PlayerElytraBoostEvent boostEvent)
			boostEvent.setShouldConsume(consume);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (consume)
			return "allow the boosting firework to be consumed";
		return "prevent the boosting firework from being consumed";
	}

}
