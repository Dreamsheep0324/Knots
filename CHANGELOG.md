# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.2] - 2026-06-18

### Added

- 自动化测试体系建设 — 从零构建覆盖 12/16 模块的单元测试体系，新增 26 个测试文件约 580 个测试用例

- Phase 2: P1 纯逻辑测试（零依赖，7 文件 ~96 用例）
  - `ThoughtGamificationUseCaseTest` — 经验值计算、等级、连续打卡、游戏化状态
  - `SubscriptionTest` — 月/年等效费用换算、状态判断、周期 displayName
  - `IntimacyTierTest` — 亲密度边界值（15 参数化）、区间连续性、属性
  - `DateCalcUtilsTest` — 闰年、天数计算、生日信息、下次生日/重复日期
  - `ZodiacUtilsTest` — 24 个星座边界日期参数化、fromBirthday
  - `SafeCallTest` — success/exception/CancellationException 重抛
  - `ListStringConverterTest` — List↔String 转换、round-trip 一致性

- Phase 3: P2 聚合逻辑测试（需 Mock，7 文件 ~39 用例）
  - `SubscriptionStatsUseCaseTest` — 空订阅、活跃合计、过期排除、分类分组、7 天到期筛选
  - `FootprintAggregationUseCaseTest` — 事件→足迹映射、无位置过滤、日期降序
  - `HomeStatsUseCaseTest` — 12-Flow combine、photoCount 聚合
  - `PhotoAlbumAggregationUseCaseTest` — CONVERSATION/MEETUP 映射、多照片、混合排序
  - `CleanCustomTypeUseCaseTest` — JSON 数组移除、最后一项→null、逗号分隔回退
  - `LunarDateUtilsTest` — 农历生日计算、格式化、天数信息
  - `RetryInterceptorTest` — PUT 不重试、SocketTimeout 重试 3 次、异常不重试

- Phase 4: 基础设施与扩展覆盖（6 文件 + 1 扩展 ~55 用例）
  - `FilterExtTest`（扩展）— 5 个 Subscription 筛选测试
  - `GetContactDisplayInfoUseCaseTest` — 星座、相识天数文本、亲密度
  - `GetAnniversaryDisplayUseCaseTest` — effectiveDate、categorizeAnniversaries
  - `HomeDataAggregationUseCaseTest` — 6-Flow 聚合、recentEvents、todayReminders
  - `WuXingHelperTest` — 五行/六亲/六神/空亡/五行关系（22 参数化）
  - `SearchStateManagerTest` — 搜索状态管理、debounce
  - `ContactListViewModelTest` — 圆环展开、卡片翻转、成员选择、排序模式

- ViewModel 测试（2 文件 ~38 用例）
  - `GiftsViewModelTest`（16 用例）— 初始化加载、筛选、收藏切换、CRUD、Flow 委托
  - `WebDavSyncViewModelTest`（22 用例）— 连接测试、上传/下载/删除备份、刷新版本、清理孤立图片、配置更新、进度百分比

- 算法引擎测试（5 文件 ~153 用例）
  - `GanZhiCalculatorTest`（~40 用例）— 时辰地支映射、干支索引、fromSolar/fromCalendar、日干支、农历月日
  - `LiuyaoEngineTest`（~25 用例）— 静卦/动爻/特殊格局（用九用六独静）、数据结构完整性、动爻详情、默认生成
  - `MeihuaEngineTest`（~30 用例）— 数字/时间/随机/外应四种起卦法、体用关系、数据结构完整性
  - `PromptBuilderTest`（~18 用例）— 六爻/梅花系统提示、用户提示构建、关键信息覆盖
  - `DataIntegrityTest`（~40 用例）— 64 卦/8 卦/8 宫/纳甲/外应数据完整性验证

### Changed

- Version Catalog 扩展：`gradle/libs.versions.toml` 新增 `junit-jupiter-params` 和 `org.json:json` 库定义
- 8 个模块 build.gradle.kts 新增测试依赖：JUnit 5 (5.10.2) + MockK (1.13.16) + Truth (1.4.2) + Turbine (1.1.0) + kotlinx-coroutines-test (1.9.0) + `useJUnitPlatform()`
- ListStringConverterTest 需 `org.json:json` JVM 依赖：`JSONArray` 是 Android SDK 类，JVM 单元测试不可用
- RetryInterceptorTest 适配当前 OkHttp 接口：移除不存在的 `withConnectionPool`，PUT 请求需 `toRequestBody()`

## [1.3.1] - 2026-06-17

### Changed

- **亲密度数值调整**：重新平衡五档亲密度阈值（初识 0-14 / 泛交 15-39 / 朋友 40-74 / 密友 75-89 / 至亲 90-100），更贴合真实社交距离感知
- **软件图标配色更新**：更新应用图标配色方案，提升视觉辨识度
- **亲密度七处重复定义统一**：`CardRarity` 枚举替换为 `IntimacyTier` 枚举（单一真相源），定义 label/cardRarity/minScore/maxScore/colorValue/stars；11 个文件从 `CardRarity`/`getCardRarity` 迁移到 `IntimacyTier`/`IntimacyTier.of()`
- **ViewModel 多个 collect 合并**：`AddContactViewModel` 7 个独立 collect 合并为 1 个 combine+collect；`AddEventViewModel` 4 个独立 collect 合并为 1 个
- **ContactDao 全表扫描优化**：新增 `getContactListItems()` 列投影查询（仅 10 列），列表页少读 ~20 列，不关联 `contact_attributes` 表
- **ImageFileManager/ImageCacheManager 合并**：翻转依赖方向（core:data 移除对 core:ui 的未使用依赖），`ImageCacheManager` 委托 `ImageFileManager`，迁移 `countPhotosFromJson` 方法
- **!! 非空断言安全化**：22 处危险 `!!` 替换为 `?.let`/`?.takeIf`/`checkNotNull`，覆盖 15 个文件
- **颜色常量集中到主题**：新增 `IntimacyColors` 数据类 + `LocalIntimacyColors` CompositionLocal + 深色模式覆盖（`LightIntimacyColors`/`DarkIntimacyColors`）；10 处 Compose 代码从 `Color(tier.colorValue)` 迁移到 `LocalIntimacyColors.current.forTier()`
- **魔法数字提取为命名常量**：`Subscription.timezone` 默认值 `"UTC"` → `DEFAULT_TIMEZONE = "Asia/Shanghai"`；`Contact.intimacyScore` 默认值 → `DEFAULT_INTIMACY_SCORE = 50`；`ThoughtGamificationUseCase` XP 权重提取为 `XP_BASE/XP_DONE_BONUS/XP_CONTACT_BONUS/XP_STREAK_BONUS`，等级公式提取 `BASE_XP_PER_LEVEL = 15`；`LiuyaoEngine` shiYaoMap 添加八宫世爻规律注释
- **Version Catalog 全量迁移**：12 个模块的 `build.gradle.kts` 从硬编码版本字符串迁移到 `libs.versions.toml` 版本目录引用，统一测试依赖版本（junit 5.10.2, mockk 1.13.16）
- **ProGuard 规则清理**：移除 `androidx.biometric` 规则（项目无此依赖）和 `NavHostFragment` keep 规则（项目使用 Navigation Compose）
- **通知机制完善**：PendingIntent 添加 `FLAG_UPDATE_CURRENT`；通知小图标从系统默认改为 `R.mipmap.ic_launcher`；通知 ID Long→Int 溢出修复（`reminderId % Int.MAX_VALUE`）；BootReceiver 添加 `MY_PACKAGE_REPLACED` intent-filter

