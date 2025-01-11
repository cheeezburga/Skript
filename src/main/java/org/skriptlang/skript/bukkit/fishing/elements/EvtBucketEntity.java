package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

public class EvtBucketEntity extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtBucketEntity.class, "Bucket Catch Entity")
			.addEvent(PlayerBucketEntityEvent.class)
			.addDescription("Called when a player catches an entity in a bucket.")
			.addExamples(
				"on bucket catch of a puffer fish",
					"\tsend \"You caught a fish with a %future event-item%!\" to player"
			)
			.build()
		);

		EventValues.registerEventValue(PlayerBucketEntityEvent.class, ItemStack.class, PlayerBucketEntityEvent::getOriginalBucket);
		EventValues.registerEventValue(PlayerBucketEntityEvent.class, ItemStack.class, PlayerBucketEntityEvent::getEntityBucket, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(PlayerBucketEntityEvent.class, Player.class, PlayerEvent::getPlayer);
		EventValues.registerEventValue(PlayerBucketEntityEvent.class, Entity.class, PlayerBucketEntityEvent::getEntity);
	}

	private EntityData<?>[] entities;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			//noinspection unchecked
			entities = ((Literal<EntityData<?>>) args[0]).getAll();

		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerBucketEntityEvent bucketEvent))
			return false;

		return entities == null || entities.length == 0 || Arrays.stream(entities)
			.map(EntityData::getType)
			.anyMatch(it -> it.isInstance(bucketEvent.getEntity()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "bucket catch" + (entities.length == 0 ? "" :
				" of " + StringUtils.join(List.of(entities), ", ", " and "));
	}

}
