# PublicGoon PvP Queue Plugin

A Minecraft Paper 1.21.1 plugin that provides a queue system for PvP matches.

## Features

- **Queue System**: Join queues for different PvP game modes (Axe, UHC, Sword)
- **Ranked vs Normal**: Choose between competitive ranked matches or casual normal matches
- **Beautiful GUI**: Interactive inventory interface for queue selection
- **Action Bar Display**: Real-time queue status with special formatting
- **Queue Management**: Track players in queue and display wait times

## Commands

### `/queue <axe|uhc|sword>`
Opens a GUI to select between ranked and normal queue for the specified game mode.

### `/queue leave`
Leave the current queue you're in.

### Aliases
- `/q` - Short alias for `/queue`

## Game Modes

- **Axe**: Axe PvP combat
- **UHC**: Ultra Hardcore PvP with golden apples
- **Sword**: Sword PvP combat

## Queue Types

- **Ranked**: Competitive matches with ELO rating system
- **Normal**: Casual matches for practice and fun

## Action Bar Format

While in queue, players will see:
```
ɪɴ ǫᴜᴇᴜᴇ ꜰᴏʀ [GameMode] - [QueueType] ([Time]s) - [Players] players
```

Example:
```
ɪɴ ǫᴜᴇᴜᴇ ꜰᴏʀ Axe - Ranked (45s) - 3 players
```

## Installation

1. Build the plugin using `./gradlew build`
2. Copy the resulting JAR file from `build/libs/` to your server's `plugins/` folder
3. Restart the server

## Requirements

- Minecraft Paper 1.21.1
- Java 21

## Usage

1. Join the server
2. Use `/queue axe` to open the queue selection GUI for Axe PvP
3. Choose between Ranked or Normal queue
4. Wait in queue while the action bar shows your status
5. Use `/queue leave` to exit the queue at any time

## Development

The plugin is structured with the following main components:

- `PublicGoon.java` - Main plugin class
- `QueueManager.java` - Manages queue operations and player tracking
- `QueueGUI.java` - Handles the inventory GUI for queue selection
- `QueueCommand.java` - Processes queue commands and GUI interactions
