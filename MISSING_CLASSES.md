# 影图+（YingTu）缺失类清单与补全记录

> 分支：`quality-p0`（从 main `aa2320e` 切出） · 日期：2026-11-08
> 核对方式：grep 枚举全部 import / 全限定引用 / 同包引用，对照仓库内已定义类；逐个核对新代码调用点的方法签名。
> **注意：本环境无 Android SDK、无 javac/Gradle，以下核对均为静态 grep + 签名核对，未经过真实 Gradle 编译验证。**
> 约定：“恢复”指从旧线父提交 `4d51378`（v1.9.21 的父提交，133 个 Java 文件）原样取回；
> “兼容实现”指新写最小实现，文件头均注明“兼容实现，待上游 webhtv 源码对齐”，方法体能降级则降级（返回空值 / 空集合 / false / 不做任何事），不编造上游行为、不实现复杂播放 / 服务器逻辑。

## 1. 背景

2026-10-16 提交 `9e45a9c`“从零重建影图，集成 webhtv 播放器”把播放器换成 webhtv 的 VideoActivity + PlayerManager，Java 文件从 133 个涨到 442 个，但 `web/`、`server/`、`service/`、`player/PlayerHelper` 等包没跟着移植进来。当前 main 是移植进行中的半成品，约 100 个被引用类无定义，照原样编不出来。
核查结论：上游任何 tag（v2.0.0–v2.0.44）里都没有 Playback* / 对话框 / ui.custom 等新播放器才要的类（`git log --all -S PlaybackAutoContext` 只命中引入引用的提交，类本身从未写过），这些类只能写兼容实现，无法从上游恢复。

## 2. 从旧线 `4d51378` 恢复的类（25 个 Java 文件）

| 类 | 来源 | 与新调用点核对结果 |
|---|---|---|
| `api/SpiderManager.java` | 旧线 | 已适配：`DexClassLoader` 改引 `Util`；`JarLoader.md5` → `Util.md5`；JS 分支 `getSpider` 改调现版 4 参签名；`proxy()` 包 try/catch（现版抛受检异常） |
| `api/VodSpider.java` | 旧线 | 直接恢复，`ApiConfig.get().getSpider()` 在现版存在 |
| `api/JsonParserHelper.java` | 旧线 | 直接恢复 |
| `api/LiveParser.java` | 旧线 | 直接恢复（与现版 `api/parser/LiveParser.java` 不同包，无冲突；旧 LiveActivity 用的 `parse(String)→List<LiveGroup>` 只有旧版有） |
| `player/ParseJob.java` | 旧线 | 已桥接新版播放器：保留旧 `create(Callback)` + `start(ParseBean,String)`；新增 `create(ParseCallback)` + `start(Result,boolean)`（PlayerManager 用），直链直接成功、无解析源时退化 WebView 嗅探 |
| `player/WebParser.java` | 旧线 | 已适配：`com.yingtu.plus.base.App` → `com.yingtu.plus.App` |
| `player/core/AbstractPlayer.java` | 旧线 | 直接恢复（含 CORE_EXO=0 / CORE_IJK=1 / CORE_MPV=2） |
| `player/core/ExoPlayerCore.java` | 旧线 | 直接恢复 |
| `player/core/IjkPlayerCore.java` | 旧线 | 直接恢复 |
| `player/core/MpvPlayerCore.java` | 旧线 | 直接恢复 |
| `player/core/PlayerFactory.java` | 旧线 | 直接恢复（SettingActivity 用的 `coreName/currentCore/setCore` 与旧版一致） |
| `ui/activity/LiveActivity.java` | 旧线 | 直接恢复（`ApiConfig.getLiveUrls()`、`LiveChannelAdapter` / `LiveGroupAdapter` 方法在现版存在） |
| `ui/activity/LivePlayActivity.java` | 旧线 | 直接恢复 |
| `ui/activity/PlayActivity.java` | 旧线 | 直接恢复（用恢复的 ParseJob 旧接口） |
| `ui/fragment/DirectLinkFragment.java` | 旧线 | 直接恢复（现版 adapter 方法一致） |
| `ui/fragment/FavVodFragment.java` | 旧线 | 直接恢复 |
| `ui/fragment/LocalFileFragment.java` | 旧线 | 直接恢复 |
| `ui/fragment/WebHomeFragment.java` | 旧线 | 直接恢复 |
| `ui/custom/PlayerGestureController.java` | 旧线 | 已适配：3 处 `com.yingtu.plus.base.App.context()` → `com.yingtu.plus.App.context()` |
| `util/UiUtil.java` | 旧线 | 已适配：3 处 `com.yingtu.plus.base.App.context()` → `com.yingtu.plus.App.context()` |
| `viewmodel/SourceViewModel.java` | 旧线 | 直接恢复（新调用方用的字段与 `getSort/getList/getDetail/search` 与旧版完全一致） |
| `web/HomeWebBridge.java` | 旧线 | 直接恢复（新 WebHomeActivity 用的三参构造器 + `UiCallback` 与旧版一致） |
| `web/WebCall.java` | 旧线 | 直接恢复 |
| `web/WebHomeSdk.java` | 旧线 | 直接恢复（`BRIDGE="fongmiBridge"`、`script(boolean)`、`viewportVars(6参)`、`dispatchEvent(2参)` 一致） |
| `web/WebServer.java` | 旧线 | 直接恢复（`get().start()`、`getAddress(String)` 一致；`server/Server` 兼容类委托给它） |

恢复的资源：37 个旧 layout（与现版 39 个文件名零重叠；被 Java 引用的 25 个 layout 原先全部缺失，现已全部恢复）、3 个 color selector（`filter_text_selector`、`source_grid_text_selector`、`tab_text_selector`）、1 个 layout（`view_progress.xml`，从 v1.9.13 提交 `b62735a` 恢复；它在 `fa75fa0` 被误删，但 `Notify.java` 仍在用 `ViewProgressBinding`）。恢复后 layout 共 77 个，Java 引用的 33 个 `R.layout.*` 已全部存在；`R.id.*` 仅剩 `R.id.bottom`（VideoActivity 1137 行，属缺失的 activity_video.xml，见第 6 节）。
Manifest：在 LiveActivity 条目后新增 PlayActivity 与 LivePlayActivity 声明（属性照抄旧 Manifest），并新增 `PlaybackService` 声明（`exported=false`，仅供 VideoActivity 绑定）。

