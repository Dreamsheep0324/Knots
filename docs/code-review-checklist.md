# 结绳 (Tang) 代码审查清单

> 审查日期：2026-05-20
> 二次验证日期：2026-05-20（逐项回到源码验证真实性，已去除误判项）

---

## P0 — 必须修复（影响功能 / 安全）

| # | 位置 | 问题 | 修复建议 |
|---|------|------|----------|
| 1 | `PhotoAlbumViewModel` L107 | **`Uri.fromFile(file)` 在 Android 10+ 崩溃**：`targetSdk >= 24` 时抛 `FileUriExposedException`，礼物照片的相册页面直接崩溃 | 改用 `FileProvider` 或直接使用 file path 字符串作为 uri |
| 2 | `BackupRepository.writeFileFromZip` L192 | **ZIP 路径遍历漏洞**：`entry.name` 未校验 `..`，恶意 ZIP 可写入 `../../shared_prefs/` 等任意路径 | 在 `writeFileFromZip` 中校验 `fileName.contains("..")` 时拒绝写入 |
| 3 | `AiRepositoryImpl.streamChat` L57-94 | **OkHttp Response body 未关闭**：`execute()` 返回的 response 没有 `use {}` 包裹，异常时连接泄漏 | 将 `val response = okHttpClient.newCall(...).execute()` 改为 `okHttpClient.newCall(...).execute().use { response -> ... }` |
| 4 | `SettingsRepositoryImpl` | **API Key 明文存储在 DataStore**：root 设备可直接读取 | 改用 `EncryptedSharedPreferences` 或 Android Keystore 加密存储 |

---

## P1 — 建议修复（数据一致性 / 架构整洁）

| # | 位置 | 问题 | 修复建议 |
|---|------|------|----------|
| 5 | `CircleMemberCrossRef` | **缺少 `(circleId, contactId)` 唯一索引**：`insertMemberCrossRef` 用 `OnConflictStrategy.IGNORE` 只忽略主键冲突，不忽略 (circleId, contactId) 重复。并发或绕过 ViewModel 直接调 DAO 时会出现重复成员 | 添加 `indices = [Index("circleId"), Index("contactId"), Index(value = ["circleId", "contactId"], unique = true)]`，并将主键改为复合主键或去掉自增 id |
| 6 | `ReminderReceiver.onReceive` | **通知显示后未标记提醒为已完成**：UI 中该提醒仍显示为"活跃"状态，用户困惑。AlarmManager 一次性 alarm 不会重复触发，所以不会重复通知，但数据库状态不一致 | 在 `onReceive` 中通过 Hilt EntryPoint 获取 ReminderDao，调用 `markReminderCompleted(reminderId)` |
| 7 | `GiftsViewModel` | **ViewModel 直接依赖 `@ApplicationContext`**：图片保存逻辑 (`ImageCacheManager.copyToInternalStorage`) 在 ViewModel 中执行，违反分层原则，且难以测试 | 将图片保存逻辑下沉到 `GiftRepository`，通过 Hilt 注入 Context 到 Repository |
| 8 | `BackupRepository` | **没有接口**：项目其他 15 个 Repository 都有接口，唯独这个没有，破坏一致性且 `BackupViewModel` 无法在测试中 mock | 抽取 `BackupRepositoryInterface`，`BackupViewModel` 依赖接口而非实现 |
| 9 | `SourceTypes` | **命名不一致**：`EVENT = "EVENT"` 全大写 vs `ALBUM_EVENT = "event"` 小写。两者用于不同场景不会交叉匹配，但维护者容易混淆，未来新增类型时可能出错 | 统一为全大写+下划线：`ALBUM_EVENT = "ALBUM_EVENT"`，同步修改 `PhotoAlbumViewModel` 中的引用 |
| 10 | `ImageCacheManager` | **无孤立图片清理机制**：删除实体后图片仍残留在磁盘，长期使用后占用存储空间 | 在 `ContactRepositoryImpl.deleteContact`、`EventRepositoryImpl.deleteEvent`、`GiftRepositoryImpl.deleteGiftById` 中添加图片删除逻辑 |

---

## P2 — 架构债务（当前可用 / 未来风险）

