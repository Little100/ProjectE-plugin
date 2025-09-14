# ProjectE
[Recipes](./help/recipe.md)
## Description

ProjectE is a Minecraft Spigot plugin inspired by the famous "Equivalent Exchange" Mod. It introduces a core system of matter transmutation to the server, allowing players to break down items into Energy-Matter Covalence (EMC) units and use that EMC to create new items.

This plugin is designed to add depth to survival mode, RPG servers, or any server that wishes to provide players with a more flexible way of managing resources.

## Core Features

 * just like project E, but its a spigot plugin

## Commands

Here are the main commands for the ProjectE plugin:

*   `/projecte`: Displays the help menu with all available commands.
*   `/projecte reload`: Reloads the plugin's configuration files.
*   `/projecte setemc <value>`: Sets the EMC value for the item held in your hand.
*   `/projecte debug`: Displays detailed debug information for the item in hand, including its EMC value, recipes, and learned status.
*   `/projecte give <player> <amount>`: Gives a specified amount of EMC to another player.
*   `/projecte noemcitem`: Opens a GUI that lists all items currently without an EMC value.
*   `/projecte bag list`: Lists all the colored Alchemical Bags you own.
*   `/projecte lang list`: Lists all available language files.
*   `/projecte lang set <language>`: Sets the display language for the plugin.

## Permissions

*   `projecte.command.setemc`: Allows use of the `/projecte setemc` command. (Default: OP)
*   `projecte.command.reload`: Allows use of the `/projecte reload` command. (Default: OP)
*   `projecte.command.debug`: Allows use of the `/projecte debug` command. (Default: OP)
*   `projecte.command.give`: Allows use of the `/projecte give` command. (Default: true)
*   `projecte.command.noemcitem`: Allows use of the `/projecte noemcitem` command. (Default: OP)
*   `projecte.interact.transmutationtable`: Allows players to open and use the Transmutation Table. (Default: true)
*   `projecte.command.bag`: Allows use of Alchemical Bag related commands. (Default: true)
*   `projecte.command.lang`: Allows use of the `/projecte lang` command. (Default: OP)

## Installation

1.  Place the `ProjectE.jar` file into your server's `plugins` folder.
2.  Restart or reload your server.
3.  The plugin will generate default configuration files in the `plugins/ProjectE/` folder.
4.  Modify `config.yml` and `emc.yml` according to your needs.
5.  Use the `/projecte reload` command to apply the changes.
