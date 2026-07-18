# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.2] - 2026-07-17

### Added

- 首页加载骨架屏：加载中显示占位卡片而非空白,频道占位数与实际列表一致避免布局跳动
- 首页全新用户引导：无任何数据时显示引导卡片与"添加联系人"入口
- 首页错误重试入口：数据加载失败时显示错误提示与重试按钮,点击重试真正重新加载
- 首页平板设置入口：平板模式恢复设置图标,与其他界面一致
- 首页写操作失败反馈：保存装饰照片失败时显示消息条提示
- 数据库升级保护：迁移测试现覆盖至当前最新版本,确保未来版本升级时数据库结构完整、历史数据不丢失

### Changed

- 首页模块深度优化：基于模块深度审查报告完成 4 批共 66 项问题修复及 17 项新发现优化点,覆盖关键 Bug、架构重构、死代码清理、跨文件重复消除、超长函数拆分、性能优化与无障碍适配
- 数据层深度优化：基于模块深度审查报告完成 4 批共 71 项问题修复,覆盖关键数据完整性 Bug、网络层重试逻辑、跨聚合职责收敛、死代码清理、跨文件重复消除与代码质量提升
- 菜谱列表加载提速：菜谱列表原加载全部字段含步骤、食材等大段文本,现仅加载列表展示所需字段,详情页仍保留完整数据
- 联系人列表排序提速：联系人列表原按更新时间全表排序无索引,现已加索引
- 网络超时按场景分开：AI 对话与云盘文件传输原共用同一网络配置,现按场景独立配置,AI 短超时快响应,云盘长超时支持大文件
- 加密存储失败不再静默降级：加密存储不可用时原回退到明文存储暴露敏感凭据,现明确报错而非降级,并在界面提示用户
- 云盘证书安全收紧：云盘客户端原默认信任所有证书存在中间人风险,现仅在用户显式开启自签名证书时使用并明确警告
- 菜谱照片扩展名按实际类型保存：菜谱照片原一律保存为 jpg,会丢失 png 透明背景与 gif 动图,现按源文件实际类型选择扩展名
- 待办排序与"截止优先"语义对齐：想法与待办原无截止日期的排在最前,现排在有截止日期的之后
- 圈层删除改为递归：删除圈子原仅删一层,孙圈会被孤立为根圈,现递归删除全部后代
- 首页关系概览颜色统一：朋友档颜色原与其他模块不一致,现统一从主题色取值
- 首页打字机动画后台暂停：打字机动画原在后台继续推进浪费电量,现切后台时立即暂停,且长延迟点也能正确响应
- 首页错误重试真正生效：原重试按钮为空函数点击无反应,现点击真正重新加载数据
- 首页轨道日历跨日刷新：今日日期与今日事件原在跨日后仍显示昨日数据,现跨日时自动刷新
- 首页平板时间层级优化：平板模式顶栏原显示秒级时间与日记横幅的日级时间语义冲突,现顶栏不显示时间,交由横幅统一呈现
- 首页秒级时钟后台暂停：顶栏秒级时钟原在后台持续更新,现切后台时暂停
- 首页时间轴副标题去重：非今日事件原在时间轴左侧与标题下方重复显示"X天前",现标题下方改为事件类型
- 首页月份切换按钮触摸区域：上下月按钮原小于推荐触摸尺寸,现恢复标准触摸区域
- 首页倒计时魔法数字清理：事件倒计时的小时计算原用魔法数字,现改用时间常量
- 首页控制器架构下沉：首页控制器原直接依赖数据仓库,统一改为通过用例访问
- 首页状态结构拆分：首页状态原为单一数据类承载 20 余字段,现拆分为数据态与弹窗态,与项目其他模块约定一致
- 首页统计配置统一：人物/事件/礼物/纪念日/对话 5 项统计的标签、颜色、图标原在多处重复定义,统一为单一来源
- 首页频道信号统一：频道列表与信号强度原分离维护,新增频道需改两处,现绑定到频道定义,新增频道只需改一处
- 首页轨道日历超长函数拆分：轨道日历原 250 余行单块绘制代码,按视觉层拆分为 15 个职责单一的绘制函数
- 首页频道卡片超长函数拆分：频道卡片原 160 行单函数,拆分为图标、进度条、信号格三个子组件
- 首页进度条组件统一：首页三处进度条（横向条、环形、信号格）原各自实现,统一为共享组件
- 首页频道图标组件统一：频道卡片与快捷入口两处图标圆原各自实现,统一为共享组件
- 首页粒子动画初始位置稳定：粒子初始位置原每次重组都随机,现固定种子保证一致
- 首页农历纪念日显示修正：农历纪念日原按公历直接解析导致显示在错误月份,现按公历生效日期显示
- 首页品牌文案统一：首页标语原在两处硬编码,统一为常量
- 首页问候语集中管理：时段问候语原硬编码分支,改为枚举集中管理
- 首页平板信号计算优化：平板模式原仍计算频道信号强度但无人读取,现仅在手机模式计算
- 首页频道卡片箭头尺寸优化：频道卡片右箭头原偏大,缩小与整体更协调
- 首页快捷入口图标空间优化：快捷入口图标圆原预留光晕空间但实际无光晕,现按需预留
- 首页骨架屏占位数对齐：骨架屏频道占位原只显示 3 个,实际有 9 个,现与实际列表一致

### Fixed

