package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registers our GameTests in 26.3's test registries (a test function, and a test instance saying where and how long it
 * runs). Tests are plain static methods marked {@link GameTest} (or made by a {@link GameTestGenerator}), found through
 * FML's scan of the mod, so a new test class needs no registration. A test's ID is
 * {@code seedtocellar:<class>/<method in snake_case>}, e.g. {@code seedtocellar:smoketests/every_crop_is_registered_and_tagged}.
 * This package is left out of the jar, so none of it reaches players.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class ModGameTests {
    private record Entry(Identifier id, Identifier template, int timeoutTicks, int setupTicks, String batch, boolean required,
                         Consumer<GameTestHelper> test) {}

    private static List<Entry> entries;

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> entries().forEach(e -> helper.register(e.id(), e.test())));
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Map<String, Holder<TestEnvironmentDefinition<?>>> batches = new HashMap<>();
        for (Entry e : entries()) {
            Holder<TestEnvironmentDefinition<?>> batch = batches.computeIfAbsent(e.batch(), name -> event.registerEnvironment(SeedToCellar.id(name)));
            // sky access: no barrier ceiling over the template (tall crops grow into the block above it, as in 1.20.1)
            event.registerTest(e.id(), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, e.id()),
                    new TestData<>(batch, Level.OVERWORLD, e.template(), e.timeoutTicks(), e.setupTicks(), e.required(), Rotation.NONE,
                            false, 1, 1, true, 0)));
        }
    }

    private static synchronized List<Entry> entries() {
        if (entries != null) return entries;
        List<Entry> found = new ArrayList<>();
        for (Class<?> type : testClasses()) {
            String prefix = type.getSimpleName().toLowerCase() + "/";
            for (Method method : type.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) continue;
                GameTest test = method.getAnnotation(GameTest.class);
                if (test != null) {
                    found.add(new Entry(SeedToCellar.id(prefix + snake(method.getName())), template(test.template()), test.timeoutTicks(),
                            test.setupTicks(), test.batch(), test.required(), helper -> invoke(method, helper)));
                }
                if (method.isAnnotationPresent(GameTestGenerator.class)) {
                    for (Object o : (List<?>) invoke(method)) {
                        GameTestGenerator.Case c = (GameTestGenerator.Case) o;
                        found.add(new Entry(SeedToCellar.id(prefix + c.name()), template(c.template()), c.timeoutTicks(), 0, "default", true, c.test()));
                    }
                }
            }
        }
        found.sort(Comparator.comparing(e -> e.id().toString()));
        return entries = List.copyOf(found);
    }

    /** Every class of ours with a test or test generator, from the mod loader's scan of our classes. */
    private static List<Class<?>> testClasses() {
        String pkg = ModGameTests.class.getPackageName() + ".";
        return ModList.get().getAllScanData().stream()
                .flatMap(scan -> java.util.stream.Stream.concat(scan.getAnnotatedBy(GameTest.class, ElementType.METHOD),
                        scan.getAnnotatedBy(GameTestGenerator.class, ElementType.METHOD)))
                .map(a -> a.clazz().getClassName())
                .filter(name -> name.startsWith(pkg))
                .distinct().sorted()
                .<Class<?>>map(name -> {
                    try {
                        return Class.forName(name);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                }).toList();
    }

    private static Identifier template(String name) {
        return name.contains(":") ? Identifier.parse(name) : SeedToCellar.id(name);
    }

    /** someTestName -> some_test_name */
    private static String snake(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /** Calls a test method, passing on a failed assertion as itself (not wrapped). */
    private static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException r) throw r;
            if (e.getCause() instanceof Error r) throw r;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private ModGameTests() {}
}
