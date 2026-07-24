package com.tang.prm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.animation.core.AnimationTokens

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    focusRequester: FocusRequester? = null,
    autoFocus: Boolean = false
) {
    val localFocusRequester = remember { FocusRequester() }
    val actualRequester = focusRequester ?: localFocusRequester

    if (autoFocus) {
        LaunchedEffect(Unit) {
            actualRequester.requestFocus()
        }
    }

    SearchTextFieldInternal(
        query = query,
        onQueryChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        shape = RoundedCornerShape(Dimens.cornerXxl),
        containerColor = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.elevationCard,
        textFieldModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        focusRequester = actualRequester,
        iconTint = TextGray.copy(alpha = AnimationTokens.Alpha.half),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        placeholderTextStyle = MaterialTheme.typography.bodyMedium,
        placeholderColor = TextGray.copy(alpha = 0.5f),
        clearButton = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(Dimens.iconSmall)
                    )
                }
            }
        }
    )
}

@Composable
internal fun SearchTextFieldInternal(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    shape: Shape,
    containerColor: Color,
    shadowElevation: Dp,
    textFieldModifier: Modifier,
    focusRequester: FocusRequester?,
    iconTint: Color,
    textStyle: TextStyle,
    placeholderTextStyle: TextStyle,
    placeholderColor: Color,
    clearButton: (@Composable () -> Unit)?
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = shadowElevation
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = (if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .then(textFieldModifier),
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = placeholderTextStyle,
                                color = placeholderColor
                            )
                        }
                        innerTextField()
                    }
                    if (clearButton != null) {
                        clearButton()
                    }
                }
            }
        )
    }
}