## 3. 新写的兼容实现（共 80 个 Java 文件）

### 3.1 播放事实与遥测（`player` 包）

| 类 | 实现的方法 / 成员 |
|---|---|
| `PlaybackAutoContext` | 枚举：Confidence、DataSaverState、DecodeMode、HdrType、Kernel、ManifestKind、MemoryPressure、NetworkCost、NetworkTransport、PathKind、PlaybackPhase、PowerState、Protocol、RenderTarget、StreamKind、ThermalState、TransferUnit、UpstreamState、ValueSource、SystemConditionTrigger（均有 `label()` 返回小写 name）；`Fact<T>`（`forSession/unknown/value/source/confidence/hasValue/isUsable/session/sampledAtElapsedMs/label`，其中 `source()`/`confidence()` 返回字符串 label）；`SessionToken`（`none/active/equals`）；`DeviceFacts/ResourceFacts/PathFacts/MediaFacts/DecoderFacts/TrackFacts/OutputFacts/ColorSnapshot/ManifestFacts/MemorySnapshot/NetworkSnapshot/RuntimeFacts/DisplayMode`（各 `unknown()`，访问器默认返回未知 / 空 / 0；另有 `DeviceFacts.diagnosticPssBytes(): Fact<Long>` 与 `MemorySnapshot.nativeHeapAllocatedBytes(): Long`）；上下文方法 `empty/session/device/resource/path/media/runtime/revision/kernel/decodeMode/currentPath/active/logSummary/stagedConfig/reloadAttempts/successfulReloads/fallbackCount/recoveryAttempts/successfulDowngrades/rollbackCount/failedActions` |
| `PlaybackAutoContextStore` | `process/snapshot/beginSession(String,long)→SessionToken/publishPlaybackFacts(→boolean)/clear(SessionToken)` |
| `PlaybackDiagnosticCollector` | 构造器 `(String,String)`；静态 `enabled/id(String)/objectId(Object,String)`；实例 `context/instanceId/emit(3参)/emit(5参)/error/begin(String,String)→Context/end(String)/evidence(Context,boolean,int)/baseline/protectedMedia`；嵌套 `Context{mediaId()}` |
| `PlaybackErrorClassifier` | 静态 `classify(Throwable,PlaybackRoute.Resolution)→Failure`（错误码取自 media3 PlaybackException，其余为 0）；`Failure{stage/route/evidence/errorCode/logSummary}`；`Stage{DECODER,DRM,LOCAL_ENDPOINT,MEDIA_PARSING,NETWORK_IO,OUTPUT,UNKNOWN}` |
| `PlaybackExperimentCoordinator` | `process/addListener(Consumer<Update>)→Registration/capture(Action)→Token/isCurrent(Token)→boolean/generation()→long`；`Update{generation/change}`、`Registration{close}`、`Token{generation}` |
| `PlaybackExperimentPolicy` | `Action`（15 个取值，见代码）；`State{strategyId/allows(Action)/enabled}`；`defaultState(boolean)` |
| `PlaybackMemoryCoordinator` | `process/addListener(Consumer<Update>)→Registration`；`Update{session/decision/generation/change}`（`decision()` 返回 `ExoMemoryPressurePolicy.Decision`）、`Registration{close}` |
| `PlaybackMemoryMonitor` | `process/beginSession(SessionToken)/endSession(SessionToken)` |
| `PlaybackResourceClassifier` | 静态 `classifyRequest(String,String,String)/merge(Classification,Classification)/observePlayer(Classification,boolean,long)/manifestTtlMs(Fact<ManifestFacts>)/classifyDataSource(String,String,String,Map)`（仅按地址后缀识别 HLS/DASH/RTMP/RTSP/本地，其余未知）；`Classification{protocol/streamKind/manifest/transferUnit/targetBytes/memoryPressure/memorySnapshot/playerPath/upstreamPath/upstreamState/performancePriority/pressureUsable/snapshotUsable/protocolUsable/streamKindUsable/playerPathUsable/upstreamPathUsable/upstreamStateUsable/observedAtElapsedMs/logSummary/buildUpon/toPathFacts/toResourceFacts}` |
| `PlaybackRoute` | 枚举 `DIRECT_REMOTE_HTTP/EXTERNAL_LOOPBACK_PROXY/APP_LOCAL_SERVICE/OTHER`；静态 `resolve(String)→Resolution`（本地回环 → 回环路由，其余直连，空地址 OTHER）；实例 `effectivePreloadThreads(int)→int`；`Resolution{route/owner/evidence/confidence/loopback/scheme/logSummary/buildUpon}`；`Owner{APP_MAIN_SERVER,APP_HLS_PROXY,UNKNOWN}`；`Evidence{DIRECT_REMOTE,EXTERNAL_LOOPBACK,LOCAL_SERVICE,UNKNOWN}` |
| `PlaybackRouteCapabilities` | 静态 `resolve(Resolution)`；实例 `observedLeg/upstreamVisibility/controlScope/logSummary/externalUpstreamOpaque()→boolean`；`ObservedLeg/UpstreamVisibility/ControlScope`（均有 `label()`） |
| `PlaybackSystemConditionCoordinator` | `process/addListener(Consumer<Update>)→Registration`；`Update{session/trigger/publishedAtElapsedMs/generation/change}`、`Registration{close}` |
| `PlaybackSystemConditionMonitor` | `process/beginSession(SessionToken)/endSession(SessionToken)/currentNetworkSnapshot()→NetworkSnapshot/currentNetworkIdentityDigest()→String` |
| `PlaybackTelemetry` | 静态 `safeLabel(String)`；`DecisionDomain`（17 个取值）、`DecisionOutcome`（7 个取值）；`DecisionInput{bool/number/unknown/decimal/text}`（text 有 `(String,String,String,String)` 与 `(String,String,ValueSource,Confidence)` 两个重载）；`DecisionEvent`（8 参构造器：域 / 结果 / 5 个字符串 / 输入列表）；`Metric<T>{of/unknown/known/value/source/confidence}`；`RuntimeObservation`（13 参全 Metric 构造器 + 全部访问器） |
| `PlaybackTelemetryCoordinator` | `process/publishDecision(SessionToken,DecisionEvent)/publishDecision(String,DecisionEvent,long)/beginSession(SessionToken,long)/publishRuntime(SessionToken,RuntimeObservation,long)/endSession(SessionToken,String,RuntimeObservation,long)` |
| `PlaybackThroughputHistory` | 公开无参构造器（ExoThroughputCoordinator 以 `new` 持有独立实例）+ `process()` 单例；`record(PlaybackAutoContext,Evidence,long)/lookup(PlaybackAutoContext,String,long)→Match（恒不可用）/invalidate()`；`Evidence`（9 参构造器）；`Match{unavailable/usable/ageMs/confidence/reason/bitsPerSecond/pathKind}`；`Reason{NO_MATCH}` |
| `PlaybackBufferingTracker` | `reset/getRebufferCount()→int/getRebufferTotalMs(long)→long/isBuffering()→boolean/update(7参)→Event（恒 null）`；`Event{type/phase/durationMs/rebufferCount/rebufferTotalMs/positionMs/forwardBufferedMs/playbackState/loading}` |
| `PlaybackFactsSnapshot`（`player/engine`） | 11 参构造器（4 个 media3 Format、2 个解码器名、DecoderKind、Object、2 个 String、Boolean）+ 访问器 |
| `DecoderKind`（`player/engine`） | 枚举 `HARDWARE/SOFTWARE/UNKNOWN` |
| `PlaybackProfileAbCoordinator` | `process/beginSession(SessionToken,StartConfig,long)/observe(SessionToken,RuntimeInput,long)/endSession(SessionToken,EndConfig,long,long)/invalidate(SessionToken,InvalidationReason)`；`StartConfig`（5 参）、`RuntimeInput`（8 参）、`EndConfig`（4 参）、`InvalidationReason{GENERATION_CHANGED,UNKNOWN}` |
| `PlaybackProfileAbPolicy` | 静态 `armForProfile(Object)→Arm（恒 null）/gateAllows(State,Kernel)→boolean（恒 false）`；`Arm{UNKNOWN}`；`EnrollmentResolution{active/enrollment}`；`Enrollment{deviceDigest}` |
| `PlaybackLightweightAssessmentPolicy` | 静态 `armForProfile(Object)→PlaybackProfileAbPolicy.Arm（恒 null）` |
| `PlaybackStartupPolicy` | 静态 `resolve(boolean,boolean,boolean,boolean)→Completion`；`Completion{FIRST_FRAME,AUDIO_PLAYABLE,UNKNOWN}` |
| `PlaybackMediaFactsCoordinator` | 构造器 `(PlaybackAutoContextStore)`；`publishRenderTarget/publishDisplayFacts/beginSession/publishEngineFacts/endSession`（均为无操作） |
| `PreloadPausePolicy` | 静态 `evaluate(boolean,int,NetworkSnapshot)→Decision（恒允许）`；`Decision{allowed/reason}`；`Reason{ALLOWED,UNKNOWN}` |
| `SystemAudioDiagnosticCollector` | 静态 `snapshot(PlaybackDiagnosticCollector,PlaybackDiagnosticCollector.Context)`（无操作） |
| `OutputProgressDiagnostic` | 构造器 `(boolean)`（空壳） |
| `PcmDiagnosticProbe` | 构造器 `()`；`sample(9参)`（无操作） |
| `PlaybackTrace` | 常量 `NONE="none"`；静态 `normalize(String)/log(String,Object...)`；实例 `current/mark/hasStage/stageElapsedMs/clear/ensure/begin` |
| `PlayerHelper` | 静态 `getSubtitleMimeType/describeFormat/getDefaultUa/toBundle(Map)→Bundle/choose(Activity,String,Map,boolean,long,CharSequence)/share(Activity,String,Map,CharSequence)/onExternalResult(Intent,Runnable,Consumer<Long>)` |
| `Source` | 静态 `get()`；实例 `stop/fetch(Result)/fetch(Result,int)/parse(Vod)`（取流恒返回空串）；嵌套接口 `Extractor{match(Uri)/fetch(String)/stop/exit}` |
| `AudioPlaybackDiagnostics` | 静态 `track(Format,String)→Track/failureReason(int)→FailureReason/lastAttemptLevel(FailureReason,OutputMode,DecodeMode)→String（恒 "unknown"）/encodedTrack(Track,int)→Track`；`Track{codec}`；`Snapshot`（9 参与 12 参构造器 + 全部访问器）；`DecodeMode{HARDWARE,NONE,SOFTWARE,UNKNOWN}`、`OutputMode{OFFLOAD,PASSTHROUGH,PCM,UNKNOWN}`、`RuntimeState{FAILED,UNKNOWN}`、`FailureReason{DECODER_INIT,DECODER_RUNTIME,NONE,UNKNOWN}` |