### Security

- **ZIP 路径遍历防护修正**：`BackupRepository.writeFileFromZip` 改为与 `allowedDir.canonicalPath` 比对（而非 `parentFile.canonicalPath`），堵住恶意 ZIP 通过 `../` 覆盖任意文件的漏洞
- **加密 SharedPreferences 容错统一**：`provideEncryptedSharedPreferences` 新增 try-catch + fallback + 日志，与 `provideWebDavEncryptedPrefs` 统一容错策略；两个 fallback 均使用 `_fallback` 后缀文件名 + `Log.w` 日志，防止无 Keystore 设备崩溃
- **加密降级标记**：EncryptedSharedPreferences 创建失败时，降级为明文存储并写入 `encryption_degraded` 标记，日志级别从 `Log.w` 提升为 `Log.e`，便于 UI 层警告用户
- **WebDAV 上传原子性**：上传失败不再静默吞掉——记录失败数、manifest 仅包含成功上传的文件、上报 `SyncResult.PartialSuccess`；图片上传失败添加 `Log.w` 日志
- **OnConflictStrategy.REPLACE 级联删除防护**：`ContactDao.insertContact`、`EventDao.insertEvent`、`CircleDao.insertCircle` 从 `REPLACE` 改为 `IGNORE`，避免 SQLite REPLACE 的 DELETE+INSERT 行为触发外键级联删除
- **备份恢复失败回滚**：`restoreBackup`/`restoreDbOnly`/`restoreFromUri` 三处恢复方法添加预恢复快照备份（`.before_restore`），失败时自动回滚并重启应用
- **SimpleDateFormat 线程安全**：`WebDavClient` 移除类级别共享的 `SimpleDateFormat` 实例，改为每次调用创建新实例

### Fixed

- **相册对话照片重复显示**：`PhotoAlbumAggregationUseCase` 中 `events + conversations` 将 CONVERSATION 事件拼接两次，移除对 `getEventsByType(CONVERSATION)` 的独立订阅，combine 从 4 源减为 3 源
- **Subscription 月/年换算不一致**：WEEKLY 月等效 `price * 4.33`（4.33×12=51.96）与年等效 `price * 52` 不一致，统一使用 `WEEKS_PER_MONTH = 52.0/12.0` 系数
- **梅花易数 changedRelation 与 changedTiYongRelation 值永远相同**：`changedRelation` 应为变卦用神与原卦体卦的五行关系，而非变卦体用关系，修复为 `WuXingHelper.getElementRelation(it.yongGua.element, tiYong.tiGua.element)`
- **HomeScreen 时间显示永不更新**：`remember { System.currentTimeMillis() }` 无 key 导致时间停留在首次组合，改为 `mutableLongStateOf` + `LaunchedEffect` 每秒更新
- **SSE 流读取可能无限循环**：`readUtf8Line()` 返回 null 时 `continue` 导致循环无法退出（source 可能未 exhausted），改为 `break`
- **insertContact 缺少事务包裹**：先插入 Contact 再插入 Attributes 两步不在事务中，用 `database.withTransaction` 包裹
- **updateEvent/updateGift 先删照片再更新数据库**：照片先删后更新导致失败时数据丢失，改为先更新 DB 成功后再删除文件
- **deleteEvent/deleteGift 文件 I/O 在事务内**：事务内收集待删除路径，事务外再删文件
- **updateContact 文件 I/O 在事务内**：事务返回旧头像路径，事务外删除文件
- **EncryptedSharedPreferences 降级无感知**：降级为明文存储时用户无感知，添加 `encryption_degraded` 标记和 `Log.e` 日志
- **ContactDetailViewModel 回调反模式**：`deleteContact(onDeleted: () -> Unit)` 接收 lambda 回调持有 UI 导航引用，改为 `Channel<ContactDetailEvent>` + `receiveAsFlow()`，UI 层通过 `LaunchedEffect` 观察事件
- **AiViewModel 公开可变 lambda 属性**：`var onAnalysisComplete: ((String) -> Unit)?` 改为 `Channel<String>` + `receiveAsFlow()`，MeihuaAiDeepSection 同步适配
- **编辑表单 collect 覆盖风险**：`AddAnniversaryViewModel.loadAnniversary` 和 `AddContactViewModel.loadContact` 从 `collect` 持续监听改为 `.first()` 一次性读取
- **Compose 列表过滤未缓存**：`ChatScreen.filteredConversations` 和 `ContactListScreen.sortedCircles` 用 `remember` 缓存过滤结果
- **CircleDao N+1 写入**：`updateCircleWithMembers`/`insertCircleWithMembers` 循环逐条 INSERT 改为批量 `insertMemberCrossRefs(List<CircleMemberCrossRef>)`
- **LiuyaoEngine 7 处非空断言**：`!!` 替换为 `?: throw IllegalStateException("描述性消息")`
- **4 处迁移 Cursor 资源泄漏**：`Migration_1_32`/`Migration_24_31`/`Migration_31_33`/`Migration_32_33` 的 Cursor 改为 `.use { }` 包裹
- **JSON 解析脆弱性**：`ContactMapper.parseJsonList` 和 `Migration_31_33` 从 `split(",")` 改为 `JSONArray` 解析，含逗号值不再被错误分割
- **SafeCall 破坏协程取消语义**：`safeDbCall`/`safeApiCall` 合并为 `safeCall`，添加 `CancellationException` 重抛
- **7 处重复代码消除**：`MILLIS_PER_DAY` 常量统一、`getIntimacyLevel` 删除（统一用 `IntimacyTier.of()`）、`GanZhiCalculator.fromCalendar` 便捷方法、`ExternalOmenData.mod6` 工具函数、`FilterExt` 中 `getIntimacyLevel` 引用修正
- **农历纪念日按公历计算**：`AnniversaryRepositoryImpl.effectiveDate()` 检测 `isLunar` 后调用 `LunarUtils.lunarToSolar()` 转换为当年对应公历日期，支持闰月（`isLeapMonth` 参数）
  - `LunarUtils.lunarToSolar()` 修复 `isLeap` 参数未生效：使用负月份约定（`-lunarMonth` 表示闰月）
  - 数据库 v38→v39：`anniversaries` 新增 `isLeapMonth` 列，`contacts` 新增 `isLeapMonthBirthday` 列
  - 新建/编辑纪念日支持闰月切换（当农历开启时显示闰月开关）
  - 新建/编辑人物生日支持闰月标记
