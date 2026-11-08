# 剩余未改的空 catch / printStackTrace 清单

P0 第 3 步（2026-11-08）生成。统计范围：`app/src/main/java` 与 `catvod/src/main/java` 下全部 Java 文件，
统计时已剔除字符串与注释里的干扰（即只数真正的代码）。

## 已改完的关键路径（本步，不在下表里）

- `ApiConfig.java`：3 处空 catch（缓存订阅、嗅探规则、直播地址）+ `optInt` 静默默认值，全部改成 AppLog。
- `LiveParser.java`：15 处 `printStackTrace`（Setting 内各字段解析）全部改成 AppLog。
- `PlayerManager.java`：12 处——起播/错误回调路径 4 处（刷新直链、刷新结果无效、解析失败、播放器最终报错）+ 8 处空 catch（状态读取类）。
- `WebParser.java`：混合内容、SSL 错误、另 2 处同文件静默 catch。
- `AppBackup.java`：本步改动与 catch 无关（SHA-256 清单），无剩余 printStackTrace/空 catch。

## 怎么读下表

- **printStackTrace**：仍直接打堆栈、未走 AppLog 的次数。
- **空 catch**：catch 体里没有任何语句的次数（最优先补的下一批）。
- **ignored 命名**：异常变量名叫 `ignored` 的 catch 次数；其中一部分体内已有日志/处理，
  只看名字会高估，复核时以“空 catch”列为准。
- **catch 总数**：该文件 catch 总数，供判断规模。

**合计（107 个文件）：printStackTrace 41 处，空 catch 144 处，ignored 命名 218 处，catch 总数 700 处。**
任务原文说的“约 90 处”低估了：全仓库静默点（printStackTrace + 空 catch）实际为 185 处。

