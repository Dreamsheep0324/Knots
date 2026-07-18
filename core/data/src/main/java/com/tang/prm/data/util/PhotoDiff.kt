package com.tang.prm.data.util

/**
 * REP-Q-5 / REP-C-4 修复：抽取 photos 差集计算逻辑。
 *
 * 三个 Repository（Event/Gift/Recipe）的 update 方法都遵循相同模式：
 * 1. 事务内读取旧实体的 photos
 * 2. 计算旧 photos - 新 photos 的差集（即被移除的照片）
 * 3. 差集非空时返回，事务外删除文件
 *
 * 此前该差集计算逻辑在 3 处重复，现统一为本函数。
 *
 * @param old 旧实体，可能为 null（首次更新或已被删除）
 * @param newPhotos 新的 photos 列表
 * @param photoProvider 从实体提取 photos 的函数
 * @return 被移除的 photos 集合，null 表示无移除（避免空集合并触发文件删除逻辑）
 */
fun <T> computeRemovedPhotos(
    old: T?,
    newPhotos: List<String>,
    photoProvider: (T) -> List<String>
): Set<String>? =
    old?.let { (photoProvider(it).toSet() - newPhotos.toSet()).takeIf { it.isNotEmpty() } }