### 3.2 播放服务 / 路由 / web（`playback`、`service`、`server`、`web` 包）

| 类 | 实现的方法 / 成员 |
|---|---|
| `playback/PlaybackEventCollector` | `get/onProgress(History,PlayerManager)/updateHistory(History)/onStop(PlayerManager)` |
| `playback/PlaybackOrientation` | 静态 `getRotateOrientation(boolean)/getEnterFullscreenOrientation(boolean)/getExitFullscreenOrientation(boolean)/getPortAutoRotateOrientation()/getLandAutoRotateOrientation()/getPortraitVideoSizeOrientation()/getLandscapeVideoSizeOrientation()`（均返回 ActivityInfo 方向常量） |
| `service/PlaybackService` | `extends Service`；嵌套接口 `NavigationCallback{onNext/onPrev/onStop/onReplay/onAudio}`；`setDesktopLyricsSnapshot(LyricsResult,List<LyricsLine>)/dispatchNext()` |
| `server/Server` | `get/start/getAddress()/getAddress(String)/getAddress(boolean)`（委托给已恢复的 `WebServer`） |
| `web/HomeWebController` | 静态 `requestExtensionReload()`（无操作） |
| `web/WebHomeInlineVodStore` | 常量 `KEY="webhome_inline"`（真实值在树内无据，见第 6 节）；静态 `detail(String)→Result/player(String,String)→Result`（均返回空 Result） |
| `web/ext/WebHomeExtensionRegistry` | `get/clear/setGlobalSources(List<Site>,String)` |

### 3.3 小类