- 修复菜谱图片在清理孤儿图片或备份恢复时被误删且无法找回
- 修复菜谱每次编辑保存后标签关联永久丢失
- 修复新建菜谱保存后标签关联从未被持久化
- 修复无关联联系人的纪念日保存时崩溃
- 修复数据库恢复失败时应用直接闪退无任何错误提示
- 修复数据库恢复成功后应用可能无法自动重启
- 修复清理全部数据后应用可能无法自动重启
- 修复 AI 对话接口在网络抖动时重复扣费、重复消耗配额
- 修复网络重试对非幂等写操作（如 AI 对话）盲目重试
- 修复网络重试漏掉常见连接异常类型导致放弃重试
- 修复网络重试不处理服务器临时错误（5xx、429）
- 修复待办事项无截止日期的排在最前与"截止优先"语义冲突
- 修复删除联系人后收藏记录残留为孤儿数据
- 修复删除礼物或想法后收藏记录残留为孤儿数据
- 修复礼物编辑时照片因竞态条件可能被误删
- 修复礼物编辑无事务保护,中途失败会留下半更新状态
- 修复数据库回滚后 wal/shm 文件残留导致下次打开异常
- 修复云盘备份清单保留已删除文件的过期条目,恢复时尝试拉取已不存在的文件
- 修复圈层删除时孙圈及更深后代被孤立为根圈,层级关系破坏
- 修复数据库迁移测试形同虚设：遗漏迁移注册、目标版本落后、列名错误,整个迁移测试套件实际处于失效状态
- 修复菜谱数据完整性与图片备份恢复链路：菜谱图片此前不参与备份恢复,触发"备份恢复"会丢失全部菜谱照片
- 修复首页关系概览朋友档颜色与其他模块不一致
- 修复首页数据加载失败时卡死在加载中无错误提示
- 修复首页重试按钮点击无反应
- 修复首页打字机动画在后台继续推进浪费电量
- 修复首页打字机长延迟点切后台仍继续推进
- 修复首页轨道日历跨日后仍显示昨日数据
- 修复首页农历纪念日显示在错误月份
- 修复首页时间格式化未指定语言区域
- 修复首页平板模式无设置入口
- 修复首页平板模式顶栏秒级时间与横幅日级时间语义冲突
- 修复首页顶栏秒级时钟在后台持续更新
- 修复首页时间轴非今日事件副标题与时间标签重复
- 修复首页月份切换按钮触摸区域过小
- 修复首页全新用户无引导卡片
- 修复首页加载中无骨架屏
- 修复首页保存装饰照片失败无反馈
- 修复首页轨道日历今日事件在跨日后仍显示昨日事件

### Removed

- 清理数据层死代码：33 个以上无调用方的 DAO 方法、2 个仅测试使用的映射方法、3 个工具类死方法、1 个完全无引用的工具文件,以及多个仓库接口的死方法
- 清理首页模块死代码：5 个从未读取的状态字段、2 个仅测试调用的方法、频道卡片死参数、未使用的颜色别名与导入
- 清理首页测试目录错位：测试原落在应用模块且包名不一致,迁移至首页模块

## [1.5.1] - 2026-07-17

### Added

- 事件模块测试守护：为事件列表与新建事件两个核心控制器补充单元测试，覆盖月份范围计算、日期筛选、过滤逻辑、保存校验、新建与编辑分支等场景；并扩展事件详情控制器的测试用例，覆盖"事件有效但不存在"（如已被删除）的兜底场景
- 反思模块测试守护：为想法与收藏两个核心控制器补充单元测试，覆盖想法增删改、待办切换、收藏筛选等场景

### Changed

