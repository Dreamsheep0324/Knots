# 结绳 (Tang) 全项目测试实现文档

## 一、测试分层策略

| 层级 | 测试类型 | 运行环境 | 优先级 | 占比 |
|------|---------|---------|--------|------|
| 引擎层 | 单元测试 | JVM | 🔴 最高 | 40% |
| 工具层 | 单元测试 | JVM | 🔴 最高 | 15% |
| Mapper层 | 单元测试 | JVM | 🟡 高 | 10% |
| Repository层 | 单元测试 | JVM (Mock) | 🟡 高 | 15% |
| ViewModel层 | 单元测试 | JVM (Mock) | 🟢 中 | 10% |
| DAO层 | 仪器测试 | Android | 🟢 中 | 5% |
| UI层 | 仪器测试 | Android | ⚪ 低 | 5% |

---

## 二、依赖配置

### 2.1 build.gradle.kts 新增依赖

```kotlin
dependencies {
    // ===== 测试依赖 =====
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    // MockK (Kotlin Mock 框架)
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("io.mockk:mockk-android:1.13.10")

    // Kotlin Coroutines 测试
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // Turbine (Flow 测试)
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // Truth (断言库，比 JUnit Assert 更可读)
    testImplementation("com.google.truth:truth:1.4.2")

    // Room 测试
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // AndroidX Test
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### 2.2 启用 JUnit 5

```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 2.3 测试目录结构

```
app/src/
├── test/java/com/tang/prm/
│   ├── engine/
│   │   ├── meihua/
│   │   │   ├── MeihuaEngineTest.kt
│   │   │   └── MeihuaPromptBuilderTest.kt
│   │   ├── liuyao/
│   │   │   ├── LiuyaoEngineTest.kt
│   │   │   └── LiuyaoPromptBuilderTest.kt
│   │   └── core/
│   │       ├── GanZhiCalculatorTest.kt
│   │       └── WuXingHelperTest.kt
│   ├── util/
│   │   ├── DateUtilsTest.kt
│   │   ├── LunarUtilsTest.kt
│   │   ├── SqlUtilsTest.kt
│   │   └── ImageCacheManagerTest.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── IntimacyLevelTest.kt
│   │   │   ├── GiftTypeTest.kt
│   │   │   ├── ThemeModeTest.kt
│   │   │   ├── ThoughtTypeTest.kt
│   │   │   ├── EventTypesTest.kt
│   │   │   ├── CustomCategoriesTest.kt
│   │   │   └── ChatDtoTest.kt
│   │   └── usecase/
│   │       └── HomeStatsUseCaseTest.kt
│   ├── data/
│   │   ├── mapper/
│   │   │   ├── ContactMapperTest.kt
│   │   │   ├── EventMapperTest.kt
│   │   │   ├── AnniversaryMapperTest.kt
│   │   │   ├── GiftMapperTest.kt
│   │   │   ├── ThoughtMapperTest.kt
│   │   │   ├── FavoriteMapperTest.kt
│   │   │   ├── TodoMapperTest.kt
│   │   │   ├── ReminderMapperTest.kt
│   │   │   ├── CircleMapperTest.kt
│   │   │   ├── DivinationMapperTest.kt
│   │   │   └── CustomTypeMapperTest.kt
│   │   └── repository/
│   │       ├── ContactRepositoryImplTest.kt
│   │       ├── ContactTagRepositoryImplTest.kt
│   │       ├── ContactGroupRepositoryImplTest.kt
│   │       ├── EventRepositoryImplTest.kt
│   │       ├── AnniversaryRepositoryImplTest.kt
│   │       ├── GiftRepositoryImplTest.kt
│   │       ├── ThoughtRepositoryImplTest.kt
│   │       ├── FavoriteRepositoryImplTest.kt
│   │       ├── TodoRepositoryImplTest.kt
│   │       ├── ReminderRepositoryImplTest.kt
│   │       ├── CircleRepositoryImplTest.kt
│   │       ├── ChatRepositoryImplTest.kt
│   │       ├── SettingsRepositoryImplTest.kt
│   │       ├── AiRepositoryImplTest.kt
│   │       ├── BackupRepositoryTest.kt
│   │       └── DivinationRepositoryImplTest.kt
│   └── ui/
│       ├── home/
│       │   ├── HomeViewModelTest.kt
│       │   ├── GiftsViewModelTest.kt
│       │   ├── ThoughtsViewModelTest.kt
│       │   ├── FavoritesViewModelTest.kt
│       │   ├── FootprintsViewModelTest.kt
│       │   ├── ContactListViewModelTest.kt
│       │   └── PhotoAlbumViewModelTest.kt
│       ├── contacts/
│       │   ├── ContactsViewModelTest.kt
│       │   ├── ContactDetailViewModelTest.kt
│       │   └── AddContactViewModelTest.kt
│       ├── events/
│       │   ├── EventsViewModelTest.kt
│       │   ├── EventDetailViewModelTest.kt
│       │   └── AddEventViewModelTest.kt
│       ├── anniversary/
│       │   ├── AnniversariesViewModelTest.kt
│       │   ├── AnniversaryDetailViewModelTest.kt
│       │   └── AddAnniversaryViewModelTest.kt
│       ├── chat/
│       │   ├── ChatViewModelTest.kt
│       │   ├── ChatDetailViewModelTest.kt
│       │   └── AddChatViewModelTest.kt
│       ├── divination/
│       │   ├── DivinationViewModelTest.kt
│       │   ├── DivinationHistoryViewModelTest.kt
│       │   └── AiViewModelTest.kt
│       └── profile/
│           ├── ProfileViewModelTest.kt
│           ├── SettingsViewModelTest.kt
│           └── BackupViewModelTest.kt
│   └── service/
│       └── ReminderReceiverTest.kt
└── androidTest/java/com/tang/prm/
    └── data/local/dao/
        ├── ContactDaoTest.kt
        ├── EventDaoTest.kt
        ├── AnniversaryDaoTest.kt
        ├── GiftDaoTest.kt
        ├── CircleDaoTest.kt
        ├── ThoughtDaoTest.kt
        ├── TodoDaoTest.kt
        ├── FavoriteDaoTest.kt
        ├── ReminderDaoTest.kt
        ├── DivinationRecordDaoTest.kt
        └── CustomTypeDaoTest.kt
```

