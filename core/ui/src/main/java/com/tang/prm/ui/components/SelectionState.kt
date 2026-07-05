package com.tang.prm.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * 平板双栏布局中，列表选中项的持久化状态。
 * 旋转/配置变更后选中项不丢失。
 */
@Composable
fun <T> rememberSelectedId(): MutableState<T?> = rememberSaveable { mutableStateOf(null) }