- 事件模块深度优化：基于模块深度审查报告完成 4 批共 36 项问题修复，覆盖加载失败兜底、健壮性强化、用户体验、架构重构、死代码清理与跨文件重复消除
- 事件详情页加载失败兜底：手机与平板模式下，事件加载失败或已被删除时原显示空白，现显示未找到提示与返回按钮；加载中显示进度指示器
- 事件参与者头像可点击：手机详情页参与者头像原点击无响应，现点击跳转到对应联系人详情
- 事件平板空状态引导：平板模式空状态补充"新建事件"按钮，与手机版一致
- 事件当日列表可展开：平板模式当日事件列表原限制 5 条且无展开，现支持点击展开/收起查看全部
- 事件分享异常反馈：分享时图片处理失败原静默吞没，现补充日志便于定位
- 事件保存校验反馈：标题或类型为空时原静默返回，现通过消息条提示"请填写标题和事件类型"
- 事件照片索引修复：详情页照片浏览原使用位置查找，遇到重复照片时点击第二张打开的是第一张，现改用全局索引
- 事件日历花费字段清理：日历统计原保留总额计算但从未显示，违反"全应用去除花费字段"约定，彻底移除
- 事件日期选择器逻辑简化：原创建两个日历实例合并日期与时间，抽取为公共函数，调用处简化
- 事件创建者常量化：详情页"我"硬编码提取为常量，便于未来扩展
- 事件控制器架构下沉：事件列表控制器原直接依赖多个数据仓库，统一改为通过用例访问；新建事件控制器原直接读取参考数据，统一改为通过用例访问
- 事件分享工具独立文件：分享函数原与界面组件混杂在同一文件，移至独立工具文件，职责更清晰
- 事件平板冗余调用清理：平板详情页原重复设置事件 ID，因控制器初始化时已从导航参数读取，属冗余调用，移除
- 事件模块跨文件重复消除：天气/情绪颜色解析、事件类型显示名、自定义类型查找、参与者首字母占位、日期分组、筛选标签等 6 类重复逻辑抽取为共享工具或组件
- 事件模块超长函数拆分：平板详情页主视觉区与信息侧栏、日历组件的日期计算与事件卡片、事件主界面与平板左右面板等多个超长函数按职责拆分为职责单一的子组件
- 事件模块同名重载区分：拍立得照片组件原编辑态与查看态同名易混，编辑态重命名以区分语义
- 事件模块颜色解析统一：事件主题色原有两个同名函数逻辑不同，统一为单一函数
- 反思模块深度优化：基于模块深度审查报告完成 4 批共 33 项问题修复及 4 项新发现优化点，覆盖关键 Bug、假数据清理、主题适配、架构下沉、死代码清理与跨文件重复消除
- 相册来源标识统一：相册事件视图的来源标签原因标识大小写不匹配导致颜色、图标与分组标题全部失效，统一改用常量
- 足迹年份筛选增强：足迹年份标签原无条目计数，现显示各年足迹数；年份计算改用更稳定的时间接口
- 想法待办切换清理：想法从待办切回普通时，原残留到期日期，现自动清空
- 收藏列表完整显示：收藏列表原限制 7 条，现完整显示并可滚动查看全部
- 想法卡片展开检测优化：想法卡片"展开"按钮原按字符数判断是否显示，现按实际是否截断判断
- 想法等级加载占位：想法等级横幅原在数据加载前显示空白，现显示加载指示器
- 足迹时间轴懒加载：足迹时间轴原将全部内容塞入单个列表项导致无法按需加载，现按月份分组懒加载，长列表滚动更流畅
- 足迹顶部标签动态化：足迹顶部卡片标签原固定为"最新足迹"，现根据年份筛选显示"X年足迹"
- 相册照片网格统一：每日视图与事件视图的照片网格原为两套重复实现，统一为共享组件
- 想法样式与联系人标签统一：想法类型样式与联系人头像标签原在各界面重复实现，统一为共享组件与扩展属性
- 反思平板内边距适配：足迹、想法、收藏三个界面平板模式原无内边距，现增加居中内边距
- 收藏夹终端主题色适配：收藏夹终端风格配色原硬编码，现适配深浅色模式
- 相册大图手势统一：相册大图查看原手写双指检测，统一为标准手势检测
- 足迹事件类型样式统一：足迹卡片事件类型原重复查找且标签与图标来源可能不一致，统一返回完整样式
- 反思模块架构下沉：想法与收藏两个控制器原直接依赖数据仓库，统一改为通过用例访问
- 反思模块天气颜色缓存：足迹卡片天气颜色原每次重绘都重新解析，现缓存复用
- 收藏夹树状视图性能优化：树状视图原对每个分类都全量筛选一次，现预计算分组，查找更高效
- 想法联系人横滚完整显示：想法联系人横滚条原限制 8 个，现可滚动查看全部

### Fixed

- 修复事件详情页加载失败或事件已删除时显示空白无返回按钮
- 修复事件详情页参与者头像点击无响应
- 修复事件平板空状态无新建引导
- 修复事件当日列表超过 5 条无法展开查看全部
- 修复事件保存时标题或类型为空静默返回无反馈
- 修复事件详情页重复照片点击打开错误
- 修复事件分享图片处理异常静默吞没
- 修复事件日历统计残留花费字段计算
- 修复相册事件视图来源标签颜色、图标与分组标题失效
- 修复足迹加载时显示空白无加载指示
- 修复想法从待办切回普通时到期日期残留
- 修复收藏列表限制 7 条无法查看全部
- 修复想法等级横幅加载前显示空白
- 修复足迹时间轴卡片事件类型标签与图标颜色来源不一致
- 修复想法联系人横滚条限制 8 个无法查看全部

### Removed

- 清理事件模块死代码：未使用的联系人选择、日期选择、事件删除、删除确认等方法与状态，未使用的照片删除方法，未使用的表单参数等
- 清理反思模块死代码：足迹列表三个未使用事件类型函数、足迹控制器未使用的筛选字段与方法、收藏控制器未使用的移除方法、想法控制器闲置的搜索状态管理器、足迹控制器残留的联系人列表死字段
- 清理收藏夹假数据：移除日志中的虚假删除计数与查询耗时，以及等级、经验值、数据量等装饰性假数据字段

## [1.5.0] - 2026-07-16

### Added