---

## 三、引擎层测试（最高优先级）

### 3.1 GanZhiCalculatorTest

> 干支计算是占卜引擎的基础，错误会级联影响所有占卜结果

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 已知日期的年干支 | `getYearGanZhi(2024, 1, 1)` | 2024年1月1日 | 甲辰 |
| 2 | 已知日期的月干支 | `getMonthGanZhi(2024, 3, 1)` | 2024年3月 | 丁卯 |
| 3 | 已知日期的日干支 | `getDayGanZhi(2024, 1, 1)` | 2024年1月1日 | 甲子 |
| 4 | 时辰地支映射 | `getDiZhiByHour(0)` | 0时 | 子 |
| 5 | 时辰地支映射 | `getDiZhiByHour(12)` | 12时 | 午 |
| 6 | 时辰地支映射 | `getDiZhiByHour(23)` | 23时 | 子 |
| 7 | 跨年干支切换 | `getYearGanZhi(2023, 12, 31)` | 2023年12月31日 | 癸卯 |
| 8 | 闰年2月天数 | `getDaysInMonth(2024, 2)` | 2024年2月 | 29 |
| 9 | 平年2月天数 | `getDaysInMonth(2023, 2)` | 2023年2月 | 28 |
| 10 | 六十甲子循环 | 连续61天日干支 | 首尾相同 | 首尾天干地支一致 |

### 3.2 WuXingHelperTest

> 五行生克关系是梅花和六爻分析的核心

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 天干→五行 | `getWuXingByGan("甲")` | 甲 | 木 |
| 2 | 地支→五行 | `getWuXingByZhi("子")` | 子 | 水 |
| 3 | 五行相生 | `isSheng("木", "火")` | 木→火 | true |
| 4 | 五行相克 | `isKe("木", "土")` | 木→土 | true |
| 5 | 五行不生 | `isSheng("木", "金")` | 木→金 | false |
| 6 | 五行不克 | `isKe("木", "水")` | 木→水 | false |
| 7 | 六亲关系-比和 | `getLiuQin("木", "木")` | 同五行 | 比肩 |
| 8 | 六亲关系-我生 | `getLiuQin("木", "火")` | 木→火 | 子孙 |
| 9 | 六亲关系-我克 | `getLiuQin("木", "土")` | 木→土 | 妻财 |
| 10 | 六亲关系-生我 | `getLiuQin("木", "水")` | 水→木 | 父母 |
| 11 | 六亲关系-克我 | `getLiuQin("木", "金")` | 金→木 | 官鬼 |
| 12 | 六兽分配 | `getLiuShou(0..5, 甲日)` | 甲日 | 青龙起首 |
| 13 | 空亡判断 | `isKongWang(甲子日, 戌亥)` | 戌亥空 | true |
| 14 | 季节旺衰 | `getWangShuai("木", 春季)` | 木在春 | 旺 |

### 3.3 MeihuaEngineTest

> 梅花易数核心算法，起卦错误则全盘皆错

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 时间起卦-上卦 | `castByTime(2024,1,1,11)` | 午时 | 上卦由年月日和除8余数定 |
| 2 | 时间起卦-下卦 | `castByTime(2024,1,1,11)` | 午时 | 下卦由年月日时和除8余数定 |
| 3 | 时间起卦-动爻 | `castByTime(2024,1,1,11)` | 午时 | 动爻由总和除6余数定 |
| 4 | 数字起卦 | `castByNumber(3, 5)` | 上3下5 | 上卦为离,下卦为巽 |
| 5 | 数字起卦-动爻 | `castByNumber(3, 5, 2)` | 动爻2 | 第2爻动 |
| 6 | 随机起卦 | `castRandom()` | 任意 | 返回有效HexagramInfo |
| 7 | 体用分析-上卦为体 | 体用分析 | 动爻在下卦 | 上卦为体,下卦为用 |
| 8 | 体用分析-下卦为体 | 体用分析 | 动爻在上卦 | 下卦为体,上卦为用 |
| 9 | 互卦计算 | 互卦 | 任意主卦 | 取2-3-4爻为下互,3-4-5爻为上互 |
| 10 | 变卦计算 | 变卦 | 动爻 | 动爻阴阳互变 |
| 11 | 外应起卦 | `castByOmen(方位, 物象)` | 东南/动物 | 根据外应数据起卦 |
| 12 | 卦名正确性 | 起卦结果卦名 | 乾上乾下 | 乾为天 |
| 13 | 卦名正确性 | 起卦结果卦名 | 坎上离下 | 水火既济 |
| 14 | 边界-余数为0 | 总和除8余0 | 坤卦(第8卦) | 不崩溃,返回坤 |
| 15 | 边界-动爻余0 | 总和除6余0 | 第6爻动 | 不崩溃,返回第6爻 |

### 3.4 LiuyaoEngineTest

> 六爻核心算法，纳甲和六亲关系复杂易错

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 时间起卦-六爻生成 | `castByTime(2024,1,1,11)` | 午时 | 6个爻位各有阴阳 |
| 2 | 动爻标记 | 三次投币 | 背背背=老阳 | 动爻,变阴 |
| 3 | 静爻标记 | 三次投币 | 字字字=老阴 | 动爻,变阳 |
| 4 | 纳甲装配 | 纳甲 | 乾卦 | 内卦甲子起,外卦壬午起 |
| 5 | 六亲装配 | 六亲 | 乾宫卦 | 金为比肩,木为妻财等 |
| 6 | 用神选取-自占 | `getYongShen(自占, 乾宫)` | 自测 | 取世爻所属六亲 |
| 7 | 用神选取-代占 | `getYongShen(代占父母, 乾宫)` | 代占父母 | 取父母爻 |
| 8 | 卦宫归属 | `getPalace(卦象)` | 天风姤 | 乾宫 |
| 9 | 世应位置 | 世应 | 八纯卦 | 世在六爻,应在三爻 |
| 10 | 旺衰判断 | 旺衰 | 春季木爻 | 旺 |
| 11 | 空亡标记 | 空亡 | 甲子旬戌亥空 | 戌亥爻标空亡 |
| 12 | 暗动判断 | 暗动 | 日冲静爻 | 标记暗动 |

