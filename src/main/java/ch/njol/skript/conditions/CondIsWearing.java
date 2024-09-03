package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Is Wearing")
@Description("Checks whether an entity is wearing some items (usually armor).")
@Examples({
	"player is wearing an iron chestplate and iron leggings",
	"player is wearing all diamond armour",
	"target is wearing wolf armor"
})
@Since("1.0")
public class CondIsWearing extends Condition {
	
	static {
		PropertyCondition.register(CondIsWearing.class, "wearing %itemtypes%", "livingentities");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	private Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) vars[0];
		types = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		return entities.check(event,
				entity -> types.check(event,
						type -> {
							EntityEquipment equip = entity.getEquipment();
							if (equip == null)
								return false; // No equipment -> not wearing anything
							for (ItemStack armorContent : equip.getArmorContents()) {
								if (type.isOfType(armorContent) ^ type.isAll())
									return !type.isAll();
							}
							return type.isAll();
						}),
				isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, entities,
				"wearing " + types.toString(e, debug));
	}
	
}
