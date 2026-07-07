# Design: ore vein generation

Date: 2026-07-07
Status: implemented in `feature/ore-vein-generation`

## Goals

1. Real-geology ore distribution: ore comes from large, named, discrete
   deposits ("veins") instead of scattered blobs. A vein is a find — the
   intended loop is "locate a few veins covering your resources and work them
   for a long time."
2. Universal and cheap to extend: a new vein type is one readable builder
   line; adding one must be safe for existing worlds.
3. Veins have identity: a name, a list of contained ores with weights, and a
   per-instance richness roll (poor veins are mostly rock between ores, rich
   veins are nearly solid ore).
4. Veins are spread out but randomly placed, and can never overlap.

Vein *finding* tools (prospecting) are planned separately; this feature only
generates the veins.

## Decisions

### The model: deterministic region grid

The overworld is partitioned into 24x24-chunk regions (384x384 blocks). Every
region hosts exactly one vein. Everything about a region's vein — type,
center, size, richness — is a pure function of `(worldSeed, regionX, regionZ)`.
Nothing is stored: any chunk, generated at any time in any order, recomputes
the same answer (the TerraFirmaCraft approach; no save data, no caches, no
cross-chunk writes, no race conditions with threaded chunk gen).

- Vein type is chosen by weighted rendezvous hashing over all registered vein
  *names*: each type scores `hash(worldSeed, region, name)` scaled by its
  spawn weight; the highest score wins. Selection is independent of
  registration order, so adding a vein type later only reassigns the regions
  the new type wins — every other vein in an existing world stays put.
- Instance rolls (center, radius, thickness, richness) come from a mod-owned
  splitmix64-seeded generator keyed by (worldSeed, regionX, regionZ, salt) —
  the same recipe as vanilla's `setLargeFeatureWithSalt`, but owned by the mod
  so vein layouts cannot shift if Minecraft changes its RNG internals, and so
  the math core stays free of Minecraft classes (the unit-test rule in
  `build.gradle`).
- The center is confined to the central half of the region. With the max
  horizontal radius capped at 96 blocks (region size / 4), veins in adjacent
  regions can at worst meet exactly at the border — non-overlap is a
  structural guarantee, not a runtime check. Neighbor spacing works out to
  193 blocks minimum, ~384 typical, ~576+ max.

### Vein type definitions

Vein types are one-liner builders in `content/worldgen/GtbwVeins.java`,
registered into a plain static registry in `api/worldgen/vein/`:

```java
public static final VeinType CASSITERITE = register(
    VeinType.builder("cassiterite")
        .ore(ModBlocks.TIN_ORE, ModBlocks.DEEPSLATE_TIN_ORE, 80)
        .ore(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, 20)
        .richness(0.20f, 0.50f)   // rolled per vein instance
        .yBand(20, 70)            // vein center constrained to this band
        .radius(48, 80)           // horizontal, rolled per instance
        .thickness(12, 24)        // vertical, rolled per instance
        .spawnWeight(10));        // rarity relative to other vein types
```

Each ore entry carries a stone + deepslate variant; the placed block always
matches the host rock. Registration validates at mod construction and fails
fast with the offending vein named: unique name, radius within the cap,
non-empty ores, positive weights, richness in (0,1], sane Y-band.

### Geometry and richness

A vein is a flattened ellipsoid lens: circular footprint, vertical semi-axis
= thickness/2. For a position at offset (dx, dy, dz) from the center:

```
d = sqrt( (dx^2 + dz^2)/radius^2 + dy^2/(thickness/2)^2 )   inside if d <= 1
```

with roughly +/-12% positional-noise wobble on the boundary so lenses are not
perfect eggs. Ore probability at a position is
`richness * min(1, 1.25 * (1 - d))`: a saturated core plateau thinning to
nothing at the fringe, so hitting the edge of a vein points you inward —
prospecting-by-digging works before dedicated tools exist.

