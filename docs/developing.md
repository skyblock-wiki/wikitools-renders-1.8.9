## Developer Notes

- When bumping version, pay attention that the version number exists in two places: `build.gradle` and `WikiToolsRendersInfo.java`.
- Mod settings exist in three places: `build.gradle`, `mcmod.info`, and `WikiToolsRendersInfo.java`.

## Current Practices

- Use coding best practices.
- The top-level project directory has two possible subdirectories: `feature` and `common`.
    - Each subdirectory of `feature` consists of a feature of the mod.
    - Normally, feature code should remain isolated to that feature until it becomes beneficial to share a piece of code.
    - The `common` directory consists of code that are sparingly shared between features.
