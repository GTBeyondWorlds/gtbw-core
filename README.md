# GTBW Core

Core mod for **GT Beyond Worlds**.

A [NeoForge](https://neoforged.net/) mod for **Minecraft 1.21.1**. Early days:
so far it adds base metal resources (copper, bronze, tin, steel) and the first
two multiblock structures (Coke Oven, Bricked Blast Furnace). See
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the project is organized
and how to add content.

## Requirements

- **JDK 21** (Minecraft 1.21.1 targets Java 21)

## Development

Run from the project root:

```bash
# Launch the game with the mod (client)
./gradlew runClient

# Build the mod jar (output in build/libs/)
./gradlew build

# Regenerate JSON assets/data after changing datagen providers
./gradlew runData
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Models, blockstates, loot tables, tags, recipes, and lang files are generated
by `runData` into `src/generated/resources` (committed). Don't hand-edit them —
change the providers in the `datagen` package and re-run.

## Project details

| | |
|---|---|
| Mod ID | `gtbwcore` |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.234 |
| Java | 21 |

## Contributing

Branch off `main`, open a pull request, get one review — see
[CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow.

## License

All Rights Reserved. See [LICENSE](LICENSE).
