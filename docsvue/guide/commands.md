# 指令列表

本页面列出了 ProjectE 插件的所有可用指令及其用法。

## 基础指令

### `/projecte`

显示插件的帮助菜单，列出所有可用指令。

**用法**:
```
/projecte
/pe
```

**权限**: 无需权限

**示例**:
```
/projecte
```

---

## 管理指令

### `/projecte reload`

重新加载插件的所有配置文件，包括语言文件、EMC 值、配方等。

**用法**:
```
/projecte reload
/pe reload
```

**权限**: `projecte.command.reload`

**示例**:
```
/projecte reload
```

::: tip 提示
使用此命令后，所有配置更改将立即生效，无需重启服务器。
:::

---

### `/projecte setemc <数值>`

为你手中持有的物品设置自定义 EMC 值。

**用法**:
```
/projecte setemc <数值>
/pe setemc <数值>
```

**参数**:
- `<数值>`: EMC 值，必须是正整数

**权限**: `projecte.command.setemc`

**示例**:
```
/projecte setemc 1000
/pe setemc 256
```

::: warning 注意
- 设置的 EMC 值会保存到 `custommoditememc.yml` 文件中
- 设置为 0 将移除该物品的自定义 EMC 值
- 某些物品可能无法设置 EMC 值
:::

---

### `/projecte debug`

显示手中物品的详细调试信息。

**用法**:
```
/projecte debug
/pe debug
```

**权限**: `projecte.command.debug`

**显示信息**:
- 物品的 EMC 值
- 物品的配方信息
- 物品的学习状态
- 物品的 PDC（持久化数据容器）信息
- 物品的自定义模型数据

**示例**:
```
/projecte debug
```

---

## EMC 管理

### `/projecte give <玩家> <数量>`

将指定数量的 EMC 给予另一位玩家。

**用法**:
```
/projecte give <玩家> <数量>
/pe give <玩家> <数量>
```

**参数**:
- `<玩家>`: 目标玩家的用户名
- `<数量>`: 要给予的 EMC 数量

**权限**: `projecte.command.give`

**示例**:
```
/projecte give Steve 10000
/pe give Alex 5000
```

::: tip 提示
- 可以使用 `@p`、`@a` 等选择器
- EMC 会直接添加到目标玩家的账户中
:::

---

### `/projecte noemcitem`

打开一个 GUI，显示所有当前没有 EMC 值的物品。

**用法**:
```
/projecte noemcitem
/pe noemcitem
```

**权限**: `projecte.command.noemcitem`

**功能**:
- 查看所有未设置 EMC 的物品
- 点击物品可以快速设置 EMC 值
- 支持搜索和过滤

**示例**:
```
/projecte noemcitem
```

---

## 炼金术袋管理

### `/projecte bag list`

列出你拥有的所有颜色的炼金术袋。

**用法**:
```
/projecte bag list
/pe bag list
```

**权限**: `projecte.command.bag`

**示例**:
```
/projecte bag list
```

**输出示例**:
```
你拥有以下炼金术袋:
- 红色炼金术袋
- 蓝色炼金术袋
- 绿色炼金术袋
```

---

### `/projecte bag open <颜色>`

打开指定颜色的炼金术袋。

**用法**:
```
/projecte bag open <颜色>
/pe bag open <颜色>
```

**参数**:
- `<颜色>`: 袋子的颜色（white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue, brown, green, red, black）

**权限**: `projecte.command.bag`

**示例**:
```
/projecte bag open red
/pe bag open blue
```

---

## 语言设置

### `/projecte lang list`

列出所有可用的语言文件。

**用法**:
```
/projecte lang list
/pe lang list
```

**权限**: `projecte.command.lang`

**示例**:
```
/projecte lang list
```

**输出示例**:
```
可用语言:
- zh_cn (简体中文)
- en_us (English)
- lzh (文言文)
```

---

### `/projecte lang set <语言>`

设置插件的显示语言。

**用法**:
```
/projecte lang set <语言>
/pe lang set <语言>
```