---

## 四、工具层测试

### 4.1 DateUtilsTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 相对时间-刚刚 | `toRelativeTime(now)` | 当前时间 | "刚刚" |
| 2 | 相对时间-分钟前 | `toRelativeTime(5分钟前)` | 5分钟前 | "5分钟前" |
| 3 | 相对时间-小时前 | `toRelativeTime(3小时前)` | 3小时前 | "3小时前" |
| 4 | 相对时间-昨天 | `toRelativeTime(昨天)` | 昨天 | "昨天" |
| 5 | 相对时间-天数前 | `toRelativeTime(7天前)` | 7天前 | "7天前" |
| 6 | 生日年龄计算 | `calculateAge(2000,1,1)` | 2000年生 | 正确年龄 |
| 7 | 生日天数倒计时 | `daysUntilBirthday(月,日)` | 下一个生日 | 正确天数 |
| 8 | 日期格式化 | `formatDate(timestamp)` | 时间戳 | "yyyy-MM-dd" |
| 9 | 时间格式化 | `formatTime(timestamp)` | 时间戳 | "HH:mm" |
| 10 | 边界-未来时间 | `toRelativeTime(未来)` | 未来时间 | 不崩溃 |

### 4.2 LunarUtilsTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 农历转阳历 | `lunarToSolar(2024,1,1)` | 农历正月初一 | 正确阳历日期 |
| 2 | 阳历转农历 | `solarToLunar(2024,2,10)` | 2024年2月10日 | 农历正月初一 |
| 3 | 获取节气 | `getSolarTerm(2024,3)` | 2024年3月 | 惊蛰/春分日期 |
| 4 | 闰月处理 | `lunarToSolar(2023,闰2月,1)` | 闰二月 | 正确阳历 |
| 5 | 大月小月 | 农历月份天数 | 大月30天/小月29天 | 正确天数 |

### 4.3 SqlUtilsTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 转义% | `escapeLike("test%name")` | 含% | "test\\%name" |
| 2 | 转义_ | `escapeLike("test_name")` | 含_ | "test\\_name" |
| 3 | 转义\ | `escapeLike("test\\name")` | 含\ | "test\\\\name" |
| 4 | 无特殊字符 | `escapeLike("normal")` | 普通字符串 | "normal" |
| 5 | 空字符串 | `escapeLike("")` | 空串 | "" |

---

## 五、领域模型测试

### 5.1 IntimacyLevelTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 亲密值0 | `fromValue(0)` | 0 | 陌生 |
| 2 | 亲密值25 | `fromValue(25)` | 25 | 相识 |
| 3 | 亲密值50 | `fromValue(50)` | 50 | 朋友 |
| 4 | 亲密值75 | `fromValue(75)` | 75 | 亲密 |
| 5 | 亲密值100 | `fromValue(100)` | 100 | 家人 |
| 6 | 边界值24 | `fromValue(24)` | 24 | 陌生 |
| 7 | 边界值26 | `fromValue(26)` | 26 | 相识 |
| 8 | 超出范围 | `fromValue(150)` | 150 | 家人(上限) |
| 9 | 负值 | `fromValue(-10)` | -10 | 陌生(下限) |

### 5.2 GiftTypeTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 所有枚举值 | `GiftType.values()` | - | 包含所有8种类型 |
| 2 | 枚举名称 | `GiftType.DIGITAL.name` | - | "DIGITAL" |
| 3 | 枚举序号连续 | `values().map { it.ordinal }` | - | 0..7连续 |

### 5.3 ThemeModeTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 所有枚举值 | `ThemeMode.values()` | - | 包含LIGHT/DARK/SYSTEM |
| 2 | 枚举名称 | `ThemeMode.LIGHT.name` | - | "LIGHT" |
| 3 | 枚举名称 | `ThemeMode.DARK.name` | - | "DARK" |
| 4 | 枚举名称 | `ThemeMode.SYSTEM.name` | - | "SYSTEM" |

### 5.4 ThoughtTypeTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 所有枚举值 | `ThoughtType.values()` | - | 包含所有想法类型 |
| 2 | 枚举完整性 | 遍历所有值 | - | 每个枚举都有displayName |

---

## 六、Mapper层测试

### 6.1 ContactMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |
| 4 | 空字段处理 | `toDomain(entity.copy(phone=null))` | phone为null | Domain.phone为null |
| 5 | 列表转换 | `toDomainList(entities)` | 多个Entity | 对应Domain列表 |

### 6.2 EventMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |
| 4 | 参与者列表序列化 | `toEntity(含参与者)` | 有参与者 | 正确序列化 |

### 6.3 AnniversaryMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 农历字段保留 | `toDomain(lunarEntity)` | isLunar=true | isLunar=true |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.4 GiftMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | GiftType枚举映射 | `toDomain(含DIGITAL类型)` | type=DIGITAL | GiftType.DIGITAL |
| 4 | 送出/收出方向 | `toDomain(isGiven=true)` | isGiven=true | isGiven=true |
| 5 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.5 ThoughtMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | ThoughtType枚举映射 | `toDomain(含类型)` | type字段 | 正确ThoughtType |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.6 FavoriteMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | sourceType多态 | `toDomain(contact类型)` | sourceType="CONTACT" | 正确映射 |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.7 TodoMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 完成状态映射 | `toDomain(isCompleted=true)` | isCompleted=true | isCompleted=true |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.8 ReminderMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 重复间隔映射 | `toDomain(含repeatInterval)` | repeatInterval | 正确映射 |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.9 CircleMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | 成员ID列表处理 | `toDomain(含memberIds)` | memberIds JSON | 正确解析为List |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.10 DivinationMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | JSON数据字段 | `toDomain(含meihuaData)` | data字段JSON | 正确反序列化 |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

