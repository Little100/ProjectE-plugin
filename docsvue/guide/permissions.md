
# 权限系统

ProjectE 插件提供了完善的权限系统,允许服务器管理员精确控制玩家可以使用的功能。

## 权限节点总览

### 指令权限

| 权限节点 | 描述 | 默认值 |
|---------|------|--------|
| `projecte.command.reload` | 允许使用 `/projecte reload` 指令 | OP |
| `projecte.command.setemc` | 允许使用 `/projecte setemc` 指令 | OP |
| `projecte.command.debug` | 允许使用 `/projecte debug` 指令 | OP |
| `projecte.command.give` | 允许使用 `/projecte give` 指令 | 所有人 |
| `projecte.command.noemcitem` | 允许使用 `/projecte noemcitem` 指令 | OP |
| `projecte.command.bag` | 允许使用炼金术袋相关指令 | 所有人 |
| `projecte.command.lang` | 允许使用 `/projecte lang` 指令 | OP |
| `projecte.command.pdcitem` | 允许使用 `/projecte pdcitem` 指令 | OP |
| `projecte.command.collector` | 允许使用能量收集器管理指令 | OP |
| `projecte.command.condenser` | 允许使用能量凝聚器管理指令 | OP |

### 功能权限

| 权限节点 | 描述 | 默认值 |
|---------|------|--------|
| `projecte.interact.transmutationtable` | 允许打开和使用转换桌 | 所有人 |
| `projecte.interact.energycollector` | 允许使用能量收集器 | 所有人 |
| `projecte.interact.condenser` | 允许使用能量凝聚器 | 所有人 |
| `projecte.interact.alchemicalchest` | 允许使用炼金术箱子 | 所有人 |
| `projecte.interact.darkmatterfurnace` | 允许使用暗物质熔炉 | 所有人 |
| `projecte.interact.redmatterfurnace` | 允许使用红物质熔炉 | 所有人 |

### 物品使用权限

| 权限节点 | 描述 | 默认值 |
|---------|------|--------|
| `projecte.item.philosopherstone` | 允许使用贤者之石 | 所有人 |
| `projecte.item.kleinstar` | 允许使用克莱因之星 | 所有人 |
| `projecte.item.alchemicalbag` | 允许使用炼金术袋 | 所有人 |
| `projecte.item.diviningrod` | 允许使用探矿杖 | 所有人 |
| `projecte.item.repairtalisman` | 允许使用修复护符 | 所有人 |
| `projecte.item.darkmattertools` | 允许使用暗物质工具 | 所有人 |
| `projecte.item.redmattertools` | 允许使用红物质工具 | 所有人 |

### 护甲和饰品权限

| 权限节点 | 描述 | 默认值 |
|---------|------|--------|
| `projecte.armor.gemhelmet` | 允许使用宝石头盔 | 所有人 |
| `projecte.armor.gemchestplate` | 允许使用宝石胸甲 | 所有人 |
| `projecte.armor.gemleggings` | 允许使用宝石护腿 | 所有人 |
| `projecte.armor.gemboots` | 允许使用宝石靴子 | 所有人 |
| `projecte.accessory.bodystone` | 允许使用身体之石 | 所有人 |
| `projecte.accessory.soulstone` | 允许使用灵魂之石 | 所有人 |
| `projecte.accessory.mindstone` | 允许使用心灵之石 | 所有人 |
| `projecte.accessory.lifestone` | 允许使用生命之石 | 所有人 |

## 权限组配置示例

### LuckPerms 配置

#### 普通玩家组

```bash
# 基础权限
lp group default permission set projecte.command.give true
lp group default permission set projecte.interact.transmutationtable true
lp group default permission set projecte.item.philosopherstone true
lp group default permission set projecte.item.kleinstar true
lp group default permission set projecte.item.alchemicalbag true
```

#### VIP 玩家组

```bash
# VIP 额外权限
lp group vip permission set projecte.item.darkmattertools true
lp group vip permission set projecte.armor.gemhelmet true
lp group vip permission set projecte.interact.energycollector true
lp group vip permission set projecte.interact.condenser true
```

#### 管理员组

```bash
# 管理员权限
lp group admin permission set projecte.* true
```

### PermissionsEx 配置

编辑 `permissions.yml`:

```yaml
groups:
  default:
    permissions:
      - projecte.command.give
      - projecte.interact.transmutationtable
      - projecte.item.philosopherstone
      - projecte.item.kleinstar
      - projecte.item.alchemicalbag
  
  vip:
    inheritance:
      - default
    permissions:
      - projecte.item.darkmattertools
      - projecte.armor.gemhel