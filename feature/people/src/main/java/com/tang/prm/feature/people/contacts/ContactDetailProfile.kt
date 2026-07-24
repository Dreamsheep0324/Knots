package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tang.prm.domain.model.Contact
import com.tang.prm.feature.people.contacts.components.PersonRelationTypeInfo
import com.tang.prm.feature.people.contacts.components.PersonRelationsMode
import com.tang.prm.feature.people.contacts.components.PersonRelationsSection
import com.tang.prm.ui.components.DetailInfoRow
import com.tang.prm.ui.components.DetailSection
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.domain.util.parseListField
import com.tang.prm.ui.animation.core.AnimationTokens

@Composable
internal fun ProfileContent(
    contact: Contact,
    uiState: ContactDetailUiState,
    onNavigateToContactDetail: (Long) -> Unit = {}
) {

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (contact.birthday != null || contact.knowingDate != null) {
            DetailSection(title = "基本信息", accentColor = SignalSky) {
                contact.birthday?.let {
                    DetailInfoRow(icon = Icons.Default.Cake, label = "生日", value = DateUtils.formatYearMonthDayChineseFull(it), iconColor = AnniversaryBirthday, valueColor = AnniversaryBirthday)
                }
                contact.knowingDate?.let {
                    DetailInfoRow(icon = Icons.Default.CalendarToday, label = "相识日期", value = DateUtils.formatYearMonthDayChineseFull(it), iconColor = SignalSky, valueColor = SignalSky)
                }
            }
        }

        if (uiState.data.personRelations.isNotEmpty()) {
            DetailSection(title = "人物关系", accentColor = Color(0xFF0F766E)) {
                val typeInfoMap = remember(uiState.data.personRelationTypes) {
                    uiState.data.personRelationTypes.associate { type ->
                        type.id to PersonRelationTypeInfo(
                            name = type.name,
                            color = type.color?.toComposeColor(Color(0xFF0F766E))
                        )
                    }
                }
                PersonRelationsSection(
                    relations = uiState.data.personRelations,
                    typeInfoMap = typeInfoMap,
                    mode = PersonRelationsMode.VIEWER,
                    onRelationClick = { relation ->
                        relation.targetContactId?.let { targetId ->
                            onNavigateToContactDetail(targetId)
                        }
                    },
                    onRelationDelete = {}
                )
            }
        }

        if (hasLocationInfo(contact) || hasWorkInfo(contact)) {
            DetailSection(title = "工作", accentColor = SignalAmber) {
                contact.city?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.LocationCity, label = "城市", value = it, iconColor = SignalPurple)
                }
                contact.address?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.Home, label = "详细地址", value = it, iconColor = SignalGreen, maxLines = 2)
                }
                contact.company?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.BusinessCenter, label = "公司", value = it, iconColor = SignalAmber)
                }
                contact.jobTitle?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.Work, label = "职位", value = it, iconColor = SceneOrange)
                }
            }
        }

        if (hasContactInfo(contact)) {
            DetailSection(title = "联系方式", accentColor = Color(0xFF4DD0E1)) {
                contact.phone?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.Phone, label = "电话", value = it, iconColor = Color(0xFF4DD0E1))
                }
                contact.email?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.MoreHoriz, label = "其他", value = it, iconColor = SignalSky)
                }
            }
        }

        if (hasPersonalFeatureTags(contact)) {
            DetailSection(title = "个人特征", accentColor = InsightPink) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    contact.hobby?.takeIf { it.isNotBlank() }?.let {
                        FeatureTagSectionWithColors("爱好", parseListField(it).map { tag -> tag to uiState.data.hobbyOptions.find { o -> o.name == tag }?.color }, InsightPink)
                    }
                    contact.habit?.takeIf { it.isNotBlank() }?.let {
                        FeatureTagSectionWithColors("习惯", parseListField(it).map { tag -> tag to uiState.data.habitOptions.find { o -> o.name == tag }?.color }, SignalSky)
                    }
                    contact.diet?.takeIf { it.isNotBlank() }?.let {
                        FeatureTagSectionWithColors("饮食偏好", parseListField(it).map { tag -> tag to uiState.data.dietOptions.find { o -> o.name == tag }?.color }, SceneOrange)
                    }
                    contact.skill?.takeIf { it.isNotBlank() }?.let {
                        FeatureTagSectionWithColors("技能", parseListField(it).map { tag -> tag to uiState.data.skillOptions.find { o -> o.name == tag }?.color }, SignalPurple)
                    }
                    contact.mbti?.takeIf { it.isNotBlank() }?.let {
                        FeatureTagSection("MBTI", listOf(it), SignalElectric)
                    }
                }
            }
        }

        if (hasFamilyInfo(contact)) {
            DetailSection(title = "家庭/社交", accentColor = SignalGold) {
                contact.spouseName?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.Favorite, label = "配偶", value = it, iconColor = InsightPink)
                }
                if (contact.childrenCount > 0) {
                    DetailInfoRow(icon = Icons.Default.ChildCare, label = "子女", value = "${contact.childrenCount}人", iconColor = SignalSky)
                }
                contact.childrenNames?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.Face, label = "子女姓名", value = it, iconColor = SignalGreen)
                }
                contact.introducer?.takeIf { it.isNotBlank() }?.let {
                    DetailInfoRow(icon = Icons.Default.PersonAdd, label = "介绍人", value = it, iconColor = SignalPurple)
                }
            }
        }
    }
}

@Composable
private fun FeatureTagSection(label: String, tags: List<String>, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags, key = { it }) { tag ->
                Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = AnimationTokens.Alpha.faint)) {
                    Text(text = tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FeatureTagSectionWithColors(label: String, tagsWithColors: List<Pair<String, String?>>, defaultColor: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tagsWithColors, key = { it.first }) { (tag, colorHex) ->
                val tagColor = colorHex?.let {
                    it.toComposeColor(defaultColor)
                } ?: defaultColor
                Surface(shape = RoundedCornerShape(8.dp), color = tagColor.copy(alpha = AnimationTokens.Alpha.faint)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (colorHex != null) {
                            Box(modifier = Modifier.size(7.dp).background(tagColor, CircleShape))
                        }
                        Text(text = tag, style = MaterialTheme.typography.bodySmall, color = tagColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

internal fun hasPersonalFeatureTags(contact: Contact): Boolean {
    return contact.hobby?.isNotBlank() == true || contact.habit?.isNotBlank() == true || contact.diet?.isNotBlank() == true || contact.skill?.isNotBlank() == true || contact.mbti?.isNotBlank() == true
}

internal fun hasContactInfo(contact: Contact): Boolean = contact.phone?.isNotBlank() == true || contact.email?.isNotBlank() == true
internal fun hasLocationInfo(contact: Contact): Boolean = contact.city?.isNotBlank() == true || contact.address?.isNotBlank() == true
internal fun hasWorkInfo(contact: Contact): Boolean = contact.company?.isNotBlank() == true || contact.jobTitle?.isNotBlank() == true
internal fun hasFamilyInfo(contact: Contact): Boolean = contact.spouseName?.isNotBlank() == true || contact.childrenCount > 0 || contact.childrenNames?.isNotBlank() == true || contact.introducer?.isNotBlank() == true
