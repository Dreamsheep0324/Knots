package com.tang.prm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.Dimens

/**
 * 平板专用搜索栏。
 *
 * 与手机版 [SearchBar] 的区别：
 * - 12dp 圆角（vs 24dp）
 * - surfaceVariant 背景（vs surface）
 * - 无阴影
 * - 无清除按钮（平板搜索栏通常不需要）
 *
 * 统一了 ContactsTabletScreen / AnniversaryTabletScreen / ChatTabletScreen
 * 中重复的搜索栏实现。
 *
 * @param query 当前搜索文本
 * @param onQueryChange 搜索文本变更回调
 * @param modifier 修饰符
 * @param placeholder 占位提示文本
 */
@Composable
fun TabletSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索"
) {
    SearchTextFieldInternal(
        query = query,
        onQueryChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        shape = RoundedCornerShape(Dimens.cornerMedium),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 0.dp,
        textFieldModifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 14.dp),
        focusRequester = null,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholderTextStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
        placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        clearButton = null
    )
}
