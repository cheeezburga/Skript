package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Ignite/Extinguish")
@Description("Lights entities on fire or extinguishes them.")
@Examples({
	"ignite the player",
	"extinguish the player"
})
@Since("1.4")
public class EffIgnite extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffIgnite.class,
			"(ignite|set fire to) %entities% [for %-timespan%]",
			"(set|light) %entities% on fire [for %-timespan%]",
			"extinguish %entities%");
	}

	private static final int DEFAULT_DURATION = 8 * 20; // default is 8 seconds for lava and fire.

	private Node node;
	private @Nullable Expression<Timespan> duration;
	private Expression<Entity> entities;
	private boolean ignite;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entities = (Expression<Entity>) exprs[0];
		ignite = exprs.length > 1;
		if (ignite)
			duration = (Expression<Timespan>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		int duration;
		if (this.duration == null) {
			duration = ignite ? DEFAULT_DURATION : 0;
		} else {
			Timespan timespan = this.duration.getSingle(event);
			if (timespan == null) {
				error("The provided timespan was null.");
				return;
			}
			duration = (int) timespan.getAs(Timespan.TimePeriod.TICK);
		}
		for (Entity entity : entities.getArray(event)) {
			if (event instanceof EntityDamageEvent entityDamageEvent && entityDamageEvent.getEntity() == entity && !Delay.isDelayed(event)) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> entity.setFireTicks(duration));
			} else {
				if (event instanceof EntityCombustEvent entityCombustEvent && entityCombustEvent.getEntity() == entity && !Delay.isDelayed(event))
					entityCombustEvent.setCancelled(true);// can't change the duration, thus simply cancel the event (and create a new one)
				entity.setFireTicks(duration);
			}
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return this.duration == null ? null : this.duration.toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (ignite)
			return "set " + entities.toString(event, debug) + " on fire for " + (duration != null ? duration.toString(event, debug) : new Timespan(Timespan.TimePeriod.TICK, DEFAULT_DURATION).toString());
		return "extinguish " + entities.toString(event, debug);
	}

}
