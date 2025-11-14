# 安装指南

本指南将帮助你在服务器上安装和配置 ProjectE 插件。

## 前置要求

在安装 ProjectE 之前，请确保你的服务器满足以下要求：

### 必需条件

- **Minecraft 版本**: 1.16.5 或更高版本（推荐 1.20+）
- **服务端类型**: Spigot 或 Paper（强烈推荐使用 Paper）
- **Java 版本**: Java 17 或更高版本（推荐 Java 21）
- **服务器权限**: 需要有服务器文件的访问权限

### 推荐配置

- **内存**: 至少 2GB RAM
- **CPU**: 多核处理器
- **存储**: 至少 100MB 可用空间

### 可选依赖

- **Geyser**: 如果需要支持基岩版玩家（实验性功能）
- **Folia**: 如果使用 Folia 多线程服务端

## 安装步骤

### 1. 下载插件

从以下渠道下载最新版本的 ProjectE 插件：

- [GitHub Releases](https://github.com/yourusername/ProjectE-plugin/releases)
- [SpigotMC](https://www.spigotmc.org/)
- [Modrinth](https://modrinth.com/)

下载 `ProjectE-x.x.x.jar` 文件（x.x.x 是版本号）。

### 2. 安装插件文件

1. 找到你的服务器根目录
2. 进入 `plugins` 文件夹（如果不存在则创建）
3. 将下载的 `ProjectE-x.x.x.jar` 文件复制到 `plugins` 文件夹中

```
服务器根目录/
├── plugins/
│   └── ProjectE-1.2.4.jar  ← 放在这里
├── world/
├── server.jar
└── ...
```

### 3. 安装资源包和数据包（推荐）

ProjectE 提供了官方资源包和数据包，以获得最佳的视觉体验。

#### 安装资源包

1. 在 `plugins/ProjectE/pack/` 目录下找到 `ProjectE Resourcepack.zip`
2. 将资源包上传到你的服务器或文件托管服务
3. 在 `server.properties` 中配置资源包 URL：

```properties
resource-pack=https://your-server.com/ProjectE-Resourcepack.zip
resource-pack-sha1=<SHA1哈希值>
require-resource-pack=false
```

#### 安装数据包

1. 在 `plugins/ProjectE/pack/` 目录下找到 `ProjectE Datapack.zip`
2. 将数据包解压到世界文件夹的 `datapacks` 目录：

```
world/
└── datapacks/
    └── ProjectE Datapack/
        ├── data/
        └── pack.mcmeta
```

3. 重新加载数据包：

```
/reload
```

### 4. 启动服务器

1. 启动或重启你的 Minecraft 服务器
2. 观察控制台输出，确认插件正确加载

你应该看到类似以下的输出：

```
[ProjectE] Enabling ProjectE v1.2.4
[ProjectE] Loading configuration files...
[ProjectE] Loading EMC values...
[ProjectE] Registering recipes...
[ProjectE] ProjectE has been enabled successfully!
```

### 5. 验证安装

在游戏中或控制台执行以下命令来验证安装：

```
/projecte
```

如果看到插件的帮助菜单，说明安装成功！

## 首次配置

### 生成的文件结构

首次启动后，插件会在 `plugins/ProjectE/` 目录下生成以下文件：

```
plugins/ProjectE/
├── config.yml              # 主配置文件
├── accessories.yml         # 饰品配置
├── command.yml            # 指令配置
├── condenser.yml          # 凝聚器配置
├── condenser_mk2.yml      # MK2凝聚器配置
├── custommoditememc.yml   # 自定义EMC值
├── darkmatterfurnace.yml  # 暗物质熔炉配置
├── devices.yml            # 设备配置
├── mapping.yml            # 物品映射
├── op_item.yml            # OP物品配置
├── recipe.yml             # 配方配置
├── redmatterfurnace.yml   # 红物质熔炉配置
├── lang/                  # 语言文件
│   ├── zh_cn.yml
│   ├── en_us.yml
│   └── lzh.yml
├── pack/                  # 资源包和数据包
│   ├── ProjectE Resourcepack.zip
│   └── ProjectE Datapack.zip
└── data/                  # 数据存储
    └── players/           # 玩家数据
```

### 基础配置

编辑 `config.yml` 进行基础配置：

```yaml
# 语言设置
language: zh_cn

# EMC 设置
emc:
  # 是否启用 EMC 系统
  enabled: true
  # 默认 EMC 倍率
  multiplier: 1.0

# 转换桌设置
transmutation-table:
  # 是否启用转换桌
  enabled: true
  # 学习模式 (NORMAL/CREATIVE)
  learn-mode: NORMAL

# 资源包设置
resource-pack:
  # 是否强制使用资源包
  force: false
  # 资源包 URL
  url: ""
```

### 重新加载配置

修改配置后，使用以下命令重新加载：

```
/projecte reload
```

## 常见问题

### 插件无法加载

**问题**: 服务器启动时插件报错或无法加载

**解决方案**:
1. 检查 Java 版本是否满足要求（Java 17+）
2. 确认服务端类型是 Spigot 或 Paper
3. 查看控制台的完整错误信息
4. 确保没有其他冲突的插件

### 资源包无法加载

**问题**: 玩家进入服务器后没有看到自定义材质

**解决方案**:
1. 确认资源包 URL 可以正常访问
2. 检查 `server.properties` 中的配置是否正确
3. 确认 SHA1 哈希值是否匹配
4. 让玩家手动接受资源包

### 数据包不生效

**问题**: 游戏中的物品没有自定义模型

**解决方案**:
1. 确认数据包已正确放置在 `world/datapacks/` 目录
2. 执行 `/reload` 命令重新加载数据包
3. 使用 `/datapack list` 检查数据包是否已启用
4. 如果数据包被禁用，使用 `/datapack enable "file/ProjectE Datapack"`

### 权限问题

**问题**: 玩家无法使用某些功能

**解决方案**:
1. 检查权限插件配置
2. 确认玩家拥有相应的权限节点
3. 查看 [权限列表](./permissions.md) 了解所有权限

## 升级指南

### 从旧版本升级

1. **备份数据**: 在升级前备份 `plugins/ProjectE/` 整个目录
2. **停止服务器**: 完全关闭服务器
3. **替换插件**: 用新版本的 jar 文件替换旧版本
4. **启动服务器**: 启动服务器，插件会自动迁移数据
5. **检查配置**: 查看是否有新的配置选项需要设置

### 数据迁移

插件会自动处理数据迁移，但建议：

1. 在测试服务器上先测试新版本
2. 保留旧版本的备份至少一周
3. 通知玩家升级时间，避免数据丢失

## 卸载插件

如果需要卸载 ProjectE：

1. **停止服务器**
2. **备份数据**（如果以后可能重新安装）
3. **删除插件文件**: 删除 `plugins/ProjectE-x.x.x.jar`
4. **删除数据**（可选）: 删除 `plugins/ProjectE/` 目录
5. **移除数据包**: 从 `world/datapacks/` 中移除 ProjectE 数据包
6. **启动服务器**

::: warning 注意
卸载插件后，所有玩家的 EMC 数据和学习记录将会丢失！
:::

## 下一步

安装完成后，你可以：

- 查看 [快速开始](./getting-started.md) 学习基础使用
- 阅读 [指令列表](./commands.md) 了解所有可用指令
- 配置 [权限系统](./permissions.md) 管理玩家权限
- 自定义 [配置文件](../config/) 调整插件行为

## 获取帮助

如果在安装过程中遇到问题：

1. 查看控制台的完整错误日志
2. 检查 [常见问题](./faq.md)
3. 在 [GitHub Issues](https://github.com/yourusername/ProjectE-plugin/issues) 提问
4. 加入我们的社区讨论