| 类 | 实现的方法 / 成员 |
|---|---|
| `bean/CastVideo` | 构造器 `(String,String,long,Map<String,String>)`；`getName/getUrl/getPosition/getHeaders` |
| `exception/ExtractException` | `extends Exception`；构造器 `(String)`、`(String,Throwable)` |
| `gitcloud/secure/GitCloudTokenStore` | 静态 `exportTokens()→JsonObject（空对象）/importTokens(JsonObject)→int（恒 0）` |
| `update/OciArtifact` | `isValid()→boolean（恒 false）` |
| `Product` | 静态 `getDeviceType()→int（恒 0）` |
| `utils/PiP` | `update(Activity,View)/update(Activity,boolean)/update(Activity,int,int,float)/enter(Activity,int,int,float)→boolean（恒 false）/setAudioMode(Activity,boolean)` |
| `utils/Timer` | `get/reset()` |
| `ui/activity/FileActivity` | `extends BaseActivity`；`getLayoutId()→0、init()` 空实现，`onCreate` 后直接 `finish()` |
| `ui/activity/PlaybackRecoveryActivity` | 同上（空页面，直接结束） |
| `com.fongmi.chaquo.Loader` | `spider(String)→Spider`（恒返回 `SpiderNull` 空爬虫） |
| `com.fongmi.hook.Hook` | 构造器 `(String,String)`；`getSign/getPkg` |

### 3.4 适配器（`ui/adapter`，均继承 `RecyclerView.Adapter`，布局复用已恢复的旧线 item）

| 类 | 构造器与方法 |
|---|---|
| `EpisodeAdapter` | 构造器 `(OnClickListener,int viewType)`；`OnClickListener{onItemClick(Episode)}`；`addAll(List<Episode>)/getItems/get(int)/getActivated()→Episode/getPosition/isEmpty/getItemCount`；布局 `item_series` |
| `EpisodeGroupAdapter` | 构造器 `(OnClickListener)`；`OnClickListener{onItemClick(Group)}`；静态 `build(int,int,boolean)→List<Group>`（每组 50 集）；`Group{start/end/name/selected}`；`addAll/setSelected(Group)/getPosition/getItems/isEmpty/getItemCount`；布局 `item_category_tab` |
| `FlagAdapter` | 构造器 `(OnClickListener)`；`OnClickListener{onItemClick(Flag)}`；`addAll/add(Flag)/getItems/get(int)/getActivated()→Flag/getPosition/setSelected(Flag)/setSelected(String)/toggle(Flag)/reverse/isEmpty/getItemCount`；布局 `item_series_flag`（显示 `flag.getShow()`） |
| `ParseAdapter` | 构造器 `(OnClickListener,int viewType)`；`OnClickListener{onItemClick(Parse)}`；`reload()`（从 `VodConfig.get().getParses()` 拉取）`/first()→Parse/get(int)/getPosition/isEmpty/getItemCount`；布局 `item_category_tab` |
| `QualityAdapter` | 构造器 `(OnClickListener)`；`OnClickListener{onItemClick(Result)}`；`addAll(Result)`（按 `Result.getUrl()` 的多清晰度地址拆项，每项为原结果的 Gson 深拷贝并置 url 位置）；`get(int)/getPosition/getItemCount`；布局 `item_category_tab`（显示地址的 `n` 字段） |
| `QuickAdapter` | 构造器 `(OnClickListener)`；`OnClickListener{onItemClick(Vod)}`；`getItems/get(int)/getBestPosition()（恒首项）/addAll(List<Vod>)/clear/remove(int)/isEmpty/getItemCount`；布局 `item_grid_movie`（Glide 加载海报，沿用 `GridMovieAdapter` 写法） |

### 3.5 自定义 View（`ui/custom`）

| 类 | 实现的方法 / 成员 |
|---|---|
| `SpaceItemDecoration` | 构造器 `(int)`、`(int,int)`；`getItemOffsets` 四边留白 |
| `CustomKeyDown` | 静态 `create(Activity,PlayerView)→CustomKeyDown`；`onTouchEvent(MotionEvent)→boolean（恒 false）/setLock/getScale()（恒 1）/resetScale`；`Listener`（12 个手势方法：`onScale/onSpeedUp/onSpeedEnd/onBright/onVolume/onFlingUp/onFlingDown/onSeeking/onSeekEnd/onSingleTap/onDoubleTap/onTouchEnd`） |
| `CustomMovement` | 静态 `bind(View)`（无操作） |
| `CustomSeekView` | `extends View`（空壳） |
| `PlayerOsdController` | 构造器 `(View×7,Source,float)`；`Source{getPlayer()→PlayerManager/getTitle()→String}`；`isDiagnosticsVisible/setControlsVisible/toggleDiagnostics/setDiagnosticsVisible/start/stop/release` |
| `AudioPlayerBackgroundDrawable` | `extends GradientDrawable`；构造器 7 参 `(int,int,boolean,boolean,boolean,int,int)`；`setRecordHaloAnchor(float,float,float)` |
| `KaraokeResultView` | `extends LinearLayout`；构造器 `(Context)`；`setResult(KaraokeResult)→KaraokeResultView/setAction(Runnable)/getPreferredDialogWidth()/requestActionFocus()→boolean` |
| `LyricsOverlayView` | `extends View`；构造器 `(Context)`；`setDesktopMode(boolean)/update(long)/update(long,boolean)/refreshStyle/setLyrics(LyricsResult,List<LyricsLine>)/clear()` |
| `LutQuickPanel` | `extends View`；`toggle(PlayerManager,PlayerView,Runnable,ImportCallback)/selectImported(LutPreset,PlayerManager,PlayerView,Runnable)/refreshList/hideIfVisible()→boolean（恒 false）`；`ImportCallback{onImportLut/onSelectLutDir}` |

### 3.6 对话框（`ui/dialog`，均继承 `DialogFragment`，`show(Activity)` 走 `getSupportFragmentManager()`；打开后为空白对话框）

