package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.EffSecSpawn;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Drop")
@Description("Drops one or more items.")
@Examples({
	"on death of creeper:",
		"\tdrop 1 TNT"
})
@Since("1.0")
public class EffDrop extends Effect {

	static {
		Skript.registerEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%] [(1:without velocity)]");
	}

	public static @Nullable Entity lastSpawned = null;

	private Expression<?> drops;
	private Expression<Location> locations;
	private boolean useVelocity;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		drops = exprs[0];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		useVelocity = parseResult.mark == 0;
		return true;
	}

	@Override
	public void execute(Event event) {
		Object[] drops = this.drops.getArray(event);
		for (Location loc : locations.getArray(event)) {
			Location itemDropLoc = loc.clone().subtract(0.5, 0.5, 0.5); // dropItemNaturally adds 0.15 to 0.85 randomly to all coordinates
			for (Object drop : drops) {
				if (drop instanceof Experience) {
					ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
					orb.setExperience(((Experience) drop).getXP() + orb.getExperience()); // ensure we maintain previous experience, due to spigot xp merging behavior
					EffSecSpawn.lastSpawned = orb;
				} else {
					if (drop instanceof ItemStack itemStack)
						drop = new ItemType(itemStack);
					for (ItemStack is : ((ItemType) drop).getItem().getAll()) {
						if (!ItemUtils.isAir(is.getType()) && is.getAmount() > 0) {
							if (useVelocity) {
								lastSpawned = loc.getWorld().dropItemNaturally(itemDropLoc, is);
							} else {
								Item item = loc.getWorld().dropItem(loc, is);
								item.teleport(loc);
								item.setVelocity(new Vector(0, 0, 0));
								lastSpawned = item;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "drop " + drops.toString(event, debug) + " " + locations.toString(event, debug);
	}

}