- 菜谱功能：全新菜谱管理模块，记录你的拿手菜与珍藏食谱，让每一道家常味道都有迹可循
  - 三种浏览方式（顶部视图切换按钮与事件界面风格统一，位于右上角"新增"按钮旁）：
    - 食谱清单：紧凑列表视图，一行一菜，左侧缩略图配右侧菜名、星级评分与标签，适合快速翻阅全部菜谱
    - 美食画廊：大图卡片视图，满宽成品照配底部渐变遮罩与悬浮标题，突出视觉诱惑，适合欣赏已收藏的佳作
    - 菜品导览：分类分组浏览，按菜系或类型分组展示，适合按场景挑选今日做法
  - 菜谱详情页（杂志式设计）：
    - Hero 区：高幅成品照配大圆角与底部渐变遮罩烘托氛围；返回/收藏/编辑三个玻璃质感圆形操作按钮独立成栏置于照片上方，避免遮挡画面并避开状态栏
    - 标题区：大号粗体菜名搭配金色星级评分徽章，彰显菜品"地位"
    - 食材清单：阴影外卡包裹，内部按食材类别分色子卡展示（蔬菜绿/肉类红/海鲜蓝/调料金），一眼看清所需材料
    - 烹饪步骤：时间线布局呈现，编号圆点配主题色条贯穿，步骤描述清晰可循，做菜时跟着步骤走不迷路
    - 标签 chips：细边框样式，标记菜系、口味、难度等自定义分类
    - 关联联系人：绿色描边头像展示，记录"谁教的做法"或"谁爱吃的菜"，让菜谱与人物故事相连
    - 画廊照片区：带阴影的成品照展示，多角度呈现菜品
    - 备忘笔记区：紫色强调条配独立配色，记录火候心得、替代食材、家人口味等私人备注
    - 删除按钮：全宽描边样式，独立于其他操作，避免误触
  - 新建/编辑菜谱页：表单含菜名、成品照片、星级评分（0-5 星）、食材清单（动态增删）、烹饪步骤（动态增删）、标签、备忘笔记、关联联系人，保存时自动记录创建与更新时间
  - 数据模型：菜谱表存储名称、照片、评分、食材列表、步骤列表、标签列表、备注、关联联系人、时间戳，列表字段以 JSON 数组形式持久化

### Changed

- 首页统计查询优化：首页信号卡片显示的事件数、礼物数、对话数等统计，改用轻量计数查询直接从数据库读取数量，避免此前将完整数据列表加载到内存再统计的方式，首页刷新更省内存
- 首页重绘优化：通过稳定时间状态与点击回调，减少首页界面在数据未变化时的不必要重绘，滑动更流畅
- 搜索防抖优化：所有列表页搜索框统一改为条件防抖——清空搜索框时立即响应恢复全量列表，仅在输入关键词时才触发防抖，解决此前清空搜索框后列表短暂滞后的体验问题
- 通用列表解析工具抽取：将各模块重复实现的"JSON 数组与逗号分隔字符串双格式解析"逻辑抽取为公共工具，多模块统一调用，消除重复代码
- 通用界面组件抽取：将多个详情页重复实现的"分区卡片"和"图标渐变标题"组件抽取到公共组件库，后续详情页可直接复用
- 统一 JSON 序列化库：全项目统一使用同一套序列化方案，移除冗余的 JSON 库依赖，减少安装包体积，消除运行时反射开销
- 梅花易数引擎加固：修复多处非空断言风险，八卦索引访问增加越界校验，遇异常时抛出描述性错误便于定位
- 网络重试机制优化：降低最大重试次数（从 3 次降为 2 次）避免用户长时间等待无效响应，重试间增加随机抖动，避免多设备同时遇到网络抖动后同步重试对服务器造成二次压力
- 订阅价格格式化统一：将分散在多处的价格格式化逻辑抽取为公共函数，统一管理各币种符号与金额展示；新增未知币种兜底分支，避免显示空字符串
- WebDAV 客户端日志补全：异常捕获处补充警告日志记录堆栈，便于线上问题定位，不再静默吞没
- 备份模块重构：将此前的超大类按职责拆分为三个辅助类（打包解包工具、图片引用查询、文档操作助手），主类仅保留流程编排；对外接口完全不变，调用方零感知，后续维护与扩展更清晰
- 事件查询性能优化：将"过滤对话类型事件"的逻辑从内存层下沉到数据库查询层，事件列表刷新时不再加载对话事件到内存再丢弃，减少内存开销
- 云同步模块重构：将云同步主类按职责拆分为"配置存储"（负责服务器地址与密码的持久化、旧版密码迁移）和"文件差异计算"（负责本地与云端的对比）两个独立辅助类，主类仅保留同步流程编排；配置读写、差异对比与同步调度职责分离，后续单独调整任一部分互不影响
- 菜谱业务逻辑下沉：将菜谱详情页的食材份量缩放计算、新建菜谱时的食材合并与空行过滤等业务规则，从界面层下沉到领域层，界面层仅负责状态展示与调用；业务规则集中管理，后续调整份量换算或保存策略无需改界面
- 日期格式化统一：备份文件名的时间戳解析与展示格式统一收纳到日期工具中，云同步界面、云端备份列表、备份文件详情页均走同一套格式化入口，消除散落各处的重复格式化代码
- 分区标题组件统一：将占卜模块与设置模块各自重复实现的"终端风分区标题"和"简约分区标签"统一收纳到公共组件库，后续各界面统一复用，消除复制粘贴
- 通用标题组件扩展：将表单分区标题组件扩展支持自定义图标与配色，订阅统计页的"内联分区标题"统一改用公共组件，消除订阅模块的重复实现
- 长函数拆分：将云同步主类、AI 对话仓库、新建联系人、相册聚合等四处的超长函数按职责拆分为多个小函数，单个函数职责单一，便于阅读与定位问题
- 接口转发层精简：移除两个仅做"原样转发"的中间层（云同步与备份恢复），界面层直接调用对应仓库；中间层无任何业务逻辑属过度设计，移除后调用链路更短、依赖更直接
- 备份模块命名规范化：将唯一未按"XxxImpl"命名的备份主类重命名为统一命名风格，并将进程重启副作用从数据层提取到应用层接口，数据层不再直接决定应用生命周期
- 菜谱关联联系人批量插入优化：菜谱保存时关联联系人原为循环单条插入，联系人数较多时事务持锁时间线性增长；改为批量插入，减少数据库锁竞争与界面卡顿
- 菜谱与礼物照片并发复制：保存菜谱或礼物时照片原为串行复制，多张原图时用户等待感强；改为并发复制，保存响应更快
- 多界面写操作异常处理统一：订阅、首页、事件、占卜、设置、收藏等多个界面的删除/更新/写入操作未捕获异常，数据库约束冲突或 IO 异常时直接崩溃；统一封装错误处理包装器，失败时记录日志并反馈错误状态
- 公共头像与图片组件加占位：公共头像组件与图片槽位组件加载图片时无占位与错误占位，首帧空白与加载失败无视觉反馈；改为统一加载态与错误态占位，列表滚动不再出现"白块闪烁"，头像加载失败显示首字母兜底
- 菜谱详情页参数列表精简：菜谱详情内容组件参数达 10 个，每个不稳定的回调在父级重组时都会触发子级重组；将回调封装为单一数据类，子组件可跳过不必要的重组
- 菜谱与订阅颜色常量统一：菜谱星级金色、订阅图表调色板、联系人详情图标色、订阅表单图标色等多处颜色在各自文件分别定义或硬编码，暗色模式无法自适应；抽取到主题颜色常量体系统一管理
- 平板路由修复：修复平板模式下部分路由参数传递的遗留问题
- 联系人模块深度优化：基于模块深度审查报告完成 5 批共 35 项问题修复，覆盖崩溃风险、空状态缺失、拖拽排序缺陷、写操作异常处理、性能与体验、架构重构、死代码清理与跨文件重复消除
- 联系人详情页空状态补全：事件、纪念、礼物、对话、想法五个分区原列表为空时显示空白，补全空状态图标与文案
- 联系人拖拽排序体验修复：修复长按拖拽首次交换后手势中断、进入排序模式顺序突变、退出排序模式顺序丢失三项缺陷，并持久化拖拽结果
- 联系人列表视图模式记忆：移除每次返回列表页强制重置为网格视图的逻辑
- 联系人详情页兜底强化：加载失败或联系人已删除时显示未找到提示与返回按钮；头像为空时显示姓名首字母占位；认识时长按天粒度刷新
- 联系人卡牌最近互动时间真实化：原硬编码显示固定时间，改为基于真实最后互动时间计算
- 联系人详情平板巨型文件拆分：将千行级单一文件按职责拆分为顶栏、主视觉区、内容区、卡片组件四个独立文件
- 联系人新建表单架构下沉：新建联系人界面原直接依赖多个数据仓库，统一改为通过用例访问
- 联系人详情跨端数据逻辑共享：手机与平板详情页原各自实现事件类型解析、礼物方向、纪念日颜色等逻辑，抽取为共享工具函数
- 联系人亲密度进度条统一组件：原多处重复实现，抽取为共享组件，顺便修复分数越界导致进度条溢出
- 联系人卡牌正弦波动画性能优化：降低像素采样密度，减少每帧重建路径的开销
- 联系人平板列表嵌套懒加载优化：去除同方向嵌套懒加载容器，改为扁平化分组项
- 联系人卡牌扫描线动画周期统一：一处魔法数字统一为动画令牌
- 联系人拖拽阈值密度适配：原硬编码像素值改为基于屏幕密度转换，跨密度表现一致
- 联系人超长组件拆分：卡牌背面、想法详情对话框、平板内容区等超长函数按区域拆分为多个职责单一的子组件
- 联系人平板详情内容区参数精简：多参数函数的回调合并为单一数据类

