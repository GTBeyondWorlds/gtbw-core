# Design: project structure for scale + first two multiblocks

Date: 2026-07-06
Status: implemented in `feature/multiblock-foundation`

## Goals

1. Restructure the repo so it can grow to hundreds of materials and thousands
   of items/machines without collapsing under hand-written registrations and
   JSON files.
2. Add the first two multiblock structures as placeholders (formation logic
   only, no processing yet): the Coke Oven and the Bricked Blast Furnace.

## Decisions

### Structure

- Package-by-layer at the top (`api` / `content` / `registry` / `client` /
  `datagen`), package-by-feature inside `content`. Rationale and rules live in
  `docs/ARCHITECTURE.md`, which is the durable version of this section.
- Move `item/ModItems` and `item/ModCreativeTabs` to `registry/`; add
  `registry/ModBlocks` with a one-line block+item registration helper.
- The creative tab switches from a hand-maintained list to iterating the
  deferred registers, so new content shows up automatically.

### Datagen from day one

The MDK already wires `src/generated/resources` and the `runData` run; we now
use it. All existing hand-written JSON (item models, lang, `c:` tags, recipes)
migrates to providers with identical ids and values:

- Recipes keep their exact file names (e.g.
  `copper_ingot_from_smelting_copper_dust`), xp 0.7, 200 ticks smelting /
  100 ticks blasting, steel blasting-only, copper resulting in the vanilla
  ingot. Datagen additionally emits recipe-unlock advancements, which the
  hand-written files lacked — an intentional improvement.
- Tags keep the `c:dusts(/x)` and `c:ingots(/x)` shape.
- New for blocks: blockstates/models, loot tables (drop self), and
  `minecraft:mineable/pickaxe` block tags.

### Multiblock API

Pure geometry core (no Minecraft imports, unit-tested) + thin integration:

- Patterns are declared as layer strings, bottom layer first, rows
  front-to-back, characters left-to-right as seen when standing in front of
  the machine. Exactly one controller character anchors the pattern; spaces
  mean "must be air" (hollow interiors are enforced, not ignored).
- The controller block carries `FACING` (set on placement, pointing at the
  placer) and `FORMED`. Right-click validates: on success the controller
  reports "<name> formed!" and lights its front; on failure it reports the
  first wrong position and what belongs there.
- A formed controller re-validates on neighbor changes and via a 20-tick
  scheduled tick, so breaking any structure block un-forms it within a
  second. Event-driven invalidation is deferred until machine counts make the
  polling cost real.
- No block entities yet — controllers are stateless until processing logic
  arrives (see roadmap in ARCHITECTURE.md).

### The two structures

Both controllers sit on the second layer, centered on the front face; because
patterns are anchored at the controller, that is the only position that can
ever form.

**Coke Oven** — 3x3x3 of Coke Oven Bricks, hollow center: 25 bricks +
controller = 26 blocks, 1 air.

```
layer 3   BBB BBB BBB      (top)
layer 2   BCB B.B BBB      (C = controller, front row; . = air)
layer 1   BBB BBB BBB      (bottom)
```

**Bricked Blast Furnace** — 3x3 footprint, 4 high, solid base, hollow center
column above it (open chimney): 32 pieces + controller = 33 blocks.

```
layer 4   BBB B.B BBB      (top)
layer 3   BBB B.B BBB
layer 2   BCB B.B BBB      (C = controller, front row)
layer 1   BBB BBB BBB      (solid base)
```

### Placeholder assets

Simple generated 16x16 brick textures; controller front texture has a dark
opening that glows when formed. All models/blockstates via datagen.

## Testing

- JUnit 5 on the pure core: layout parsing/validation (single controller,
  rectangular layers), cell classification counts for both real patterns, and
  facing math pinned for all four rotations with an asymmetric fixture.
- `gradlew build` runs the tests in CI; a dedicated-server boot smoke test
  verifies registration at runtime locally.

## Out of scope (deliberately)

Recipe processing, fuel/heat, GUIs, block entities, GameTests, material
system. Each is sequenced in the ARCHITECTURE.md roadmap.
