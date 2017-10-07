# Serenity: Community Edition

Serenity CE is licensed under the GNU General Public License version 3.

Serenity: Community Edition is an open-source release of the Serenity client for Minecraft 1.8.
The source is near-untouched as of the initial commit, the only changes since the commercial b50 release being removals of phone-home functions such as the update notifier, and security features.
Serenity: Community Edition has been released in place of commercial Serenity releases.

## Initializing a development environment

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew genIntellijRuns
```

For OptiFine, use [simpledeobf](https://github.com/octarine-noise/simpledeobf) to deobfuscate OptiFine,
include it in the classpath, and then use the `OptiFineTweakerDevWrapper` followed by the `SerenityCascadingTweaker` tweak classes instead of the `SerenityTweaker` class.

<!-- TODO: Video of development environment setup -->
