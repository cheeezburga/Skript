package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.stream.Stream;

@Name("Enchantment Level")
@Description("The level of a particular <a href='classes.html#enchantment'>enchantment</a> on an item.")
@Examples({
	"player's tool is a sword of sharpness:",
		"\tmessage \"You have a sword of sharpness %level of sharpness of the player's tool% equipped\""
})
@Since("2.0")
public class ExprEnchantmentLevel extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprEnchantmentLevel.class, Integer.class, ExpressionType.PROPERTY,
			"[the] [enchant[ment]] level[s] of %enchantmenttypes%",
			"%enchantmenttypes%'[s] [enchant[ment]] level[s]",

			"[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
			"[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
			"%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
			"%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;
	private @UnknownNullability Expression<Enchantment> enchants;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[(matchedPattern <= 1 || matchedPattern >= 4) ? 0 : 1];
		if (matchedPattern >= 2)
			enchants = (Expression<Enchantment>) exprs[matchedPattern <= 3 ? 0 : 1];
		return true;
	}

	@Override
	protected Integer[] get(Event event) {
		Enchantment[] enchants = this.enchants != null ? this.enchants.getArray(event) : null;

		return objects.stream(event)
			.flatMap(object -> {
				if (object instanceof EnchantmentType enchantmentType) {
					return Stream.of(enchantmentType.getLevel());
				} else if (object instanceof ItemType item) {
					Stream<EnchantmentType> enchantsOnItem = Arrays.stream(item.getEnchantmentTypes());
					if (enchants != null)
						enchantsOnItem = enchantsOnItem.filter(enchantmentType -> CollectionUtils.contains(enchants, enchantmentType.getType()));
					return enchantsOnItem.map(EnchantmentType::getLevel);
				}
				return Stream.empty();
			}).toArray(Integer[]::new);
	}

	@Override
	@Nullable
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (enchants == null || delta == null)
			return;

		int changeValue = ((Number) delta[0]).intValue();
		for (Object object : objects.getArray(event)) {
			if (object instanceof ItemType item) {
				for (Enchantment enchantment : enchants.getArray(event)) {
					EnchantmentType enchantmentType = item.getEnchantmentType(enchantment);
					int oldLevel = enchantmentType == null ? 0 : enchantmentType.getLevel();
					int newLevel = switch (mode) {
						case ADD -> oldLevel + changeValue;
						case REMOVE -> oldLevel - changeValue;
						case SET -> changeValue;
						default -> oldLevel;
					};

					if (newLevel <= 0) {
						item.removeEnchantments(new EnchantmentType(enchantment));
					} else {
						item.addEnchantments(new EnchantmentType(enchantment, newLevel));
					}
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle() && (enchants == null || enchants.isSingle());
	}

	@Override
	public @NotNull Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		if (enchants == null)
			return "enchantment level of " + objects.toString(event, debug);
		return "enchantment level of " + enchants.toString(event, debug) + " on " + objects.toString(event, debug);
	}

}
