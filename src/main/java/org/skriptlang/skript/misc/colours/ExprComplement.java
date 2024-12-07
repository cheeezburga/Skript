package org.skriptlang.skript.misc.colours;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import org.jetbrains.annotations.Nullable;

public class ExprComplement extends SimplePropertyExpression<Color, Color> {

	static {
		register(ExprComplement.class, Color.class, "complement[ary colo[u]r]", "colours");
	}

	@Override
	public @Nullable Color convert(Color from) {
		return ColourUtils.complementColor(from);
	}

	@Override
	protected String getPropertyName() {
		return "complement";
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

}