### 6.11 CustomTypeMapperTest

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | Entity→Domain | `toDomain(entity)` | 完整Entity | 对应Domain对象 |
| 2 | Domain→Entity | `toEntity(domain)` | 完整Domain | 对应Entity对象 |
| 3 | category字段映射 | `toDomain(含category)` | category | 正确映射 |
| 4 | 双向转换一致性 | `toEntity(toDomain(entity))` | Entity | 与原始Entity一致 |

---

## 七、Repository层测试

### 7.1 ContactRepositoryImplTest

> 使用 MockK 模拟 DAO 层

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有联系人 | `getAllContacts()` | contactDao | 返回映射后的Domain列表 |
| 2 | 搜索联系人 | `searchContacts("张")` | contactDao | 调用DAO的search方法 |
| 3 | 插入联系人 | `insertContact(domain)` | contactDao | 调用DAO的insert,传入Entity |
| 4 | 更新联系人 | `updateContact(domain)` | contactDao | 调用DAO的update |
| 5 | 删除联系人 | `deleteContact(id)` | contactDao | 调用DAO的delete |
| 6 | 获取联系人统计 | `getContactStats()` | contactDao | 返回统计数据 |

### 7.2 SettingsRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 读取主题模式 | `themeMode.first()` | dataStore | 返回默认值SYSTEM |
| 2 | 写入主题模式 | `setThemeMode(DARK)` | dataStore | 调用edit写入 |
| 3 | 读取用户名 | `userName.first()` | dataStore | 返回默认空串 |
| 4 | 写入用户名 | `setUserName("测试")` | dataStore | 调用edit写入 |

### 7.3 AiRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 流式聊天-正常响应 | `chatFlow(request)` | okHttpClient | 返回Flow,emit多个chunk |
| 2 | 流式聊天-网络错误 | `chatFlow(request)` | okHttpClient | 抛出IOException |
| 3 | 测试连接-成功 | `testConnection()` | okHttpClient | 返回true |
| 4 | 测试连接-失败 | `testConnection()` | okHttpClient | 返回false |

### 7.4 EventRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有事件 | `getAllEvents()` | eventDao | 返回映射后的Domain列表 |
| 2 | 按日期范围查询 | `getEventsByDateRange(start, end)` | eventDao | 返回范围内事件 |
| 3 | 插入事件 | `insertEvent(domain)` | eventDao | 调用insert,传入Entity |
| 4 | 更新事件 | `updateEvent(domain)` | eventDao | 调用update |
| 5 | 删除事件 | `deleteEvent(id)` | eventDao | 调用delete |
| 6 | 参与者管理 | `addParticipant(eventId, contactId)` | eventDao | 调用insertParticipant |
| 7 | 获取事件含参与者 | `getEventWithParticipants(id)` | eventDao | 返回事件+参与者列表 |

### 7.5 AnniversaryRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有纪念日 | `getAllAnniversaries()` | anniversaryDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | anniversaryDao | 返回该联系人纪念日 |
| 3 | 插入纪念日 | `insertAnniversary(domain)` | anniversaryDao | 调用insert |
| 4 | 更新纪念日 | `updateAnniversary(domain)` | anniversaryDao | 调用update |
| 5 | 删除纪念日 | `deleteAnniversary(id)` | anniversaryDao | 调用delete |
| 6 | 获取即将到来 | `getUpcoming(days)` | anniversaryDao | 返回未来N天内的纪念日 |

### 7.6 GiftRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有礼物 | `getAllGifts()` | giftDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | giftDao | 返回该联系人礼物 |
| 3 | 按方向筛选 | `getByDirection(isGiven=true)` | giftDao | 返回送出的礼物 |
| 4 | 插入礼物 | `insertGift(domain)` | giftDao | 调用insert |
| 5 | 更新礼物 | `updateGift(domain)` | giftDao | 调用update |
| 6 | 删除礼物 | `deleteGift(id)` | giftDao | 调用delete |

### 7.7 ThoughtRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有想法 | `getAllThoughts()` | thoughtDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | thoughtDao | 返回该联系人想法 |
| 3 | 按类型筛选 | `getByType(ThoughtType.INSIGHT)` | thoughtDao | 返回指定类型想法 |
| 4 | 插入想法 | `insertThought(domain)` | thoughtDao | 调用insert |
| 5 | 更新想法 | `updateThought(domain)` | thoughtDao | 调用update |
| 6 | 删除想法 | `deleteThought(id)` | thoughtDao | 调用delete |

### 7.8 FavoriteRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有收藏 | `getAllFavorites()` | favoriteDao | 返回映射后的Domain列表 |
| 2 | 按来源类型筛选 | `getBySourceType("CONTACT")` | favoriteDao | 返回联系人收藏 |
| 3 | 添加收藏 | `addFavorite(domain)` | favoriteDao | 调用insert |
| 4 | 取消收藏 | `removeFavorite(id)` | favoriteDao | 调用delete |
| 5 | 检查是否已收藏 | `isFavorite(sourceType, sourceId)` | favoriteDao | 返回true/false |

### 7.9 TodoRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有待办 | `getAllTodos()` | todoDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | todoDao | 返回该联系人待办 |
| 3 | 更新完成状态 | `updateCompleted(id, true)` | todoDao | 调用update |
| 4 | 插入待办 | `insertTodo(domain)` | todoDao | 调用insert |
| 5 | 删除待办 | `deleteTodo(id)` | todoDao | 调用delete |

### 7.10 ReminderRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有提醒 | `getAllReminders()` | reminderDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | reminderDao | 返回该联系人提醒 |
| 3 | 按纪念日筛选 | `getByAnniversary(anniversaryId)` | reminderDao | 返回该纪念日提醒 |
| 4 | 插入提醒 | `insertReminder(domain)` | reminderDao | 调用insert |
| 5 | 更新提醒 | `updateReminder(domain)` | reminderDao | 调用update |
| 6 | 标记完成 | `markCompleted(id)` | reminderDao | 调用update |

