package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprCommandBlockCommand extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprCommandBlockCommand.class, String.class, "[command[ ]block] command", "blocks/entities");
	}

	@Override
	public @Nullable String convert(Object holder) {
		String command = "";
		if (holder instanceof Block && ((Block) holder).getState() instanceof CommandBlock) {
			command = ((CommandBlock) ((Block) holder).getState()).getCommand();
		} else if (holder instanceof CommandMinecart) {
			command = ((CommandMinecart) holder).getCommand();
		}
		return (command.isEmpty()) ? null : command;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		String newCommand = delta == null ? null : ((String) delta[0]);
		for (Object holder : getExpr().getArray(event)) {
			switch (mode) {
				case RESET:
				case DELETE:
				case SET:
					if (holder instanceof Block && ((Block) holder).getState() instanceof CommandBlock) {
						CommandBlock state = ((CommandBlock) ((Block) holder).getState());
						state.setCommand(newCommand);
						state.update();
					} else if (holder instanceof CommandMinecart) {
						((CommandMinecart) holder).setCommand(newCommand);
					}
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "command block command";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

}
