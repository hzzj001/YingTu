# 影图+（YingTu）quality-p0 静态核对报告（第 6 步：验证）

- 核对时间：2026-11-08 12:42（北京时间，UTC+8）
- 分支：`quality-p0`，HEAD：`aa2320e`
- 核对脚本：`verify_static.py`（只读仓库文件，不改源码）
- Java 文件总数：712 个；已定义类型全限定名：1587 个；仓库内包：68 个
- 结论先行：真实 Gradle 编译未完成（本机 Gradle daemon 无法通信，见 §0），以下均为静态核对结果，不能替代编译验证。

## 0. 编译尝试摘要

（本次运行未传入编译摘要）

## 1. 引用核对

- import 总数：7126（其中构建生成类 R/BuildConfig/databinding：151；外部依赖：5072）
- 全限定引用扫描命中：777 处（构建生成类：1）

### 1.1 import 未解决：0 个类

无。全部 import 的仓库内类均已定义。

### 1.2 全限定引用未解决：0 个类

无。全部全限定引用的仓库内类均已定义。

### 1.3 疑似未解析的类型位置引用：3 个标识符（需人工复核）

口径：出现在 `new` / `extends` / `implements` / `throws` / `instanceof` / 注解 / 泛型 / 强制转换 / `Xxx.yyy` 限定符位置的大写标识符，在本文件内既无定义、也不属于同包类、import、java.lang、星号导入（Android 框架按 android.jar 判定，其他外部星号导入按“无法判定”排除）。已做两项去噪：`Outer.Inner` 中 Outer 已解析时 Inner 按嵌套类型解析（Map.Entry、RecyclerView.Adapter、PlaybackAutoContext.Fact 等均属此类，不再误报）；方法实参里仅以逗号分隔出现的全大写标识按枚举常量排除。此类信号可能是缺类，也可能是缺 import，仍可能有少量误报，须逐项人工确认，不直接计入 1.1/1.2。

| 标识符 | 引用文件数 | 引用文件（示例） |
|---|---|---|
| `LayoutParams` | 2 | app/src/main/java/com/yingtu/plus/ui/custom/CustomWallView.java、app/src/main/java/com/yingtu/plus/ui/custom/KaraokeStatusView.java |
| `ScaleType` | 1 | app/src/main/java/com/yingtu/plus/ui/custom/ContextWallImageView.java |
| `VH` | 1 | app/src/main/java/com/yingtu/plus/ui/adapter/BaseDiffAdapter.java |

## 2. 括号 / 花括号平衡检查（去字符串与注释后统计）

- 新增/修改的 Java 文件：245 个（`git status --porcelain`）
- 其中括号不平衡：0 个。

- 全量检查：其余文件全部平衡。

## 3. MISSING_CLASSES.md 兑现检查

- §2 恢复类：25/25 个存在。全部存在。
- §3 兼容类：80/80 个存在。全部存在。
- 随机抽 20 个兼容类（种子 20261108）：20/20 个存在：
  - `gitcloud/secure/GitCloudTokenStore`（提示：无）：存在
  - `CustomKeyDown`（提示：ui/custom）：存在
  - `PlaybackTrace`（提示：player）：存在
  - `web/ext/WebHomeExtensionRegistry`（提示：playback）：存在
  - `com.fongmi.hook.Hook`（提示：无）：存在
  - `InfoDialog`（提示：ui/dialog）：存在
  - `PlaybackAutoContextStore`（提示：player）：存在
  - `DanmakuDialog`（提示：ui/dialog）：存在
  - `QualityAdapter`（提示：ui/adapter）：存在
  - `PlaybackRouteCapabilities`（提示：player）：存在
  - `ControlDialog`（提示：ui/dialog）：存在
  - `PlaybackTelemetry`（提示：player）：存在
  - `PlaybackAutoContext`（提示：player）：存在
  - `ui/activity/PlaybackRecoveryActivity`（提示：无）：存在
  - `ReceiveDialog`（提示：ui/dialog）：存在
  - `EpisodeGroupAdapter`（提示：ui/adapter）：存在
  - `CustomSeekView`（提示：ui/custom）：存在
  - `CodecCapabilityDialog`（提示：ui/dialog）：存在
  - `PlaybackExperimentPolicy`（提示：player）：存在
  - `PlaybackBufferingTracker`（提示：player）：存在