**参数**:
- `<语言>`: 语言代码（zh_cn, en_us, lzh 等）

**权限**: `projecte.command.lang`

**示例**:
```
/projecte lang set zh_cn
/pe lang set en_us
```

::: tip 提示
更改语言后会自动重新加载配置文件。
:::

---

## 设备管理指令

### `/projecte collector`

管理能量收集器。

**用法**:
```
/projecte collector info
/projecte collector list
```

**子命令**:
- `info`: 显示当前查看的收集器信息
- `list`: 列出所有能量收集器

**权限**: `projecte.command.collector`

---

### `/projecte condenser`

管理能量凝聚器。

**用法**:
```
/projecte condenser info
/projecte condenser list
```

**子命令**:
- `info`: 显示当前查看的凝聚器信息
- `list`: 列出所有能量凝聚器

**权限**: `projecte.command.condenser`

---

## 开发者指令

### `/projecte pdcitem`

打开 PDC 物品调试 GUI，用于查看和编辑物品的持久化数据。

**用法**:
```
/projecte pdcitem
/pe pdcitem
```

**权限**: `projecte.command.pdcitem`

**功能**:
- 查看物品的所有 PDC 数据
- 编辑 PDC 键值对
- 删除 PDC 数据

::: danger 警告
此命令仅供开发和调试使用，不当使用可能导致物品损坏！
:::

---

## 指令别名

为了方便使用，大部分指令都支持简短的别名：

| 完整指令 | 别名 |
|---------|------|
| `/projecte` | `/pe` |
| `/projecte reload` | `/pe rl` |
| `/projecte setemc` | `/pe semc` |
| `/projecte debug` | `/pe dbg` |
| `/projecte give` | `/pe g` |

---

## 权限总览

| 指令 | 权限节点 | 默认 |
|------|---------|------|
| `/projecte` | 无 | 所有人 |
| `/projecte reload` | `projecte.command.reload` | OP |
| `/projecte setemc` | `projecte.command.setemc` | OP |
| `/projecte debug` | `projecte.command.debug` | OP |
| `/projecte give` | `projecte.command.give` | 所有人 |
| `/projecte noemcitem` | `projecte.command.noemcitem` | OP |
| `/projecte bag` | `projecte.command.bag` | 所有人 |
| `/projecte lang` | `projecte.command.lang` | OP |
| `/projecte pdcitem` | `projecte.command.pdcitem` | OP |

查看完整的权限列表，请访问 [权限系统](./permissions.md) 页面。

---

## 使用技巧

### 批量操作

某些指令支持使用选择器进行批量操作：

```bash
# 给所有在线玩家 1000 EMC
/projecte give @a 1000

# 给最近的玩家 5000 EMC
/projecte give @p 5000
```

### 命令方块支持

大部分指令都支持在命令方块中使用：

```bash
# 在命令方块中设置
/projecte give @p[distance=..5] 100
```

### 控制台使用

所有管理指令都可以在服务器控制台中使用：

```bash
# 在控制台中重新加载配置
projecte reload

# 在控制台中给予玩家 EMC
projecte give Steve 10000
```

---

## 常见问题

### 指令无法使用

**问题**: 输入指令后提示"未知指令"

**解决方案**:
1. 确认插件已正确加载
2. 检查指令拼写是否正确
3. 尝试使用完整指令而非别名

### 权限不足

**问题**: 提示"你没有权限使用此指令"

**解决方案**:
1. 检查权限插件配置
2. 确认拥有相应的权限节点
3. 联系服务器管理员

### 指令参数错误

**问题**: 提示"参数错误"或"用法错误"

**解决方案**:
1. 检查参数格式是否正确
2. 确认参数数量是否匹配
3. 查看本页面的指令用法说明

---

## 下一步

- 了解 [权限系统](./permissions.md) 配置玩家权限
- 查看 [配置文件](../config/) 自定义插件行为
- 学习 [物品列表](../items/) 了解所有可用物品