| 类 | 链式接口 / Listener |
|---|---|
| `EpisodeGridDialog` | `create/reverse(boolean)/episodes(List<Episode>)/show(Activity)` |
| `EpisodeListDialog` | `create/flags(List<Flag>)/reverse(boolean)/show(Activity)` |
| `VideoContentDialog` | `create/content(CharSequence)/show(Activity)` |
| `QuickSearchDialog` | `create/title(String)/keyword(String)/listener(QuickAdapter.OnClickListener)/searchListener(SearchListener)/items(List<Vod>)/show(Activity)`；实例 `clear/addAll(List<Vod>)/isActive()`；`SearchListener{onSearch(String)}` |
| `CastDialog` | `create/history(History)/video(CastVideo)/fm(boolean)/show(Activity)`；`Listener{onCasted()}` |
| `InfoDialog` | `create/title(CharSequence)/headers(Map)/url(String)/show(Activity)`；`Listener{onShare(CharSequence)}` |
| `ControlDialog` | `create/parent(ActivityVideoBinding)/history(History)/parse(boolean)/player(PlayerManager)/show(Activity)`；实例 `setPlayer()`；`Listener`（19 个方法：`getControlBinding/getControlPlayer/getControlHistory/isControlParseEnabled/isControlAudioContent/isDanmakuFullscreen/onRepeatModeChanged/onParse/onEpisodeColumn/onCompactEpisodeTitleChanged/onTrackPanel/onTitlePanel/onDanmakuPanel/onCodecCapabilityPanel/onPlayParamsPanel/onLutPanel/onImmersiveAudioModeChanged/onKaraokeModeChanged/onKaraokeTrackPanel`，与 VideoActivity 的 `@Override` 一一对应；其中 `onTrackPanel` 同时在 `TrackDialog.Listener`、`onDanmakuPanel` 同时在 `DanmakuDialog.Host`，Java 允许同一实现满足多个接口） |
| `TrackDialog` | `create/type(int)/player(PlayerManager)/show(Activity)`；`Listener{onTrackPanel(int)}` |
| `TitleDialog` | `create/player(PlayerManager)/show(Activity)` |
| `DanmakuDialog` | `create/player(PlayerManager)/show(Activity)`；`Host{onDanmakuPanel()}` |
| `SubtitleDialog` | `create/view(View)/player(PlayerManager)/show(Activity)` |
| `ReceiveDialog` | `create/event(CastEvent)/show(Activity)` |
| `LutPanelDialog` | `create/player(PlayerManager)/show(Activity)` |
| `CodecCapabilityDialog` | 静态 `show(Activity,PlayerManager)` |
| `PlayerKernelDialog` | 静态 `show(Activity,int,Consumer<Integer>)`（`switchPlayerKernel(int)` 的方法引用即 `Consumer<Integer>`） |

## 4. 其他小修补（改动现有文件）

| 文件 | 改动（一句话） |
|---|---|
| `App.java` | 增加 `setHook(Hook)` / `getHook()`（TVBus 调用；只保存，不安装 Hook） |
| `setting/PlaybackExperimentSetting.java` | `getState()` 返回值由 `boolean` 改为 `PlaybackExperimentPolicy.State`（PlayerManager 两处调用点都要求 State，要调 `strategyId()/allows()`；原 boolean 版无其他调用方） |
| `setting/PlaybackProfileAbSetting.java` | `getEnrollmentResolution()` 返回值由 `int` 改为 `PlaybackProfileAbPolicy.EnrollmentResolution`（PlayerManager 要求该类型；入组与否仍以 `isEnrolled()` 为准） |
| `setting/PlaybackLightweightAssessmentSetting.java` | `coordinator()` 返回值由 `Object` 改为 `PlaybackProfileAbCoordinator`；新增 `getEnrollmentResolution()`（与 ProfileAb 同形） |
| `setting/PlaybackPerformanceSetting.java` | 新增 `getProfile(int)` 重载（PlayerManager / ExoUtil 的调用点均带参；暂不按类型区分） |
| `player/ParseJob.java` | 见第 2 节（桥接新版播放器） |
| `api/SpiderManager.java` | 见第 2 节（JarLoader / JsLoader 适配） |
| `player/WebParser.java`、`ui/custom/PlayerGestureController.java`、`util/UiUtil.java` | `com.yingtu.plus.base.App` → `com.yingtu.plus.App` |
| `catvod/.../bean/Doh.java` | `import com.github.catvod.crawler.R` → `import com.github.catvod.R`（catvod 模块自有 res，库模块 R 应为 `com.github.catvod.R`） |
| 11 个文件（AppDatabase、PlayerManager、ExoPlayerEngine、IjkSimplePlayer、ExoDiagnosticAudioOutput、ExoDiagnosticCollector、MediaSourceFactory、IjkRuntimeProfileStore、KaraokeController、LrcLibClient、VideoActivity） | 全限定 `com.fongmi.android.tv.*` 改定向到 `com.yingtu.plus.*`（App / impl.Callback / exo.ExoDiagnosticCollector / exo.subtitle.ExoSubtitleSession / ui.custom.LutQuickPanel.ImportCallback / player.PlaybackAutoContext.* / player.PlaybackDiagnosticCollector / player.SystemAudioDiagnosticCollector / player.OutputProgressDiagnostic / player.PcmDiagnosticProbe；其中 ExoDiagnosticCollector 的 `prepare/effects` 与现版签名一致，impl.Callback 有无参构造器与 `success()/error()`） |
| `AndroidManifest.xml` | 新增 PlayActivity、LivePlayActivity、PlaybackService（service）声明 |

## 5. 不是缺失（已核实，无需处理）

| 项 | 说明 |
|---|---|
| `com.yingtu.plus.R`、`com.yingtu.plus.BuildConfig` | 构建时生成（R 含 layout/id/drawable/string 等；BuildConfig 含 VERSION_CODE、APPLICATION_ID、VERSION_NAME、MEDIA3_VERSION、DEBUG、BUILD_TAG、BUILD_TIME、FLAVOR_mode） |
| `com.github.catvod.R` | catvod 库模块构建时生成（Doh.java 用它的 `doh_url` / `doh_name` 数组） |
| `databinding/ViewProgressBinding` | 由 `view_progress.xml` 生成（本次已从 `b62735a` 恢复该 layout，见第 2 节） |
| 字符串字面量里的 catvod 类名 | `com.github.catvod.spider.Init`、`Proxy`、`parser.Json`、`Mix`、`parser.merge.P1.P`、`js.Function` 只出现在字符串字面量（`loadClass("...")` 反射进 spider jar），是运行时类名，非编译期引用 |
| `DiagnosticEvent.Status` | 已存在（`DiagnosticEvent.java` 第 9 行 enum，含 NOT_OBSERVABLE 等）；`Status.WITHIN_CAP` 等是其他文件内的局部 enum，不是它的常量 |
| `HawkConfig.API_URL` | 已存在（`HawkConfig.java` 第 9 行） |
| `PlaybackException` | `androidx.media3.common.PlaybackException`（PlayerManager 已 import），非缺失 |
| `com.yingtu.plus.base.App` | 不存在，旧线类已移到 `com.yingtu.plus.App`；恢复文件中的引用已全部改定向（第 4 节） |

