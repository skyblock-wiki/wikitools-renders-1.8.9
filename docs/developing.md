## Developer Notes

- When bumping version, modify `gradle.properties`.
- Mod settings may exist in these places: `gradle.properties`, `build.gradle`, `mcmod.info`, and `WikiToolsRendersIdentity.java`.

## Current Practices

- Use coding best practices.
- Write good tests, automated or manual.
- The top-level project directory has two possible subdirectories: `feature` and `common`.
    - Each subdirectory of `feature` consists of a feature of the mod.
    - Normally, feature code should remain isolated to that feature until it becomes beneficial to share a piece of code.
    - The `common` directory consists of code that are sparingly shared between features.
