package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Name("Block Sound")
@Description("Gets the sound that a given block, blockdata, or itemtype will use in a specific scenario.")
@Examples({
	"play sound (break sound of dirt) at all players",
	"set {_sounds::*} to place sounds of dirt, grass block, blue wool and stone"
})
@Since("INSERT VERSION")
public class ExprBlockSound extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprBlockSound.class, String.class, ExpressionType.COMBINED,
			"[the] break sound[s] of %blocks/blockdatas/itemtypes%",
			"%blocks/blockdatas/itemtypes%'[s] break sound[s]",

			"[the] fall sound[s] of %blocks/blockdatas/itemtypes%",
			"%blocks/blockdatas/itemtypes%'[s] fall sound[s]",

			"[the] hit sound[s] of %blocks/blockdatas/itemtypes%",
			"%blocks/blockdatas/itemtypes%'[s] hit sound[s]",

			"[the] place sound[s] of %blocks/blockdatas/itemtypes%",
			"%blocks/blockdatas/itemtypes%'[s] place sound[s]",

			"[the] step sound[s] of %blocks/blockdatas/itemtypes%",
			"%blocks/blockdatas/itemtypes%'[s] step sound[s]");
	}

	private static final int BREAK = 0;
	private static final int FALL = 2;
	private static final int HIT = 4;
	private static final int PLACE = 6;
	private static final int STEP = 8;

	private int soundPattern;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		soundPattern = matchedPattern;
		objects = exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return objects.stream(event)
			.map(this::convertAndGetSound)
			.filter(Objects::nonNull)
			.distinct()
			.map(Sound::name)
			.toArray(String[]::new);
	}

	private @Nullable Sound convertAndGetSound(Object object) {
		SoundGroup group = null;

		if (object instanceof Block block) {
			group = block.getBlockData().getSoundGroup();
		} else if (object instanceof BlockData data) {
			group = data.getSoundGroup();
		} else if (object instanceof ItemType item) {
			if (item.hasBlock())
				group = item.getMaterial().createBlockData().getSoundGroup();
		}

		if (group == null)
			return null;

		return switch (this.soundPattern) {
			case BREAK, BREAK + 1 -> group.getBreakSound();
			case FALL, FALL + 1 -> group.getFallSound();
			case HIT, HIT + 1 -> group.getHitSound();
			case PLACE, PLACE + 1 -> group.getPlaceSound();
			case STEP, STEP + 1 -> group.getStepSound();
			default -> null;
		};
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle();
	}

	@Override
	public @NotNull Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return switch (this.soundPattern) {
			case BREAK, BREAK + 1 -> "break";
			case FALL, FALL + 1 -> "fall";
			case HIT, HIT + 1 -> "hit";
			case PLACE, PLACE + 1 -> "place";
			case STEP, STEP + 1 -> "step";
			default -> null;
		} + " sound of " + objects.toString(event, debug);
	}

}
