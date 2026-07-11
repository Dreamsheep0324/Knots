package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.Dimens

/**
 * 平板顶部栏（大标题 + 副标题 + 搜索栏 + 添加按钮）。
 *
 * 统一了 ContactsTabletScreen / AnniversaryTabletScreen 中重复的顶部栏布局。
 *
 * @param title 大标题
 * @param subtitle 副标题
 * @param searchQuery 搜索文本
 * @param onSearchQueryChange 搜索文本变更回调
 * @param searchPlaceholder 搜索栏占位文本
 * @param onAddClick 添加按钮点击回调
 * @param addContentDescription 添加按钮内容描述
 * @param modifier 修饰符
 * @param searchInline 是否将搜索栏放在标题行内（true），否则放在标题行下方（false）
 */
@Composable
fun TabletTopBar(
    title: String,
    subtitle: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    onAddClick: () -> Unit,
    addContentDescription: String,
    modifier: Modifier = Modifier,
    searchInline: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 48.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (searchInline) {
                TabletSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = searchPlaceholder,
                    modifier = Modifier.width(360.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingLg))
            }

            // 添加按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = addContentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (!searchInline) {
            Spacer(modifier = Modifier.height(Dimens.spacingLg))
            TabletSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = searchPlaceholder,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