- **WebDAV 下载失败仍报告成功**：恢复流程重排为"数据库恢复→图片下载→清理→报告"；数据库恢复失败时 `return@flow` 立即停止；图片部分失败时 emit `SyncResult.PartialSuccess`
- **SSE 流不响应取消**：`AiRepositoryImpl.streamChat` 改用 `launch(Dispatchers.IO)` 执行阻塞 IO + `awaitClose` 立即注册取消 + `finally { close() }` 确保 Flow 完成时关闭连接
- **appScope 协程作用域永不取消**：自动备份从 `MainActivity` 下沉到 `TangApplication`，使用 `ProcessLifecycleOwner` + `DefaultLifecycleObserver` 监听应用前后台，旋转屏幕不再泄漏协程
- **数据库迁移图断裂 v28→v31**：新增 `MIGRATION_28_31`（anniversaries 重建外键 + events 添加 customTypeName），注册到 DatabaseModule
- **WebDavClient Response 未关闭**：15 处 `okHttpClient.newCall(request).execute()` 统一改为 `.use {}` 包裹，防止连接泄漏
- **通知 ID 碰撞**：`ReminderReceiver` 使用 `AtomicInteger` 自增计数器替代 `currentTimeMillis().toInt()`
- **GiftRepositoryImpl 缺少事务保护**：`deleteGiftById`/`deleteGiftsByContactId` 添加 `database.withTransaction` 包裹，先查后删在同一事务内
- **ContactRepositoryImpl 文件 I/O 在事务内**：文件删除操作移到 `withTransaction` 外部，事务内仅做数据库操作
- **SubscriptionRepositoryImpl 缺 @Singleton**：补上 `@Singleton` 注解
- **AvatarCropDialog 位图泄漏**：`DisposableEffect` 回收预览位图；catch 块回收 sourceBitmap/outputBitmap；draw-phase 状态写入改为 `onGloballyPositioned`；`onCropComplete` 包裹在 `LaunchedEffect`；手势状态改用 `rememberSaveable`
- **OrbitalCalendarCanvas draw 阶段内存分配**：`gridColors`/`rayColors`/`dashPathEffect`/`sortedSignalKeys` 及 7 个 `TextStyle` 提升到 `remember` 块
- **SearchStateManager 没有 debounce**：新增 `debouncedQuery` Flow（300ms debounce + distinctUntilChanged）
- **HomeViewModel greeting 不随时间更新**：`greetingFlow` 改为 30 分钟刷新周期
- **uploadFileWithProgress 进度估算**：移除 `(written * 0.9).toLong()` 进度扭曲系数
- **restartApp 用 System.exit**：添加 KDoc 说明必须使用 System.exit 的原因（Room DB 文件覆盖后连接不可重建）；3 处恢复方法补 `database.checkpoint()`；添加 `Log.i` 记录重启原因
- **catch 静默吞没**：BackupRepository 3 处 + WebDavRepositoryImpl 1 处空 catch 块补充 `Log.w` 日志
- **ReminderReceiver 编译错误**：合并重复的 `companion object`（`CHANNEL_ID` + `notificationIdCounter`）

### Architecture

- **Version Catalog 启用**：创建 `gradle/libs.versions.toml`（27 版本 + 40 库 + 6 bundle + 8 插件），全量迁移 16 个模块从硬编码依赖到 `libs.xxx` 引用
- **HexagramData 查找优化**：预构建 `bySymbolMap`/`byBinaryMap`/`byNameMap` lazy Map，O(n)→O(1)；`!!` 替换为 `requireNotNull` 带描述性错误信息
- **ViewModel 事件通道统一**：`ContactDetailViewModel` 和 `AiViewModel` 的回调/lambda 模式统一改为 `Channel` + `receiveAsFlow()` 单向事件流
- **DAO insert 策略统一**：所有 `insertXxx` 方法从 `OnConflictStrategy.REPLACE` 改为 `IGNORE`，强制使用 `updateXxx` 更新已有数据

### Database

- 数据库版本 v38→v39：`anniversaries` 新增 `isLeapMonth INTEGER NOT NULL DEFAULT 0`，`contacts` 新增 `isLeapMonthBirthday INTEGER NOT NULL DEFAULT 0`
- 新增 `MIGRATION_28_31`：覆盖 v1.0.0 用户（v28）直接升级到 v31 的路径

## [1.3.0] - 2026-06-07

### Added

- **订阅功能** — 完整的订阅管理模块 (`:feature:subscription`)
  - 订阅列表页：搜索栏、统计摘要卡片、分类筛选标签、分类分组卡片、空状态
  - 新建/编辑页：TagSelector 单选分类（图标+颜色）、价格与周期选择、日期选择、备注
  - 订阅详情页：HeroCard、日均/月均/年均费用转换、订阅时长（年/月/日）、扣费进度、日期信息、周期信息、备注
  - 订阅统计页：年度预估、关键指标网格、分类占比圆环图（含每分类订阅列表）、扣费日历
  - 首页订阅角标：HomeStatsUseCase 12-Flow combine，首页信号卡片显示订阅数量
  - `SubscriptionCycle` 枚举：WEEKLY/MONTHLY/QUARTERLY/YEARLY/ONE_TIME
  - `computeNextBillingDate()` 扩展：基于 startDate + cycle 计算下次扣费日期
  - `computedStatus()` 扩展：在 VM 层计算 EXPIRED 状态（nextBillingDate < now）
  - 数据库 v33→v34：新增 `subscriptions` 表

- **头像裁剪功能** — 新建/编辑人物时上传头像支持裁剪
  - `AvatarCropDialog` 组件：圆形蒙版预览、单指拖动、双指缩放
  - Canvas 直接绘制图片 + 4 矩形遮罩（兼容所有设备）
  - 降采样加载预览图（`loadSampledBitmap`，800px 采样，避免大图卡顿）
  - 裁剪输出 512x512 JPEG，预览与裁剪坐标精确映射（`scaleFactor` 偏移量缩放）

- **相册多人物显示** — 每日/事件/网格视图均显示所有关联人物
  - `AlbumPhoto` 新增 `allContactNames`/`allContactAvatars` 列表字段
  - `PhotoAlbumAggregationUseCase` 收集所有参与者信息（不再只取 `firstOrNull()`）
  - 每日视图：顿号连接显示所有联系人名字
  - 事件视图：遍历显示所有联系人头像+名字
  - 照片查看器：显示所有联系人头像+名字
  - 网格视图：不显示人物，右上角显示类型图标（事件/对话/礼物）

- **扣费日历组件** — 订阅统计页新增月历视图
  - 完整月历网格（日一二三四五六）
  - 扣费日高亮（Amber 底色），今天高亮（Primary 底色）
  - 每个扣费日下方显示分类颜色圆点
  - 右上角显示本月扣费次数标签