| # | 位置 | 问题 | 说明 |
|---|------|------|------|
| 11 | `AnniversaryRepositoryImpl.getUpcomingAnniversaries` | **全表加载 + 内存过滤**：取出全部纪念日后在内存中计算下次日期并过滤 | 个人 App 数据量 <500 条时无感。但"下次日期"涉及农历转换无法用 SQL 表达，当前方案是合理的折中。数据量增长后需重新评估 |
| 12 | `EventRepositoryImpl.getPhotoCount` / `GiftRepositoryImpl.getGiftPhotoCount` | **每次 Flow 发射都解析全部 JSON 计数**：`getAllPhotosRaw()` 返回 photos 列 JSON，逐条 `JSONArray.length()` | 百级数据量下 <5ms。添加冗余 `photo_count` 字段需维护一致性，当前收益不大 |
| 13 | `GiftsViewModel.addGift` | **图片保存失败静默忽略**：`ImageCacheManager.copyToInternalStorage` 返回 null 时被跳过，用户无感知 | 真实 UX 问题。应在保存后检查是否有失败，向用户提示 |
| 14 | `AnniversaryEntity.contactId` 非空 | **节日类型纪念日不一定关联联系人**：`contactId` 是非空 Long，节日类只能传 0，外键不会匹配但关联查询返回 null contact | 功能可用但不优雅。长期应将 `contactId` 改为可空 |
| 15 | `Anniversary` 领域模型包含 `contactName`/`contactAvatar` | **UI 展示字段混入领域模型**：`toEntity()` 忽略这两个字段，`toDomain()` 从关联查询填充 | 不影响数据一致性，但违反领域模型纯净原则。长期应创建 `AnniversaryUiModel` |
| 16 | `AnniversariesViewModel` vs `AnniversaryRepositoryImpl` | **时间取值方式不一致**：Repository 用 `System.currentTimeMillis()` 精确到毫秒，ViewModel 用 `Calendar.set(HOUR, 0)` 取当天零点。跨午夜时可能导致"即将到来"/"已过期"判断不一致 | 边界情况，概率极低。统一为一处取值即可 |
| 17 | `BootReceiver.rescheduleReminders` | **CoroutineScope 泄漏**：`CoroutineScope(Dispatchers.IO + SupervisorJob())` 不受生命周期管理，虽然 `goAsync()` 限制了总时间，但 Scope 永远不会被 cancel | 改为使用 `pendingResult` 作为生命周期锚点，在 finally 中 cancel scope |

---

## P3 — 代码质量改善（锦上添花）

| # | 位置 | 问题 | 建议 |
|---|------|------|------|
| 18 | `CircleRepositoryImpl.getCirclesForContact` / `getRootCircles` | **N+1 查询**：对每个 Circle 额外调用 `getMemberIdsForCircleOnce`。`getAllCircles` 用的是 `getAllCirclesWithMembers` 一次查询，但这两个方法没有 | 改用 `getAllCirclesWithMembers` + 内存过滤，或在 DAO 中添加对应的 WithMembers 查询 |
| 19 | `HomeViewModel.currentTimeFlow` | **每秒发射**：导致使用该 Flow 的 Composable 每秒重组 | 增大间隔到 30 秒或 1 分钟，或仅在可见时更新 |
| 20 | `DivinationMapper` | **风格不统一**：10 个 Mapper 用扩展函数，1 个用 object 单例 | 统一为扩展函数风格 |
| 21 | `PhotoAlbumViewModel` L107 | **`File.exists()` 在 Flow 管道中**：虽然 Room Flow 在后台线程发射不会 ANR，但 ViewModel 不应关心文件系统 | 将文件存在性检查移到 Repository 层 |
| 22 | `CustomCategories` (Domain) vs `CustomCategory` (Entity) | **两处重复定义**：Entity 层枚举和 Domain 层 object 内容不同步 | 统一到 Domain 层定义，Entity 层引用 Domain 的常量 |
| 23 | `FootprintsViewModel` | **手动过滤模式**：使用实例变量 + `applyFilter()` 而非响应式 Flow 管道，与项目其他 ViewModel 风格不一致 | 改用 `combine` + `stateIn` 响应式管道 |
| 24 | `ThoughtsViewModel` | **combine 11 个 Flow 用 `Array<Any>`**：类型不安全，需要手动类型转换 | 改用嵌套 combine 或 data class 封装中间结果 |
| 25 | `ContactEntity.gender` | **魔法数字**：`gender: Int = 0`，0/1/2 含义不明 | 添加 `Gender` 枚举（MALE/FEMALE/OTHER） |

---

## 不建议执行的优化

以下项目经验证后判定为**过度设计或投入产出比极低**，不建议执行：

| # | 建议 | 不做的理由 |
|---|------|------------|
| — | 纪念日 DAO 层 SQL 过滤 | "下次日期"涉及农历转换，无法用 SQL 表达，内存过滤是唯一可行方案 |
| — | photo_count 冗余字段 | 需在每次增删改时维护，增加复杂度和出错概率，百级数据量下 JSON 解析 <1ms |
| — | ContactRepository 精简 7 个 DAO 依赖 | 级联删除必需，改 ForeignKey.CASCADE 需改所有关联表，风险大收益小 |
| — | Event 拆分为多表 | 过度设计，type + 可空字段在个人 App 规模下完全够用 |
| — | Contact 拆分核心/详细模型 | 30 字段 data class 在 Kotlin 中很常见，拆分增加映射复杂度 |
| — | 抽取更多 UseCase | 业务逻辑简单，强行抽取只增加文件数 |
| — | ViewModel 基类抽取 | 各 ViewModel 初始化差异大，强行统一增加理解成本 |
| — | NavGraphs 拆分 | 文件大但结构清晰，拆分后需多文件跳转降低可读性 |
| — | 合并 Room 迁移脚本 | 已发布迁移不可修改，合并可能导致已升级用户数据丢失 |
| — | DateUtils Locale 问题 | `Locale.getDefault()` 在 App 生命周期内几乎不变 |
| — | HomeViewModel combine 数量优化 | `distinctUntilChanged` 已保证低发射频率，combine 开销极低 |
| — | ContactListViewModel 改 stateIn | `collect {}` 在 viewModelScope 中运行，销毁时自动取消，无实际问题 |