| 文件 | printStackTrace | 空 catch | ignored 命名 | catch 总数 |
| --- | ---: | ---: | ---: | ---: |
| `app/src/main/java/com/yingtu/plus/player/PlayerManager.java` | 0 | 10 | 14 | 46 |
| `app/src/main/java/com/yingtu/plus/player/mpv/MpvConfigStore.java` | 0 | 10 | 13 | 19 |
| `app/src/main/java/com/yingtu/plus/web/WebServer.java` | 1 | 7 | 7 | 12 |
| `app/src/main/java/com/yingtu/plus/api/loader/JarLoader.java` | 5 | 1 | 1 | 10 |
| `app/src/main/java/com/yingtu/plus/util/Prefers.java` | 0 | 6 | 6 | 11 |
| `app/src/main/java/com/yingtu/plus/utils/ImgUtil.java` | 6 | 0 | 0 | 6 |
| `app/src/main/java/com/yingtu/plus/viewmodel/SourceViewModel.java` | 4 | 2 | 2 | 6 |
| `app/src/main/java/com/yingtu/plus/player/engine/IjkSimplePlayer.java` | 0 | 5 | 8 | 21 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoUtil.java` | 0 | 4 | 5 | 9 |
| `app/src/main/java/com/yingtu/plus/player/karaoke/KaraokeTrackRepository.java` | 0 | 4 | 4 | 13 |
| `app/src/main/java/com/yingtu/plus/player/lut/LutStore.java` | 0 | 4 | 4 | 9 |
| `app/src/main/java/com/yingtu/plus/player/core/IjkPlayerCore.java` | 0 | 4 | 4 | 8 |
| `app/src/main/java/com/yingtu/plus/utils/LoginStateSync.java` | 0 | 4 | 4 | 19 |
| `catvod/src/main/java/com/github/catvod/net/OkHttp.java` | 4 | 0 | 0 | 5 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoDecoderRuntimeProfileStore.java` | 0 | 3 | 5 | 5 |
| `app/src/main/java/com/yingtu/plus/player/ijk/IjkRuntimeProfileStore.java` | 0 | 3 | 5 | 5 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/LyricsRepository.java` | 0 | 3 | 3 | 15 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/QqMusicClient.java` | 0 | 3 | 3 | 12 |
| `app/src/main/java/com/yingtu/plus/ui/activity/PlayActivity.java` | 1 | 2 | 2 | 3 |
| `app/src/main/java/com/yingtu/plus/util/Sniffer.java` | 0 | 3 | 3 | 4 |
| `app/src/main/java/com/yingtu/plus/utils/FileUtil.java` | 3 | 0 | 0 | 4 |
| `app/src/main/java/com/yingtu/plus/utils/SearchSuggest.java` | 0 | 3 | 3 | 3 |
| `app/src/main/java/com/yingtu/plus/api/JsonParserHelper.java` | 0 | 2 | 2 | 3 |
| `app/src/main/java/com/yingtu/plus/api/config/BaseConfig.java` | 2 | 0 | 0 | 3 |
| `app/src/main/java/com/yingtu/plus/api/loader/PanProxyCompat.java` | 0 | 2 | 2 | 9 |
| `app/src/main/java/com/yingtu/plus/model/SiteViewModel.java` | 2 | 0 | 0 | 0 |
| `app/src/main/java/com/yingtu/plus/player/codec/CodecCapabilityInspector.java` | 0 | 2 | 5 | 10 |
| `app/src/main/java/com/yingtu/plus/player/danmaku/LiveDanmakuNetworkMonitor.java` | 0 | 2 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/player/diagnostic/PanNetworkDiagnosticRunner.java` | 0 | 2 | 3 | 14 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoDv5VideoSink.java` | 0 | 2 | 2 | 3 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoFrameSchedulingPerfettoTrace.java` | 0 | 2 | 3 | 3 |
| `app/src/main/java/com/yingtu/plus/player/extractor/JianPian.java` | 2 | 0 | 0 | 2 |
| `app/src/main/java/com/yingtu/plus/player/extractor/MpdSanitizer.java` | 0 | 2 | 3 | 7 |
| `app/src/main/java/com/yingtu/plus/player/iso/IsoDiskPageStore.java` | 0 | 2 | 2 | 4 |
| `app/src/main/java/com/yingtu/plus/player/karaoke/KaraokeAudioExtractor.java` | 0 | 2 | 4 | 8 |
| `app/src/main/java/com/yingtu/plus/player/karaoke/KaraokeMicRecorder.java` | 0 | 2 | 2 | 5 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/TtmlClient.java` | 0 | 2 | 2 | 6 |
| `app/src/main/java/com/yingtu/plus/ui/fragment/WebHomeFragment.java` | 0 | 2 | 2 | 3 |
| `app/src/main/java/com/yingtu/plus/utils/Notify.java` | 0 | 2 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/utils/NsdDeviceDiscovery.java` | 0 | 2 | 2 | 2 |
| `catvod/src/main/java/com/github/catvod/utils/Path.java` | 0 | 2 | 2 | 8 |
| `app/src/main/java/com/yingtu/plus/api/DanmakuApi.java` | 0 | 1 | 1 | 2 |
| `app/src/main/java/com/yingtu/plus/api/LiveApi.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/SpiderManager.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/VodSpider.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/config/LiveConfig.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/loader/JsLoader.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/loader/PyLoader.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/bean/Site.java` | 0 | 1 | 1 | 3 |
| `app/src/main/java/com/yingtu/plus/model/LiveViewModel.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/diagnostic/PanEndpointParser.java` | 0 | 1 | 2 | 5 |
| `app/src/main/java/com/yingtu/plus/player/engine/ExoPlayerEngine.java` | 0 | 1 | 3 | 5 |
| `app/src/main/java/com/yingtu/plus/player/exo/DolbyVisionP81ExtractorsFactory.java` | 0 | 1 | 2 | 5 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoDecoderRuntimeKey.java` | 0 | 1 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoMemoryPressureCoordinator.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/exo/HttpEofRecoveryDataSource.java` | 0 | 1 | 1 | 2 |
| `app/src/main/java/com/yingtu/plus/player/extractor/Force.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/player/extractor/MpdEdlResolver.java` | 0 | 1 | 2 | 3 |
| `app/src/main/java/com/yingtu/plus/player/extractor/TVBus.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/extractor/Youtube.java` | 1 | 0 | 0 | 3 |
| `app/src/main/java/com/yingtu/plus/player/iso/IsoPageCache.java` | 0 | 1 | 2 | 5 |
| `app/src/main/java/com/yingtu/plus/player/iso/IsoPlaybackSession.java` | 0 | 1 | 1 | 3 |
| `app/src/main/java/com/yingtu/plus/player/iso/IsoTrackMetadataResolver.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/karaoke/BasicPitchTfliteGenerator.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/karaoke/KaraokePitchTrackGenerator.java` | 0 | 1 | 1 | 3 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/DesktopLyricsWindow.java` | 0 | 1 | 1 | 3 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/KuwoClient.java` | 0 | 1 | 1 | 8 |
| `app/src/main/java/com/yingtu/plus/player/lyrics/LyricsController.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/core/MpvPlayerCore.java` | 0 | 1 | 1 | 2 |
| `app/src/main/java/com/yingtu/plus/player/core/PlayerFactory.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/ui/activity/FirstActivity.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/ui/activity/HomeActivity.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/ui/activity/VideoActivity.java` | 0 | 1 | 2 | 9 |
| `app/src/main/java/com/yingtu/plus/util/MediaScanner.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/utils/Clock.java` | 0 | 1 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/utils/DanmakuSearchListFocusFixer.java` | 0 | 1 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/utils/ScanTask.java` | 0 | 1 | 3 | 3 |
| `app/src/main/java/com/yingtu/plus/utils/Util.java` | 1 | 0 | 1 | 7 |
| `catvod/src/main/java/com/github/catvod/crawler/DebugLogStore.java` | 0 | 1 | 2 | 2 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/DiagnosticLogBuffer.java` | 0 | 1 | 4 | 7 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/DiagnosticText.java` | 0 | 1 | 1 | 1 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/RollingDiagnosticFile.java` | 0 | 1 | 1 | 3 |
| `catvod/src/main/java/com/github/catvod/utils/Shell.java` | 1 | 0 | 0 | 1 |
| `app/src/main/java/com/yingtu/plus/api/parser/EpgParser.java` | 0 | 0 | 1 | 5 |
| `app/src/main/java/com/yingtu/plus/bean/Epg.java` | 0 | 0 | 1 | 2 |
| `app/src/main/java/com/yingtu/plus/player/engine/MpvAudioCapabilities.java` | 0 | 0 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/player/engine/MpvPlayerEngine.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoCompressedAudioDirectPolicy.java` | 0 | 0 | 1 | 11 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoDiagnosticCodecAdapter.java` | 0 | 0 | 4 | 8 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoDiagnosticCollector.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/exo/ExoFrameSchedulingExperimentIdentity.java` | 0 | 0 | 2 | 2 |
| `app/src/main/java/com/yingtu/plus/player/exo/PlaybackAnalyticsListener.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/exo/PlaybackBytePositionDataSource.java` | 0 | 0 | 3 | 5 |
| `app/src/main/java/com/yingtu/plus/player/exo/PreCache.java` | 0 | 0 | 1 | 2 |
| `app/src/main/java/com/yingtu/plus/player/ijk/IjkRuntimeProfileKey.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/iso/IsoSessionManager.java` | 0 | 0 | 1 | 4 |
| `app/src/main/java/com/yingtu/plus/player/mpv/MpvDirectAudioPolicy.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/player/mpv/MpvHlsVariantController.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/ui/activity/WebHomeActivity.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/util/AppLog.java` | 0 | 0 | 1 | 1 |
| `app/src/main/java/com/yingtu/plus/utils/WebViewUtil.java` | 0 | 0 | 1 | 4 |
| `catvod/src/main/java/com/github/catvod/bean/Doh.java` | 0 | 0 | 1 | 2 |
| `catvod/src/main/java/com/github/catvod/crawler/SpiderDebug.java` | 0 | 0 | 1 | 2 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/DiagnosticAccess.java` | 0 | 0 | 1 | 1 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/DiagnosticReport.java` | 0 | 0 | 5 | 5 |
| `catvod/src/main/java/com/github/catvod/crawler/diagnostics/JournaledDiagnosticFile.java` | 0 | 0 | 1 | 6 |
| `catvod/src/main/java/com/github/catvod/net/OkHttpLogPolicy.java` | 0 | 0 | 1 | 1 |

## 建议的下一批（按“离用户最近”排）

1. `ImgUtil.java`（6 处 printStackTrace）、`JarLoader.java`（5 处）、`SourceViewModel.java`（4 处）：
   图片加载与源加载失败时用户只看到空白/加载失败，无日志可查。
2. `WebServer.java`（7 处空 catch + 1 处 printStackTrace）、`OkHttp.java`（catvod，4 处 printStackTrace）：
   本地服务与网络层，失败静默最难定位。
3. `MpvConfigStore.java`（10 处空 catch）、`IjkSimplePlayer.java`（5 处）、`PlayerManager.java` 剩余 10 处空 catch：
   播放器配置与引擎层，多为防御性取值，改时只加 debug 日志、不改默认值。
4. `Prefers.java`（6 处空 catch）：读写设置失败静默返回默认值，改时只加 debug 日志。
5. `AppLog.java` 自身 1 处 ignored 命名是 `format()` 里的兜底 catch（格式化失败时原样返回消息），
   属于有意为之，不用改。

## 口径说明

- 本清单只统计“静默失败点”，不统计体内已有 `SpiderDebug.log` / `PlaybackTrace.log` / `AppLog` 的 catch，
  那些已有日志，不算未改。
- LiveParser 的失败只记日志、不弹 Toast：它是纯静态解析，无 Context/UI，单行失败只丢该字段、
  不影响整条直播源解析，弹提示会刷屏；同理 ApiConfig 启动期缓存失败只记日志。
