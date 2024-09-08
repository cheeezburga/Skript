package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
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
@Description({
	"Gets the sound that a given block, blockdata, or itemtype will use in a specific scenario.",
	"This will return a string in the form of \"SOUND_EXAMPLE\", which can be used in the play sound syntax.",
	"",
	"Check out <a href=\"https://www.digminecraft.com/lists/sound_list_pc.php\">this website</a> for a list of sounds in Minecraft (Java Edition), " +
		"or <a href=\"https://minecraft.wiki/w/Sound\">this one</a> to go to the sounds wiki page."
})
@Examples({
	"play sound (break sound of dirt) at all players",
	"set {_sounds::*} to place sounds of dirt, grass block, blue wool and stone"
})
@Since("INSERT VERSION")
public class ExprBlockSound extends SimpleExpression<String> {

	private static final int BREAK = 1, FALL = 2, HIT = 3, PLACE = 4, STEP = 5;

	static {
		SimplePropertyExpression.register(ExprBlockSound.class, String.class, "(1:break|2:fall|3:hit|4:place|5:step) sound[s]", "blocks/blockdatas/itemtypes");
	}

	private int soundType;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		soundType = parseResult.mark;
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

		return switch (this.soundType) {
			case BREAK -> group.getBreakSound();
			case FALL -> group.getFallSound();
			case HIT -> group.getHitSound();
			case PLACE -> group.getPlaceSound();
			case STEP -> group.getStepSound();
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
		return switch (this.soundType) {
			case BREAK -> "break";
			case FALL -> "fall";
			case HIT -> "hit";
			case PLACE -> "place";
			case STEP -> "step";
			default -> null;
		} + " sound of " + objects.toString(event, debug);
	}

}