### Fixed

- 修复订阅详情页英镑符号显示缺失：某分支未返回英镑符号导致订阅详情页价格显示为空
- 修复订阅价格未知币种显示空字符串：价格格式化缺少兜底分支，未知币种金额不显示
- 修复 WebDAV 客户端异常被静默吞没：多处异常捕获块为空实现，导致线上问题无法定位
- 修复梅花易数引擎多处非空断言风险：八卦索引直接访问存在越界崩溃风险
- 修复菜谱详情页顶栏紧贴状态栏：初始设计返回/收藏/编辑按钮叠在 Hero 照片上，既遮挡画面又紧贴状态栏；改为将操作按钮上移形成独立操作栏，照片下移避让状态栏，并微调照片高度使整体更协调
- 清理礼物与事件界面残留的「花费」字段：硬约束要求全应用不再使用花费字段，但事件日历统计卡、事件标签、礼物详情卡、礼物磁带卡、联系人详情卡、联系人平板详情卡共 6 处界面仍渲染历史金额；本次彻底从展示侧移除金额分支，并清理事件日历的总额统计与格式化函数
- 修复首页统计在数据源类型变更时崩溃：首页信号卡片聚合多源数据时使用不安全强转，一旦上游返回类型变化即抛类型转换异常导致首页白屏；改为安全转换 + 默认值兜底
- 修复全局主题在嵌入式上下文中崩溃：主题组件直接将视图上下文强转为 Activity，在弹窗、菜单等包装上下文中会崩溃；改为递归解包上下文后安全获取 Activity，无法获取时跳过状态栏配置
- 修复自动备份静默失效：应用初始化协程未捕获异常，一旦指纹计算失败则后续自动备份永不触发且用户无感知；补充异常捕获与错误日志，失败时明确提示自动备份可能失效
- 修复纪念日提醒触发时崩溃：提醒广播接收器未配置异常处理器，Hilt 入口不可用时直接崩溃且无法标记提醒完成；补充异常处理器，失败时记录日志并确保广播结果完成
- 修复农历转换并发崩溃：农历工具单例的缓存 Map 为普通可变 Map，多协程并发调用时可能触发并发修改异常；改为并发安全 Map，消除联系人详情、纪念日、事件列表等并发场景的崩溃风险
- 修复数据库恢复失败后数据库损坏：备份恢复失败时回滚操作的异常被静默吞没，调用方误以为回滚成功但实际数据库文件可能已损坏；新增致命错误类型，回滚失败时明确上报并引导用户重新安装
- 修复平板对话界面硬编码白色背景：平板对话三栏布局中左栏、搜索框、右栏角色面板、内层头像框共 4 处硬编码白色，不跟随主题；改为使用主题的表面色，未来主题微调或切换暗色时这些色块不再脱节
- 修复平板事件详情硬编码绿色圆点：事件详情杂志式布局的状态圆点固定为绿色，与事件类型颜色脱钩（聚餐应为琥珀、通话应为紫等）；改为使用事件类型的主题色
- 修复平板对话列表自造头像组件：平板对话左栏列表项用 Box+AsyncImage+Text 重新拼装头像，缺少公共头像组件的兜底字体与形状统一处理；改为复用公共头像组件
- 修复足迹页 Hero 卡片自造 Surface：足迹页主卡片用 Surface 自行拼装形状、边框、阴影与点击行为，与公共卡片组件重复；改为复用公共卡片组件
- 修复多模块绕过主题系统自造暗色判断：圈子、订阅、对话详情等模块直接判断系统暗色模式而非使用主题语义色，当用户手动选择与系统不同的主题时颜色会与主题不一致；改为使用主题的语义色
- 修复图片文件管理器异常吞没：图片复制到内部存储、JSON 照片计数等操作在异常时静默返回 null 或 0，用户选图后部分图片静默丢失且无日志；补充警告日志便于定位
- 修复事件分享与 AI 对话静默吞没异常：事件详情分享时图片 URI 处理失败完全静默，用户点击分享无反应；AI 流式对话中损坏的数据块静默跳过，用户误以为回复完整；改为补充日志与可恢复提示
- 修复平板模式导航异常被静默：平板模式导航跳转中打开外链失败完全静默，用户点击"立即更新"无反应；改为补充错误反馈
- 修复联系人详情页五个分区的空状态文案与图标永不显示：调用方传入的暂无事件记录等提示被注解掩盖而丢失
- 修复联系人拖拽排序首次交换后中断、进入排序模式顺序突变、退出排序模式顺序丢失
- 修复联系人亲密度进度条分数越界导致进度条溢出与整行布局错位
- 修复联系人新建保存无反馈：保存失败静默吞没异常且可重复触发多次写入
- 修复联系人详情四个写操作静默失败：删除联系人、收藏切换、待办切换、删除想法均无异常处理
- 修复联系人列表与详情数据流无错误兜底：上游异常导致界面永久冻结、搜索筛选失效
- 修复联系人表单助手数据库操作异常吞没：新增自定义类型与生日纪念日同步失败时无反馈
- 修复联系人详情页头像为空显示空白圆
- 修复联系人加载失败或已删除时页面空白无返回按钮
- 修复联系人详情认识时长跨日不刷新
- 修复联系人卡牌最近互动时间硬编码不反映真实数据

