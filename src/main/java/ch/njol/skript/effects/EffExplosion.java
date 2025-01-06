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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Explosion")
@Description({
	"Creates an explosion of a given force. The Minecraft Wiki has an" +
	"<a href='https://www.minecraft.wiki/w/Explosion'>article on explosions</a> " +
	"which lists the explosion forces of TNT, creepers, etc.",
	"Hint: use a force of 0 to create a fake explosion that does no damage whatsoever, or use the explosion effect introduced in Skript 2.0.",
	"Starting with Bukkit 1.4.5 and Skript 2.0 you can use safe explosions which will damage entities but won't destroy any blocks."
})
@Examples({
	"create an explosion of force 10 at the player",
	"create an explosion of force 0 at the victim"
})
@Since("1.0")
public class EffExplosion extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffExplosion.class,
			"[(create|make)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1:with fire)]",
			"[(create|make)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
			"[(create|make)] [a] fake explosion [%directions% %locations%]",
			"[(create|make)] [an] explosion[ ]effect [%directions% %locations%]");
	}

	private Node node;
	private @Nullable Expression<Number> force;
	private Expression<Location> locations;
	private boolean blockDamage;
	private boolean setFire;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		node = getParser().getNode();
		force = matchedPattern <= 1 ? (Expression<Number>) exprs[0] : null;
		blockDamage = matchedPattern != 1;
		setFire = parser.mark == 1;
		locations = Direction.combine((Expression<? extends Direction>) exprs[exprs.length - 2], (Expression<? extends Location>) exprs[exprs.length - 1]);
		return true;
	}

	@Override
	public void execute(Event event) {
		Number power = force != null ? force.getSingle(event) : 0;
		if (power == null) {
			error("The force of the explosion, " + toHighlight() + ", was null.");
			return;
		}
		for (Location location : locations.getArray(event)) {
			if (location.getWorld() == null)
				continue;

			if (!blockDamage) {
				location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), power.floatValue(), false, false);
			} else {
				location.getWorld().createExplosion(location, power.floatValue(), setFire);
			}
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return force == null ? null : force.toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (force != null)
			builder.append("create explosion of force", force, locations);
		else
			builder.append("create explosion effect ", locations);
		return builder.toString();
	}

}
