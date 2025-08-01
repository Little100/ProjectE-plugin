# ProjectE
[合成表](./help/recipe.md)
[English Version](./help/README_en.md)

## 描述

ProjectE 是一个 Minecraft Spigot 插件，其灵感来源于著名的 "等价交换" Mod。它为服务器带来了一个核心的物质转换系统，允许玩家将物品分解为能量物质单位 (EMC)，并使用 EMC 创造新的物品。

这个插件旨在为生存模式、RPG 服务器或任何希望为玩家提供更灵活资源管理方式的服务器增加深度。

## 核心功能

 * 不必多说，几乎每个人都应该玩过或者了解过此模组。

## 指令

以下是 ProjectE 插件的主要指令：

*   `/projecte`: 显示所有可用指令的帮助菜单。
*   `/projecte reload`: 重新加载插件的配置文件。
*   `/projecte setemc <数值>`: 为你手中持有的物品设置 EMC 值。
*   `/projecte debug`: 显示手中物品的详细调试信息，包括其 EMC 值、配方和学习状态。
*   `/projecte give <玩家> <数量>`: 将指定数量的 EMC 给予另一位玩家。
*   `/projecte noemcitem`: 打开一个 GUI，列出所有当前没有 EMC 值的物品。
*   `/projecte bag list`: 列出你拥有的所有颜色的炼金术袋。
*   `/projecte lang list`: 列出所有可用的语言文件。
*   `/projecte lang set <语言>`: 设置插件的显示语言。

## 权限

*   `projecte.command.setemc`: 允许使用 `/projecte setemc` 指令。 (默认: OP)
*   `projecte.command.reload`: 允许使用 `/projecte reload` 指令。 (默认: OP)
*   `projecte.command.debug`: 允许使用 `/projecte debug` 指令。 (默认: OP)
*   `projecte.command.give`: 允许使用 `/projecte give` 指令。 (默认: 所有人)
*   `projecte.command.noemcitem`: 允许使用 `/projecte noemcitem` 指令。 (默认: OP)
*   `projecte.interact.transmutationtable`: 允许玩家打开和使用转换桌。 (默认: 所有人)
*   `projecte.command.bag`: 允许使用炼金术袋相关指令。 (默认: 所有人)
*   `projecte.command.lang`: 允许使用 `/projecte lang` 指令。 (默认: OP)

## 安装

1.  将 `ProjectE.jar` 文件放入您服务器的 `plugins` 文件夹中。
2.  重启或重新加载您的服务器。
3.  插件将在 `plugins/ProjectE/` 文件夹中生成默认的配置文件。
4.  根据您的需要修改 `config.yml`。
5.  使用 `/projecte reload` 指令应用更改。
