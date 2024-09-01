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
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Explode Creeper")
@Description("Starts the explosion process of a creeper or instantly explodes it.")
@Examples({
	"start explosion of the last spawned creeper",
	"stop ignition of the last spawned creeper"
})
@Since("2.5")
@RequiredPlugins("Paper 1.13+ (start, stop)/Spigot 1.14+ (start)")
public class EffExplodeCreeper extends Effect {

	static {
		Skript.registerEffect(EffExplodeCreeper.class,
				"instantly explode [creeper[s]] %livingentities%",
				"explode [creeper[s]] %livingentities% instantly",
				"ignite creeper[s] %livingentities%",
				"start (ignition|explosion) [process] of [creeper[s]] %livingentities%",
				"stop (ignition|explosion) [process] of [creeper[s]] %livingentities%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;

	private boolean instant, stop;

	/*
	 * setIgnited() was added in Paper 1.13
	 * ignite() was added in Spigot 1.14, so we can use setIgnited() 
	 * to offer this functionality to Paper 1.13 users.
	 */
	private static final boolean PAPER = Skript.methodExists(Creeper.class, "setIgnited", boolean.class);

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 4) {
			if (!PAPER) {
				Skript.error("Stopping the ignition process is only possible on Paper 1.13+", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		entities = (Expression<LivingEntity>) exprs[0];
		instant = matchedPattern == 0;
		stop = matchedPattern == 4;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Creeper creeper) {
				if (instant) {
					creeper.explode();
				} else if (stop) {
					creeper.setIgnited(false);
				} else {
					if (PAPER) {
						creeper.setIgnited(true);
					} else {
						creeper.ignite();
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (stop)
			return "stop the explosion process of " + entities.toString(event, debug);
		return (instant ? "instantly explode " : "start the explosion process of ") + entities.toString(event, debug);
	}

}
