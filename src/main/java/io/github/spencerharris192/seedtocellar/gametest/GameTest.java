package io.github.spencerharris192.seedtocellar.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A GameTest: a {@code public static void name(GameTestHelper helper)} method. {@link ModGameTests} finds and registers
 * every one in the mod, so a new test (or test class) needs nothing else.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
    /** The structure the test is built in: data/seedtocellar/structure/[template].nbt. */
    String template();

    /** Fails the test if it hasn't succeeded by then. */
    int timeoutTicks() default 100;

    /** Ticks to wait after building the structure, before the test starts. */
    int setupTicks() default 0;

    /** Tests in the same batch run together; a separate batch keeps slow tests apart. */
    String batch() default "default";

    boolean required() default true;
}
