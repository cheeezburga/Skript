package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ExprEntitySound extends SimpleExpression<String> {

	static {
		if (Skript.methodExists(LivingEntity.class, "getDeathSound")) {
			Skript.registerExpression(ExprEntitySound.class, String.class, ExpressionType.COMBINED,
				"[the] (damage|hurt) sound[s] of %livingentities%",
				"%livingentities%'[s] (damage|hurt) sound[s]",

				"[the] death sound[s] of %livingentities%",
				"%livingentities%'[s] death sound[s]",

				"[the] [high:(tall|high)|(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%] of %livingentities%",
				"%livingentities%'[s] [high:(tall|high)|low:(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%]",

				"[the] swim[ming] sound[s] of %livingentities%",
				"%livingentities%'[s] swim[ming] sound[s]",

				"[the] [fast:(fast|speedy)] splash sound[s] of %livingentities%",
				"%livingentities%'[s] [fast:(fast|speedy)] splash sound[s]",

				"[the] eat[ing] sound of %livingentities% [(with|using|[while] eating [a]) %-itemtype%]",
				"%livingentities%'[s] eat[ing] sound",

				"[the] drink[ing] sound of %livingentities% [(with|using|[while] drinking [a]) %-itemtype%]",
				"%livingentities%'[s] drink[ing] sound");
		}
	}

	private static final int DAMAGE = 0;
	private static final int DEATH = 2;
	private static final int FALL = 4;
	private static final int SWIM = 6;
	private static final int SPLASH = 8;
	private static final int EAT_WITH_ITEM = 10;
	private static final int EAT = 11;
	private static final int DRINK_WITH_ITEM = 12;
	private static final int DRINK = 13;

	private int sound;
	private boolean big;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Number> height;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> item;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sound = matchedPattern;
		big = parseResult.hasTag("high") || parseResult.hasTag("fast");
		if (matchedPattern == FALL || matchedPattern == FALL + 1)
			height = (Expression<Number>) exprs[0];
		if (matchedPattern == EAT_WITH_ITEM || matchedPattern == DRINK_WITH_ITEM)
			item = (Expression<ItemType>) exprs[1];
		entities = (Expression<LivingEntity>) ((matchedPattern == FALL) ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		Set<String> sounds = new HashSet<>();
		for (LivingEntity entity : entities.getArray(event)) {
			Sound sound = getEntitySound(entity, event);
			if (sound != null)
				sounds.add(sound.name());
		}
		return sounds.toArray(String[]::new);
	}

	private @Nullable Sound getEntitySound(LivingEntity entity, Event event) {
		Sound sound = null;
		switch (this.sound) {
			case DAMAGE, DAMAGE + 1 -> sound = entity.getHurtSound();
			case DEATH, DEATH + 1 -> sound = entity.getDeathSound();
			case FALL, FALL + 1 -> sound = getFallSound(entity, event);
			case SWIM, SWIM + 1 -> sound = entity.getSwimSound();
			case SPLASH, SPLASH + 1 -> sound = big ? entity.getSwimHighSpeedSplashSound() : entity.getSwimSplashSound();
			case EAT_WITH_ITEM, DRINK_WITH_ITEM -> sound = getConsumeSound(entity, event);
			case EAT -> sound = entity.getEatingSound(new ItemStack(Material.COOKED_BEEF));
			case DRINK -> sound = entity.getDrinkingSound(new ItemStack(Material.POTION));
		}
		return sound;
	}

	private Sound getFallSound(LivingEntity entity, Event event) {
		//noinspection ConstantValue
		int height = this.height == null ? -1 : this.height.getOptionalSingle(event).orElse(-1).intValue();
		return height != -1 ? entity.getFallDamageSound(height) :
			(big ? entity.getFallDamageSoundBig() : entity.getFallDamageSoundSmall());
	}

	private Sound getConsumeSound(LivingEntity entity, Event event) {
		ItemStack defaultItem = new ItemStack(sound == EAT_WITH_ITEM ? Material.COOKED_BEEF : Material.POTION);
		//noinspection ConstantValue
		ItemStack item = this.item == null ? defaultItem : this.item.getOptionalSingle(event).orElse(new ItemType(defaultItem)).getRandom();
		if (item == null)
			item = defaultItem;
		return sound == EAT_WITH_ITEM ? entity.getEatingSound(item) : entity.getDrinkingSound(item);
	}

	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@SuppressWarnings("ConstantValue")
	public String toString(@Nullable Event event, boolean debug) {
		String name = "unknown";
		switch (sound) {
			case DAMAGE, DAMAGE + 1 -> name = "damage";
			case DEATH, DEATH + 1 -> name = "death";
			case FALL, FALL + 1 -> {
				if (this.height == null) {
					name = big ? "high fall damage" : "normal fall damage";
				} else {
					name = "fall damage from a height of " + this.height.toString(event, debug);
				}
			}
			case SWIM, SWIM + 1 -> name = "swim";
			case SPLASH, SPLASH + 1 -> name = big ? "speedy splash" : "splash";
			case EAT_WITH_ITEM, DRINK_WITH_ITEM -> name = (sound == EAT_WITH_ITEM ? "eating" : "drinking") + (this.item == null ? " with default item" : " " + this.item.toString(event, debug));
			case EAT, DRINK -> name = sound == EAT ? "eating" : "drinking";
		}
		return name + " sound of " + entities.toString(event, debug);
	}

}
