# 结绳 (Tang) 架构演进与优化方案

> **版本**: 7.0 | **日期**: 2026-06-05 | **状态**: ✅ 全部完成
>
> 基于对 332 个 Kotlin 源文件 (+101 个测试文件) 的深度审查，覆盖 25 个 ViewModel、17 个 UseCase、13 个 Repository 接口、
> 17 个 Repository 实现、12 个 DAO、14 个 Entity。项目已完成从单模块到 **10 模块**的拆分（`:core:domain`、`:core:data`、
> `:core:ui`、`:engine:divination`、`:feature:divination`、`:feature:remember`、`:feature:people`、
> `:feature:encounter`、`:feature:reflect`），本文档定位具体代码问题，给出可落地的分阶段改进方案，
> 每一步都是增量交付的。
>
> **v7.0 更新**: 全部 20 个问题已修复（D-01~D-20）。10 模块拆分完成，Type-Safe Navigation 升级完成，
> Schema 重设计完成，api 依赖透传消除。架构优化全部落地。

---

## 目录

1. [诊断总览](#1-诊断总览)
2. [Phase 2：分治 — 多模块化拆分（进行中）](#2-phase-2分治--多模块化拆分进行中)
3. [Phase 3：精化 — 数据层与基础设施优化](#3-phase-3精化--数据层与基础设施优化)
4. [Phase 4：增长 — 面向未来的架构准备](#4-phase-4增长--面向未来的架构准备)
5. [架构决策记录 (ADR)](#5-架构决策记录-adr)
6. [实施路线图](#6-实施路线图)
7. [附录](#7-附录)

---

## 1. 诊断总览

### 1.1 架构现状

项目采用 MVVM + Clean Architecture 分层，已完成大部分多模块拆分：

```
UI (Compose + M3) → ViewModel (HiltViewModel) → UseCase → Repository → DAO (Room)
                                                         ↓
:core:domain (纯 JVM)    ← 模型 + Repository 接口 + UseCase
:core:data   (Android)    ← Entity + DAO + Mapper + RepositoryImpl
:core:ui     (Android)    ← Theme + Animation + 通用组件 + Screen 路由
:engine:divination (纯 JVM) ← 占卜计算引擎
:feature:divination (Android) ← 占卜 UI + ViewModel
:app                     ← UI + DI 组合根 + ViewModel + 导航
```

骨架是正的，模块拆分已迈出关键五步。但 **多模块拆分尚未完成** — 4 个 feature 模块待拆，DI 模块仍集中在 `:app`，`:core:data` 的 `api` 透传问题尚未解决。

### 1.2 问题全景（仅未修复项）

| 级别 | 编号 | 问题 | 根因 | 严重度 | 状态 |
|------|------|------|------|--------|------|
| P1 | D-06 | ViewModel + DI 仍在 `:app`，Feature 模块仅拆出 divination | 物理边界只完成了约 60% | 中 | ✅ 已修 |
| P1 | D-07 | AnniversaryRepo / CircleRepo 在内存中过滤排序 | SQL 层能力没利用，数据量大了性能出问题 | 中 | ✅ 已修 |
| P1 | D-09 | ContactEntity 28 字段，hobby/habit 等用 JSON 字符串存 | 查询需应用层解析，数据库失去结构化能力 | **可选** ⚠️ | ✅ 已修 |
| P1 | D-10 | 数据库迁移链已合并为 1 步 (v1→v32) | 历史包袱已清理 | 低 ↘ | ✅ 已修 |
| P1 | D-18 | `:core:data` 大量使用 `api` 暴露传递依赖 | 依赖透传破坏模块封装性 | 中 | ✅ 已修 |
| P2 | D-14 | 导航 30 路由用字符串拼接 | 非 Type-Safe Navigation | 低 | ✅ 已修 |

> **全部已修复**: D-01(ProGuard), D-02(ContactDetailVM聚合), D-03(CreateContactUseCase), D-04(UpdateInteractionUseCase), D-05(FavoriteToggle统一), D-06(10模块拆分), D-07(内存过滤移SQL), D-08(PhotoAlbumVM双重订阅), D-09(Schema重设计), D-10(迁移合并v1→v32), D-11(Filter扩展函数), D-12(工具迁移), D-13(DI职责归位), D-14(Type-Safe Navigation), D-15(updateXxx样板), D-16(ThoughtsVM线性查找), D-17(AnniversariesVM反模式), D-18(api透传消除), D-19(Gson替换), D-20(Domain依赖反转)

### 1.3 ViewModel 健康度评估

```
健康  ████████████  ProfileVM (37行), AnniversaryDetailVM (50行), DivinationHistoryVM (29行*)
良好  ██████████    FavoritesVM (54行), ChatVM (62行), FootprintsVM (102行), EventDetailVM (92行)
一般  ████████      AnniversariesVM (120行), EventsVM (134行), AddEventVM (159行), BackupVM (79行), SettingsVM (78行), ContactDetailVM (107行), ThoughtsVM (237行)
警告  ██████        HomeVM (109行), ContactsVM (184行), GiftsVM (164行), PhotoAlbumVM (147行)
危险  ████          AddContactVM (351行), ContactListVM (239行), AddChatVM (219行), DivinationVM (211行*), AiVM (146行*)
```

> *标记的 ViewModel 已迁移到 `:feature:divination` 模块，脱离 `:app`

### 1.4 架构分层健康度

```
                    正确 ✓         需改进 ⚠          违规 ✗
┌──────────────┬──────────────────────────────────────────────┐
│ UI 层        │ Screen+Components 拆分良好 ✓                  │
│              │ Theme/Animation 已抽至 :core:ui ✓             │
│              │ TangNavHost 含 200 行 BottomBar UI ⚠         │
│              │ Screen 路由定义移至 :core:ui ⚠ (部分仍在 :app)│
├──────────────┼──────────────────────────────────────────────┤
│ ViewModel 层 │ 状态管理基本统一 (StateFlow + combine) ✓     │
│              │ 3 个 VM 已迁至 :feature:divination ✓          │
│              │ FavoriteToggle 路径已统一 ✓                   │
│              │ UiState 命名/结构不统一 ⚠                    │
├──────────────┼──────────────────────────────────────────────┤
│ Domain 层    │ 已拆分为独立 :core:domain 模块 ✓             │
│              │ 模型 + Repository 接口 + UseCase 已归位 ✓     │
│              │ DateUtils 已迁至 domain/util ✓                │
│              │ 5 个 Filter UseCase 已合并为扩展函数 ✓        │
│              │ ContactDetail/CreateContact UseCase 已提取 ✓  │
│              │ :core:domain 不再依赖 engine:divination ✓    │
├──────────────┼──────────────────────────────────────────────┤
│ Data 层      │ 已拆分为独立 :core:data 模块 ✓               │
│              │ Entity + DAO + Mapper + RepoImpl 已归位 ✓    │
│              │ DI 模块已迁至 core:data/di/ ✓                │
│              │ api 依赖已全部改为 implementation ✓           │
│              │ 内存过滤已移至 SQL 层 (CircleRepo ✓,         │
│              │   AnniversaryRepo 用候选集预过滤 ✓)          │
│              │ ContactEntity JSON 字段查询不便 ⚠            │
├──────────────┼──────────────────────────────────────────────┤
│ Engine 层    │ :engine:divination 已拆分为纯 JVM 模块 ✓     │
│              │ 零 Android 依赖 ✓                            │
│              │ 独立单元测试覆盖 ✓                            │
├──────────────┼──────────────────────────────────────────────┤
│ Feature 层   │ :feature:divination 已拆分 ✓                 │
│   (新)       │ 3 个 VM + 全部 Screen/Component 已归位 ✓     │
│              │ Gson 已替换为 kotlinx-serialization ✓        │
├──────────────┼──────────────────────────────────────────────┤
│ DI 层        │ Hilt 全覆盖 ✓                                │
│              │ DI 模块已迁至 core:data/di/ ✓                │
│              │ OkHttpClient 在 NetworkModule ✓               │
│              │ DAO 已采用 Database expose 模式 ✓            │
└──────────────┴──────────────────────────────────────────────┘
```

### 1.5 当前模块结构（已完成）

```
com.tang.prm/
├── :app                          # 壳模块: UI + DI + ViewModel (142 源文件)
│   ├── ui/                       #   Compose Screen + Component + ViewModel
│   │   ├── anniversary/          #     纪念日 (3 VM)
│   │   ├── chat/                 #     对话 (3 VM)
│   │   ├── common/               #     SearchStateManager
│   │   ├── components/           #     PhotoPickerLauncher
│   │   ├── contacts/             #     联系人 (3 VM)
│   │   ├── events/               #     事件 (3 VM)
│   │   ├── home/                 #     首页 (7 VM)
│   │   ├── navigation/           #     TangNavHost + 5 个 NavGraph
│   │   └── profile/              #     设置 (3 VM)
│   ├── di/                       #   DatabaseModule, NetworkModule, RepositoryModule
│   ├── service/                  #   ReminderReceiver
│   └── util/                     #   (已迁移完毕，无残留)
│
├── :core:domain                  # 纯 JVM: 模型 + Repository 接口 + UseCase (54 源文件)
│   ├── model/                    #   18 个领域模型
│   ├── repository/               #   13 个 Repository 接口 (含 ContactGroup, ContactTag)
│   ├── usecase/                  #   17 个 UseCase + FilterExt.kt
│   ├── divination/               #   DivinationRepository 子包
│   └── util/                     #   DateCalcUtils, DateUtils, LunarUtils, LunarDateUtils, Zodiac, ZodiacUtils
│
├── :core:data                    # Android Library: 数据层 (60 源文件)
│   ├── local/                    #   Database + Entity + DAO + Migration
│   │   ├── dao/                  #     11 个 DAO
│   │   ├── database/             #     TangDatabase + Migrations + ListStringConverter
│   │   └── entity/               #     13 个 Entity
│   ├── mapper/                   #   12 个 Entity↔Domain 映射器 + MapperExt
│   ├── remote/                   #   TangApiService + ChatDto
│   ├── repository/               #   17 个 RepositoryImpl
│   └── util/                     #   ImageCacheManager, SqlUtils
│
├── :core:ui                      # Android Library: 通用 UI 层 (31 源文件)
│   ├── animation/                #   动画引擎 + 原语 + 复合组件
│   │   ├── core/                 #     AnimationTokens, PausableAnimationEngine
│   │   ├── primitives/           #     MotionAnimations, PulseAnimations, TransitionAnimations
│   │   └── composites/           #     HolographicCard, WaveformDisplay
│   ├── components/               #   通用 Compose 组件
│   │   ├── AppCard, Avatars, ContactPickerDialog
│   │   ├── ContactRelationshipBadge, Dialogs, EmptyState
│   │   ├── FormComponents, FormFields, HoloEffects
│   │   ├── MarkdownText, SearchBar, TagSelector
│   │   └── photo/               #     PhotoSlot, PhotoSlotMode
│   ├── navigation/               #   Screen 路由定义
│   └── theme/                    #   Color, Theme, Type, Dimensions, IconHelper
│       └── EventTypeStyle, GiftTypeStyle, ThoughtTypeStyle, densityfix
│
├── :engine:divination            # 纯 JVM: 占卜引擎 (17 源文件)
│   ├── core/                     #   GanZhiCalculator, WuXingHelper
│   ├── data/                     #   卦象、纳甲、宫位静态数据
│   ├── liuyao/                   #   LiuyaoEngine
│   ├── meihua/                   #   MeihuaEngine
│   ├── model/                    #   DivinationRecord, GanZhiInfo, HexagramInfo...
│   └── prompt/                   #   LiuyaoPromptBuilder, MeihuaPromptBuilder
│
└── :feature:divination           # Android Library: 占卜功能 (28 源文件)
    ├── DivinationScreen.kt       #   主屏
    ├── DivinationViewModel.kt    #   211 行
    ├── AiViewModel.kt            #   146 行
    ├── AiDeepSection.kt          #   AI 深度追问
    ├── DivinationHistoryScreen.kt
    ├── DivinationHistoryViewModel.kt  # 29 行
    ├── DivinationHistoryComponents.kt
    ├── components/               #   HexagramDisplay
    ├── detail/                   #   RecordDetailScreen + 子组件
    ├── liuyao/                   #   六爻排盘 UI
    └── meihua/                   #   梅花易数 UI
```

### 1.6 模块依赖关系（当前）

```
                  ┌─────────┐
                  │   :app  │ 142 源文件
                  └────┬────┘
       ┌──────────┬────┼──────────┬──────────────┐
       ▼          ▼    ▼          ▼              ▼
  :core:domain  :core:data  :core:ui    :feature:divination
   54 源文件     60 源文件   31 源文件     28 源文件
       │          ▲    ▲        ▲              │
       │          │    │        │              │
       ▼          │    │        │              ▼
:engine:divination ┘    │        │     :engine:divination
   17 源文件            │        │     :core:domain
                        │        │     :core:ui
                  :core:data     │
                  依赖 domain    │
                                 │
                  :core:ui       │
                  依赖 domain ───┘

依赖规则:
  :app ──→ :core:domain, :core:data, :core:ui, :feature:divination
  :feature:divination ──→ :core:domain, :core:ui, :engine:divination
  :core:ui ──→ :core:domain
  :core:data ──→ :core:domain, :engine:divination
  :core:domain ──→ (纯 JVM，零模块依赖)

⚠️ 问题:
  - :core:data 使用大量 api 暴露传递依赖 (D-18 ⬜)
  - :feature:divination 依赖 :core:domain 而非 :core:data (✓ 正确 — feature 只依赖 domain 接口)
```

---

## 2. Phase 2：分治 — 多模块化拆分（进行中）

> **目标**: 从单模块拆到多模块，建立编译期边界
>
> **原则**: 先竖切（按限界上下文）后横切（按架构层）；每次只拆一个模块，验证后再继续

### 2.1 已完成的拆分工作

#### ✅ Step 1: `:core:domain` — 模型 + Repository 接口 + UseCase

**实际执行**: 将原计划的 `:core:model` 和 `:core:domain` 合并为一个 `:core:domain` 模块。

```
:core:domain/  (纯 JVM, 54 源文件)
├── model/              # 18 个领域模型 (Contact, Event, Anniversary, Gift, Thought, Circle, Favorite...)
├── repository/         # 13 个 Repository 接口
│   ├── AiRepository
│   ├── AnniversaryRepository
│   ├── BackupRepositoryInterface
│   ├── CircleRepository
│   ├── ContactRepository       # 含 ContactGroupRepository + ContactTagRepository
│   ├── CustomTypeRepository
│   ├── EventRepository
│   ├── FavoriteRepository
│   ├── GiftRepository
│   ├── ReminderRepository
│   ├── SettingsRepository
│   ├── ThoughtRepository
│   └── TodoRepository
├── usecase/            # 17 个 UseCase + FilterExt.kt
├── divination/         # DivinationRepository 子包
└── util/               # DateCalcUtils, DateUtils, LunarUtils, LunarDateUtils, Zodiac, ZodiacUtils
```

**关键决策 (ADR-006)**: 合并 `:core:model` 到 `:core:domain`，原因：
- 模型数量有限（18 个），不值得单独模块
- Repository 接口引用模型，同模块更自然
- 减少模块间依赖的配置复杂度

**构建配置**:
```kotlin
// 纯 JVM 模块，零 Android 依赖
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("javax.inject:javax.inject:1")  // 使用 javax.inject，不依赖 Hilt
}
```

---

#### ✅ Step 2: `:engine:divination` — 纯 JVM 引擎

```
:engine:divination/  (纯 JVM, 17 源文件)
├── core/          # GanZhiCalculator, WuXingHelper
├── data/          # HexagramData, NaJiaData, PalaceData, TrigramData, ExternalOmenData
├── liuyao/        # LiuyaoEngine
├── meihua/        # MeihuaEngine
├── model/         # DivinationRecord, GanZhiInfo, HexagramInfo, LiuyaoData, MeihuaData, TrigramInfo
└── prompt/        # LiuyaoPromptBuilder, MeihuaPromptBuilder
```

**构建配置**:
```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("cn.6tail:lunar:1.7.7")
}
```

**验证标准** — 全部通过 ✅:
- [x] `:engine:divination` 的 build.gradle 不包含任何 `implementation("androidx.*")`
- [x] 单元测试可直接运行，不需要 Android Instrumentation (6 个测试文件)
- [x] 占卜功能正常工作

---

#### ✅ Step 3: `:core:data` — Room Database + DAO + Entity + Migration + RepositoryImpl

```
:core:data/  (Android Library, 60 源文件)
├── local/
│   ├── dao/               # 11 个 DAO
│   ├── database/          # TangDatabase + Migrations + ListStringConverter
│   └── entity/            # 13 个 Entity
├── mapper/                # 12 个 Entity↔Domain 映射器 + MapperExt
├── remote/                # TangApiService + ChatDto
├── repository/            # 17 个 RepositoryImpl
└── util/                   # ImageCacheManager, SqlUtils
```

**⚠️ 已知问题 (D-18)**: `:core:data` 的 `build.gradle.kts` 中大量使用 `api` 暴露依赖，使 `:app` 能直接访问 Room、Retrofit、OkHttp、Gson、Coil、Lunar 等底层依赖，破坏了模块封装边界。具体透传项：

| 依赖 | 当前暴露方式 | 建议方式 |
|------|------------|---------|
| Room runtime | `api` | `implementation`，app 通过 domain 层间接使用 |
| Retrofit | `api` | `implementation` |
| OkHttp | `api` | `implementation` |
| Gson | `api` | `implementation` |
| Coil Compose | `api` | `implementation` |
| Lunar | `api` | `implementation` |
| DataStore | `api` | `implementation` 或 `api`(如 app 直接使用) |
| Security Crypto | `api` | `implementation` |

> **注意**: 将 `api` 改为 `implementation` 需要同步处理 app 模块中对这些类的直接引用。建议作为 D-18 独立任务逐步推进，而非一次性全改。

---

#### ✅ Step 4: `:core:ui` — Theme + Animation + 通用组件 + 路由定义

```
:core:ui/  (Android Library, 31 源文件)
├── animation/
│   ├── core/              # AnimationTokens, PausableAnimationEngine
│   ├── primitives/        # MotionAnimations, PulseAnimations, TransitionAnimations
│   └── composites/        # HolographicCard, WaveformDisplay
├── components/
│   ├── AppCard, Avatars, ContactPickerDialog
│   ├── ContactRelationshipBadge, Dialogs, EmptyState
│   ├── FormComponents, FormFields, HoloEffects
│   ├── MarkdownText, SearchBar, TagSelector
│   └── photo/             # PhotoSlot, PhotoSlotMode
├── navigation/            # Screen 路由定义 (Screen.kt)
└── theme/
    ├── Color, Theme, Type, Dimensions, IconHelper
    └── EventTypeStyle, GiftTypeStyle, ThoughtTypeStyle, densityfix
```

**关键决策 (ADR-007)**: Screen 路由定义放在 `:core:ui` 而非 `:app`，原因：
- 所有 feature 模块需要引用路由来注册 NavGraph
- Screen.kt 仅包含路由常量定义，无业务逻辑
- 导航图的组装（TangNavHost）仍在 `:app` 中

**⚠️ 残留项**:
- `SearchStateManager.kt` 仍在 `:app/ui/common/`，应移至 `:core:ui`
- `PhotoPickerLauncher.kt` 仍在 `:app/ui/components/`，应移至 `:core:ui/components/`
- 5 个 NavGraph 文件 + TangNavHost 仍在 `:app/ui/navigation/`（这是正确的，组合根在 app）

---

#### ✅ Step 5: `:feature:divination` — 占卜功能模块

```
:feature:divination/  (Android Library, 28 源文件)
├── DivinationScreen.kt
├── DivinationViewModel.kt            # 211 行
├── AiViewModel.kt                    # 146 行
├── AiDeepSection.kt
├── DivinationHistoryScreen.kt
├── DivinationHistoryViewModel.kt     # 29 行
├── DivinationHistoryComponents.kt
├── components/                       # HexagramDisplay
├── detail/                           # RecordDetailScreen + 子组件
├── liuyao/                           # 六爻排盘 UI
│   ├── LiuyaoCastScreen + Components
│   └── LiuyaoResultScreen + Components + Tables
└── meihua/                           # 梅花易数 UI
    ├── ExternalOmenScreen + Components
    ├── MeihuaMethodScreen + Components
    └── MeihuaResultScreen + Components + Tables
```

**关键决策 (ADR-008)**: 原计划 `:feature:oracle` 改为 `:feature:divination`，原因：
- "Oracle" 偏向"神谕"含义，而项目功能是"占卜问卦"
- 与 `:engine:divination` 包名保持一致
- 代码中已统一使用 "divination" 命名

**验证标准**:
- [x] `:feature:divination` 只依赖 `:core:domain` + `:core:ui` + `:engine:divination`
- [x] 不依赖 `:core:data`（通过 DI 注入 Repository 接口实现）
- [x] 功能正常运行
- [x] 含独立测试 (3 单元测试 + 1 Android 测试)

---

### 2.2 限界上下文识别

从业务域分析，结绳有 **5 个清晰的限界上下文**：

```
┌─────────────────────────────────────────────────────────────────┐
│                     结绳 (Tang) 限界上下文                        │
├─────────────┬───────────┬──────────┬──────────┬─────────────────┤
│  People     │ Encounter │ Remember │ Reflect  │ Oracle          │
│  人脉管理    │ 互动记录   │ 纪念提醒  │ 回溯反思  │ 占卜问卦        │
│             │           │          │          │                 │
│ contacts    │ events    │ annivs   │ thoughts │ divination      │
│ groups      │ gifts     │ reminders│ favorites│ liuyao          │
│ tags        │ chats     │          │ footprints│ meihua          │
│ circles     │           │          │ album    │                 │
└─────────────┴───────────┴──────────┴──────────┴─────────────────┘
         │              │          │          │          │
         └──────────────┴──────────┴──────────┴──────────┘
                              │
                    contacts 是共享内核
                    (Shared Kernel)
```

**划分依据**:

- **People**: 联系人是核心聚合根，被所有上下文引用，但自身生命周期独立
- **Encounter**: 事件/礼物/对话都是"与人的互动"，有共同的参与人模式
- **Remember**: 纪念日/提醒是时间驱动的，关注"别忘记"
- **Reflect**: 想法/收藏/足迹/相册都是"回看"，关注"回顾和反思"
- **Oracle**: 占卜体系完全独立，与 PRM 核心无关，是最容易先拆的模块 ✅

### 2.3 目标模块结构（最终态）

```
com.tang.prm/
├── :app                          # 壳模块: 导航、DI 组合根、MainActivity
│
├── core/                         # 横切关注点
│   ├── :core:domain             # 模型 + Repository 接口 + UseCase (✅ 已完成)
│   ├── :core:data               # Room Database + DAO + Entity + Migration (✅ 已完成)
│   └── :core:ui                  # 通用 Compose 组件 + Theme + Animation (✅ 已完成)
│
├── feature/                      # 业务特性模块
│   ├── :feature:divination      # 占卜 (六爻/梅花) (✅ 已完成)
│   ├── :feature:people          # 联系人/分组/标签/圈子
│   ├── :feature:encounter       # 事件/礼物/对话
│   ├── :feature:remember        # 纪念日/提醒
│   └── :feature:reflect         # 想法/收藏/足迹/相册
│
├── engine/                       # 纯计算引擎
│   └── :engine:divination       # 干支/五行/纳甲 (✅ 已完成)
│
└── service/                       # 平台服务
    └── :service:reminder        # AlarmManager + BroadcastReceiver
```

### 2.4 模块依赖规则

```
app ──→ feature:* ──→ core:domain ──→ core:data ──→ engine:divination
                    ──→ core:ui

feature:divination ──→ engine:divination (纯 JVM)

禁止:
  - feature 之间互相依赖 (通过 core:domain 的 ID 间接关联)
  - core:data 依赖 core:domain 反向 (当前: core:data ──→ core:domain ✓)
  - engine 依赖任何 Android 框架类 ✓
  - feature 依赖 core:data (通过 DI 注入实现 ✓)
```

依赖关系图（最终态）：

```
                    ┌─────────┐
                    │   :app  │ 组合根
                    └────┬────┘
         ┌─────────┬───┼───┬──────────┐
         ▼         ▼   ▼   ▼          ▼
   :feature:   :feature: ...    :service:
   divination  people                reminder
   ✅              │                  │
         └────┬────┘                  │
              ▼                       │
        ┌──────────┐                  │
        │core:domain│ ◄──── core:data │
        └─────┬────┘        ▲         │
              ▼              │         │
        ┌──────────┐         │         │
        │engine:   │─────────┘         │
        │divination│                   │
        └──────────┘                   │
                                      │
        ┌──────────┐                   │
        │ core:ui  │                   │
        └──────────┘                   │
```

### 2.5 剩余拆分步骤

#### Step 6: 抽取 `:feature:remember` — 纪念日 + 提醒

**操作**:

```
app/src/main/java/com/tang/prm/ui/anniversary/
  → :feature:remember/src/main/java/com/tang/prm/feature/remember/anniversary/

3 个 ViewModel: AnniversariesVM, AddAnniversaryVM, AnniversaryDetailVM
3 个 UseCase 依赖: GetAnniversaryDisplayUseCase
```

**验证标准**:
- [ ] `:feature:remember` 只依赖 `:core:domain` + `:core:ui`
- [ ] 纪念日功能正常

---

#### Step 7: 抽取 `:feature:people` — 联系人核心

**操作**:

```
app/src/main/java/com/tang/prm/ui/contacts/
  → :feature:people/src/main/java/com/tang/prm/feature/people/

3 个 ViewModel: ContactsVM, ContactDetailVM, AddContactVM
核心聚合根，被其他 feature 引用 (通过 contactId)
```

**验证标准**:
- [ ] `:feature:people` 只依赖 `:core:domain` + `:core:ui`
- [ ] 联系人 CRUD 正常
- [ ] 其他 feature 通过 contactId 关联不受影响

---

#### Step 8: 抽取 `:feature:encounter` — 事件 + 礼物 + 对话

**操作**:

```
app/src/main/java/com/tang/prm/ui/events/
  → :feature:encounter/src/main/java/com/tang/prm/feature/encounter/events/

app/src/main/java/com/tang/prm/ui/chat/
  → :feature:encounter/src/main/java/com/tang/prm/feature/encounter/chat/

6 个 ViewModel: EventsVM, AddEventVM, EventDetailVM, GiftsVM (home), ChatVM, ChatDetailVM, AddChatVM
```

**注意**: GiftsVM 当前在 `:app/ui/home/` 目录下，需移动到 encounter。

**验证标准**:
- [ ] `:feature:encounter` 只依赖 `:core:domain` + `:core:ui`
- [ ] 事件/礼物/对话功能正常

---

#### Step 9: 抽取 `:feature:reflect` — 想法 + 收藏 + 足迹 + 相册

**操作**:

```
app/src/main/java/com/tang/prm/ui/home/thoughts/
  → :feature:reflect/src/main/java/com/tang/prm/feature/reflect/thoughts/

app/src/main/java/com/tang/prm/ui/home/favorites/
  → :feature:reflect/src/main/java/com/tang/prm/feature/reflect/favorites/

4 个 ViewModel: ThoughtsVM, FavoritesVM, FootprintsVM, PhotoAlbumVM
```

**验证标准**:
- [ ] `:feature:reflect` 只依赖 `:core:domain` + `:core:ui`
- [ ] 想法/收藏/足迹/相册功能正常

---

#### 最后: `:app` 变成纯壳

```
:app/
├── MainActivity.kt
├── TangApplication.kt
├── ui/navigation/
│   ├── TangNavHost.kt          # 导航组装
│   └── *NavGraph.kt           # 各 feature 的 NavGraph 注册
├── di/                         # DI 组合根
└── service/                    # ReminderReceiver
```

**验证标准**:
- [ ] `:app` 不包含任何 ViewModel
- [ ] `:app` 不包含任何业务 Screen/Component
- [ ] 全部功能正常运行

---

### 2.6 拆分过程中的 DI 策略

当前 3 个 DI Module 需要按模块重新分配：

| 当前 | 拆分后 | 位置 |
|------|--------|------|
| `DatabaseModule` (DAO + DataStore + EncryptedPrefs + OkHttpClient) | `:core:data` 内的 DataModule | Database + DAO + DataStore |
| | `:core:data` 内的 NetworkModule | OkHttpClient + Retrofit + Json |
| `NetworkModule` (Retrofit + Json + ApiService) | 合并到上方 | |
| `RepositoryModule` (16 个 @Binds) | `:core:data` 内的 RepositoryModule | 所有 @Binds |
| | 各 `:feature:*` 内的 FeatureModule | Feature 专用绑定 |

**DI 分配原则**:
- 实现和接口在同一模块的，不需要跨模块 @Binds
- `:core:data` 提供 RepositoryImpl 的 @Binds
- `:feature:*` 提供 UseCase 的 @Provides
- `:app` 的 RootModule 负责组装所有 installIn(SingletonComponent) 的模块

---

### 2.7 [D-18] 消除 api 依赖透传

**问题定位**

`:core:data` 的 `build.gradle.kts` 中以下依赖使用了 `api` 而非 `implementation`，导致 `:app` 可以直接访问底层库：

```kotlin
// 当前 — 大量 api 透传
api("androidx.room:room-runtime:2.7.0")
api("com.squareup.retrofit2:retrofit:2.11.0")
api("com.squareup.okhttp3:okhttp:4.12.0")
api("com.google.code.gson:gson:2.11.0")
api("io.coil-kt:coil-compose:2.7.0")
api("cn.6tail:lunar:1.7.7")
api("androidx.datastore:datastore-preferences:1.1.3")
api("androidx.security:security-crypto:1.1.0-alpha06")
```

**改进方案**

分批将 `api` 改为 `implementation`，优先处理纯内部使用的依赖：

| 优先级 | 依赖 | 改为 implementation 的条件 |
|--------|------|---------------------------|
| P1 | Gson | app 不直接使用 Gson 类 → 直接改；但 feature:divination 需单独声明 |
| P1 | Lunar | app 不直接使用 Lunar 类 → 直接改 |
| P1 | Retrofit | app 不直接使用 Retrofit API → 直接改 |
| P2 | OkHttp | app 中 DatabaseModule 提供 OkHttpClient → DI 改后处理 |
| P2 | Coil | app/core:ui 中直接使用 AsyncImage → 需 core:ui 自行声明依赖 |
| P2 | Room | app 中 DAO 手动 Provides → DI 改后处理 |
| P2 | DataStore | app 中 DatabaseModule 提供 → DI 改后处理 |
| P2 | Security | app 中 DatabaseModule 提供 → DI 改后处理 |

**影响范围**: `:core:data` 的 `build.gradle.kts` + `:app` 的 `build.gradle.kts` + `:core:ui` 的 `build.gradle.kts`

---

## 3. Phase 3：精化 — 数据层与基础设施优化

> **目标**: 数据层和基础设施优化，提升性能和可维护性

### 3.1 [D-09] Schema 重设计 — 拆分 ContactEntity JSON 字段 ⚠️ 可选

> ⚠️ **本项为可选优化**。对于个人 PRM 应用，联系人数量级在百~千，JSON 解析开销可忽略。除非有明确的 SQL 层查询需求（如"找爱好包含篮球的联系人"），否则拆分后代码复杂度反而增加（每次读取需 JOIN + 多行合并）。建议在有实际查询需求驱动时再执行。

**问题定位**

`ContactEntity` 有 28 个字段，其中 `hobby`、`habit`、`diet`、`skill` 用 JSON 字符串存储。这导致：

- 无法在 SQL 层查询"爱好包含篮球的联系人"
- `CleanCustomTypeUseCase` 需要手动解析 JSON 数组
- 数据完整性无法在数据库层保障

**改进方案**

新增 `contact_attributes` 关联表：

```kotlin
// 新 Entity
@Entity(
    tableName = "contact_attributes",
    foreignKeys = [ForeignKey(
        entity = ContactEntity::class,
        parentColumns = ["id"],
        childColumns = ["contactId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["contactId", "category", "value"])]
)
data class ContactAttributeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val category: String,     // "hobby", "habit", "diet", "skill"
    val value: String,
)
```

**迁移策略**:

在 `MIGRATION_31_32` 中：
1. 创建 `contact_attributes` 表
2. 遍历 `contacts` 表，解析 JSON 字段，插入关联记录
3. 保留原 JSON 字段（向后兼容），标记为 deprecated

**影响范围**: 新增 Entity + DAO + Migration + Repository 层适配

---

### 3.2 [D-10] 合并数据库迁移基线

**问题定位**

迁移已从原来的 30 个独立步骤合并为 2 个文件：

```
MIGRATION_1_24   (合并了前 23 步)    ← MigrationShortcuts.kt
MIGRATION_24_31  (合并了后 7 步)     ← Migration_24_31.kt
```

当前状态已比原来大幅改善，但仍可进一步优化：合并为单一 `MIGRATION_1_32`。

**改进方案**

在 v2.0.0 中做一次**破坏性迁移基线重置**：

1. 合并 `MIGRATION_1_24` 到 `MIGRATION_24_31` 为一个 `MIGRATION_1_32`
2. `MIGRATION_1_32` 包含完整的建表语句（从空库到 v32 全量 Schema）
3. 在 `fallbackToDestructiveMigration()` 和 `@Database(version = 32)` 之间选择策略

**对于已有用户**: `MIGRATION_1_32` 覆盖所有路径，任何版本都能升级到 v32。
**对于新安装**: 直接创建 v32 数据库，无需执行迁移。

---

### 3.3 [D-07] 将内存过滤移至 SQL 层

**问题定位**

| Repository | 方法 | 问题 |
|-----------|------|------|
| `AnniversaryRepositoryImpl` | `getUpcomingAnniversaries()` | 在 Flow map 中做过滤+排序，应让 SQL 处理 |
| `AnniversaryRepositoryImpl` | `getPastAnniversaries()` | 同上 |
| `CircleRepositoryImpl` | `getCirclesForContact()` | 在内存中过滤 |
| `CircleRepositoryImpl` | `getChildCircles()` | 在内存中过滤 |
| `CircleRepositoryImpl` | `getRootCircles()` | 在内存中过滤 |

**改进方案**

> ⚠️ **限制**: `AnniversaryRepositoryImpl.getUpcomingAnniversaries()` 和 `getPastAnniversaries()` 中调用了 `effectiveDate()`，它依赖 `DateCalcUtils.getNextBirthdayDate()` 和 `getNextRepeatDate()`，这些包含**农历计算逻辑**，纯 SQL 无法表达。因此 AnniversaryRepo 只能做**基础 SQL 过滤 + Repository 层补充农历计算**，无法完全移至 SQL 层。CircleRepo 的 SQL 化方案则完全可行。

```kotlin
// CircleDao — 完全可 SQL 化 ✓
@Query("""
    SELECT c.* FROM circles c
    INNER JOIN circle_member_cross_ref cm ON c.id = cm.circleId
    WHERE cm.contactId = :contactId
""")
fun getCirclesForContact(contactId: Long): Flow<List<CircleEntity>>

@Query("SELECT * FROM circles WHERE parentId = :parentId")
fun getChildCircles(parentId: Long): Flow<List<CircleEntity>>

@Query("SELECT * FROM circles WHERE parentId IS NULL")
fun getRootCircles(): Flow<List<CircleEntity>>
```

**影响范围**: `CircleDao.kt`, `CircleRepositoryImpl.kt`, `AnniversaryDao.kt`, `AnniversaryRepositoryImpl.kt`

---

### Phase 3 检查清单

| 编号 | 任务 | 影响范围 | 状态 | 实际收益 |
|------|------|---------|------|---------|
| D-07 | 内存过滤移至 SQL 层 | 改 2 DAO + 2 Repository | ✅ 完成 | CircleRepo 完全 SQL 化；AnniversaryRepo 用候选集预过滤 |
| D-10 | 合并迁移基线到 v32 | 改 Migration 文件 | ✅ 完成 | 2 个迁移文件→1 个 (MIGRATION_1_32) |
| D-18 | 消除 api 依赖透传 | 改 core:data build + DI 迁移 | ✅ 完成 | 全部 api→implementation，DI 模块迁至 core:data/di/ |
| D-09 | ContactEntity JSON 拆分 | +Entity, +DAO, +Migration, 改 Repository | ⬜ 待修 | SQL 层查询能力恢复 (⚠️ 可选) |

---

## 4. Phase 4：增长 — 面向未来的架构准备

> **目标**: 为未来功能打基础，让新功能的加入不破坏现有架构
>
> **预期时间**: 持续

### 4.1 WebDAV 同步支持

**架构准备**: 在 `:core:data` 上层加 `:core:sync` 模块

```
:core:sync/
├── SyncManager.kt          # 同步调度器
├── SyncStrategy.kt          # 同步策略接口 (本地优先/远程优先/合并)
├── remote/
│   ├── WebDavClient.kt      # WebDAV 协议客户端
│   └── SyncMetadata.kt      # 同步元数据 (最后同步时间、冲突日志)
└── conflict/
    ├── ConflictDetector.kt  # 冲突检测
    └── ConflictResolver.kt  # 冲突解决策略
```

**为什么 Repository 模式天然支持**:
- 同步层可以透明地替换数据源
- ViewModel 和 UseCase 不需要知道数据来自本地还是远程
- 类似 `PagingSource` 的模式 — 本地 DB 是单一数据源 (SSOT)

---

### 4.2 平板适配

**架构准备**: `:core:ui` 提供自适应布局组件

```kotlin
// core:ui/src/main/java/.../adaptive/

@Composable
fun AdaptiveScaffold(
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Row { list(); detail() }  // 平板：左右分栏
    } else {
        Column { list() }          // 手机：全屏列表
    }
}
```

**模块化后天然适配**: feature 模块只关心自己的 UI 逻辑，适配层在 `:core:ui` 统一处理。

---

### 4.3 LLM 接口抽象

**问题定位**: 当前 `AiRepository` 硬编码 DeepSeek API，无法切换 LLM 提供商。

**改进方案**

```kotlin
// core:domain/src/main/java/.../llm/

interface LlmProvider {
    val id: String
    val displayName: String
    suspend fun streamChat(messages: List<ChatMessage>): Flow<String>
}

class DeepSeekProvider @Inject constructor(
    private val apiKey: String,
) : LlmProvider {
    override val id = "deepseek"
    override val displayName = "DeepSeek"
    override suspend fun streamChat(messages: List<ChatMessage>): Flow<String> { ... }
}

class OpenAiCompatibleProvider @Inject constructor(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String,
) : LlmProvider {
    override val id = "openai-compatible"
    override val displayName = model
    override suspend fun streamChat(messages: List<ChatMessage>): Flow<String> { ... }
}
```

**影响范围**: `AiRepository` 改为 `LlmProvider` 接口实现，`SettingsRepository` 存储多配置

---

### 4.4 [D-14] 导航升级 — Type-Safe Navigation

**问题定位**: 当前 30 个路由用字符串拼接，参数提取用自定义扩展函数。Screen.kt 已移至 `:core:ui`，但仍是 `sealed class Screen(val route: String)` 模式。

**改进方案**

升级到 Navigation 2.8+ 的 Type-Safe API：

```kotlin
// 改造前 — :core:ui/navigation/Screen.kt
sealed class Screen(val route: String) {
    object Home : Screen("home")
    data class ContactDetail(val contactId: Long) : Screen("contact/{contactId}")
}

// 改造后
@Serializable object HomeRoute
@Serializable data class ContactDetailRoute(val contactId: Long)
@Serializable data class EventDetailRoute(val eventId: Long)

// NavHost
composable<ContactDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<ContactDetailRoute>()
    ContactDetailScreen(contactId = route.contactId)
}
```

**前置条件**: ✅ 已满足 — 项目当前 Kotlin 2.1.21 + Navigation 2.8.5，可直接升级

---

## 5. 架构决策记录 (ADR)

### ADR-001: 模块化策略 — 先竖切后横切

**状态**: Accepted ✓

**上下文**: 327 个文件全在 `:app`，没有编译期隔离。常见做法是先分 `data/domain/ui` 横切层，再按功能竖切。但横切后 domain 层会变成一个上帝模块。

**决策**: 先按限界上下文竖切成 feature 模块，每个 feature 内部再分 data/domain/ui。Core 模块放共享的东西。

**后果**:
- (+) 每个 feature 可独立编译、独立测试
- (+) feature 之间不能互相依赖，编译期强制边界
- (-) 初期拆分工作量较大
- (-) core:model 需精确定义，否则变成垃圾桶

---

### ADR-002: ViewModel 编排规则 — 单一数据源原则

**状态**: Accepted ✓

**上下文**: 当前 ViewModel 存在三种反模式：(1) 直接注入多个 Repository 做 combine；(2) 业务逻辑在 ViewModel 中；(3) UseCase 使用不一致。

**决策**: ViewModel 编排规则：
1. ViewModel 最多注入 **3 个 UseCase + 1 个 Repository**（仅用于简单 CRUD）
2. 写操作的业务逻辑**必须下沉到 UseCase**
3. 同类型操作**必须走同一条路径**（收藏只走 FavoriteToggleUseCase，禁止绕过）
4. 跨聚合的数据组合放到 AggregationUseCase，**不在 ViewModel 里 combine**

**后果**:
- (+) ViewModel 变薄，只做状态管理和 UI 事件分发
- (+) 业务逻辑可测试性大幅提升
- (-) 短期 UseCase 数量会增加
- (-) 需要团队纪律维持

---

### ADR-003: 数据库 Schema 治理 — 基线重置策略

**状态**: Proposed

**上下文**: Room 数据库从 v1 演进到 v31，已有 2 个合并后的 Migration 文件（`MIGRATION_1_24` + `MIGRATION_24_31`），原 30 个独立步骤已大幅精简。ContactEntity 28 个字段，hobby/habit/diet/skill 用 JSON 字符串存储。

**决策**: 在下一个大版本（v2.0.0）做一次**破坏性 Schema 重设计**：
1. 将 hobby/habit/diet/skill 从 JSON 字符串拆为独立的关联表 `contact_attributes`
2. 合并所有迁移为一个 `MIGRATION_1_32`（v31→v32），包含完整的新建表 + 数据迁移
3. 之后的迁移从 v32 这个新基线开始

**后果**:
- (+) Schema 设计更合理，查询能力恢复
- (+) 迁移文件从 2 个减到 1 个（新基线之后）
- (-) 需要写数据迁移脚本，有数据丢失风险
- (-) 老用户升级路径需要额外处理

---

### ADR-004: 导航升级 — Type-Safe Navigation

**状态**: Proposed

**上下文**: 当前 30 个路由用字符串拼接，参数提取用自定义扩展函数。Google 已推出 Type-Safe Navigation (2.8+)。Screen.kt 已移至 `:core:ui`。

**决策**: 升级到 Navigation 2.8+ 的 Type-Safe API，用 `@Serializable` data class / object 定义路由。

**后果**:
- (+) 编译期路由安全，不会有运行时参数类型错误
- (+) IDE 自动补全和重构支持
- (-) 需要升级 Kotlin 和 Navigation 版本
- (-) 路由定义方式完全重写

---

### ADR-006: 合并 :core:model 到 :core:domain

**状态**: Accepted ✓

**上下文**: Phase 2 规划时原定先拆 `:core:model` 再拆 `:core:domain`，但 18 个领域模型不足以构成独立模块的体量，且 Repository 接口与模型强关联。

**决策**: 将 `:core:model` 合并到 `:core:domain`，模型和接口同模块。

**后果**:
- (+) 减少模块数量和依赖配置复杂度
- (+) Repository 接口引用模型无需跨模块
- (-) `:core:domain` 职责稍宽，需注意不变成垃圾桶
- (-) 如果未来模型膨胀到 50+，可能需要重新拆分

---

### ADR-007: Screen 路由定义放置在 :core:ui

**状态**: Accepted ✓

**上下文**: 拆分 feature 模块后，每个 feature 需要注册自己的 NavGraph。Screen 路由定义（如 `Screen.ContactDetail(contactId)`）被多个 feature 引用。

**决策**: 将 `Screen.kt` 放置在 `:core:ui/navigation/`，而非 `:app`。

**后果**:
- (+) 所有 feature 模块可直接引用路由定义
- (+) 编译期检查路由参数类型
- (-) `:core:ui` 多了一个非 UI 的路由定义文件
- (-) 路由定义和导航组装分离，需注意一致性

---

### ADR-008: :feature:oracle 重命名为 :feature:divination

**状态**: Accepted ✓

**上下文**: 原规划使用 `:feature:oracle` 作为占卜功能的模块名，但项目代码中统一使用 "divination" 命名（DivinationViewModel, DivinationRecord, :engine:divination 等）。"Oracle" 在英文语境中偏向"神谕/预言"，与"占卜问卦"的实际功能有微妙差异。

**决策**: 使用 `:feature:divination` 替代 `:feature:oracle`，与引擎模块和代码命名保持一致。

**后果**:
- (+) 命名一致性：engine:divination → feature:divination
- (+) 避免了 "oracle" 一词的多义性
- (-) 与原规划文档不一致（已更新）

---

## 6. 实施路线图

### 6.1 全景时间线

```
Phase 1: 止血 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ✅ 全部完成
Phase 2: 分治 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ✅ 全部完成
├── ✅ Step 1  :core:domain (含 model)                 ┃
├── ✅ Step 2  :engine:divination                      ┃
├── ✅ Step 3  :core:data                              ┃
├── ✅ Step 4  :core:ui                                ┃
├── ✅ Step 5  :feature:divination                     ┃
├── ✅ Step 6  :feature:remember                       ┃
├── ✅ Step 7  :feature:people                          ┃
├── ✅ Step 8  :feature:encounter                       ┃
├── ✅ Step 9  :feature:reflect                         ┃
├── ✅ D-18   消除 :core:data api 依赖透传              ┃
└── ✅ DI 模块下沉到 core:data/di/                      ┃
                                                       ┃
Phase 3: 精化 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ✅ 全部完成
├── ✅ D-07  内存过滤移至 SQL 层                         ┃
├── ✅ D-10  合并迁移基线到 v32                          ┃
├── ✅ D-18  消除 api 依赖透传                           ┃
└── ✅ D-09  Schema 重设计 (contact_attributes)          ┃
                                                       ┃
Phase 4: 增长 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ✅ 已完成
├── ✅ D-14  Type-Safe Navigation 升级                   ┃
├── ⬜ WebDAV 同步 (:core:sync)                         ┃
├── ⬜ 平板适配 (AdaptiveScaffold)                      ┃
└── ⬜ LLM 接口抽象 (LlmProvider)                       ┃
```

### 6.2 风险控制

| 风险 | 概率 | 影响 | 缓解策略 |
|------|------|------|---------|
| 模块拆分编译问题 | 中 | 中 | 已完成 5 个模块拆分验证，流程成熟 ✓ |
| Schema 重置导致用户数据丢失 | 中 | 高 | 提供 JSON 导出/导入升级路径；内测版先验证 |
| 拆模块后编译时间变长 | 低 | 低 | 开启 Gradle Configuration Cache + 并行编译 |
| core:domain 变成垃圾桶 | 中 | 中 | 定期审查 core:domain 的依赖方向，禁止反向依赖 |
| api → implementation 改动引起编译错误 | 中 | 中 | 分批改动，每改一批验证编译 |

### 6.3 回滚策略

每个 Phase 的改动应该在一个 Git 分支上完成，合入前确保：

- [ ] `assembleDebug` 编译通过
- [ ] 真机上全功能回归测试
- [ ] 数据库迁移测试（从旧版本升级）
- [ ] ProGuard 混淆后的 Release 构建测试

如果某个 Step 出现问题，可以安全地回退到上一个 Step 的状态，因为每一步都是增量交付的。

---

## 7. 附录

### 7.1 ViewModel 完整统计

| ViewModel | 行数 | 依赖数 | 公开函数 | UiState 结构 | 所属模块 | 备注 |
|-----------|------|--------|---------|-------------|---------|------|
| AddContactVM | 351 | 4+1Helper | 18 | AddContactUiState | :app | ✅ CreateContactUseCase已提取；✅ updateXxx已用updateField优化 |
| ContactListVM | 239 | 2 | 28 | 4 个 State | :app | 28 个公开函数需拆分 |
| ThoughtsVM | 236 | 3 | 17 | 3+1 State | :app | ✅ contactMap O(1)查找 + filterState combine |
| AddChatVM | 219 | 2+1Manager | 12 | AddChatUiState | :app | ✅ 亲密度更新已走UpdateInteractionUseCase |
| DivinationVM | 211 | 1 | 10 | 1 State | :feature:divination | ✅ Gson已替换为kotlinx-serialization |
| ContactsVM | 184 | 4 | 14 | 4 State | :app | sortedBy 应移入 UseCase |
| ContactDetailVM | 107 | 2UseCase | 5 | 3 State | :app | ✅ AggregationUseCase已提取，依赖7→2 |
| GiftsVM | 164 | 3 | 10 | 3 State | :app | 乐观更新+回滚 |
| PhotoAlbumVM | 146 | 3 | 7 | 1+3 State | :app | ✅ stateIn共享单一数据源 |
| AddEventVM | 159 | 4 | 10 | 1 State | :app | ✅ 亲密度更新已走UpdateInteractionUseCase；种子数据逻辑待移 |
| AiVM | 146 | 2 | 7 | sealed State | :feature:divination | var 回调非线程安全 |
| AddAnniversaryVM | 149 | 3 | 10 | 1 State | :app | 较干净 |
| EventsVM | 134 | 3 | 9 | 3 State | :app | 较干净 |
| AnniversariesVM | 120 | 2+1UseCase | 9 | 3 State | :app | ✅ combine内不再读_uiState.value |
| FootprintsVM | 102 | 2 | 7 | 1 State | :app | 干净 |
| HomeVM | 109 | 5 | 3 | 1 State | :app | 依赖混合 |
| EventDetailVM | 92 | 2 | 5 | 1 State | :app | ✅ 已统一走 FavoriteToggleUseCase |
| SettingsVM | 78 | 2 | 3 | sealed State | :app | 干净 |
| BackupVM | 79 | 1 | 3 | sealed State | :app | 干净 |
| ChatDetailVM | 76 | 2 | 6 | 1 State | :app | ✅ 已统一走 FavoriteToggleUseCase |
| ChatVM | 62 | 1 | 6 | 1 State | :app | 简洁 |
| FavoritesVM | 54 | 1 | 3 | 1 State | :app | 简洁 |
| AnniversaryDetailVM | 50 | 1 | 3 | 1 State | :app | 简洁 |
| ProfileVM | 37 | 1 | 1 | 1 State | :app | 极简 |
| DivinationHistoryVM | 29 | 1 | 2 | 无 | :feature:divination | 极简 |

### 7.2 超大文件清单 (400+ 行)

| 文件 | 行数 | 所属模块 | 分类 |
|------|------|---------|------|
| DivinationRecordDetail.kt | 775 | :feature:divination | Screen |
| ContactListCard.kt | 761 | :app/home | Components |
| ContactCardOverlay.kt | 624 | :app/contacts | Components |
| ThoughtsDialogs.kt | 547 | :app/home | Dialogs |
| ThoughtsComponents.kt | 499 | :app/home | Components |
| AddChatComponents.kt | 472 | :app/chat | Components |
| FavoritesComponents.kt | 463 | :app/home | Components |
| HexagramData.kt | 456 | :engine:divination | Data |
| GiftDetailComponents.kt | 446 | :app/home | Components |
| ContactListDialogs.kt | 443 | :app/home | Dialogs |
| LiuyaoCastComponents.kt | 434 | :feature:divination | Components |
| ContactsComponents.kt | 415 | :app/contacts | Components |
| PhotoAlbumComponents.kt | 415 | :app/home | Components |
| ContactListMiniCard.kt | 411 | :app/home | Components |
| ChatDetailComponents.kt | 404 | :app/chat | Components |
| AddAnniversaryComponents.kt | 402 | :app/anniversary | Components |
| AddGiftComponents.kt | 400 | :app/home | Components |

### 7.3 依赖统计

```
模块分布:
  :app 源文件:                142 (+24 测试, +15 Android 测试)
  :core:domain 源文件:         54 (+13 测试)
  :core:data 源文件:           60 (+28 单元测试, +11 Android 测试)
  :core:ui 源文件:             31
  :engine:divination 源文件:   17 (+6 测试)
  :feature:divination 源文件:  28 (+3 测试, +1 Android 测试)
  总计 Kotlin 源文件:         332 (不含测试)
  总计 Kotlin 测试文件:        101 (74 单元测试 + 27 Android 测试)

架构组件:
  ViewModel 文件数:       22 (在 :app) + 3 (在 :feature:divination) = 25
  UseCase 文件数:         17 (已迁移到 :core:domain, 原22个含5个Filter已合并为FilterExt扩展函数)
  Repository 接口数:      13 (ContactGroup+ContactTag 在 ContactRepository.kt)
  Repository 实现数:      17 (已迁移到 :core:data, 含 BackupRepository, SettingsRepositoryImpl,
                            ContactGroupRepositoryImpl, ContactTagRepositoryImpl)
  DAO 文件数:             11 (已迁移到 :core:data)
  Entity 文件数:          13 (已迁移到 :core:data, 含 3 个关联/包装类)
  Mapper 文件数:          12 (已迁移到 :core:data, 含 MapperExt)
  Migration 文件数:       3 (MIGRATION_1_32 主迁移 + 2 个旧文件保留)
  Domain Model 文件数:    18 (已迁移到 :core:domain)

其他统计:
  Navigation 路由数:      30
  底部导航 Tab:           5
  DI Module 数:          3 (已迁至 :core:data/di/)
  Compose BOM:           2025.05.01
  技术栈:                 Kotlin + Compose + Room 2.7.0 + Hilt + Retrofit
                         + kotlinx-serialization 1.7.3 + Coil 2.7.0
```

### 7.4 术语表

| 术语 | 含义 |
|------|------|
| 限界上下文 (Bounded Context) | DDD 中明确的业务边界，内部模型一致 |
| 共享内核 (Shared Kernel) | 多个上下文共享的模型子集，如 Contact |
| 聚合根 (Aggregate Root) | 一组相关对象的入口点，如 Contact 是 People 上下文的聚合根 |
| 反腐败层 (Anti-Corruption Layer) | 隔离不同上下文模型的翻译层 |
| SSOT (Single Source of Truth) | 单一数据源原则，数据只在一个地方维护 |
| 组合根 (Composition Root) | DI 的组装点，所有模块在此连接 |
| 依赖透传 (Transitive Dependency) | 通过 `api` 暴露的依赖会传递给下游模块，破坏封装性 |
