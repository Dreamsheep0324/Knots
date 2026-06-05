package com.tang.prm.feature.reflect.thoughts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.usecase.ContactThoughts
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.SignalAmber

@Composable
internal fun ContactStoriesRow(
    contactThoughts: List<ContactThoughts>,
    selectedContactId: Long?,
    onContactClick: (Long) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(contactThoughts.take(8), key = { it.contact.id }) { ct ->
            val isSelected = ct.contact.id == selectedContactId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(52.dp)
                    .clickable { onContactClick(ct.contact.id) }
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 2.dp,
                        color = if (isSelected) SignalAmber else SignalAmber.copy(alpha = AnimationTokens.Alpha.subtle)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ContactAvatar(
                            avatar = ct.contact.avatar,
                            name = ct.contact.name,
                            size = 44
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    ct.contact.name,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) SignalAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}