### Removed

- 清理死代码：被公共组件替代的旧版平板顶栏实现、未被任何界面引用的常量文件、多处未使用的导入与函数参数；同步清理编译后新发现的未使用导入
- 清理无业务逻辑的中间转发层：云同步与备份恢复两个中间层所有方法均为原样转发至仓库，无任何校验、转换或聚合逻辑，属过度设计；移除后界面层直接注入对应仓库
- 清理未被调用的联系人展示信息查询：该查询自创建以来无任何界面或代码调用，属遗留死代码
- 清理数据层对占卜引擎的死依赖：数据层声明依赖占卜引擎模块，但实际零代码引用，真正使用方是占卜功能模块；移除该声明，分层依赖更准确
- 清理联系人模块死代码：未被调用的分组与亲密度筛选状态、农历生日切换函数、分组数据加载、未使用参数与导入、冗余颜色别名、主从模式遗留的死代码等
- 清理联系人表单助手未调用的生日纪念日同步方法：真实生效逻辑已在用例层

## [1.4.1] - 2026-07-10

### Added

- 内部测试构建类型：新增可与正式版共存的测试构建版本，启用混淆但保留调试能力，便于在真实环境中验证功能
- 数据库稳定性增强：为联系人、待办、提醒、圈子等核心数据表补充外键约束，删除联系人时自动清理关联的待办和提醒，确保关联数据的一致性
- 统一构建配置：引入统一的构建配置体系，集中管理所有模块的编译选项与代码检查，消除各模块重复配置
- 依赖打包管理：将相关依赖打包成集合统一声明，简化各模块的依赖配置
- 公共界面组件：抽取平板模式通用的顶栏、搜索栏、视图切换按钮等组件，减少重复代码
- 统一形状体系：建立项目级圆角形状定义，替代各组件自行定义的散落值
- 尺寸常量集中管理：将散落各处的尺寸数值统一收纳到尺寸常量文件中
- 重组优化注解：为不可变数据类添加注解，减少界面不必要的重绘
- 对话模块测试覆盖：为对话列表、对话详情、新建对话三个界面补充单元测试，提升对话功能的可靠性
- 数据库迁移测试：为近两个版本的数据库升级路径补充自动化验证，确保升级过程数据完整
- 加密降级提醒：当加密存储因设备原因降级为明文时，在界面上提示用户，避免无感知的安全风险
- 混淆规则完善：补充注解保留和导航组件的混淆规则，避免正式版运行时错误

