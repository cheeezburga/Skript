/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Colorable;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;

@Name("Color Items")
@Description("Colors items in a given <a href='classes.html#color'>color</a>. " +
		"You can also use RGB codes if you feel limited with the 16 default colors. " +
		"RGB codes are three numbers from 0 to 255 in the order <code>(red, green, blue)</code>, where <code>(0,0,0)</code> is black and <code>(255,255,255)</code> is white. " +
		"Armor is colorable for all Minecraft versions. With Minecraft 1.11 or newer you can also color potions and maps. Note that the colors might not look exactly how you'd expect.")
@Examples({"dye player's helmet blue",
		"color the player's tool red"})
@Since("2.0, 2.2-dev26 (maps and potions)")
public class EffDye extends Effect {

	static {
		Skript.registerEffect(EffDye.class, "(dye|colo[u]r) %itemtypes/livingentities/blocks% [as] %color%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> objects;
	@SuppressWarnings("null")
	private Expression<Color> color;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		objects = exprs[0];
		color = (Expression<Color>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		Color color = this.color.getSingle(e);
		Object[] objects = this.objects.getArray(e);
		
		if (color == null)
			return;
		
		org.bukkit.Color bukkit = color.asBukkitColor();
		DyeColor dye = color.asDyeColor();
		
		for (Object object : objects) {
			if (object instanceof ItemType) {
				ItemType item = (ItemType) object;
				ItemMeta preMeta = item.getItemMeta();

				if (preMeta instanceof LeatherArmorMeta meta) {
					meta.setColor(bukkit);
					item.setItemMeta(meta);
				} else if (preMeta instanceof MapMeta meta) {
					meta.setColor(bukkit);
					item.setItemMeta(meta);
				} else if (preMeta instanceof PotionMeta meta) {
					meta.setColor(bukkit);
					item.setItemMeta(meta);
				}
			} else if (object instanceof Block block) {
				if (block.getState() instanceof Colorable state)
					state.setColor(dye);
			} else if (object instanceof LivingEntity entity) {
				if (entity instanceof Colorable)
					((Colorable) entity).setColor(dye);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "dye " + objects.toString(e, debug) + " " + color.toString(e, debug);
	}

}
