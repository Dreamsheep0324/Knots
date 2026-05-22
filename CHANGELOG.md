# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