### Changed

- 应用更新检查优化：将版本检查逻辑从界面层下沉到数据层，架构更清晰，后续维护更便捷
- 颜色体系统一：将三种混用的颜色定义风格统一到主题系统，深色模式下配色更协调
- 亲密度配色统一：平板联系人页改用项目统一的亲密度配色体系，与手机端保持一致
- 视图状态拆分优化：将多个界面的状态对象拆分为数据状态和对话框状态，避免弹窗变化触发整屏重绘
- 状态订阅模式统一：将多个界面各自独立订阅改为统一聚合订阅，消除数据闪烁和顺序不可控问题
- 状态订阅超时统一：将各界面状态订阅的超时时间统一为标准值，消除不一致
- 统一错误处理：统一各界面处理错误的策略，消除吞掉、弹提示、只记日志等不一致风格
- 事件详情更新事务化：事件更新涉及多表写入，改为事务包裹，避免中途出错留下半新半旧数据
- 圈子排序事务化：圈子排序更新改为事务操作，避免中途出错留下不完整排序
- 设置存储异步化：将同步写入改为异步写入，避免阻塞主线程
- 对话详情事件通道扩容：将零容量通道改为缓冲通道，避免界面未就绪时事件丢失
- 首页平板页数据读取异步化：将首页平板页直接读取本地配置改为通过视图模型异步读取，避免主线程卡顿
- 备份图片引用查询优化：备份时改为直接查询图片字段，避免全表数据加载到内存再过滤
- 备份初始化移出主线程：将备份界面初始化时的文件读取操作移出主线程，避免界面卡顿
- 模块依赖方向修正：修正界面基础模块反向依赖数据实现层的问题，恢复正确的依赖方向
- 平板联系人网格懒加载：平板联系人网格改为按行懒加载，避免一个分组内所有卡片同时组合
- 对话平板页视图模型稳定性：修复切换时视图模型重建导致草稿和滚动位置丢失的问题
- 数据库迁移解析修复：数据库迁移中含逗号的值此前会被错误分割，改为标准 JSON 解析
- 静态代码分析严格化：静态检查从宽松模式切换为严格模式，配置项目专属规则集与基线文件，持续保障代码质量

### Fixed

- 修复删除联系人不清理关联数据：待办事项和提醒此前会残留为无法追溯的孤儿记录
- 修复备份恢复后界面卡在"恢复中"：恢复完成后进程被重启导致界面永远收不到成功信号
- 修复首页问候语后台无限循环：问候语更新协程在界面销毁后仍在后台运行，耗电且引发测试异常
- 修复 WebDAV 同步页一进就自动联网：页面初始化时直接发起网络请求，每次配置变更都会重复触发
- 修复订阅列表副作用位置错误：副作用写在了数据流转换中而非收集处，数据流出错时状态不一致
- 修复拍立得照片删除按钮位置错误：删除按钮叠在照片左上角而非预期的右上角
- 修复平板首页对话统计图标用错：对话数误用了联系人图标，应为聊气泡图标
- 修复浮动动画性能退化：动画状态每帧变化导致协程每秒创建销毁数十次，退化为性能极差的模式
- 修复平板装饰卡片动画不可暂停：切走后动画仍在后台运行，改为可暂停模式
- 修复全息卡片组件名不副实：两个全息效果组件标了参数却从不使用，实际不绘制任何内容
- 修复平板纪念日页空回调参数：函数声明了点击回调但永远传空，是死参数
- 修复玻璃风导航项布局抖动：选中状态切换时宽度变化导致相邻项微抖
- 修复手机版列表动画延迟：线性列表使用了错落入场动画导致滚动时动画重复触发和后面卡片延迟出现，已移除
- 修复非平板模式下纪念日详情页编辑和删除按钮重复显示：顶部栏和内容区同时显示了两组相同按钮，现已根据屏幕模式仅在对应位置显示一次
- 修复圈子模块横向依赖：圈子模块此前直接依赖了首页模块，违反模块间禁止互依赖的约束
- 修复 APK 签名缺少 V3：补充 V3 签名支持，为未来密钥轮换预留能力
- 修复测试形同虚设：部分测试用桩接替代了验证调用，导致无论是否调用了正确方法测试都通过
- 修复测试等待方法空实现：等待数据就绪的方法永远立即返回，等于没有等待
- 修复测试不稳定：用固定延时等待异步结果改为响应式等待，消除机器快慢导致的测试抖动

### Removed

- 清理无用代码：移除未被任何界面调用的 Markdown 文本组件、重复的图片缓存管理类、未使用的首页常量文件
- 清理平板对话列表冗余过滤：数据层已过滤一次，界面层又重复过滤一次，移除冗余的第二次

## [1.4.0] - 2026-07-05

