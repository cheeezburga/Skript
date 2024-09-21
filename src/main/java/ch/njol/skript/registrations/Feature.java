package ch.njol.skript.registrations;

import ch.njol.skript.SkriptAddon;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

import java.net.URI;

/**
 * Experimental feature toggles as provided by Skript itself.
 */
public enum Feature implements Experiment {
	;

	private final String codeName;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;
	private final @Nullable URI feedbackLink;

	Feature(String codeName, LifeCycle phase, @Nullable URI feedbackLink, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		this.feedbackLink = feedbackLink;
		this.compiledPattern = switch (patterns.length) {
			case 0 -> PatternCompiler.compile(codeName);
			case 1 -> PatternCompiler.compile(patterns[0]);
			default -> PatternCompiler.compile('(' + String.join("|", patterns) + ')');
		};
	}

	Feature(String codeName, LifeCycle phase) {
		this(codeName, phase, null, codeName);
	}

	Feature(String codeName, LifeCycle phase, @Nullable URI feedbackLink) {
		this(codeName, phase, feedbackLink, codeName);
	}

	Feature(String codeName, LifeCycle phase, String... patterns) {
		this(codeName, phase, null, patterns);
	}

	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (Feature value : values()) {
			manager.register(addon, value);
		}
	}

	@Override
	public String codeName() {
		return codeName;
	}

	@Override
	public LifeCycle phase() {
		return phase;
	}

	@Override
	public SkriptPattern pattern() {
		return compiledPattern;
	}

	@Override
	public @Nullable URI feedbackLink() {
		return feedbackLink;
	}

}