## 6. 仍未解决

| 项 | 原因 |
|---|---|
| `PlaybackActivity`（VideoActivity 的基类，`ui/activity/PlaybackActivity.java` 缺失） | 无法合理兼容。VideoActivity（6619 行）继承它，以 `@Override` 定义约 25 个基类方法（`getBinding/getNavigationCallback/getExoView/getSeekView/onServiceConnected/onControllerReady/onPlayerRebuilt/initView/getPlaybackKey/onPrepare/onTracksChanged/onTitlesChanged/onError/onReload/onReclaim/onStateChanged/onPlayingChanged/onPlayerPositionDiscontinuity/onSizeChanged/onSurfaceAttached/onSubtitleClick/onTimeChanged` 等，另有 `onLutSelected/onLutImport/onLutDir` 三个未归属到任何对话框 Listener 的方法也属此类），并调用大量继承方法（`player()/service()/getActivity()/getId()/isOwner()/isFullscreen()/isLock()/isUseParse()/isAudioOnly()/seekTo()/setAudioOnly()/getHistoryKey()/getEpisode()` 等，方法面约 100+），语义为播放器 Service 绑定与生命周期核心，硬写必错。按任务要求留在清单，不写错的实现。另：VideoActivity 调用的 `mBinding.exo.getDanmakuController()` 在 media3 `PlayerView` 上不存在，也归入此类（绑定类型缺失导致无法核定）。 |
| `ActivityVideoBinding` 与 `activity_video.xml` | layout 在上游任何提交里都不存在（`git log --all -- "*/activity_video.xml"` 无记录），`mBinding` 字段约 60+ 个（`control` 149 次、`episode` 31 次、`widget` 29 次、`audioStage` 21 次、`video` 16 次、`exo` 11 次、`lutQuick` 5 次等），无真实 layout 无法生成 databinding，手写绑定类属编造。连带：`R.id.bottom`（VideoActivity 1137 行）与 `ControlDialog.Listener.getControlBinding()` 的返回类型也因此无法解析。 |
| `WebHomeInlineVodStore.KEY` 的真实值 | 树内无据（仅知它是 String 常量，`KEY.equals(key)` 路由内联源）；本次暂填 `"webhome_inline"`，运行时内联源可能路由不到，需上游对齐后修正。 |
| `EpisodeGroupAdapter.build` 的分组大小 | 树内无据；本次按每组 50 集实现（FongMi 旧例），仅影响分组展示，不影响编译。 |
| `ControlDialog.Listener` 等面板方法的归属 | `onTrackPanel/onTitlePanel/onDanmakuPanel/onCodecCapabilityPanel/onPlayParamsPanel/onLutPanel/onImmersiveAudioModeChanged/onKaraokeModeChanged/onKaraokeTrackPanel` 在 VideoActivity 内无调用点，按“控制对话框负责打开各子面板”归属到 `ControlDialog.Listener`（`onTrackPanel` 同时放进 `TrackDialog.Listener`，`onDanmakuPanel` 同时放进 `DanmakuDialog.Host`）；若上游真实归属不同，VideoActivity 的 `@Override` 仍能通过（方法都在 VideoActivity 里定义了），只需在对话框类内调整调用方。 |

## 7. 自查结果

- Java 文件：553 → 629 个；已定义 FQN：841 → 999 个。
- 重跑 import 口径扫描：缺失由 84 个降至 31 个；其中 24 个是本扫描器对嵌套类型的误报（`PlaybackAutoContext.*`、`PlaybackDiagnosticCollector.Context`、`PlaybackExperimentPolicy.State`、`PlaybackTelemetry.DecisionEvent/RuntimeObservation` 均已在新文件内定义，扫描器只统计顶层类型），剩余真实项为：构建生成的 `R` / `BuildConfig` / `com.github.catvod.R`（3 个）、第 6 节的 `ActivityVideoBinding`（1 个，ControlDialog 也引用它故计 2 处）。
- 全限定引用扫描：`com.fongmi.android.tv.*` 24 处已全部改定向（现为 0 处）；剩余为字符串字面量反射名与已存在的 `DiagnosticEvent.Status`，见第 5 节。
- 资源核对：Java 引用的 33 个 `R.layout.*` 已全部存在；`R.id.*` 仅剩 `R.id.bottom`（属 activity_video.xml，见第 6 节）；`view_progress.xml` 的引用资源已核对齐全。
- `git status --short`：18 个已修改文件 + 144 个未跟踪文件（按单个文件计：105 个 Java = 25 个恢复 + 80 个兼容实现；38 个 layout；另有 MISSING_CLASSES.md 本身）。3 个 color 文件在 HEAD 中已存在，本次从旧线恢复的内容与之一致、无 git 变化。未 push，未改 origin，未动 APK。
- 链式调用复核（补查）：`context.device()/resource()/path()/media()/runtime()`、`Classification`、`Resolution`、`Fact` 各访问器全部存在；复核发现并补了 5 处缺失：`DeviceFacts.diagnosticPssBytes(): Fact<Long>`、`MemorySnapshot.nativeHeapAllocatedBytes(): Long`、`PlaybackRoute.Owner.APP_HLS_PROXY`、`AudioPlaybackDiagnostics.DecodeMode.HARDWARE`、`PlaybackErrorClassifier.Stage` 增加 `DECODER/DRM/OUTPUT`（PlayerManager 的 `failure.stage()` switch 与 IjkRuntimeProfilePolicy 均用到）。
- 枚举常量复核：全部闭合。`Protocol.HTTP_1_1` 实为 `okhttp3.Protocol`（库类），`Stage.COMPONENT_OPENED/HTTP_OPENED/SOURCE_SET` 等属已存在的 `IjkPlayerEngine.OpenStage`，`Reason` 的大量常量属各策略自有的 `Reason` 枚举，`RuntimeState.BLACKLIST_THRESHOLD` 实为 `ExoTunnelingRuntimeState` 的常量——均非本兼容类的缺失。
- VideoActivity `@Override` 复核（共 95 个）：除 Android/既有超类回调外，其余均由本轮写的接口覆盖（`ControlDialog.Listener` 的 `getControlBinding()`、`PlayerOsdController.Source` 的 `getPlayer()/getTitle()` 均已声明）；未覆盖的 `getBinding/getExoView/initView/onControllerReady/onError/onPlayerRebuilt/onPrepare/onReclaim/onReload/onServiceConnected/onSizeChanged/onStateChanged/onSurfaceAttached/onTitlesChanged/onTracksChanged/getPlaybackKey/getSeekView/onLutSelected/onLutImport/onLutDir/onSubtitleClick/getControlHistory/getControlPlayer` 均属第 6 节的 PlaybackActivity 表面。
- 迟到的旧扫描（`/tmp/usages.py` 全量成员枚举）复核：逐项核对后只剩 1 处真缺失——`PlaybackThroughputHistory` 的构造器原为私有，而 `ExoThroughputCoordinator` 有 3 处 `new PlaybackThroughputHistory()`，已改为公开无参构造器（`process()` 单例保留）；`PlaybackBufferingTracker()` 与 `PlaybackMediaFactsCoordinator(PlaybackAutoContextStore)` 的外部 `new` 均已是公开构造器。其余信号均为误报：各 `Loader.INSTANCE` 是各文件自己的私有嵌套 `Loader` 类（`BaseLoader`/`LiveConfig`/`VodConfig`/`WallConfig`/`RuleConfig` 内各一），卡拉 OK 的 `Source` 是 `KaraokeGithubTrackProvider` 内的私有嵌套类，`Source.MEDIA3/CUSTOM` 等属 `ExoPreloadTrafficCoordinator.Source` 等已存在类的嵌套枚举，`PlaybackExperimentPolicy.Action` 13 个被用常量与 `PlaybackExperimentCoordinator.Token` 均已存在。
- 未经过真实 Gradle 编译验证：本环境无 Android SDK、无 javac/Gradle，上述核对均为 grep/脚本静态核对。

