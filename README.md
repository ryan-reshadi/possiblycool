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
For class-directory launches, run `mvn compile` first, then launch with `java -cp target/classes GameRun.App`. This uses the same output directory as Maven and Eclipse instead of potentially stale classes in `bin`. When running from an IDE, use the project's Maven classpath.

Choose a hero to begin a generated branching run. The tutorial is the first route node, followed by combat rooms, occasional shops, and a final boss. Progress, class loadouts, coins, route choices, and shop stock are saved locally per character in `~/.possiblycool/player-progress.json`. Leaving during a combat room lets you replay that room; defeat ends the run while preserving profile progress. Shops sell class-specific gear and powers, and right-clicking items in the inventory salvages them for coins.

The test suite is headless and runs during `mvn clean verify`. Continuous integration verifies the build on Windows, macOS, and Linux.

## Controls

- `W`, `A`, `S`, `D`: move
- `Space`: roll
- `R`: activate the ultimate when charged
- `Q`, `E`, `F`: use ability slots
- `I`: open or close inventory
- `1`-`9` or the mouse wheel: select a hotbar item
- Left-click: attack; click a nearby chest to open it

The bottom-right HUD summary shows the selected item's remaining uses and stack count. The `[I] Inventory` hint is displayed at the top-right.
