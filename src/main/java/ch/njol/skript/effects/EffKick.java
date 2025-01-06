package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Kick")
@Description("Kicks a player from the server.")
@Examples({
	"on place of TNT, lava, or obsidian:",
		"\tkick the player due to \"You may not place %block%!\"",
		"\tcancel the event"
})
@Since("1.0")
public class EffKick extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffKick.class, "kick %players% [(by reason of|because [of]|on account of|due to) %-string%]");
	}

	private Node node;
	private Expression<Player> players;
	private @Nullable Expression<String> reason;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		players = (Expression<Player>) exprs[0];
		reason = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		String reason = this.reason != null ? this.reason.getSingle(event) : "";
		if (reason == null) {
			error("The provided reason was null.");
			return;
		}
		for (Player player : players.getArray(event)) {
			if (event instanceof PlayerLoginEvent loginEvent && player.equals(loginEvent.getPlayer()) && !Delay.isDelayed(event)) {
				loginEvent.disallow(Result.KICK_OTHER, reason);
			} else if (event instanceof PlayerKickEvent kickEvent && player.equals(kickEvent.getPlayer()) && !Delay.isDelayed(event)) {
				kickEvent.setLeaveMessage(reason);
			} else {
				player.kickPlayer(reason);
			}
		}
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public @Nullable String toHighlight() {
		return reason == null ? null : reason.toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("kick", players);
		if (reason != null)
			builder.append("on account of", reason);
		return builder.toString();
	}

}