## 8. 2026-11-08 补 25 处疑似引用：改从上游 webhtv 移植（fish2018/webhtv，HEAD 4e30ffa）

此前本轮先按“缺 import 补 import、真缺类写最小兼容实现”处理了 25 项（见 VERIFY_STATIC.md §1.3 旧表）；
后可可提供上游出处，改按“真缺类先找上游，找到就改包名移植，找不到才写兼容实现”重做。
本节记录移植结果；旧的兼容实现文件凡被上游真身替换的，均已删除或覆盖。

### 8.1 25 项逐项结果（最终状态）

| 标识符 | 最终处理 |
|---|---|
| ActivityLifecycleCallbacks | 补 import（android.app.Application.ActivityLifecycleCallbacks），未变 |
| Builder | 补 import（MediaCodecVideoRenderer.Builder），未变 |
| Commands | 补 import（Player.Commands），未变 |
| ConfigurationException | 全限定改写（AudioOutputProvider/AudioSink 各自的嵌套类型），未变 |
| Creator | 补 import（Parcelable.Creator，11 个 bean 文件），未变 |
| DiagnosticControls | **改移植上游真身**（player/DiagnosticControls.java，107 行，覆盖旧兼容实现）；另移植 FelBindProbeControl（96 行）；并给兼容类 PlaybackDiagnosticCollector 补了上游用到的 Context.trace()/attempt()/generation() 与 ended()/engine()（均为无害的兼容值：trace=mediaId，attempt/generation=0，ended=false，engine=""） |
| EpisodeGridLayoutPolicy | **改移植上游真身**（mobile 版，ui.activity 包，17 行）；删除旧的 player 包兼容实现；VideoActivity 的 import 已删（同包无需 import）。注意：上游实现比旧兼容实现简单——getMaxSpan 横屏或平板 6、否则 4；getAvailableWidth 优先 measuredWidth（且方向一致时才用） |
| ErrorAction | 补 import（PlayerEngine.ErrorAction），未变；核对上游 PlayerEngine 同样在 220 行定义该枚举，影图侧一致 |
| EventListener | 补 import（BandwidthMeter.EventListener），未变 |
| FolderActivity | **改移植上游真身**（mobile 版，74 行，覆盖旧兼容实现）；同时移植 FolderFragment（106 行）、ui.base.BaseFragment（69 行）、TypeFragment（209 行）、VodAdapter（66 行）、CustomScroller（79 行）、FixedNestedScrollView（43 行）、BaseVodHolder + VodList/Oval/RectHolder、BaseDiffAdapter（129 行）、BaseItemCallback（19 行），以及 activity_folder.xml、fragment_folder.xml、fragment_type.xml、adapter_vod_list/oval/rect.xml；VodFragment 仅写了编译桩（见 8.3），完整移植待下一批 |
| InitializationException | 全限定改写（AudioOutputProvider/AudioSink 各自的嵌套类型），未变 |
| Listener | 补 import（VideoSink.Listener），未变 |
| MediaItemData | 补 import（SimpleBasePlayer.MediaItemData），未变 |
| NonNull / Nullable | 各补 import（androidx.annotation），未变 |
| PlaybackActivity | **改移植上游真身**（937 行，抽象类；此前明确留空，本批纳入）。同时移植其直接依赖：ui.base.BaseActivity（mobile 版，152 行）、DiscMenuDialog（96 行）+ BaseBottomSheetDialog（93 行）、SurfaceDiagnosticCollector（121 行）+ PixelDiagnosticProbe（104 行）、PlaybackService（696 行，覆盖旧兼容桩）、BrowseTree（161 行）+ LiveBrowse（128 行）+ VodBrowse（322 行）、CustomSeekView（326 行，覆盖旧桩）、CustomWallView（372 行）。另补方法级移植：PlayerSetting.isAutoPlay()/putAutoPlay()、History.canSave()（position > 0）、Setting.wrapDisplay/wrapLanguage/wrapUiScale/getDynamicColor 等一组方法（见 8.2）。Manifest 的 PlaybackService 声明已按上游改成 exported=true + foregroundServiceType=mediaPlayback + MediaLibraryService/MediaBrowserService intent-filter |
| PositionSupplier | 补 import（Player.PositionSupplier），未变 |
| ReadRequest | 补 import（RemoteIsoSource.ReadRequest），未变；核对上游 RemoteIsoSource 同样在 27 行定义该嵌套类 |
| ReadResult | 补 import（Extractor.ReadResult），未变 |
| RuntimeMetrics | 补 import（PlayerEngine.RuntimeMetrics），未变；核对上游 PlayerEngine 同样在 309 行定义该 record |
| SeekPoints | 补 import（SeekMap.SeekPoints），未变 |
| State | 补 import（SimpleBasePlayer.State），未变 |
| VideoPlaybackDetails | 补 import（PlayerEngine.VideoPlaybackDetails），未变；核对上游 PlayerEngine 同样在 260 行定义该 record |
| VideoSinkException | 补 import（VideoSink.VideoSinkException），未变 |
| WriteException | 全限定改写（AudioOutput.WriteException / AudioSink.WriteException），未变 |