All per-block decisions (ore-or-rock, which ore) use a positional hash of
(instanceSeed, x, y, z) — the same idea as vanilla's positional random —
never sequential draws, so neighboring chunks always agree about the vein
they share. A mid-roll vein (radius 64, thickness 18) yields roughly
10k-35k ore blocks depending on the richness roll; the largest, richest
rolls approach 70k.

### Placement rules

- Only blocks in `stone_ore_replaceables` / `deepslate_ore_replaceables` are
  replaced. Caves, dirt, fluids, and player blocks are untouched; a cave
  through a vein simply exposes it.
- Center Y is inset by thickness/2 so the lens fits its configured band.

### Integration

- One custom `VeinFeature` (`registry/ModFeatures`), added to
  `#minecraft:is_overworld` at the `underground_ores` step. Per chunk it
  checks its own region and the 8 neighbors, AABB-rejects almost always, and
  writes only inside its own chunk (the worldgen engine only permits writes
  near the decorated chunk; per-chunk carving sidesteps the limit entirely).
- Datagen (`DatapackBuiltinEntriesProvider`) emits the configured/placed
  feature, the `neoforge:add_features` biome modifier, and a
  `neoforge:remove_features` modifier that deletes vanilla scattered
  generation for covered ores only: copper (`ore_copper`, `ore_copper_large`),
  iron (`ore_iron_upper`, `ore_iron_middle`, `ore_iron_small`),
  coal (`ore_coal_lower`, `ore_coal_upper`). Veins are THE source for those
  ores; uncovered ores (gold, redstone, ...) keep vanilla gen until they get
  veins.

### v1 content

New: `tin_ore` + `deepslate_tin_ore` blocks, `raw_tin` item, smelting and
blasting recipes (raw tin -> tin ingot), fortune-aware loot tables, stone-tier
mining — full datagen coverage. Starting roster (tuned after in-world review):

| Vein           | Ores               | Richness  | Y-band  | Weight |
|----------------|--------------------|-----------|---------|--------|
| `cassiterite`  | tin 80 / copper 20 | 0.20-0.50 | 20..70  | 10     |
| `chalcopyrite` | copper 75 / iron 25| 0.25-0.60 | 0..60   | 10     |
| `magnetite`    | iron 100           | 0.30-0.65 | -30..40 | 10     |
| `coal_seam`    | coal 100           | 0.35-0.70 | 30..80  | 12     |

### Known caveats (accepted)

- Veins generate in new chunks only; no retrogen. Determinism keeps an
  opt-in retrogen pass possible later.
- Adding a vein type mid-world can hand an ungenerated part of a region to
  the new type (hybrid region at the frontier). Removing or renaming one
  (rename = remove + add; the name is the identity) re-resolves its regions.
  Both are rare-edge, documented worldgen-mod tradeoffs.

## Testing

JUnit on the pure core (`VeinMath` holds no Minecraft block references; ores
are indices into the definition):

- Determinism: same (seed, region) -> identical instance regardless of call
  order.
- Non-overlap property test across thousands of seeds and adjacent region
  pairs — the load-bearing guarantee gets the heaviest test.
- Confinement: centers in the central zone, geometry within bounds, radius
  cap respected.
- Rendezvous stability: adding a type only reassigns regions it wins.
- Distribution: ore weights and richness rolls converge to configuration;
  falloff monotonic in d.
- Validation: every fail-fast rule fires.

In-game: op-only `/gtbwvein` debug command reporting the current region's vein
(type, center, richness, distance) — doubles as the seed of future
prospecting tools — plus a fresh-world flyover. `/gtbwvein show` outlines the
vein's boundary shell with glowing markers visible through terrain (auto-clear
after 60s, `/gtbwvein hide` to clear early), so vein size and shape can be
judged without mining the deposit out.

## Out of scope (deliberately)

Prospecting/vein-finding tools, surface indicators, retrogen, biome or
dimension filters, zoned ore layouts inside veins, JSON/datapack vein
definitions, and touching vanilla gen for ores without veins. Each can layer
onto this system without reshaping it.