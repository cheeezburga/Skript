package org.skriptlang.skript.bukkit.fishing;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.fishing.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.IOException;

public class FishingModule implements AddonModule {

	public static void load() throws IOException {

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.fishing", "elements");
	}

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();
		CondFishingLure.register(registry);
		CondIsInOpenWater.register(registry);
		EffFishingLure.register(registry);
		EffPullHookedEntity.register(registry);
		ExprFishingApproachAngle.register(registry);
		ExprFishingBiteTime.register(registry);
		ExprFishingHook.register(registry);
		ExprFishingHookEntity.register(registry);
		ExprFishingWaitTime.register(registry);
	}

}