- **WebDAV 增量同步** — 通过 WebDAV 协议将数据增量同步到用户自有云存储，仅传输变更文件
  - `WebDavClient`：15 个协议方法，按职责分为 5 组（连接与目录/旧版全量文件/增量同步专用/小文件与元数据），基于 OkHttp + XmlPullParser 解析 multistatus XML 响应
  - `SyncManifest` + `FileEntry`：增量同步清单模型（kotlinx-serialization），记录版本号/时间戳/数据库文件名/图片文件列表，作为同步索引
  - `WebDavRepositoryImpl` 增量同步调度：
    - 上传 7 步流程：确保远程目录→读取远端清单→扫描本地文件（过滤孤立文件）→计算 diff→上传数据库（删除旧 DB）→逐个上传图片+清理远端多余→更新并上传 manifest
    - 下载 6 步流程：读取远端清单（无清单降级全量 ZIP）→扫描本地文件→计算反向 diff→逐个下载图片→删除本地多余图片→恢复数据库
    - `computeFileDiff()`/`computeDownloadDiff()`：基于 `FileEntry.modified` 时间戳的双向 diff
    - `isDbChanged()`：比较本地 DB 文件（含 WAL）修改时间与 manifest 中 `dbTimestamp`
  - 远程目录结构：`/knots_backup/`（manifest.json + db/ + images/ + gift_photos/）
  - `WebDavSyncUseCase`：8 个方法（含 `cleanOrphanedImages`），Domain 层统一入口
  - `WebDavSyncScreen` 完整 UI：服务器配置表单、测试连接、上传/恢复操作卡片、自动同步开关、清理孤立图片按钮（`CleanOrphanedImagesButton`）、云端版本列表（增量版本显示图片数量）、上次同步时间
  - 兼容性：无 manifest 时自动降级为旧版全量 ZIP 流程
  - 设置页新增独立入口（WebDAV 同步），与备份恢复并列

### Changed

- **订阅详情页重组**：
  - 移除 PaymentForecastCard（费用预测）和 ForecastRow 组件
  - CostConversionRow 移至 HeroCard 下方，标签改为日均/月均/年均
  - SubscriptionTimelineCard 移至 BillingProgressCard 上方
  - SubscriptionTimelineCard 移除累计已付（Coral 高亮），时长顺序改为年/月/日

- **订阅统计页重设计**：
  - 移除 CostAnalysisCard 和 SubscriptionHealthCard
  - YearlyProjectionCard 移至最上方
  - CategoryBreakdownCard 改为圆环图设计（Canvas 绘制，中心显示年度总计，下方图例含每分类订阅列表）
  - 圆环配色优化：柔蓝、薄荷绿、暖金、珊瑚红、天青、紫藤、蜜橙、青碧

- **足迹列表**：移除每条足迹右下角的联系人头像和名字

- **TagSelector 图标修复**：事件类型和订阅分类的图标列表修正（之前两组图标写反了）

### Fixed

- 修复订阅保存失败：`nextBillingDate` 默认为当前时间导致立即 EXPIRED，改为 `computeNextBillingDate()` 计算未来日期
- 修复新建人物日期无法选择：`Modifier.then(if...)` 不可点击，改为条件变量 + `clip().clickable()`
- 修复首页动画重置：LazyColumn 回收 OrbitalCalendarCanvas 导致动画状态丢失，改为 Column + verticalScroll
- 修复首页订阅角标显示为零：补全 SubscriptionRepository → HomeStatsUseCase → HomeViewModel → HomeScreen 数据链
- 修复相册"查看全部X张"点击无反应：`clickable { }` 空实现改为 `clickable { onPhotoClick(4) }`
- 修复相册只显示第一个人物：UseCase 中 `participants.firstOrNull()` 改为收集所有参与者
- 修复编辑礼物替换照片后旧照片文件残留：`updateGift()` 新增 `oldPhotos.toSet() - newPhotos.toSet()` 差集计算并删除被替换的照片文件
- 修复编辑事件替换照片后旧照片文件残留：`updateEvent()` 同上逻辑，删除被移除的照片文件
- 修复删除联系人后头像和礼物照片文件未清理：`deleteContact()` 在 CASCADE 删除前收集图片路径，CASCADE 后清理文件
- 修复编辑联系人头像变更后旧头像文件残留：`updateContact()` 头像变更时删除旧头像文件
- 修复 WebDAV 云端备份大小始终显示 0B：HEAD 请求不可靠，改用 PROPFIND + XML 解析 `getcontentlength` 获取文件大小
- 修复 WebDAV 远程数据库 ZIP 文件累积：上传新备份后自动删除旧文件
- 修复孤立图片文件累积：新增 `cleanOrphanedImages()` 清理未被数据库引用的图片文件
- 修复备份包含孤立图片文件：`backupToFileInternal()` 只打包数据库引用的图片，跳过孤立文件
- 修复 `computeDataFingerprint` 统计孤立文件导致误触发自动备份：改为只统计数据库引用的图片
- 修复 `AddChatViewModel.loadContacts()` 使用 `collect` 导致旧值处理未取消：改为 `collectLatest`

### Architecture

- **EventsViewModel 响应式重构 (ARCH-1)**：3 个独立 `launch + collectLatest` 命令式管道 → `combine + stateIn` 声明式单一数据源管道
  - 移除 `_uiState` MutableStateFlow、`init` 块、`loadContacts()`/`loadEventTypes()`/`loadEvents()`/`clearError()` 方法
  - `uiState` 改为 `combine(dataFlow, uiSelections)` + `stateIn(WhileSubscribed(30_000))` 派生
  - 嵌套 combine 解决 Kotlin `combine` 最多 5 参数限制（`dataFlow` 聚合 3 个数据源，`uiSelections` 聚合 5 个 UI 状态）
  - 新增 `UiSelections` 私有数据类承载 UI 选择状态

- **ImageFileManager 层级纠偏 (ARCH-2)**：消除 `core:data` → `core:ui` 的反向依赖
  - 新增 `core:data/util/ImageFileManager.kt`：图片文件 I/O 操作（copyToInternalStorage/deleteImage/deleteLocalPhotos/isLocalPath/fileExists）
  - `ContactRepositoryImpl`/`GiftRepositoryImpl`/`EventRepositoryImpl`/`BackupRepository` 全部改用 `ImageFileManager`
  - `ImageCacheManager`（core:ui）恢复为独立实现，不再代理 `ImageFileManager`（core:ui 无法依赖 core:data，会造成循环依赖）

- **BackupRepository 代码去重 (ARCH-3)**：消除 `getReferencedImageFileNames()` 和 `getReferencedImageFilesSync()` 约 30 行重复代码
  - 合并为统一的 `queryReferencedImageFileNames()` 私有方法
  - `getReferencedImageFileNames()`（公开接口）委托调用 `queryReferencedImageFileNames()`
  - `backupToFileInternal()` 和 `computeDataFingerprint()` 内部调用也改为 `queryReferencedImageFileNames()`

## [1.2.1] - 2026-06-05

### Architecture

- **多模块化拆分完成（Phase 1-9）**：从单 `:app` 模块拆分为 **10 模块**架构
  - `:engine:divination` — 纯 JVM 模块，占卜引擎（零 Android/UI 依赖）
  - `:core:domain` — 纯 JVM 模块，Model、Repository 接口、UseCase、DateUtils
  - `:core:data` — Android Library 模块，Room、Retrofit、RepositoryImpl
  - `:core:ui` — Android Library 模块，theme、animation、components、navigation、PhotoPickerLauncher、SearchStateManager
  - `:feature:divination` — Compose 模块，占卜 Screen / ViewModel / 测试
  - `:feature:remember` — Compose 模块，纪念日 Screen / ViewModel / 测试（10 源文件 + 4 测试）
  - `:feature:people` — Compose 模块，联系人 Screen / ViewModel / 测试（15 源文件，含 overlay 卡片翻转）
  - `:feature:encounter` — Compose 模块，事件 + 对话 + 礼物 Screen / ViewModel / 测试（28 源文件 + 8 测试）
  - `:feature:reflect` — Compose 模块，想法 + 收藏 + 足迹 + 相册 Screen / ViewModel / 测试（32 源文件 + 4 测试）
  - `:app` — UI 壳，Activity、Navigation、DI 入口

