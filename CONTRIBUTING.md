# Contributing to GTBW Core

## Workflow

This repo uses GitHub flow: `main` is the only long-lived branch, it is always
buildable, and everything reaches it through a reviewed pull request.

1. Branch off `main`, named by intent:
   - `feature/<topic>` — new content or capability
   - `fix/<topic>` — bug fixes
   - `chore/<topic>` — build, CI, tooling, docs housekeeping
2. Keep the branch focused on one change; the PR template's "no unrelated
   changes" checkbox is meant seriously.
3. Open a PR against `main` and fill in the template.
4. Merge requirements: CI green, one approval from the Core Dev team, squash
   merge. Never merge your own PR, and direct pushes to `main` are blocked.
5. Delete the branch after the merge.

Releases are semver tags on `main` (the version lives in `gradle.properties`
as `mod_version`). There is no separate development branch; if multi-version
Minecraft support ever forks our history, we will add per-version branches
(`1.21.1`, …) rather than a `dev` branch.

## Before opening a PR

- `./gradlew build` passes locally — this compiles, runs the unit tests, and
  packs the jar. CI runs exactly the same task.
- If you touched anything in the `datagen` package, run `./gradlew runData`
  and commit the regenerated `src/generated/resources`. Never hand-edit files
  in there; change the provider instead.
- If you changed in-game behavior, actually run it (`./gradlew runClient`)
  and check the relevant box in the PR template.
- New pure logic (no Minecraft classes) gets JUnit tests in `src/test/java`.

## Conventions

- Read `docs/ARCHITECTURE.md` first — it defines the package layout,
  registration rules, and the asset/datagen policy. Design decisions worth
  recording go in `docs/design/` as dated markdown files.
- Commit subjects are imperative ("Add coke oven", not "Added coke oven");
  the body explains *why* when it is not obvious from the diff.
- Java 21, UTF-8, match the style of the surrounding code.
