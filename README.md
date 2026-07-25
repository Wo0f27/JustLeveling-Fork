# JustLevelingFork 1.20.1

JustLevelingFork is a server-authoritative RPG progression mod for Minecraft 1.20.1. This branch builds matching Fabric and Forge artifacts from a shared codebase.

## Requirements

- Java 17
- Minecraft 1.20.1
- Fabric Loader + Fabric API, or Forge 47.4.x

Optional integrations include Mod Menu, Trinkets/Curios, KubeJS, FTB Quests, Questlog, Better Combat, TACZ, Scorched Guns 2, and Iron's Spells 'n Spellbooks.

## Building

On Windows:

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17"
.\gradlew.bat clean build
```

On Linux or macOS:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew clean build
```

Release jars are written to `fabric/build/libs` and `forge/build/libs`.

## Configuration

The common settings control aptitude limits and costs, item-drop behavior, UI preferences, and Treasure Hunter/Convergence item lists.

- Fabric: `config/justlevelingfork.json`
- Forge: `config/justlevelingfork.toml`
- Skills: `config/justlevelingfork.skills.json`
- Passives: `config/justlevelingfork.passives.json`
- Titles: `config/justlevelingfork.titles.json`
- Item restrictions: `config/justlevelingfork.lock_items.json`

Malformed JSON files recover to validated defaults instead of preventing startup. Server-owned gameplay settings, item restrictions, and title definitions are synchronized to connected clients.

## Operator commands

All administrative commands require permission level 2.

- `/aptitudes <player> <aptitude> get`
- `/aptitudes <player> <aptitude> set <level>`
- `/aptitudes <player> <aptitude> add <levels>`
- `/aptitudes <player> <aptitude> subtract <levels>`
- `/titles <player> <title> set <true|false>`
- `/registeritem <aptitude> <level>` — registers the held item; use level `0` to remove that aptitude requirement.
- `/aptitudesreload` — reloads item restrictions and synchronizes connected players.
- `/titlesreload` — reloads title definitions and synchronizes connected players.
- `/updateaptitudelevel <level>` — changes the per-aptitude maximum.
- `/globallimit <level>` — changes the maximum combined aptitude level.

## License

CC0 1.0 Universal. See [LICENSE](LICENSE).
