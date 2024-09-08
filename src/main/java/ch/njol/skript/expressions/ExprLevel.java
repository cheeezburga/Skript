package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Level")
@Description("The level of a player.")
@Examples({
	"reduce the victim's level by 1",
	"set the player's level to 0"
})
@Since("<i>unknown</i> (before 2.1)")
@Events("level change")
public class ExprLevel extends SimplePropertyExpression<Player, Long> {

	static {
		register(ExprLevel.class, Long.class, "level", "players");
	}
	
	@Override
	protected Long[] get(Event event, Player[] source) {
		return super.get(source, player -> {
			if (event instanceof PlayerLevelChangeEvent playerLevelChangeEvent && playerLevelChangeEvent.getPlayer() == player && !Delay.isDelayed(event)) {
				return (long) (getTime() < 0 ? playerLevelChangeEvent.getOldLevel() : playerLevelChangeEvent.getNewLevel());
			}
			return (long) player.getLevel();
		});
	}
	
	@Override
	@Nullable
	public Long convert(Player player) {
		assert false;
		return null;
	}
	
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		if (getParser().isCurrentEvent(PlayerRespawnEvent.class) && !getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Cannot change a player's level in a respawn event. Add a delay of 1 tick or change the 'new level' in a death event.");
			return null;
		}
		if (getParser().isCurrentEvent(EntityDeathEvent.class) && getTime() == 0 && getExpr().isDefault() && !getParser().getHasDelayBefore().isTrue()) {
			Skript.warning("Changing the player's level in a death event will change the player's level before he dies. " +
					"Use either 'past level of player' or 'new level of player' to clearly state whether to change the level before or after he dies.");
		}
		if (getTime() == -1 && !getParser().isCurrentEvent(EntityDeathEvent.class))
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		
		int l = delta == null ? 0 : ((Number) delta[0]).intValue();
		boolean isPlayerDeathEvent = event instanceof PlayerDeathEvent;
		
		for (Player player : getExpr().getArray(event)) {
			int level = player.getLevel();
			boolean isRelevantDeathEvent = isPlayerDeathEvent
				&& getTime() > 0
				&& ((PlayerDeathEvent) event).getEntity() == player
				&& !Delay.isDelayed(event);

			if (isRelevantDeathEvent)
				level = ((PlayerDeathEvent) event).getNewLevel();

			switch (mode) {
				case SET -> level = l;
				case ADD -> level += l;
				case REMOVE -> level -= l;
				case DELETE, RESET -> level = 0;
            }

			if (level < 0)
				continue;

			if (isRelevantDeathEvent) {
				((PlayerDeathEvent) event).setNewLevel(level);
			} else {
				player.setLevel(level);
			}
		}
	}

	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), PlayerLevelChangeEvent.class, PlayerDeathEvent.class);
	}
	
	@Override
	protected String getPropertyName() {
		return "level";
	}
	
}
