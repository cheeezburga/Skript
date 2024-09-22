package ch.njol.skript.doc;

import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.LifeCycle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides experimental information to elements which are gated behind an opt-in {@link Experiment}.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Experimental {

	String name();
	LifeCycle phase();
	String feedbackLink() default "";

}
