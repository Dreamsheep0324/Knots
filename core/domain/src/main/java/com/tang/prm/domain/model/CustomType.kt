package com.tang.prm.domain.model

/**
 * 用户自定义类型条目。
 *
 * @param category 所属分类键，取值见 [CustomCategories]（如 "EVENT_TYPE" / "EMOTION"）
 * @param key 业务去重键（同 category 内唯一），用于程序比对；为空时以 [name] 为键
 * @param isDefault 是否为系统预置默认项（不可删除）
 */
data class CustomType(
    val id: Long = 0,
    val category: String,
    val name: String,
    val key: String = "",
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Q-7 修复：CustomCategories 文档化。
 *
 * 自定义类型分类键的单一来源。所有常量用于 [CustomType.category] 字段，作为 custom_types 表的
 * 分组维度。新增分类时必须在此添加常量，避免散落字符串字面量。
 *
 * 调用方：ContactFormReferenceDataUseCase（关系/教育/爱好/习惯/饮食/技能）、
 * AddEventViewModel（事件类型/情绪/天气）、AddAnniversaryViewModel（纪念日类型）、
 * AddSubscriptionViewModel（订阅分类）、FootprintAggregationUseCase、
 * CleanCustomTypeUseCase、DeleteCustomTypeUseCase、ObserveEventsAggregateUseCase。
 *
 * 保留 object + const val 形式（而非改 enum）：数据库存储为 String，enum 会引入额外序列化，
 * 且当前 11 个分类无行为差异，不需要 enum 的方法绑定。
 */
object CustomCategories {
    /** 事件自定义类型（如"聚餐"/"见面"之外的扩展类型） */
    const val EVENT_TYPE = "EVENT_TYPE"
    /** 事件情绪标签 */
    const val EMOTION = "EMOTION"
    /** 事件天气标签 */
    const val WEATHER = "WEATHER"
    /** 联系人关系标签（如"同事"/"朋友"） */
    const val RELATIONSHIP = "RELATIONSHIP"
    /** 人物关系类型（此人物与其他人物的关系，如"配偶"/"父母"/"大学同学"） */
    const val PERSON_RELATION = "PERSON_RELATION"
    /** 纪念日自定义类型（如"生日"/"纪念日"之外的扩展类型） */
    const val ANNIVERSARY_TYPE = "ANNIVERSARY_TYPE"
    /** 联系人教育背景（如"本科"/"硕士"） */
    const val EDUCATION = "EDUCATION"
    /** 联系人爱好标签 */
    const val HOBBY = "HOBBY"
    /** 联系人习惯标签 */
    const val HABIT = "HABIT"
    /** 联系人饮食偏好 */
    const val DIET = "DIET"
    /** 联系人技能标签 */
    const val SKILL = "SKILL"
    /** 订阅自定义分类（如"娱乐"/"工具"） */
    const val SUBSCRIPTION_CATEGORY = "SUBSCRIPTION_CATEGORY"
}
