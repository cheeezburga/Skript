package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExprLastDeathLocationTest extends SkriptJUnitTest {

	private Player player;
	private Effect get, set;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
		get = Effect.parse("set last death location of {_player} to location(0,0,0)", null);
		set = Effect.parse("set {_loc} to last death location of {_player}", null);
	}

	@Test
	public void test() {
		if (get == null)
			Assert.fail("Get statement was null");
		if (set == null)
			Assert.fail("Set statement was null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", player, event, true);
		Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

		player.setLastDeathLocation(location);
		EasyMock.expectLastCall();
		EasyMock.replay(player);
		TriggerItem.walk(set, event);
		EasyMock.verify(player);

		EasyMock.resetToNice(player);
		player.getLastDeathLocation();
		EasyMock.expectLastCall();
		EasyMock.replay(player);
		TriggerItem.walk(get, event);
		EasyMock.verify(player);
	}

}
