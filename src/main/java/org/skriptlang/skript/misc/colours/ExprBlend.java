package org.skriptlang.skript.misc.colours;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprBlend extends SimpleExpression<Color> {

	static {
		Skript.registerExpression(ExprBlend.class, Color.class, ExpressionType.COMBINED,
			"%colours% (blended|mixed) with %colours% [by [[a[n] (factor|amount)] %-number%]",
			"blend of %colours% (and|with) %colours% [by [[a[n] (factor|amount)] %-number%]");
	}

	private Expression<Color> colours, blendWith;
	private Expression<Number> amount;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.colours = (Expression<Color>) exprs[0];
		this.blendWith = (Expression<Color>) exprs[1];
		this.amount	= (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected Color @Nullable [] get(Event event) {

		return new Color[0];
	}

	@Override
	public boolean isSingle() {
		return this.colours.isSingle();
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
        return new SyntaxStringBuilder(event, debug)
			.append(this.colours)
			.append("blended with")
			.append(this.blendWith)
			.append("by a factor of")
			.append(this.amount)
			.toString();
	}

}