- **`:core:ui` 模块拆分**：将共享 UI 资源从 app 迁移到独立模块
  - `ui/theme/` — 9 个文件（Color、Dimensions、EventTypeStyle、GiftTypeStyle、IconHelper、Theme、ThoughtTypeStyle、Type、densityfix）
  - `ui/animation/` — 7 个文件（HolographicCard、WaveformDisplay、AnimationTokens、PausableAnimationEngine、MotionAnimations、PulseAnimations、TransitionAnimations）
  - `ui/components/` — 14 个文件（AppCard、Avatars、ContactPickerDialog、ContactRelationshipBadge、Dialogs、EmptyState、FormComponents、FormFields、HoloEffects、MarkdownText、SearchBar、TagSelector、PhotoSlot、PhotoSlotMode）
  - `ui/navigation/` — Screen.kt
  - `res/font/` — vt323_regular.ttf
  - `PhotoPickerLauncher` 保留在 app 中（依赖 `ImageCacheManager` 在 core:data 中）

- **`:feature:divination` 模块拆分**：占卜 UI 独立为 feature 模块
  - 26 个源文件迁移（DivinationScreen、DivinationViewModel、AiViewModel、DivinationHistoryScreen 等）
  - 3 个单元测试文件迁移
  - 包名从 `com.tang.prm.ui.divination` 更新为 `com.tang.prm.feature.divination`
  - `HomeNavGraph.kt` 引用同步更新

- **`DateUtils` 迁移**：从 `app/util/` 迁移到 `core:domain/util/`（纯 JVM 工具类，39 个引用文件同步更新包名）

- **Type-Safe Navigation 升级**：30+ 路由从字符串拼接改为 `@Serializable` 类型安全导航
  - `Screen` sealed class 重构为 `@Serializable object` / `@Serializable data class`
  - 46+ 处导航调用从 `Screen.X.createRoute(id)` 更新为 `RouteType(id)`
  - 移除 `navArg<T>()` 辅助函数，改用 `toRoute<T>()`

- **Schema 重设计**：ContactEntity JSON 字段拆分为关联表
  - 新增 `contact_attributes` 关联表（`contactId` + `category` + `value`），外键 CASCADE 删除
  - 新增 `ContactAttributeDao`（按联系人/类别查询、按属性值反查联系人）
  - 数据库 v32→v33，迁移脚本自动解析 hobby/habit/diet/skill JSON 到新表
  - `ContactRepositoryImpl` 用 `combine` 合并属性数据，写入时同步更新两张表

- **跨模块依赖解耦**：为纯 JVM 模块消除 Android 依赖
  - `BackupModels`（BackupInfo、BackupResult、RestoreResult、ClearDataResult）从 data 层移入 domain 层
  - `FootprintItem`、`AlbumPhoto` 从 UI 层下沉到 domain 层
  - `IntimacyLevel.color: Color` → `colorValue: Long`（消除 Compose 依赖）
  - `Zodiac.iconRes: Int` → `iconName: String`，`color: Color` → `colorValue: Long`（消除 Android/Compose 依赖）
  - `GiftRepository` / `BackupRepositoryInterface` 中 `Uri` → `String`（消除 android.net.Uri 依赖）
  - `CleanCustomTypeUseCase` 中 `org.json.JSONArray` → `kotlinx.serialization.json`（消除 Android SDK 依赖）

- **Repository 接口精简**：5 个 Repository 接口方法数减少 28%（61→44），组合逻辑由 UseCase 承载
  - `ContactRepository`：移除 `removeRelationshipFromAll()`、`removeEducationFromAll()`、`getContactsWithListFieldValue()`，UseCase 改用 `getFilteredContacts`/`getAllContacts` + `updateContact` 组合
  - `EventRepository`：移除 7 个组合/统计方法（`getConversationEvents`、`getConversationCount`、`getPhotoCount`、`getFootprintCount`、`insertEventWithParticipants`、`updateEventWithParticipants`、`getAllEventsIncludingConversations`），新增 `EventManageUseCase` 承载事件+参与者事务
  - `GiftRepository`：移除 3 个组合方法（`getGiftsWithExistingPhotos`、`getContactsWithGifts`、`getGiftPhotoCount`），保留 `saveGiftWithPhotos`（文件 I/O 必须在 data 层）和 `deleteGiftsByContactId`（级联删除）
  - `SettingsRepository`：移除 5 个 suspend getter（`getAiApiKey`、`getAiBaseUrl`、`getAiModel`、`getAiGender`、`getAiBirthDate`），统一用 `Flow.first()` 访问
  - `BackupRepositoryInterface`：Uri→String 已完成，data 层类型已移到 domain

- **依赖清理**：`app/build.gradle.kts` 从 30+ 行精简到 20 行，Room/Retrofit/OkHttp/Gson/DataStore/Coil/lunar/security-crypto 等通过 `:core:data` 的 `api` 传递获取

- **跨模块 smart cast 修复**：12+ 个文件提取局部变量解决 Kotlin 2.x 跨模块 public API 属性不支持 smart cast 的问题

- **DAO 清理**：移除不再使用的 DAO 方法（`ContactDao.clearRelationship/clearEducation`、`EventDao.getConversationEventsWithParticipants/getConversationCount/getAllPhotosRaw/getFootprintCount`、`GiftDao.getContactsWithGifts/getAllPhotosRaw`）

### Testing

- **Compose UI 测试体系**：从零建立 androidTest 测试基础设施，覆盖核心用户路径
  - `BaseComposeTest` — 通用 Compose 测试基类（waitForAnimation / waitForData）
  - `HiltComposeTest` — Hilt + Compose 集成测试基类
  - `HomeScreenTest` — 首页渲染测试（频道网格、底部导航、时间显示）
  - `ContactsScreenTest` — 联系人列表测试（空状态、搜索栏、FAB）
  - `AddContactScreenTest` — 新建人物测试（表单字段、头像选择器、保存按钮状态）
  - `EventsScreenTest` — 事件列表测试（空状态、FAB、视图模式切换）
  - `AnniversariesScreenTest` — 纪念日测试（空状态、FAB、筛选标签、卡片渲染）
  - `ProfileScreenTest` — 个人中心测试（标题、设置项、关于、备份）
  - `SearchBarTest` — 搜索栏组件测试（提示文本、输入、清除、回调）
  - `AppCardTest` — 卡片组件测试（标题、内容、点击）
  - `TagSelectorTest` — 标签选择器测试（显示、高亮、单选/多选、回调）
  - `PhotoSlotTest` — 图片组件测试（AVATAR/THUMBNAIL/POLAROID 三种模式 + PhotoSelectionArea）
  - `EmptyStateTest` — 空状态组件测试（标题、描述、操作按钮）
  - `NavigationTest` — 底部导航测试（5 个 Tab 切换）
  - `DivinationScreenTest` — 占卜页面测试（方式选择、梅花/六爻、历史按钮）
  - `BaselineProfileGenerator` — Baseline Profile 生成器（7 个关键用户旅程）
