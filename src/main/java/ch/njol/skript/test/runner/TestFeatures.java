package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

import java.net.URI;

/**
 * Features available only in test scripts.
 */
public enum TestFeatures implements Experiment {
	EXAMPLE_FEATURE("example feature", LifeCycle.STABLE),
	DEPRECATED_FEATURE("deprecated feature", LifeCycle.DEPRECATED),
	TEST_FEATURE("test", LifeCycle.EXPERIMENTAL, "test[ing]", "fizz[ ]buzz")
	;

	private final String codeName;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;
	private final @Nullable URI feedbackLink;

	TestFeatures(String codeName, LifeCycle phase, @Nullable URI feedbackLink, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		this.feedbackLink = feedbackLink;
		switch (patterns.length) {
			case 0:
				this.compiledPattern = PatternCompiler.compile(codeName);
				break;
			case 1:
				this.compiledPattern = PatternCompiler.compile(patterns[0]);
				break;
			default:
				this.compiledPattern = PatternCompiler.compile('(' + String.join("|", patterns) + ')');
				break;
		}
	}

	TestFeatures(String codeName, LifeCycle phase) {
		this(codeName, phase, codeName);
	}

	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (TestFeatures value : values()) {
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

	static {
		registerAll(Skript.getAddonInstance(), Skript.experiments());
	}

}
