package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Knockback")
@Description({
	"Apply the same velocity as a knockback to living entities in a direction.",
	"Mechanics such as knockback resistance will be factored in."
})
@Examples({
	"knockback player north",
	"knock victim (vector from attacker to victim) with strength 10"
})
@Since("2.7")
@RequiredPlugins("Paper 1.19.2+")
public class EffKnockback extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		if (Skript.methodExists(LivingEntity.class, "knockback", double.class, double.class, double.class))
			Skript.registerEffect(EffKnockback.class, "(apply knockback to|knock[back]) %livingentities% [%direction%] [with (strength|force) %-number%]");
	}

	private Node node;
	private Expression<LivingEntity> entities;
	private Expression<Direction> direction;
	private @Nullable Expression<Number> strength;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entities = (Expression<LivingEntity>) exprs[0];
		direction = (Expression<Direction>) exprs[1];
		strength = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Direction direction = this.direction.getSingle(event);
		if (direction == null) {
			error("The provided direction was null.");
			return;
		}

		double strength = 1.0;
		if (this.strength != null) {
			Number providedStrength = this.strength.getSingle(event);
			if (providedStrength == null)
				warning("The provided knockback strength was null, so defaulted to 1.0.");
		}

		for (LivingEntity livingEntity : entities.getArray(event)) {
			Vector directionVector = direction.getDirection(livingEntity);
			// Flip the direction, because LivingEntity#knockback() takes the direction of the source of the knockback,
			// not the direction of the actual knockback.
			directionVector.multiply(-1);
			livingEntity.knockback(strength, directionVector.getX(), directionVector.getZ());
			// ensure velocity is sent to client
			livingEntity.setVelocity(livingEntity.getVelocity());
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("knockback", entities, direction, "with strength");
		builder.append(strength == null ? "1" : strength);
		return builder.toString();
	}

}
