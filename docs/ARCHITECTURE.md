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
| `api/`     | Reusable abstractions with no content in them. Code here may not depend on `content/` or `registry/`. Current: `api.multiblock` (structure patterns and the controller block base class). Future: materials, machine builders, energy. |
| `content/` | Actual game content, sliced by feature (`content.machine.cokeoven`, `content.machine.blastfurnace`, …). A feature package owns everything specific to that feature. |
| `registry/`| The central `DeferredRegister` holders: `ModBlocks`, `ModItems`, `ModCreativeTabs` (later `ModBlockEntities`, `ModMenus`, …). Content classes are instantiated here but defined in `content/`. |
| `client/`  | Client-only code (renderers, screens). Nothing outside this package may reference client-only Minecraft classes. Empty so far. |
| `datagen/` | Data generators. Runs only during `gradlew runData`, never ships in the jar. |

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
- `MultiblockControllerBlock` — base class for controller blocks. Right-click
  validates the structure and toggles the `formed` blockstate property;
  a formed controller re-validates on neighbor changes and via a slow
  scheduled tick so broken structures un-form.

Controllers get block entities the moment they gain inventories or recipe
logic; until then they stay stateless. Event-driven structure invalidation
(instead of the scheduled re-check) becomes worthwhile once multiblock counts
are large; the API surface will not change when that lands.

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
