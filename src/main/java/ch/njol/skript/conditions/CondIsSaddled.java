package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Steerable;
import org.bukkit.inventory.ItemStack;

public class CondIsSaddled extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsSaddled.class, "[exact:(exactly|properly)] saddled", "livingentities");
	}

	private boolean exact;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		exact = parseResult.hasTag("exact");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Steerable steerable) {
			return steerable.hasSaddle();
		} else if (entity instanceof AbstractHorse horse) {
			ItemStack saddle = horse.getInventory().getSaddle();
			return exact ? (saddle != null && saddle.equals(new ItemStack(Material.SADDLE))) : (saddle != null);
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return exact ? "an exact saddle" : "a saddle";
	}
}
