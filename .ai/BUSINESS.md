# CodeLens Pro 业务知识

核验日期：2026-09-20。
依据：当前 README、构建配置、插件描述和下列源码的静态检查；未执行构建、测试或 IDE 验收。

## 产品定位与边界

CodeLens Pro 是 IntelliJ IDEA 编辑器代码缩略图插件，用紧凑代码概览辅助导航和理解文件结构。
README 和插件描述声明：插件在 IDE 本地工作，不收集、传输或存储源码、项目文件、遥测或个人数据；无广告与跟踪。
插件受 CodeGlance Pro 启发，但拥有独立身份和实现，双方无隶属或背书关系；许可证为 MIT。
证据：`README.md`、`LICENSE`、`src/main/resources/META-INF/plugin.xml`。

## 身份与兼容性

插件 ID 为 `com.codelens.pro`，代码包为 `com.codelens.pro`，当前版本为 1.0.7。
当前 README 声明支持 IntelliJ IDEA 2026.1+；构建配置声明 sinceBuild 261，使用 JDK 21、Kotlin 2.4.0、IntelliJ Platform Gradle Plugin 2.16.0。
这些是声明的兼容范围，实际 IDE 兼容性仍需构建与插件验证确认。
证据：`gradle.properties`、`build.gradle.kts`、`README.md`、`src/main/resources/META-INF/plugin.xml`。

## 用户入口与主要功能

打开文件后在符合条件的编辑器右侧显示缩略图。Ctrl + Shift + G 切换显示，设置入口为 Settings | Tools | CodeLens Pro。
支持点击或拖动导航、拖动左边缘调整宽度、视口与光标标记、错误和警告标记、主题颜色、折叠感知布局、短文件不拉伸及隐藏原滚动条。
README 描述采用缓存图像、重绘节流、文档重建防抖和大文件轻量渲染以控制开销。
证据：`README.md`、`src/main/resources/META-INF/plugin.xml`。

## 编辑器适用范围与生命周期

当前代码行为：排除单行、renderer 模式和嵌入对话框的编辑器；允许 CONSOLE；MAIN_EDITOR 和 DIFF 需要关联虚拟文件。
启动活动初始化 EditorLensManager，监听编辑器创建与释放，同时为已有编辑器尝试挂载。
挂载还检查编辑器未释放、全局启用、未重复挂载和容器使用 BorderLayout；释放时移除面板、销毁监听资源并恢复滚动条。
证据：`src/main/kotlin/com/codelens/pro/EditorEligibility.kt`、`CodeLensProStartupActivity.kt`、`EditorLensManager.kt`（后两者位于同一源码目录）。

## 设置与大文件规则

以下为当前代码默认值与约束，不替代正式产品要求：
- 默认启用、自动宽度、错误警告显示和简化语言颜色；默认不隐藏原滚动条、不启用调试日志。
- 宽度默认 90，限制 40–180。
- 大文件阈值默认 3000 行，允许 500–100000；超大文件默认 10000 行，允许 1000–300000。
- 行数严格大于超大阈值时采用 MINIMAL；否则严格大于大文件阈值采用 SIMPLIFIED；其余采用 FULL。
- 应用级设置由 PersistentStateComponent 存入 CodeLens Pro.xml。
证据：`src/main/kotlin/com/codelens/pro/CodeLensProSettings.kt`、`src/main/kotlin/com/codelens/pro/PerformanceGuard.kt`。

## 场景与影响路由

| 修改场景 | 首要检查位置（类名均在 src/main/kotlin/com/codelens/pro） |
| --- | --- |
| 插件启动、编辑器显示范围、关闭释放 | CodeLensProStartupActivity、EditorLensManager、EditorEligibility |
| 缩略图布局、折叠、短文件保护 | MinimapLayout、FoldAwareLineMapper、FoldedRangeCollector、MinimapSnapshotBuilder |
| 图像、主题和性能 | MinimapImageRenderer、CodeLensProPainter、TokenColorCollector、ColorSchemeAdapter、PerformanceGuard |
| 点击拖动、滚轮与刷新 | CodeLensProPanel、MinimapWheelScroll、DiagnosticsRefreshScheduler |
| 错误警告标记 | DaemonDiagnosticCollector、DiagnosticSeverity、HighlightLineRange |
| 设置与快捷键 | CodeLensProSettings、CodeLensProConfigurable、ToggleCodeLensProAction、plugin.xml |
| 版本与发布元数据 | gradle.properties、build.gradle.kts、CHANGELOG.md、plugin.xml |

此表是后续调查入口，不表示已完整验证各类行为。

## 文档与构建约束

`docs/superpowers/specs/` 和 `docs/superpowers/plans/` 含历史设计与计划，部分沿用旧名称 ClongLens；使用前应与当前 README、代码和最新要求核对，不能将历史计划直接视为已实现规则。
尚未确定一份覆盖当前全部功能的正式产品规格，因此 project.toml 的 formal_product_spec 保持为空。

命令登记在 `.ai/project.toml`，当前登记的命令使用本机 Gradle。项目已提供 Gradle Wrapper（`gradlew`、`gradlew.bat`），配置版本为 9.7.0；本次提交未验证 Wrapper 运行。
构建需要通过 `localIdePath` Gradle 属性或 `CODELENS_PRO_IDE_PATH` 环境变量提供本地 IntelliJ IDEA 安装路径。
本机工具路径记录在被 Git 忽略的 `.ai/project.local.toml`；提供 JDK 25 路径不改变本项目的 JDK 21 toolchain。
未发现 src/test 测试源码；构建声明 kotlin(test) 和 JUnit Platform，不等于已有测试或测试已通过。
本次未配置数据库或 Redis。未来资源需求须从实际项目配置确认。
证据：`README.md`、`build.gradle.kts`、`gradle.properties`、当前 src/docs 文件清单。