- 新增 androidTest 依赖：`mockk-android:1.13.16`、`hilt-android-testing:2.55`、`benchmark-macro-junit4:1.3.3`
- **测试覆盖**：15 个测试文件，55+ 测试用例

### Performance

- **Baseline Profile 基础设施**：创建 `BaselineProfileGenerator`，覆盖冷启动、首页浏览、事件/纪念/对话/人物页导航、占卜功能等 7 个关键旅程，占位文件 `baseline-prof.txt` 已创建

### Build & Dependencies

- **Kotlin 2.1.21**：从 1.9.24 升级到 2.1.21，K2 编译器默认启用，增量编译速度显著提升
- **Compose Compiler Plugin**：从旧版 KCP（`kotlinCompilerExtensionVersion = "1.5.14"`）迁移到 `org.jetbrains.kotlin.plugin.compose`，由 Kotlin 插件统一管理
- **KSP 2.1.21-2.0.2**：从 1.9.24-1.0.20 升级，适配 Kotlin 2.1.21
- **Compose BOM 2025.05.01**：从 2024.12.01 升级
- **Hilt 2.55**：从 2.51.1 升级
- **Room 2.7.0**：从 2.6.1 升级
- **compileSdk / targetSdk 35**：从 34 升级
- **其他依赖升级**：coil 2.7.0、kotlinx-serialization 1.7.3、lifecycle 2.8.7、core-ktx 1.15.0、activity-compose 1.9.3、navigation-compose 2.8.5、DataStore 1.1.3、gson 2.11.0、coroutines-test 1.9.0、mockk 1.13.16

### Bug Fixes

- 修复添加礼物界面图片上传 UI 消失的问题：恢复 `PhotoSelectionCard` 组件（带 AppCard 包裹 + "照片（选填）"标题）
- 修复新建人物界面头像上传 UI 消失的问题：恢复 `ProfileHeader` 组件（带 AppCard 包裹 + 渐变背景）
- 修复新建事件界面图片上传 UI 消失的问题：恢复宝丽来风格 `PolaroidPhoto` + `PolaroidPhotoAddButton` 组件
- 清理 5 个不必要的 re-export 包装文件（ThoughtsComponents.kt、ThoughtsDialogs.kt、ContactListCard.kt、AddChatComponents.kt、DivinationRecordDetail.kt），调用方改为直接 import 子包函数

## [1.2.1] - 2026-06-04

### Architecture

- **UseCase 层强化**：新增 5 个聚合型 UseCase，将多 Repository 数据聚合逻辑从 ViewModel 下沉到 Domain 层
  - `HomeDataAggregationUseCase` — 聚合 5 个 Repository，HomeViewModel 依赖 7→5
  - `FootprintAggregationUseCase` — 聚合 3 个 Repository，FootprintsViewModel 依赖 4→3
  - `PhotoAlbumAggregationUseCase` — 聚合 3 个 Repository，PhotoAlbumViewModel 依赖 5→3
  - `ContactListManageUseCase` — 承载 Circle CRUD、成员管理、排序过滤等业务逻辑，ContactListViewModel 依赖 2→1
  - `ThoughtListUseCase` — 聚合想法列表数据与过滤逻辑，ThoughtsViewModel 依赖 5→3

- **UiState 拆分**：7 个 ViewModel 的巨型 UiState 拆分为 `DataState` + `DialogState` 组合结构，对话框状态变化不再触发全屏 recomposition
  - `ContactListUiState` → `ContactListDataState` + `ContactListDialogState`
  - `ThoughtsUiState` → `ThoughtsDataState` + `ThoughtsDialogState`
  - `ContactsUiState` → `ContactsDataState` + `ContactsDialogState`
  - `EventsUiState` → `EventsDataState` + `EventsDialogState`
  - `ContactDetailUiState` → `ContactDetailDataState` + `ContactDetailDialogState`
  - `AnniversariesUiState` → `AnniversariesDataState` + `AnniversariesDialogState`
  - `GiftsUiState` → `GiftsDataState` + `GiftsDialogState`

- **UseCase 层充实**：新建 6 个 UseCase，增强 2 个现有 UseCase，业务逻辑从 ViewModel 下沉到 Domain 层
  - `ThoughtGamificationUseCase` — 游戏化计算（经验值、等级、连续打卡）从 ThoughtsUiState 计算属性迁移为独立 UseCase
  - `FavoriteToggleUseCase` — 统一 5 个 ViewModel 的收藏切换逻辑，消除重复的 `favoriteRepository.toggleFavorite` 调用
  - `FilterThoughtsUseCase` — 想法筛选（按类型/联系人/搜索词）从 ThoughtsViewModel 内联逻辑提取
  - `FilterFootprintsUseCase` — 足迹筛选（按联系人/事件类型/年份）从 FootprintsViewModel 内联逻辑提取
  - `FilterPhotosUseCase` — 照片筛选（按联系人/来源类型）从 PhotoAlbumViewModel 内联逻辑提取
  - `CleanCustomTypeUseCase` — 自定义类型清理逻辑从 ContactRepositoryImpl 迁移，Repository 层不再包含业务逻辑
  - `FilterEventsUseCase` 增强 — 新增按事件类型、搜索关键词筛选
  - `FilterContactsUseCase` 增强 — 新增按关键词、分组、关系筛选

- **消除重复模式**：
  - `SearchStateManager` — 统一 5 个 ViewModel 的搜索状态管理，消除 `_searchQuery` / `_isSearching` 重复声明
  - `CircleConstants` — 从 ContactListViewModel 提取 `PresetColors` / `WaveformTypes` 常量到独立文件
  - `MapperExt.kt` — 新增 `mapList`、`mapNullable`、`toEnumOrDefault`、`toEnumByKeyOrDefault` 扩展函数，14 个 RepositoryImpl 和 3 个 Mapper 消除 ~50 处重复模板代码

- **Repository 瘦身**：`ContactRepositoryImpl` 移除 `removeFromListFieldAll` 和 `removeFromJsonArray` 业务逻辑，迁移到 `CleanCustomTypeUseCase`；新增 `getContactsWithListFieldValue` 纯数据查询接口

- **照片逻辑统一**：`ImageCacheManager` 新增 `countPhotosFromJson()` 和 `deleteLocalPhotos()`，EventRepositoryImpl 和 GiftRepositoryImpl 的重复照片计数/删除逻辑统一调用

- **导航规范化**：HomeScreen 硬编码路由字符串（`"gifts"` / `"contact_list"` 等）替换为 `Screen` 对象引用，`ChannelDef.route: String` 改为 `ChannelDef.screen: Screen`

### File Structure

- **7 个超大 UI 文件拆分**为独立子目录，消除单文件 400+ 行问题：
  - `DivinationRecordDetail.kt`（775行）→ `detail/` 目录 7 个文件
  - `ContactListCard.kt`（761行）→ `card/` 目录 4 个文件
  - `ContactCardOverlay.kt`（624行）→ `overlay/` 目录 3 个文件
  - `ThoughtsDialogs.kt`（547行）→ `thoughts/` 目录 ThoughtDialog + ThoughtDetailDialog
  - `ThoughtsComponents.kt`（499行）→ `thoughts/` 目录 ThoughtFeedCard + FilterTabRow + ThoughtLevelBanner + ContactStoriesRow
  - `AddChatComponents.kt`（472行）→ `components/` 目录 4 个文件
  - `FavoritesComponents.kt`（463行）→ `favorites/` 目录 4 个文件

