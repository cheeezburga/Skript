package ch.njol.skript;

import ch.njol.skript.doc.Documentation;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.skriptlang.skript.lang.experiment.Experiment;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SkriptCommandTabCompleter implements TabCompleter {

	@Override
	@Nullable
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> completions = new ArrayList<>();
		
		if (!command.getName().equalsIgnoreCase("skript"))
			return null;
		
		if (args[0].equalsIgnoreCase("update") && args.length == 2) {
			completions.add("check");
			completions.add("changes");
			completions.add("download");
		} else if (args[0].equalsIgnoreCase("feedback") && args.length == 2) {
			for (Experiment experiment : Skript.experiments().registered()) {
				completions.add(experiment.codeName());
			}
		} else if (args[0].matches("(?i)(reload|disable|enable)") && args.length >= 2) {
			File scripts = Skript.getInstance().getScriptsFolder();
			String scriptsPathString = scripts.toPath().toString();
			int scriptsPathLength = scriptsPathString.length();

			String scriptArg = StringUtils.join(args, " ", 1, args.length);
			String fs = File.separator;

			boolean enable = args[0].equalsIgnoreCase("enable");

			// Live update, this will get all old and new (even not loaded) scripts
			// TODO Find a better way for caching, it isn't exactly ideal to be calling this method constantly
			try (Stream<Path> files = Files.walk(scripts.toPath())) {
				files.map(Path::toFile)
					.forEach(file -> {
						if (!(enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter()).accept(file))
							return;

						// Ignore hidden files like .git/ for users that use git source control.
						if (file.isHidden())
							return;

						String fileString = file.toString().substring(scriptsPathLength);
						if (fileString.isEmpty())
							return;

						if (file.isDirectory()) {
							fileString = fileString + fs; // Add file separator at the end of directories
						} else if (file.getParentFile().toPath().toString().equals(scriptsPathString)) {
							fileString = fileString.substring(1); // Remove file separator from the beginning of files or directories in root only
							if (fileString.isEmpty())
								return;
						}

						// Make sure the user's argument matches with the file's name or beginning of file path
						if (scriptArg.length() > 0 && !file.getName().startsWith(scriptArg) && !fileString.startsWith(scriptArg))
							return;

						// Trim off previous arguments if needed
						if (args.length > 2 && fileString.length() >= scriptArg.length())
							fileString = fileString.substring(scriptArg.lastIndexOf(" ") + 1);

						// Just in case
						if (fileString.isEmpty())
							return;

						completions.add(fileString);
					});
			} catch (Exception e) {
				//noinspection ThrowableNotThrown
				Skript.exception(e, "An error occurred while trying to update the list of disabled scripts!");
			}
			
			// These will be added even if there are incomplete script arg
			if (args.length == 2) {
				completions.add("all");
				if (args[0].equalsIgnoreCase("reload")) {
					completions.add("config");
					completions.add("aliases");
					completions.add("scripts");
				}
			}

		} else if (args.length == 1) {
			completions.add("help");
			completions.add("reload");
			completions.add("enable");
			completions.add("disable");
			completions.add("update");
			completions.add("info");
			completions.add("feedback");
			if (Documentation.getDocsTemplateDirectory().exists())
				completions.add("gen-docs");
			if (TestMode.DEV_MODE)
				completions.add("test");
		}
		
		return completions;
	}

}
