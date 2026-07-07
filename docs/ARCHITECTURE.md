# GTBW Core — Architecture

This mod will eventually contain hundreds of materials and thousands of items,
machines (single-block and multiblock), crafting components, upgrades, and
cables. Everything in this document exists to keep that growth manageable:
content is defined once in code, registration and assets are derived from those
definitions, and each subsystem lives in its own package with a clear owner.

## Package layout

All code lives under `com.gtbeyondworlds.gtbwcore`:

| Package    | Purpose |
|------------|---------|
| `api/`     | Reusable abstractions with no content in them. Code here may not depend on `content/` or `registry/`. Current: `api.multiblock` (structure patterns and the controller block base class) and `api.worldgen.vein` (deterministic ore vein engine). Future: materials, machine builders, energy. |
| `content/` | Actual game content, sliced by feature (`content.machine.cokeoven`, `content.machine.blastfurnace`, …). A feature package owns everything specific to that feature. |
| `registry/`| The central `DeferredRegister` holders: `ModBlocks`, `ModItems`, `ModCreativeTabs` (later `ModBlockEntities`, `ModMenus`, …). Content classes are instantiated here but defined in `content/`. |
| `client/`  | Client-only code (renderers, screens). Nothing outside this package may reference client-only Minecraft classes. Empty so far. |
| `datagen/` | Data generators. Runs only during `gradlew runData`, never ships in the jar. |
| `integration/` | Reserved: one subpackage per external mod we integrate with (JEI/EMI, Jade, …), so optional compat stays out of core code paths. Not created until the first integration lands. |

Dependency direction: `datagen` → `content`/`registry` → `api`. Never the
reverse.

## Registration conventions

- Every registry object goes through a `DeferredRegister` in `registry/`.
  No direct `Registry.register` calls.
- Blocks that need an item form are registered through
  `ModBlocks.registerWithItem(...)`, which creates the `BlockItem`
  automatically. One line per block, ever.
- The creative tab iterates the deferred registers instead of listing entries
  by hand; display order is registration order. When the item count justifies
  it, this will become per-category tabs.
- As the material count grows, per-material items (dust/ingot/plate/gear/…)
  will move to a material registry that generates the item registrations from
  a single material definition. Do not hand-register the fifth dust; build the
  material system instead.
- When machine counts grow past a handful, registration entries move out of
  the central `ModBlocks`/`ModItems` into per-feature holder classes in each
  `content` package (the pattern GregTech-family mods use: a `CokeOvenMachines`
  style holder per feature, aggregated at startup), with `registry/` keeping
  only the `DeferredRegister`s themselves.

## Assets and data: generated, not hand-written

All JSON under `src/generated/resources` is produced by `gradlew runData` from
the providers in `datagen/`. The generated output is committed so builds do not
depend on running datagen.

Rules:

- Never hand-edit anything in `src/generated/resources`; change the provider
  and re-run `gradlew runData`.
- `src/main/resources` contains only what cannot be generated: textures,
  `mods.toml` templates, and (later) sounds/shaders.
- New content is not done until its provider entries exist: model/blockstate,
  lang, loot table, tags, recipes.

At the current size this looks like overkill; at a thousand items it is the
difference between a one-line change and a 4,000-file diff.

## Multiblock structures

`api.multiblock` separates pure geometry from Minecraft integration so the
geometry is unit-testable without booting the game:

- `RelativePos` / `PatternFacing` — controller-relative coordinates
  (right, up, back) and the math that maps them into world space for each of
  the four horizontal facings.
- `MultiblockLayout` — a pattern parsed from layer strings (bottom layer
  first, rows front-to-back, characters left-to-right as seen when facing the
  machine). Exactly one controller character; `' '` means "must be air".
- `MultiblockPattern` — binds layout characters to `BlockState` predicates and
  matches the pattern against a level, reporting the first mismatching
  position and what was expected there.
- `MultiblockPartBlock` — base class for structure blocks. Placing or
  removing one pings nearby controllers, which is what makes formation
  event-driven.
- `MultiblockControllerBlock` — base class for controller blocks. Structures
  form and un-form automatically: any part change, neighbor change, or
  controller placement schedules a check that flips the `formed` blockstate
  property to match reality, and a slow polling heartbeat (20 ticks formed /
  40 unformed) catches changes with no callback (e.g. a foreign block pushed
  into a must-be-air cell). Right-click only reports status — what is missing
  and where.

Controllers get block entities the moment they gain inventories or recipe
logic; until then they stay stateless.

## Ore veins

`api.worldgen.vein` generates all modded ore placement: the overworld is a
grid of 24x24-chunk regions, each deterministically hosting one large vein
(type via weighted rendezvous hashing on vein names, geometry/richness via
mod-owned positional hashing — nothing is stored, every chunk recomputes
identical results). `VeinType`/`VeinMath`/`VeinInstance` are Minecraft-free
and unit-tested; `VeinDefinition`/`VeinRegistry`/`VeinFeature` bind ore
indices to blocks and carve one chunk at a time. Veins are defined as
one-liner builders in `content/worldgen/GtbwVeins.java`; vanilla scattered
generation is removed (via biome modifier) only for ores veins cover. Vein
names are permanent world identity: renaming one reshuffles its regions.
Details and caveats: `docs/design/2026-07-07-ore-vein-generation.md`.
The op-only `/gtbwvein` command reports the current region's vein.

## Testing

- Pure logic (pattern geometry, later: material math, recipe logic) is tested
  with plain JUnit in `src/test/java`; `gradlew build` runs these in CI.
- In-world behavior will use NeoForge GameTests once machines have logic worth
  testing; the run configurations are already in `build.gradle`.

## Roadmap (structural, in rough order)

1. **Material system** — one definition per material generating items, tags,
   recipes, and lang for every prefix (dust, ingot, plate, gear, …).
2. **Machine framework** — block entity base classes, energy/item capability
   wiring, GUI + menu plumbing, recipe types; single-block machines first.
3. **Multiblock logic** — recipe processing on formed structures, replacing
   the placeholder formation-only controllers.
4. **Networks** — cables/pipes as graph-based networks, not per-block ticking.
5. **GameTests** in CI for machine and multiblock behavior.
