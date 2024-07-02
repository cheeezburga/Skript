package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

public class CondIsTamed extends PropertyCondition<Entity> {

	static {
		register(CondIsTamed.class, "(tamed|domesticated)", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Tameable)
			return ((Tameable) entity).isTamed();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "tamed";
	}
}
