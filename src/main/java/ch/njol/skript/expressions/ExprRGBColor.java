package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprRGBColor extends SimpleExpression<Color> {

	static {
		Skript.registerExpression(ExprRGBColor.class, Color.class, ExpressionType.COMBINED, "[rgb] colo[u]r [from|of] \\(%number%,[ ]%number%,[ ]%number%\\)");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Number> red, green, blue;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		red = (Expression<Number>) exprs[0];
		blue = (Expression<Number>) exprs[1];
		green = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected @Nullable Color[] get(Event event) {
		Number r = red.getSingle(event);
		Number g = green.getSingle(event);
		Number b = blue.getSingle(event);
		if (r == null || g == null || b == null)
			return null;
		return CollectionUtils.array(new ColorRGB(r.intValue(), g.intValue(), b.intValue()));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return ColorRGB.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "RED: " + red.toString(event, debug) + ", GREEN: " + green.toString(event, debug) + "BLUE: " + blue.toString(event, debug);
	}

}
