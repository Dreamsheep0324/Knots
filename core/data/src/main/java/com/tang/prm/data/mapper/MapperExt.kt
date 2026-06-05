package com.tang.prm.data.mapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 将 Entity 列表 Flow 映射为 Domain 模型列表 Flow
 *
 * 用法：contactDao.getAllContacts().mapList(ContactEntity::toDomain)
 */
fun <E, D> Flow<List<E>>.mapList(mapper: (E) -> D): Flow<List<D>> =
    map { list -> list.map(mapper) }

/**
 * 将可空 Entity Flow 映射为可空 Domain 模型 Flow
 *
 * 用法：contactDao.getContactById(id).mapNullable(ContactEntity::toDomain)
 */
fun <E, D> Flow<E?>.mapNullable(mapper: (E) -> D): Flow<D?> =
    map { it?.let(mapper) }

/**
 * 从枚举名称查找枚举值，找不到则返回默认值
 *
 * 用法：type.toEnumOrDefault(EventType.OTHER)
 */
inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    try { enumValueOf<T>(this) } catch (_: Exception) { default }

/**
 * 从枚举 key 字段查找枚举值，找不到则返回默认值
 *
 * 用法：giftType.toEnumByKeyOrDefault(GiftType::key, GiftType.OTHER)
 */
inline fun <reified T : Enum<T>> String.toEnumByKeyOrDefault(
    keySelector: (T) -> String,
    default: T
): T = T::class.java.enumConstants?.find { keySelector(it) == this } ?: default