### 7.11 CircleRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有圈子 | `getAllCircles()` | circleDao | 返回映射后的Domain列表 |
| 2 | 获取圈子含成员 | `getCircleWithMembers(id)` | circleDao | 返回圈子+成员列表 |
| 3 | 插入圈子 | `insertCircle(domain)` | circleDao | 调用insert |
| 4 | 更新圈子 | `updateCircle(domain)` | circleDao | 调用update |
| 5 | 删除圈子 | `deleteCircle(id)` | circleDao | 调用delete |
| 6 | 添加成员 | `addMember(circleId, contactId)` | circleDao | 调用insertMember |
| 7 | 移除成员 | `removeMember(circleId, contactId)` | circleDao | 调用deleteMember |

### 7.12 ChatRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有对话 | `getAllChats()` | chatDao | 返回映射后的Domain列表 |
| 2 | 按联系人筛选 | `getByContact(contactId)` | chatDao | 返回该联系人对话 |
| 3 | 插入对话 | `insertChat(domain)` | chatDao | 调用insert |
| 4 | 更新对话 | `updateChat(domain)` | chatDao | 调用update |
| 5 | 删除对话 | `deleteChat(id)` | chatDao | 调用delete |

### 7.13 DivinationRepositoryImplTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有记录 | `getAllRecords()` | divinationRecordDao | 返回映射后的Domain列表 |
| 2 | 插入记录 | `insertRecord(domain)` | divinationRecordDao | 调用insert |
| 3 | 删除记录 | `deleteRecord(id)` | divinationRecordDao | 调用delete |
| 4 | 更新分析内容 | `updateAnalysis(id, content)` | divinationRecordDao | 调用update |

### 7.14 BackupRepositoryTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 备份-创建文件 | `backup(context)` | context | 生成zip文件 |
| 2 | 备份-包含数据库 | `backup(context)` | context | zip中含db文件 |
| 3 | 备份-包含偏好 | `backup(context)` | context | zip中含preferences文件 |
| 4 | 恢复-解压文件 | `restore(context, uri)` | context | 正确解压 |
| 5 | 恢复-校验完整性 | `restore(context, uri)` | context | 校验zip结构 |
| 6 | 清空数据 | `clearAllData(context)` | context | 数据库被清空 |

---

## 八、UseCase层测试

### 8.1 HomeStatsUseCaseTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 正常统计 | `getStats()` | 各Repository | 返回正确的统计数据 |
| 2 | 空数据统计 | `getStats()` | 各Repository返回0 | 所有计数为0 |
| 3 | 联系人计数 | `getStats()` | contactRepo返回5 | stats.contacts=5 |
| 4 | 事件计数 | `getStats()` | eventRepo返回10 | stats.events=10 |
| 5 | 纪念日计数 | `getStats()` | anniversaryRepo返回3 | stats.anniversaries=3 |

---

## 九、ViewModel层测试

### 9.1 HomeViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 初始加载 | 初始化 | homeStatsUseCase | uiState包含统计数据 |
| 2 | 刷新数据 | `refresh()` | homeStatsUseCase | 重新调用getStats |
| 3 | 加载失败 | 初始化 | useCase抛异常 | uiState包含错误信息 |

### 9.2 ContactsViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载联系人列表 | 初始化 | contactRepository | contacts有数据 |
| 2 | 搜索联系人 | `search("张")` | contactRepository | 过滤结果 |
| 3 | 按分组筛选 | `filterByGroup("朋友")` | contactRepository | 筛选结果 |
| 4 | 删除联系人 | `delete(id)` | contactRepository | 调用delete |

### 9.3 DivinationViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 梅花时间起卦 | `castMeihuaByTime()` | - | 生成有效MeihuaData |
| 2 | 梅花数字起卦 | `castMeihuaByNumber(3,5,2)` | - | 生成有效MeihuaData |
| 3 | 六爻起卦 | `castLiuyao()` | - | 生成有效LiuyaoData |
| 4 | 保存记录 | `saveRecord()` | divinationRepo | 调用insert |
| 5 | 重置状态 | `resetMeihua()` | - | 清空当前卦象 |

### 9.4 ContactDetailViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载联系人详情 | `loadContact(id)` | contactRepository | uiState包含联系人信息 |
| 2 | 加载关联纪念日 | `loadContact(id)` | anniversaryRepository | uiState包含纪念日列表 |
| 3 | 加载关联事件 | `loadContact(id)` | eventRepository | uiState包含事件列表 |
| 4 | 加载关联礼物 | `loadContact(id)` | giftRepository | uiState包含礼物列表 |
| 5 | 加载关联想法 | `loadContact(id)` | thoughtRepository | uiState包含想法列表 |
| 6 | 删除联系人 | `deleteContact()` | contactRepository | 调用delete |
| 7 | 更新亲密值 | `updateIntimacy(id, 80)` | contactRepository | 调用update |

### 9.5 AddContactViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 保存新联系人 | `saveContact(domain)` | contactRepository | 调用insert |
| 2 | 验证必填字段 | `saveContact(空名字)` | contactRepository | 不调用insert,返回错误 |
| 3 | 保存并添加分组 | `saveContact(含分组)` | contactRepository | 同时保存分组关联 |
| 4 | 保存并添加标签 | `saveContact(含标签)` | contactRepository | 同时保存标签关联 |

### 9.6 EventsViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载事件列表 | 初始化 | eventRepository | events有数据 |
| 2 | 按类型筛选 | `filterByType("MEET")` | eventRepository | 返回见面事件 |
| 3 | 搜索事件 | `search("聚餐")` | eventRepository | 过滤结果 |
| 4 | 按日期排序 | `sortByDate()` | eventRepository | 按时间倒序 |

### 9.7 EventDetailViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载事件详情 | `loadEvent(id)` | eventRepository | uiState包含事件信息 |
| 2 | 加载参与者 | `loadEvent(id)` | eventRepository | uiState包含参与者列表 |
| 3 | 删除事件 | `deleteEvent()` | eventRepository | 调用delete |