### Added

- 平板模式全面适配：首页、事件、纪念、对话、人物、相册六大核心界面完成平板 UI 适配，针对 2800×1840 分辨率优化，统一浅色模式。平板模式下以侧边导航栏替代底部导航栏，充分利用宽屏空间
  - 首页：日记式布局——日期横幅（大号日期 + 渐变色条 + 问候语 + 待办/纪念提醒）、今日时光时间线、关系概览条形图、极光动画装饰卡、频道快捷入口、统计概览、拍立得照片卡
  - 事件列表：日历 + 列表双栏布局——左侧为月份日历网格（含事件标记和当日事件列表），右侧为筛选标签和按日期分组的完整事件列表
  - 事件详情：杂志式双栏布局——左侧大图 Hero 区（照片浏览 + 渐变遮罩 + 类型徽章 + 标题/描述/缩略图行 + 时间信息条），右侧信息侧栏（时间卡片 + 地点天气网格 + 参与者头像列表 + 个人感悟卡 + 评论/转发/收藏/分享操作栏）
  - 纪念日：时光长廊布局——顶部即将到来 Hero 卡（大号倒数天数 + 类型徽章 + 联系人/日期）、全年 12 个月横向时间轴（当前月份高亮）、按生日/纪念日/节日三列分组展示。时间轴日期统一为「X日」格式，卡片点击无跳转
  - 对话：剧本剧场三栏布局——左栏 QQ 风格对话列表（圆形头像 + 选中蓝色半透明背景 + 紧凑两行信息）、中栏剧本舞台（衬线大标题 + 元信息标签 + 我方/对方对话气泡 + 图片消息 + 备注区）、右栏角色信息面板（双层画框头像 + 衬线大字姓名 + 亲密度星级 + 统计行 + 联系方式）
  - 人物列表：6 列卡片网格——按亲密度从高到低分组（至亲→密友→朋友→泛交→初识），每张卡片含 4:3 头像 + 亲密度色渐变 + 姓名徽章 + 星级 + 分数，错落入场动画
  - 人物详情：经典画廊精装布局——顶部 Hero 区（暖色光晕背景 + 三层画框人像 + 衬线大号姓名 + 昵称引文 + 关系/性别/年龄/MBTI/学历/相识天数元数据行 + 亲密度卡片），下方四栏内容区（档案/时光事件画廊/纪念与礼物/心绪与对话）
  - 相册：统计区 3 列横排、照片网格 6 列、每日/事件视图照片尺寸增大、内容居中显示

- 对话解析增强：对话描述支持 JSON 新格式与旧文本格式自动兼容——优先尝试 JSON 解析（支持说话人切换、结构化对话数据、内嵌图片标签），失败时回退到旧文本按行解析。新数据一律以 JSON 格式保存，旧数据可正常读取

### Changed

- 平板模式下导航图统一接收布局参数，事件详情和相册模块在平板分支下调用专属平板组件
- 相册各视图组件（统计区、网格视图、每日视图、事件视图）根据平板/手机模式自适应切换列数、间距、圆角和照片高度

### Fixed

- 修复平板模式下对话内容显示错乱：旧版解析方法不支持 JSON 格式导致对话描述被当作纯文本按行显示，改用双格式解析策略
- 修复平板对话列表选中项和添加按钮配色与其他界面不一致：选中项背景改为蓝色半透明，添加按钮改为蓝色

## [1.3.3] - 2026-06-19

### Added

- Baseline Profile 模块 (`:baselineprofile`)：新增 `com.android.test` 模块用于生成 Baseline Profile，通过 Macrobenchmark `BaselineProfileRule` 采集应用启动路径的 AOT 编译配置，提升冷启动速度
  - `baselineprofile/build.gradle.kts` — 测试模块配置，使用 `androidx.baselineprofile` 插件 + `benchmark-macro-junit4`
  - `BaselineProfileGenerator` — 启动应用并采集 profile，简化为 `startActivityAndWait()` + `device.waitForIdle()` 避免 StaleObjectException
  - `app/src/main/baseline-prof.txt` — 从设备拉取的真实 profile 数据（564KB），覆盖 Coil/Compose runtime/Hilt 等关键路径
  - `app/src/benchmarkRelease/AndroidManifest.xml` — 添加 `<profileable android:shell="true" />` 标签，benchmark release 构建必需
  - `gradle/libs.versions.toml` — 新增 `profileinstaller = "1.4.1"` 版本和依赖别名
  - `app/build.gradle.kts` — 新增 `implementation(libs.profileinstaller)` 依赖（BaselineProfileRule 运行必需）

- detekt 静态代码分析：全模块集成 detekt，CI/CD 流水线新增静态检查步骤
  - `build.gradle.kts` — 根项目应用 detekt 插件，`subprojects {}` 全局配置 `ignoreFailures = true`（渐进式采用）
  - `.github/workflows/ci.yml` — 新增 detekt 检查步骤
  - `.github/workflows/pr.yml` — 新增 detekt 检查步骤

### Changed

- CI/CD 优化：`ci.yml` 新增 `paths-ignore` 配置，文档-only 变更（`*.md`、`docs/**`、`memory/**`）跳过 CI 运行，节省构建资源
- Release 工作流修复：`release.yml` 修复 keystore base64 解码（`tr -d '\n\r '` 去除换行避免二进制损坏）、添加 `permissions: contents: write`、格式化修正

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
