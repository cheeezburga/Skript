package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;

@Name("Is Charged")
@Description("Checks if a creeper, wither, or wither skull is charged (powered).")
@Examples({"if the last spawned creeper is charged:",
	"\tbroadcast \"A charged creeper is at %location of last spawned creeper%\""})
@Since("2.5, INSERT VERSION (withers, wither skulls)")
public class CondIsCharged extends PropertyCondition<Entity> {

	static {
		register(CondIsCharged.class, "(charged|powered)", "entities");
	}

	@Override
	public boolean check(Entity entity) {
        return switch (entity) {
            case Creeper creeper -> creeper.isPowered();
            case WitherSkull witherSkull -> witherSkull.isCharged();
            case Wither wither -> wither.isCharged();
            default -> false;
        };
    }

	@Override
	protected String getPropertyName() {
		return "charged";
	}

}