### 9.8 AddEventViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 保存新事件 | `saveEvent(domain)` | eventRepository | 调用insert |
| 2 | 验证必填字段 | `saveEvent(空标题)` | eventRepository | 不调用insert |
| 3 | 保存含参与者 | `saveEvent(含参与者)` | eventRepository | 同时保存参与者关联 |

### 9.9 AnniversariesViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载纪念日列表 | 初始化 | anniversaryRepository | anniversaries有数据 |
| 2 | 按类型筛选 | `filterByType("BIRTHDAY")` | anniversaryRepository | 返回生日 |
| 3 | 即将到来排序 | `sortByUpcoming()` | anniversaryRepository | 按日期排序 |

### 9.10 AnniversaryDetailViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载纪念日详情 | `loadAnniversary(id)` | anniversaryRepository | uiState包含纪念日信息 |
| 2 | 农历日期显示 | `loadAnniversary(农历纪念日)` | anniversaryRepository | 正确显示农历信息 |
| 3 | 删除纪念日 | `deleteAnniversary()` | anniversaryRepository | 调用delete |

### 9.11 AddAnniversaryViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 保存新纪念日 | `saveAnniversary(domain)` | anniversaryRepository | 调用insert |
| 2 | 农历纪念日 | `saveAnniversary(农历)` | anniversaryRepository | isLunar=true |
| 3 | 重复设置 | `saveAnniversary(每年重复)` | anniversaryRepository | isRecurring=true |

### 9.12 ChatViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载对话列表 | 初始化 | chatRepository | chats有数据 |
| 2 | 按联系人筛选 | `filterByContact(id)` | chatRepository | 返回该联系人对话 |
| 3 | 搜索对话 | `search("关键词")` | chatRepository | 过滤结果 |

### 9.13 ChatDetailViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载对话详情 | `loadChat(id)` | chatRepository | uiState包含对话信息 |
| 2 | 删除对话 | `deleteChat()` | chatRepository | 调用delete |

### 9.14 AddChatViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 保存新对话 | `saveChat(domain)` | chatRepository | 调用insert |
| 2 | 关联联系人 | `saveChat(含联系人)` | chatRepository | 正确关联 |

### 9.15 GiftsViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载礼物列表 | 初始化 | giftRepository | gifts有数据 |
| 2 | 按方向筛选 | `filterByDirection(isGiven=true)` | giftRepository | 返回送出的礼物 |
| 3 | 按类型筛选 | `filterByType(DIGITAL)` | giftRepository | 返回数码礼物 |
| 4 | 按联系人筛选 | `filterByContact(id)` | giftRepository | 返回该联系人礼物 |

### 9.16 ThoughtsViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载想法列表 | 初始化 | thoughtRepository | thoughts有数据 |
| 2 | 按类型筛选 | `filterByType(INSIGHT)` | thoughtRepository | 返回灵感类想法 |
| 3 | 删除想法 | `deleteThought(id)` | thoughtRepository | 调用delete |

### 9.17 FavoritesViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载收藏列表 | 初始化 | favoriteRepository | favorites有数据 |
| 2 | 按来源类型筛选 | `filterBySourceType("CONTACT")` | favoriteRepository | 返回联系人收藏 |
| 3 | 取消收藏 | `removeFavorite(id)` | favoriteRepository | 调用remove |
| 4 | 添加收藏 | `addFavorite(domain)` | favoriteRepository | 调用add |

### 9.18 FootprintsViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载足迹列表 | 初始化 | eventRepository | footprints有数据 |
| 2 | 按地点聚合 | `groupByLocation()` | eventRepository | 返回地点统计 |

### 9.19 ContactListViewModelTest (圈子)

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载圈子列表 | 初始化 | circleRepository | circles有数据 |
| 2 | 创建圈子 | `createCircle(domain)` | circleRepository | 调用insert |
| 3 | 删除圈子 | `deleteCircle(id)` | circleRepository | 调用delete |
| 4 | 添加成员 | `addMember(circleId, contactId)` | circleRepository | 调用addMember |

### 9.20 PhotoAlbumViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载照片列表 | 初始化 | eventRepository | photos有数据 |
| 2 | 按联系人筛选 | `filterByContact(id)` | eventRepository | 返回该联系人照片 |

### 9.21 DivinationHistoryViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载历史记录 | 初始化 | divinationRepository | records有数据 |
| 2 | 删除记录 | `deleteRecord(record)` | divinationRepository | 调用delete |

### 9.22 ProfileViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 加载用户信息 | 初始化 | settingsRepository | uiState包含用户名 |
| 2 | 更新用户名 | `updateUserName("新名字")` | settingsRepository | 调用setUserName |
| 3 | 更新签名 | `updateBio("新签名")` | settingsRepository | 调用setBio |

### 9.23 BackupViewModelTest

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 执行备份 | `backup()` | backupRepository | 调用backup |
| 2 | 执行恢复 | `restore(uri)` | backupRepository | 调用restore |
| 3 | 清空数据 | `clearData()` | backupRepository | 调用clearAllData |

---

## 十、DAO层仪器测试

### 10.1 ContactDaoTest

> 使用内存数据库 (Room.inMemoryDatabaseBuilder)

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 搜索联系人 | search("张") | 返回匹配结果 |
| 3 | 更新联系人 | update | 数据已更新 |
| 4 | 删除联系人 | delete | 数据已删除 |
| 5 | 分组关联 | insertGroup + getContactsByGroup | 返回分组内联系人 |

### 10.2 EventDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按日期范围查询 | getByDateRange | 返回范围内事件 |
| 3 | 参与者关联 | insert + getWithParticipants | 返回事件含参与者 |
| 4 | 删除事件 | delete | 数据已删除 |

### 10.3 AnniversaryDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按联系人查询 | getByContactId | 返回该联系人纪念日 |
| 3 | 农历纪念日 | insert(isLunar=true) | 正确保存农历标记 |
| 4 | 删除纪念日 | delete | 数据已删除 |

