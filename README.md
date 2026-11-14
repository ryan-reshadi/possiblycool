# possiblycool

A Java 17 desktop dungeon crawler using Swing. The game uses a fixed 1900-by-1000 logical viewport and scales it to fit resizable windows on Windows, macOS, and Linux.

## Requirements

- JDK 17 or newer
- Apache Maven 3.8 or newer

## Build, test, and run

From the repository root:

```text
mvn clean verify
java -jar target/dungeon-crawler-1.0.0.jar
```

Images are bundled as classpath resources, so the application can be launched from a working directory other than the repository root.
For IDE or class-directory launches such as `java -cp bin GameRun.App`, run from the repository root so the `images` directory is available; the asset loader checks the classpath first, then that working directory.

Defeating Level 1's boss completes the tutorial and unlocks the level selector from the main menu. The unlock is saved between runs; levels without a registered implementation are shown as locked.

The test suite is headless and runs during `mvn clean verify`. Continuous integration verifies the build on Windows, macOS, and Linux.

## Controls

- `W`, `A`, `S`, `D`: move
- `R`: roll
- `Q`, `E`, `F`: use ability slots
- `I`: open or close inventory
- `1`-`9` or the mouse wheel: select a hotbar item
- Left-click: attack; click a nearby chest to open it

The bottom-right HUD summary shows the selected item's remaining uses and stack count. The `[I] Inventory` hint is displayed at the top-right.