### 8.2 activity_video.xml 移植与 id 核对

- 移植上游 mobile 版 activity_video.xml（改包名）。VideoActivity 用的全部 mBinding 字段均在该 layout 内有对应 id（contentLayout↔content_layout、qualityText↔quality_text，其余同名），layout 根节点 android:tag="port"，与 VideoActivity.isPort() 的判断一致。
- `R.id.bottom`（VideoActivity.java:1138，portraitParams ABOVE bottom）：不在 activity_video.xml 内，而在随同移植的 view_control_vod.xml:178 定义（`@+id/bottom`），与上游 VideoActivity:1125 的同一行用法一致。verify_static.py 的 `R.id.bottom：1 处`只是引用计数（脚本不校验该 id 是否存在），现该 id 已有定义。
- 随同移植：view_widget_vod.xml、view_control_vod.xml、view_disc_menu_tools.xml、view_control_right.xml、adapter_parse_dark.xml、view_control_seek.xml、view_empty.xml、view_control_vod_action.xml（后三者在上游只有 leanback 版，已拷入影图 main res）、adapter_vod_list/oval/rect.xml；drawable 约 30 个；raw/empty.json；styles（Player.Vod、Video.ContextText、VideoActionButton、AudioActionButton、ShapeAppearance.WebHTV.AudioCover、Vod.Grid/Grid.Large/Circle 等）；strings 约 40 条、colors 若干（含 black_20/black_90/grey_500——grey_500 上游同样未定义，按 Material Grey 500 标准值 #9E9E9E 补）、res/color 下 4 个 selector。
- 未移植：adapter_episode_grid/adapter_quick/adapter_episode_group（仅 tools:listitem 设计时引用，影图 Java 未引用）；exo_icon_play（上游 app 内同样无此文件，应来自 media3-ui 依赖库，无需复制）。
- 移植脚本：/tmp/port_upstream.py（机械复制+改名）、/tmp/fill_resources.py（资源缺口扫描补齐）；移植文件头均注明上游来源与 HEAD。

### 8.3 本批新写的兼容桩（2 个）与有意留空项

| 文件 | 原因 |
|---|---|
| `Updater.java`（新建，空实现） | **有意留空，非缺失**：上游 Updater 会向 webhtv 自己的更新通道查新版并下载 APK；影图+ 是个人 fork，没有自己的更新通道，照搬会让应用去拉上游更新。BaseActivity.onResume 只调 create().resume()，空实现即可。将来有自己的更新地址时再实现 |
| `ui/fragment/VodFragment.java`（新建，编译桩，getResult() 返回 null） | 上游 VodFragment（mobile 版 721 行）依赖约 40 个类（HomeActivity/HistoryActivity/KeepActivity/SearchActivity、ApkPushDialog 等 10 个 dialog、HomeWebController/WebHomeChrome/WebHomeChromeStartup/WebHomeViewport、TypeAdapter、impl.Callback/ConfigListener/FilterListener/SiteListener 等），本批未移植。桩只满足 FolderFragment.getParent() 的类型转换与 getResult() 调用；完整移植前 FolderActivity 的文件夹浏览在运行时不可用 |
| `Setting.java` 方法组 | 影图原 Setting 只有 43 行桩；本次按上游原文补了 wrapDisplay/wrapLanguage/wrapUiScale/getLanguage/putLanguage/applyLanguage/getUiScale/putUiScale/getDynamicColor/getThemeColor/putThemeColor/getWallColor/putWallColor 及 LANGUAGE_TRADITIONAL、UI_SCALE_MILD_COMPACT/MORE_COMPACT/SMALLER 常量（Prefers 用影图本地 util.Prefers，Trans 用 catvod 的 com.github.catvod.utils.Trans，影图 catvod 内已有） |

### 8.4 移植后静态核对（2026-11-08 本批结束时重跑 verify_static.py）

- Java 文件：634 → 663 个（+33 移植 Java −1 删除旧 EpisodeGridLayoutPolicy 兼容实现 +2 新桩 Updater/VodFragment，另有 4 个覆盖不计新增；其余为资源文件）。
- import 未解决：0；全限定未解决：0；括号不平衡（新增/修改）：0，全量另有 0。
- 疑似引用：25 → 3，剩余 3 项经逐项核对均为脚本误报：`LayoutParams`（CustomWallView 继承 FrameLayout、KaraokeStatusView 继承 LinearLayout，父类的嵌套 LayoutParams 可裸用）、`ScaleType`（ContextWallImageView 继承 AppCompatImageView，ImageView.ScaleType 可裸用）、`VH`（BaseDiffAdapter 的泛型类型参数）。另对 32 个移植文件做了本地 import 全量扫描：除构建生成的 R（9 处，正常）外无未解析引用。
- 缺失 layout：0；`R.id.bottom` 引用 1 处（已有定义，见 8.2）。
- 版本未动：versionCode=32 / versionName=2.0.1 / targetSdk=34 / compileSdk=34。
- 注意：VERIFY_STATIC.md §6 的结论文字是脚本内写死的旧文本，仍称“PlaybackActivity 与 activity_video.xml 缺失”，与 §5 的“已存在”矛盾——以 §5 为准，§6 文本需改脚本才更新，本批未改脚本。
- 未验证的仍未验证：真实 Gradle 编译仍没跑通（gradlew 引号缺陷未修、本机无 JDK/Android SDK）；view_empty.xml 用到的 LottieAnimationView 在影图依赖里没有 lottie（build.gradle 与 Java 均无），ProgressLayout 走到空状态时运行时会 ClassNotFoundException——需另加 lottie 依赖或换掉该 view，本批未动依赖。