### 10.4 GiftDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按联系人查询 | getByContactId | 返回该联系人礼物 |
| 3 | 按方向查询 | getByIsGiven | 返回送出/收到的礼物 |
| 4 | 删除礼物 | delete | 数据已删除 |

### 10.5 CircleDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询圈子 | insert + getAll | 返回插入的数据 |
| 2 | 成员关联 | insertMember + getMembers | 返回圈子成员列表 |
| 3 | 删除圈子 | delete | 圈子和关联成员已删除 |
| 4 | 移除成员 | deleteMember | 成员已移除 |

### 10.6 ThoughtDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按联系人查询 | getByContactId | 返回该联系人想法 |
| 3 | 按类型查询 | getByType | 返回指定类型想法 |
| 4 | 删除想法 | delete | 数据已删除 |

### 10.7 TodoDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按联系人查询 | getByContactId | 返回该联系人待办 |
| 3 | 更新完成状态 | updateCompleted | 状态已更新 |
| 4 | 删除待办 | delete | 数据已删除 |

### 10.8 FavoriteDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按来源类型查询 | getBySourceType | 返回指定类型收藏 |
| 3 | 删除收藏 | delete | 数据已删除 |
| 4 | 重复收藏检测 | insert重复 | 不重复插入 |

### 10.9 ReminderDaoTest

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按联系人查询 | getByContactId | 返回该联系人提醒 |
| 3 | 标记完成 | markCompleted | 状态已更新 |
| 4 | 删除提醒 | delete | 数据已删除 |

---

## 十一、补充遗漏模块测试

### 11.1 ContactTagRepositoryImplTest

> 联系人标签仓库，独立于ContactRepository

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有标签 | `getAllTags()` | tagDao | 返回映射后的Domain列表 |
| 2 | 按ID查询标签 | `getTagById(id)` | tagDao | 返回对应标签 |
| 3 | 插入标签 | `insertTag(domain)` | tagDao | 调用insert |
| 4 | 更新标签 | `updateTag(domain)` | tagDao | 调用update |
| 5 | 删除标签 | `deleteTagById(id)` | tagDao | 调用delete |

### 11.2 ContactGroupRepositoryImplTest

> 联系人分组仓库，独立于ContactRepository

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 获取所有分组 | `getAllGroups()` | groupDao | 返回映射后的Domain列表 |
| 2 | 按ID查询分组 | `getGroupById(id)` | groupDao | 返回对应分组 |
| 3 | 插入分组 | `insertGroup(domain)` | groupDao | 调用insert |
| 4 | 更新分组 | `updateGroup(domain)` | groupDao | 调用update |
| 5 | 删除分组 | `deleteGroupById(id)` | groupDao | 调用delete |

### 11.3 SettingsViewModelTest

> 设置页ViewModel（主题/AI配置/连接测试），不同于ProfileViewModel

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 读取主题模式 | `themeMode` | settingsRepository | 返回默认SYSTEM |
| 2 | 设置主题模式 | `setThemeMode(DARK)` | settingsRepository | 调用setThemeMode |
| 3 | 读取AI配置 | `aiApiKey/aiBaseUrl/aiModel` | settingsRepository | 返回配置值 |
| 4 | 设置AI API Key | `setAiApiKey("sk-xxx")` | settingsRepository | 调用setAiApiKey |
| 5 | 设置AI Base URL | `setAiBaseUrl("https://...")` | settingsRepository | 调用setAiBaseUrl |
| 6 | 设置AI模型 | `setAiModel("gpt-4")` | settingsRepository | 调用setAiModel |
| 7 | 测试连接-成功 | `testConnection()` | aiRepository | testState=Success |
| 8 | 测试连接-失败 | `testConnection()` | aiRepository | testState=Error |
| 9 | 重置测试状态 | `resetTestState()` | - | testState=Idle |

### 11.4 AiViewModelTest

> AI深度解读ViewModel，管理流式分析状态

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 初始状态 | 初始化 | - | state=Idle |
| 2 | API Key未配置 | `startMeihuaAnalysis(...)` | settingsRepository(空key) | state=Error |
| 3 | 梅花分析-正常 | `startMeihuaAnalysis(data,...)` | aiRepository | state=Streaming→Result |
| 4 | 六爻分析-正常 | `startLiuyaoAnalysis(data,...)` | aiRepository | state=Streaming→Result |
| 5 | 分析-网络错误 | `startMeihuaAnalysis(...)` | aiRepository(抛异常) | state=Error |
| 6 | 分析-超时 | `startMeihuaAnalysis(...)` | aiRepository(超时) | state=Error("请求超时") |
| 7 | 重置状态 | `reset()` | - | state=Idle |
| 8 | 保存性别 | `saveGender("女")` | settingsRepository | 调用setAiGender |
| 9 | 保存出生日期 | `saveBirthDate("2000-01-01")` | settingsRepository | 调用setAiBirthDate |
| 10 | 刷新API Key状态 | `refreshApiKeyStatus()` | settingsRepository | apiKeyConfigured更新 |

### 11.5 MeihuaPromptBuilderTest

> 梅花提示词构建器，含复杂字符串拼接逻辑

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 系统提示词非空 | `getSystemPrompt()` | - | 返回非空字符串 |
| 2 | 系统提示词含关键规则 | `getSystemPrompt()` | - | 包含"体用""旺衰""自检" |
| 3 | 用户提示词-基本信息 | `buildUserPrompt(data,...)` | 完整MeihuaData | 包含占法、性别、出生日期 |
| 4 | 用户提示词-卦象信息 | `buildUserPrompt(data,...)` | 完整MeihuaData | 包含主卦、互卦、变卦 |
| 5 | 用户提示词-体用关系 | `buildUserPrompt(data,...)` | 完整MeihuaData | 包含体卦、用卦、动爻 |
| 6 | 用户提示词-八纯卦标记 | `buildUserPrompt(八纯卦,...)` | 乾为天 | 包含"八纯卦"警告 |
| 7 | 用户提示词-非八纯卦 | `buildUserPrompt(非纯卦,...)` | 水火既济 | 不包含"八纯卦"警告 |
| 8 | 用户提示词-问题嵌入 | `buildUserPrompt(data,...,"找工作")` | question="找工作" | 包含"找工作" |