- **HomeOrbitalCalendar.kt 拆分**（627行 → 121行）：轨道罗盘按职责拆为 4 个文件
  - `OrbitalCalendarState.kt` — 状态计算（daySignalMap、todayEvents、upcomingEvents、nextEventCountdown 等）
  - `OrbitalCalendarCanvas.kt` — Canvas 绘制（动画、粒子、日期点、扫描线、十字准星等）
  - `OrbitalCalendarInfo.kt` — 事件列表 + 图例
  - `HomeOrbitalCalendar.kt` — 主入口，组装子组件

- **AddContactViewModel.kt 拆分**（390行 → 351行）：提取 `ContactFormHelper`，封装自定义类型管理、列表字段序列化、生日纪念日同步逻辑

- **AddChatViewModel.kt 拆分**（293行 → 217行）：提取 `DialogueLineManager`，封装对话行增删改移、说话人切换、描述解析与构建等纯函数

- **Dialogs.kt 拆分**（363行 → 75行）：按职责拆为 3 个文件
  - `FormComponents.kt` — 表单/详情组件（FormScreenScaffold、AppDatePicker、SectionHeader 等）
  - `ContactPickerDialog.kt` — 联系人选择对话框
  - `Dialogs.kt` — 仅保留确认/放弃对话框

- **NavGraphs.kt 拆分**（325行 → 78行）：按模块拆为 4 个文件
  - `HomeNavGraph.kt` — 首页 + 占卜子路由
  - `EventsNavGraph.kt` — 事件模块路由
  - `ChatNavGraph.kt` — 对话模块路由
  - `ContactsNavGraph.kt` — 联系人 + 纪念 + 设置路由

- **DateUtils.kt 拆分**（336行 → 122行）：按职责拆为 3 个文件
  - `LunarDateUtils.kt` — 农历相关计算
  - `DateCalcUtils.kt` — 时间差/倒计时/生日计算
  - `DateUtils.kt` — 仅保留格式化/解析

- **Re-export 文件清理**：删除 5 个不必要的 re-export 包装文件（ThoughtsComponents.kt、ThoughtsDialogs.kt、ContactListCard.kt、AddChatComponents.kt、DivinationRecordDetail.kt），调用方改为直接 import 子包函数

### Photo Upload Unification

- 新增 `ui/components/photo/` 统一图片选择组件目录：
  - `PhotoSlotMode.kt` — AVATAR / THUMBNAIL / POLAROID 三种展示模式
  - `PhotoPickerLauncher.kt` — 统一选择器，封装 `PickVisualMedia` + 自动缓存，替代 `OpenDocument` / `GetMultipleContents`
  - `PhotoSlot.kt` — 统一展示组件 + `PhotoAddSlot` + `PhotoSelectionArea`
- 改造 4 个界面统一使用 `rememberPhotoPickerLauncher`：AddContactScreen、AddEventScreen、AddChatScreen、AddGiftScreen
- 移除各 Screen 中重复的 `takePersistableUriPermission` 和 `ImageCacheManager.copyToInternalStorage` 调用

### Database

- **迁移链合并**：30 个增量迁移文件合并为 2 个跳跃迁移，减少迁移代码量和维护成本
  - `MIGRATION_1_24` — v1→v24 早期迁移合并
  - `MIGRATION_24_31` — v24→v31 迁移合并（涵盖 circle_member_cross_ref、favorites 唯一索引、divination_records、anniversaries 外键、events customTypeName）
  - 删除 30 个不再使用的单独迁移文件（Migration_1_2 ~ Migration_30_31）
  - DatabaseModule 从注册 8 个迁移简化为 2 个

### Network

- Retrofit + Kotlin Serialization 已引入，`NetworkModule` 和 `TangApiService` 已存在
- `AiRepositoryImpl.testConnection()` 使用 Retrofit，`streamChat()` 使用裸 OkHttp（SSE 流式响应 Retrofit 不原生支持，保留合理）

### Performance
- 全项目 53 处 `collectAsState` 替换为 `collectAsStateWithLifecycle`，App 后台时停止收集 Flow，节省 CPU 和电量
- HomeViewModel `SharingStarted.Eagerly` 改为 `WhileSubscribed(5000)`，无订阅者时停止上游 Flow
- 轨道罗盘 4 个独立动画协程合并为 1 个，重组次数从约 360 次/秒降至 60 次/秒
- HomeStatsUseCase 11 个 count Flow 添加 `distinctUntilChanged`，仅在值真正变化时触发重组
- Coil 全局 ImageLoader 配置：内存缓存 15% + 磁盘缓存 2% + 300ms 淡入动画
- LazyColumn/LazyRow 补充 key 设置，列表增删时高效 diff

### Refactor
- 拆分 30 个胖 Screen 文件为 Screen + Components + Dialogs + Tables 结构，所有 Screen 主函数行数降至 300 行以内
- 抽取公共 UI 组件：通用 `EmptyState`（替换 10 处重复空状态）、`AppConfirmDialog`、`FormSection`/`FormTextField`
- 补充 Domain 层 UseCase：`DeleteCustomTypeUseCase`、`FilterEventsUseCase`、`FilterContactsUseCase`、`GetContactDisplayInfoUseCase`、`GetAnniversaryDisplayUseCase`、`BackupRestoreUseCase`
- DivinationViewModel 14 个 MutableStateFlow 合并为 1 个 UiState，统一状态管理
- 新增 6 个 UseCase 单元测试 + 更新 6 个现有测试

### Bug Fixes
- 修复检查更新逻辑：当前版本号大于远程版本号时不再提示更新

## [1.2.0] - 2026-06-02

### Added
- 星座标签：根据出生日期自动计算星座，在人物详情头部标签区展示
  - 12 星座各有自定义矢量图标（24dp 视口、2dp 笔触、圆角端点），与应用整体图标风格统一
  - 每个星座拥有专属代表色和淡色背景，简约胶囊风格展示
  - `ZodiacUtils` 工具类：基于公历日期精确计算星座
- 相识天数标签：在亲密度标签旁展示相识时长
  - 基于用户填写的 `knowingDate`（相识日期）计算，而非创建时间
  - 显示格式：超过 1 年显示「X年X天」，不足 1 年显示「X天」

### Bug Fixes
- 修复删除自定义标签后人物详情界面仍显示已删除标签的问题
  - 删除 `CustomType` 时同步清理 `Contact` 表中引用该标签名称的字段
  - 单选字段（relationship/education）批量清空，多选字段（hobby/habit/diet/skill）精确移除
  - `ContactDao` 新增 `clearRelationship()`、`clearEducation()`、`getContactsWithListFieldValue()` 方法
  - `ContactRepository` 新增 `removeRelationshipFromAll()`、`removeEducationFromAll()`、`removeFromListFieldAll()` 方法

## [1.1.1] - 2026-05-22

### Bug Fixes
- 修复从 v1.0.0 升级到 v1.1.0 后频繁闪退：`Migration_29_30` 空实现导致 `anniversaries` 表 `contactId` 列未正确迁移为可空，Room 校验失败。现已使用重建表方式正确执行迁移

