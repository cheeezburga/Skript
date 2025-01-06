package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Damage/Heal/Repair")
@Description({
	"Damage, heal, or repair an entity or item.",
	"Servers running Spigot 1.20.4+ can optionally choose to specify a fake damage cause."
})
@Examples({
	"damage player by 5 hearts",
	"damage player by 3 hearts with fake cause fall",
	"heal the player",
	"repair tool of player"
})
@Since("1.0, 2.10 (damage cause)")
@RequiredPlugins("Spigot 1.20.4+ (for damage cause)")
public class EffHealth extends Effect implements SyntaxRuntimeErrorProducer {

	private static final boolean SUPPORTS_DAMAGE_SOURCE = Skript.classExists("org.bukkit.damage.DamageSource");

	static {
		Skript.registerEffect(EffHealth.class,
			"damage %livingentities/itemtypes/slots% by %number% [heart[s]] [with [fake] [damage] cause %-damagecause%]",
			"heal %livingentities% [by %-number% [heart[s]]]",
			"repair %itemtypes/slots% [by %-number%]");
	}

	private Node node;
	private Expression<?> damageables;
	private @UnknownNullability Expression<Number> amount;
	private boolean isHealing, isRepairing;
	private @UnknownNullability Expression<DamageCause> exprCause = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 && exprs[2] != null && !SUPPORTS_DAMAGE_SOURCE) {
			Skript.error("Using the fake cause extension in effect 'damage' requires Spigot 1.20.4+");
			return false;
		}
		this.node = getParser().getNode();
		this.damageables = exprs[0];
		this.isHealing = matchedPattern >= 1;
		this.isRepairing = matchedPattern == 2;
		this.amount = (Expression<Number>) exprs[1];
		if (exprs.length > 2)
			this.exprCause = (Expression<DamageCause>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		double amount = 0;
		if (this.amount != null) {
			Number amountPostCheck = this.amount.getSingle(event);
			if (amountPostCheck == null) {
				error("The " + getPatternType() + " amount was null.");
				return;
			}
			amount = amountPostCheck.doubleValue();
		}

		for (Object obj : this.damageables.getArray(event)) {
			if (obj instanceof ItemType itemType) {

				if (this.amount == null) {
					ItemUtils.setDamage(itemType, 0);
				} else {
					ItemUtils.setDamage(itemType, (int) Math2.fit(0, (ItemUtils.getDamage(itemType) + (isHealing ? -amount : amount)), ItemUtils.getMaxDamage(itemType)));
				}

			} else if (obj instanceof Slot slot) {
				ItemStack itemStack = slot.getItem();

				if (itemStack == null) {
					warning("The item in the provided slot was null, so skipping over it.");
					continue;
				}

				if (this.amount == null) {
					ItemUtils.setDamage(itemStack, 0);
				} else {
					int damageAmt = (int) Math2.fit(0, (ItemUtils.getDamage(itemStack) + (isHealing ? -amount : amount)), ItemUtils.getMaxDamage(itemStack));
					ItemUtils.setDamage(itemStack, damageAmt);
				}

				slot.setItem(itemStack);

			} else if (obj instanceof Damageable damageable) {
				if (this.amount == null) {
					HealthUtils.heal(damageable, HealthUtils.getMaxHealth(damageable));
				} else if (isHealing) {
					HealthUtils.heal(damageable, amount);
				} else {
					if (SUPPORTS_DAMAGE_SOURCE) {
						DamageCause cause = exprCause == null ? null : exprCause.getSingle(event);
						if (cause != null) {
							HealthUtils.damage(damageable, amount, DamageUtils.getDamageSourceFromCause(cause));
							return; // is this right?
						}
					}
					HealthUtils.damage(damageable, amount);
				}

			}
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return amount.toString(null, false);
	}

	private String getPatternType() {
		return isRepairing ? "repair" : isHealing ? "heal" : "damage";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append(getPatternType(), damageables);
		if (amount != null)
			builder.append("by", amount);
		if (exprCause != null && event != null)
			builder.append("with damage cause", exprCause);
		return builder.toString();
	}

}