### 11.6 LiuyaoPromptBuilderTest

> 六爻提示词构建器

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 系统提示词非空 | `getSystemPrompt()` | - | 返回非空字符串 |
| 2 | 系统提示词含六爻规则 | `getSystemPrompt()` | - | 包含"用神""六亲""纳甲" |
| 3 | 用户提示词-基本信息 | `buildUserPrompt(data,...)` | 完整LiuyaoData | 包含占法、性别、时间干支 |
| 4 | 用户提示词-卦象信息 | `buildUserPrompt(data,...)` | 完整LiuyaoData | 包含卦宫、六亲、世应 |
| 5 | 用户提示词-动爻标记 | `buildUserPrompt(含动爻,...)` | 有动爻 | 包含动爻信息 |

### 11.7 ChatDtoTest

> 远程聊天数据传输对象

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | ChatRequest构造 | `ChatRequest(model, messages)` | 参数 | stream=true, temperature=0.7 |
| 2 | ChatMessage构造 | `ChatMessage("user", "hello")` | 参数 | role="user", content="hello" |
| 3 | ChatStreamResponse解析 | `ChatStreamResponse(choices)` | 含choices | 正确解析 |
| 4 | ChatStreamResponse空choices | `ChatStreamResponse(null)` | null | choices=null不崩溃 |
| 5 | DeltaContent空content | `DeltaContent(null)` | null | content=null不崩溃 |

### 11.8 EventTypesTest

> 事件类型和来源类型常量

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 事件类型完整性 | `EventTypes` 所有常量 | - | 包含10种事件类型 |
| 2 | 来源类型完整性 | `SourceTypes` 所有常量 | - | 包含EVENT/DIALOG/PHOTO等 |
| 3 | 常量值唯一性 | 所有值去重 | - | 无重复值 |

### 11.9 CustomCategoriesTest

> 自定义类型分类常量

| # | 测试用例 | 方法 | 输入 | 预期输出 |
|---|---------|------|------|---------|
| 1 | 分类完整性 | `CustomCategories` 所有常量 | - | 包含10种分类 |
| 2 | 常量值唯一性 | 所有值去重 | - | 无重复值 |

### 11.10 ImageCacheManagerTest

> 图片缓存管理

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 复制图片到缓存 | `copyToCache(context, uri)` | context | 返回本地路径 |
| 2 | 判断本地路径 | `isLocalPath("file:///...")` | - | 返回true |
| 3 | 判断远程路径 | `isLocalPath("https://...")` | - | 返回false |
| 4 | 删除缓存图片 | `deleteCachedImage(path)` | - | 文件已删除 |

### 11.11 ReminderReceiverTest

> 提醒广播接收器（服务层）

| # | 测试用例 | 方法 | Mock对象 | 预期行为 |
|---|---------|------|---------|---------|
| 1 | 接收提醒广播 | `onReceive(context, intent)` | context + intent | 显示通知 |
| 2 | 接收开机广播 | `onReceive(context, BOOT)` | context + BOOT intent | 重新调度所有提醒 |
| 3 | 通知渠道创建 | `createNotificationChannel(context)` | context | 渠道已创建 |

### 11.12 DivinationRecordDaoTest (仪器测试)

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按类型查询 | getByType | 返回指定类型记录 |
| 3 | 更新分析内容 | updateAnalysis | 分析内容已更新 |
| 4 | 删除记录 | delete | 数据已删除 |

### 11.13 CustomTypeDaoTest (仪器测试)

| # | 测试用例 | 方法 | 预期行为 |
|---|---------|------|---------|
| 1 | 插入并查询 | insert + getAll | 返回插入的数据 |
| 2 | 按分类查询 | getByCategory | 返回指定分类类型 |
| 3 | 更新自定义类型 | update | 数据已更新 |
| 4 | 删除自定义类型 | delete | 数据已删除 |

---

## 十二、测试运行命令

```bash
# 运行所有单元测试 (JVM)
./gradlew testDebugUnitTest

# 运行指定模块测试
./gradlew testDebugUnitTest --tests "com.tang.prm.engine.*"

# 运行单个测试类
./gradlew testDebugUnitTest --tests "com.tang.prm.engine.core.GanZhiCalculatorTest"

# 运行仪器测试 (需要连接设备)
./gradlew connectedDebugAndroidTest

# 查看测试报告
# app/build/reports/tests/testDebugUnitTest/index.html
```

---

## 十三、实施顺序

| 阶段 | 内容 | 测试用例数 | 预计工作量 |
|------|------|-----------|-----------|
| **阶段1** | 引擎层 (GanZhi + WuXing + Meihua + Liuyao + PromptBuilder) | ~64 | 核心优先 |
| **阶段2** | 工具层 (DateUtils + LunarUtils + SqlUtils + ImageCacheManager) | ~24 | 快速完成 |
| **阶段3** | 领域模型 (IntimacyLevel + GiftType + ThemeMode + ThoughtType + EventTypes + CustomCategories + ChatDto) | ~30 | 简单 |
| **阶段4** | Mapper层 (11个Mapper双向转换) | ~44 | 简单重复 |
| **阶段5** | Repository层 (16个Repo CRUD) | ~83 | 需要 Mock |
| **阶段6** | ViewModel层 (25个VM交互逻辑) | ~87 | 需要 Mock |
| **阶段7** | 服务层 (ReminderReceiver) | ~3 | 需要 Mock |
| **阶段8** | DAO层 (11个DAO仪器测试) | ~43 | 需要设备 |
| **合计** | - | **~378** | - |

---

## 十四、测试命名规范

```kotlin
// 格式: `方法名_条件_预期结果`
@Test
fun `getWuXingByGan_甲_返回木`() { ... }

@Test
fun `castByTime_2024年1月1日午时_上卦为离`() { ... }

@Test
fun `toDomain_完整Entity_返回对应Domain`() { ... }

@Test
fun `searchContacts_关键词张_返回匹配结果`() { ... }
```