## [1.1.0] - 2026-05-22

### Added
- 占卜功能：支持六爻和梅花易数两大传统占卜体系
  - 六爻占卜：基于《增删卜易》方法论，支持手动摇卦输入，自动装卦（纳甲、六亲、六神、世应），AI 断卦解读
  - 梅花易数：基于邵雍《梅花易数》方法论，支持时间起卦、数字起卦、外应起卦三种方式，体用生克分析，AI 深度解读
  - 占卜引擎：`LiuyaoEngine` / `MeihuaEngine` 独立计算引擎，`GanZhiCalculator` 干支历法，`WuXingHelper` 五行生克，`TrigramData` / `HexagramData` / `NaJiaData` / `PalaceData` 完整卦象数据
  - AI 解读：`LiuyaoPromptBuilder` / `MeihuaPromptBuilder` 构建专业 Prompt，通过 SSE 流式输出断卦结果，支持深度追问
  - 占卜历史：`DivinationRecordDao` 持久化存储，历史记录浏览与回顾
  - UI 组件：卦象可视化 `HexagramDisplay`，结果页 `LiuyaoResultScreen` / `MeihuaResultScreen`，AI 深度解读 `AiDeepSection` / `MeihuaAiDeepSection`

### Security
- API Key / BaseUrl / Model 从 DataStore 明文迁移到 EncryptedSharedPreferences 加密存储（P0）
- ZIP 备份恢复添加路径遍历校验，防止恶意 ZIP 覆盖任意文件（P0）
- OkHttp Response body 使用 `execute().use {}` 确保连接关闭，防止泄漏（P0）

### Bug Fixes
- Android 10+ 使用 `Uri.fromFile()` 导致崩溃，改为 `pathString.trim()`（P0）
- `circle_member_cross_ref` 表主键改为复合主键 `(circleId, contactId)`，添加唯一约束和外键级联删除（P1）
- `EventTypes` 常量值从简短字符串改为大写常量名，避免与自定义类型冲突（P1）
- `AnniversaryEntity.contactId` 改为可空类型 `Long?`，支持无联系人的纪念日/节日（P2）
- `AnniversariesViewModel` 使用 `DateUtils.getTodayStart()` 替代 `Calendar.set(HOUR, 0)` 计算当天零点（P2）
- `ReminderReceiver` 通知显示后通过 Hilt EntryPoint 调用 `markReminderCompleted`（P1）
- `BootReceiver` CoroutineScope 泄漏修复：`finally` 中增加 `scope.cancel()`（P2）
- `AboutScreen` `longVersionCode` API 28 兼容性修复（Lint Error）
- 人物详情界面关系标签颜色不随选择改变：`AddContactScreen` 的 `onAddItem` 忽略了 color/icon 参数，导致新建关系标签时颜色未保存到数据库
- 自定义事件类型保存后显示默认类型：`Event`/`EventEntity` 新增 `customTypeName` 字段，自定义类型名称不再被强制转为 `EventType.OTHER`
- AI 配置无法及时保存密钥和测试连接：Flow 改为 `callbackFlow` + `OnSharedPreferenceChangeListener`，直接从 `encryptedPrefs` 实时响应变化
- 足迹界面事件图标未同步自定义事件类型：`FootprintsViewModel` 的 `eventType` 优先使用 `customTypeName`
- 轨道罗盘绿色辐射线没对齐今日指针：月份进度弧终点角度与今日指针对齐
- 轨道罗盘扫描线指针瞬移：`rememberContinuousRotation` 改为基于系统时间的连续角度累加，消除 360°→0° 跳变

### Refactor
- `GiftsViewModel` 图片保存逻辑下沉到 Repository，移除 `@ApplicationContext` 依赖（P1）
- `GiftRepositoryImpl` 删除礼物时清理磁盘图片文件（P1）
- `EventRepositoryImpl` 删除事件时清理磁盘图片文件（P1）
- `BackupRepository` 实现 `BackupRepositoryInterface` 接口，ViewModel 依赖接口而非实现（P1）
- `GiftDao` 新增 `getGiftByIdOnce` / `getGiftsByContactIdOnce` suspend 方法（P1）
- `EventDao` 新增 `getEventByIdOnce` suspend 方法（P1）
- `AnniversaryRepositoryImpl` 提取 `Anniversary.effectiveDate()` 扩展函数消除重复代码（P2）
- `DateUtils` 新增 `getTodayStart()` 方法（P2）
- `DivinationMapper` 从 `object` 改为扩展函数 `toDomain()` / `toEntity()`（P3）
- `FootprintsViewModel` 从手动 `applyFilter()` 重构为 `combine` + `stateIn` 响应式管道（P3）
- `ThoughtsViewModel` 从 11 个 Flow `combine(Array<Any>)` 重构为嵌套 combine + 中间数据类（P3）
- `Contact.gender` 从 `Int` 改为 `Gender` 枚举（UNKNOWN/MALE/FEMALE）（P3）
- `CircleRepositoryImpl` 多个查询方法改用 `getAllCirclesWithMembers` + 内存过滤（P3）
- `HomeViewModel.currentTimeFlow` delay 从 1000ms 调整为 30000ms（P3）
- 删除 `CustomTypeEntity` 中未使用的 `CustomCategory` enum（P3）

### Enhanced
- 轨道罗盘「赛博星图」视觉增强：
  - 粒子环：25 个微光粒子在罗盘外围缓慢旋转，独立颜色/大小/呼吸偏移
  - 脉冲波纹：今日日期点每 3 秒向外扩散一圈渐隐波纹
  - 渐变弧线：月份进度弧双层绘制（底层发光 + 顶层 Green→Sky 渐变）
  - 星座连线：相邻事件日之间用半透明弧线连接
  - 外圈光环：虚线环 + 呼吸发光 + 紫色光晕
  - 渐变扫描尾迹：Sky→Purple→Electric 三段渐变，扇形从 30° 扩展到 45°
  - 中心十字准星：极慢旋转（120s）+ 菱形端点标记

### Database
- Room 数据库版本从 28 升级到 31
- `Migration_28_29`：`circle_member_cross_ref` 表迁移为复合主键
- `Migration_29_30`：`anniversary` 表 `contactId` 列允许 NULL
- `Migration_30_31`：`events` 表新增 `customTypeName TEXT DEFAULT NULL` 列

### Testing
- 更新 9 个单元测试文件适配 P0-P3 变更
- 新增 `GenderTest` 枚举测试
- 新增 `DateUtils.getTodayStart()` 测试
- 更新 4 个 androidTest 文件适配 Entity/DAO 变更
- 全部 452 个单元测试通过

### Build
- `versionCode` 改为语义化格式 `MAJOR * 10000 + MINOR * 100 + PATCH`（10100）
- `versionName` 更新为 `1.1.0`
- 新增 `security-crypto:1.1.0-alpha06` 依赖
- ProGuard 规则新增 EncryptedSharedPreferences 和 OkHttp SSE keep/dontwarn
- Lint 修复：17 个 DefaultLocale、1 个 NewApi、1 个 ObsoleteSdkInt

## [1.0.0] - 2026-05-20

### Added
- Initial release
