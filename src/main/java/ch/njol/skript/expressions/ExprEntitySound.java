package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Name("Entity Sound")
@Description("Gets the sound that a given entity will make in a specific scenario.")
@Examples({
	"play sound (hurt sound of player) at player",
	"set {_sounds::*} to death sounds of (all mobs in radius 10 of player)"
})
@Since("INSERT VERSION")
@RequiredPlugins("Spigot 1.19.2+")
public class ExprEntitySound extends SimpleExpression<String> {

	static {
		if (Skript.methodExists(LivingEntity.class, "getDeathSound")) {
			Skript.registerExpression(ExprEntitySound.class, String.class, ExpressionType.COMBINED,
				"[the] (damage|hurt) sound[s] of %livingentities%",
				"%livingentities%'[s] (damage|hurt) sound[s]",

				"[the] death sound[s] of %livingentities%",
				"%livingentities%'[s] death sound[s]",

				"[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%",
				"%livingentities%'[s] [high:(tall|high)|low:(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%]",

				"[the] swim[ming] sound[s] of %livingentities%",
				"%livingentities%'[s] swim[ming] sound[s]",

				"[the] [fast:(fast|speedy)] splash sound[s] of %livingentities%",
				"%livingentities%'[s] [fast:(fast|speedy)] splash sound[s]",

				"[the] eat[ing] sound[s] of %livingentities% [(with|using|[while] eating [a]) %-itemtype%]",
				"%livingentities%'[s] eat[ing] sound[s]",

				"[the] drink[ing] sound[s] of %livingentities% [(with|using|[while] drinking [a]) %-itemtype%]",
				"%livingentities%'[s] drink[ing] sound[s]",

				"[the] ambient sound[s] of %livingentities%",
				"%livingentities%'[s] ambient sound[s]");
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
	private static final int AMBIENT = 14;

	private int soundPattern;
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
		soundPattern = matchedPattern;
		big = parseResult.hasTag("high") || parseResult.hasTag("fast");
		if (matchedPattern == FALL || matchedPattern == FALL + 1)
			height = (Expression<Number>) exprs[0];
		if (matchedPattern == EAT_WITH_ITEM || matchedPattern == DRINK_WITH_ITEM)
			item = (Expression<ItemType>) exprs[1];
		entities = (Expression<LivingEntity>) ((matchedPattern == FALL) ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	@SuppressWarnings("ConstantValue")
	protected String @Nullable [] get(Event event) {
		int height = this.height == null ? -1 : this.height.getOptionalSingle(event).orElse(-1).intValue();

		ItemStack defaultItem = new ItemStack(soundPattern == EAT_WITH_ITEM ? Material.COOKED_BEEF : Material.POTION);
		ItemStack item = this.item == null ? defaultItem : this.item.getOptionalSingle(event).map(ItemType::getRandom).orElse(defaultItem);

		return entities.stream(event)
			.map(entity -> getEntitySound(entity, height, item))
			.filter(Objects::nonNull)
			.distinct()
			.map(Sound::name)
			.toArray(String[]::new);
	}

	private @Nullable Sound getEntitySound(LivingEntity entity, int height, ItemStack item) {
		return switch (this.soundPattern) {
			case DAMAGE, DAMAGE + 1 -> entity.getHurtSound();
			case DEATH, DEATH + 1 -> entity.getDeathSound();
			case FALL, FALL + 1 -> {
				if (height != -1)
					yield entity.getFallDamageSound(height);
				else
					yield big ? entity.getFallDamageSoundBig() : entity.getFallDamageSoundSmall();
			}
			case SWIM, SWIM + 1 -> entity.getSwimSound();
			case SPLASH, SPLASH + 1 -> big ? entity.getSwimHighSpeedSplashSound() : entity.getSwimSplashSound();
			case EAT, EAT_WITH_ITEM -> entity.getEatingSound(item);
			case DRINK, DRINK_WITH_ITEM -> entity.getDrinkingSound(item);
			case AMBIENT -> entity instanceof Mob mob ? mob.getAmbientSound() : null;
			default -> null;
		};
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
		return switch (soundPattern) {
			case DAMAGE, DAMAGE + 1 -> "damage";
			case DEATH, DEATH + 1 -> "death";
			case FALL, FALL + 1 -> {
				if (this.height == null) {
					yield big ? "high fall damage" : "normal fall damage";
				} else {
					yield "fall damage from a height of " + this.height.toString(event, debug);
				}
			}
			case SWIM, SWIM + 1 -> "swim";
			case SPLASH, SPLASH + 1 -> big ? "speedy splash" : "splash";
			case EAT_WITH_ITEM, DRINK_WITH_ITEM -> (soundPattern == EAT_WITH_ITEM ? "eating" : "drinking") +
				(this.item == null ? " with default item" : " " + this.item.toString(event, debug));
			case EAT, DRINK -> soundPattern == EAT ? "eating" : "drinking";
			case AMBIENT -> "ambient";
			default -> "unknown";
		} + " sound of " + entities.toString(event, debug);
	}

}