## 4. 版本检查（`app/build.gradle`）

| 项 | 期望 | 实际 | 结果 |
|---|---|---|---|
| versionCode | 32 | 32 | 通过 |
| versionName | 2.0.1 | 2.0.1 | 通过 |
| targetSdk | 34 | 34 | 通过 |
| compileSdk | 34 | 34 | 通过 |
| minSdk | — | 24 | 仅记录 |
| AGP（根 build.gradle） | — | 8.2.2 | 仅记录 |
| catvod compileSdk | — | 34 | 仅记录 |
| quickjs compileSdk | — | 34 | 仅记录 |

## 5. 资源与 §6 遗留项检查

- Java 引用的 `R.layout.*`：36 个不同名字，引用总计 44 处；缺失的 layout：0 个。
- `R.id.bottom` 引用：1 处。
- `PlaybackActivity`（VideoActivity 基类）：已存在：app/src/main/java/com/yingtu/plus/ui/activity/PlaybackActivity.java。
- `activity_video.xml`：已存在。
- `ActivityVideoBinding` 引用：2 个文件（app/src/main/java/com/yingtu/plus/ui/activity/VideoActivity.java、app/src/main/java/com/yingtu/plus/ui/dialog/ControlDialog.java）。
- `WebHomeInlineVodStore.KEY` 当前值：`webhome_inline`（清单第 6 节注明真实值树内无据，需上游对齐）。

## 6. 结论与下一步建议

- 仍未解决的引用（import + 全限定）：0 个类；疑似类型位置引用：3 个标识符（待人工复核）。
- 疑似清单人工复核结论（逐项查过引用行与所在文件 import）：除个别 java.lang 误报已在脚本白名单中排除外，其余基本属实，分两类——（a）缺 import：多为 media3/Android 嵌套类型被裸用，如 AudioSink 的 ConfigurationException/InitializationException/WriteException、VideoSink 的 Listener/VideoSinkException、Extractor 的 ReadResult、SeekMap 的 SeekPoints、BandwidthMeter 的 EventListener、Player 的 Commands、SimpleBasePlayer 的 State/PositionSupplier/MediaItemData、MediaCodecVideoRenderer 的 Builder、Parcelable 的 Creator（11 个 bean 文件）、Application 的 ActivityLifecycleCallbacks、androidx.annotation 的 NonNull/Nullable（2 个 dialog 文件）；（b）缺类：仓库内无定义、无 import，如 FolderActivity、EpisodeGridLayoutPolicy、ErrorAction、RuntimeMetrics、VideoPlaybackDetails、DiagnosticControls、ReadRequest，以及 IjkSimplePlayer 用的 PositionSupplier/MediaItemData（若属 media3 嵌套类型则归入（a），否则归入（b），需下一步对照 media3 版本核定）。本清单未覆盖的 ActivityVideoBinding 缺失见 §5/§6。
- 括号平衡：见 §2。
- 版本：见 §4。
- 最大阻塞仍是 §6 的两项：`PlaybackActivity` 缺失与 `activity_video.xml` 缺失——VideoActivity（6619 行）直接继承前者、绑定后者，缺了这两项，任何编译都到不了链接阶段。
- 其次是环境阻塞：本机 Gradle daemon 无法通信（见 §0），编译验证本身还没跑通过；即使源码齐了，也需要在能跑 Gradle 的环境（或 CI）上再验证一次。
- 另：仓库自带的 `gradlew` 有引号缺陷（见 §0），在任何机器上直接跑都会失败，修编译验证前需先修它（或改用独立 Gradle）。

> 本报告由 `verify_static.py` 生成；编译部分因环境原因未完成，静态核对不能替代真实编译。