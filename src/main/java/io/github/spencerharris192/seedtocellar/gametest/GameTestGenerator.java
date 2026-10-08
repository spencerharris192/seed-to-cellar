package io.github.spencerharris192.seedtocellar.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

/**
 * Makes several GameTests at once: a {@code public static List<GameTestGenerator.Case> name()} method (one test per
 * fruit tree, say). {@link ModGameTests} registers each case like a {@link GameTest}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTestGenerator {
    /** One generated test: its name (lowercase, underscores), template, time limit and body. */
    record Case(String name, String template, int timeoutTicks, Consumer<GameTestHelper> test) {}
}
