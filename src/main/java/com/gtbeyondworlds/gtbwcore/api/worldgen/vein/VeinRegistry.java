package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All registered vein definitions. Populated once from content code during
 * mod construction (before any worldgen); reads during worldgen see immutable
 * snapshots, so the hot path never locks.
 *
 * <p>{@link #all()} and {@link #types()} are parallel lists: index {@code i}
 * of one matches index {@code i} of the other.
 */
public final class VeinRegistry {

    private static final Map<String, VeinDefinition> BY_NAME = new LinkedHashMap<>();
    private static volatile List<VeinDefinition> definitionsView = List.of();
    private static volatile List<VeinType> typesView = List.of();

    private VeinRegistry() {}

    /**
     * Registers a definition. Names are permanent world-visible identity —
     * renaming one re-rolls which regions it owns (see the design record).
     *
     * @throws IllegalStateException if the name is already registered
     */
    public static synchronized VeinDefinition register(VeinDefinition definition) {
        String name = definition.type().name();
        if (BY_NAME.putIfAbsent(name, definition) != null) {
            throw new IllegalStateException("Duplicate vein name '" + name + "'");
        }
        List<VeinDefinition> definitions = new ArrayList<>(BY_NAME.values());
        definitionsView = List.copyOf(definitions);
        typesView = definitions.stream().map(VeinDefinition::type).toList();
        return definition;
    }

    /** Immutable snapshot of every registered definition, in registration order. */
    public static List<VeinDefinition> all() {
        return definitionsView;
    }

    /** Immutable snapshot of the pure types, parallel to {@link #all()}. */
    public static List<VeinType> types() {
        return typesView;
    }
}
