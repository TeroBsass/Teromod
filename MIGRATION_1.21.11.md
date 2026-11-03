# Teromod migration: Minecraft 26.3 -> 1.21.11

Target toolchain:
- Minecraft 1.21.11
- NeoForge 21.11.48
- Java 21
- NeoGradle userdev 7.1.39
- Gradle wrapper 9.2.1

The original `master` branch remains unchanged. This branch is a dedicated migration workspace.

## Migration checklist

- [x] Retarget Gradle metadata and Java toolchain to the official 1.21.11 NeoGradle MDK baseline.
- [ ] Port 26.3-only Minecraft APIs in block, block-entity, menu and client GUI classes.
- [ ] Review data/resource JSON against 1.21.11 registries and formats.
- [ ] Run Gradle compile and game tests on Java 21.
- [ ] Produce and test the 1.21.11 NeoForge jar.

The Java source still needs API migration. This commit intentionally does not claim that the mod is already buildable.
