# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-05-20

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

### Database
- Room 数据库版本从 28 升级到 30
- `Migration_28_29`：`circle_member_cross_ref` 表迁移为复合主键
- `Migration_29_30`：`anniversary` 表 `contactId` 列允许 NULL

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
