package com.tang.prm.ui.anniversary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.ui.components.AppCard
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.animation.core.AnimationTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversaryDetailScreen(
    anniversaryId: Long,
    navController: NavController,
    viewModel: AnniversaryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(anniversaryId) {
        viewModel.loadAnniversary(anniversaryId)
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            title = "确认删除",
            message = "确定要删除这个纪念日吗？删除后无法恢复。",
            onConfirm = {
                viewModel.deleteAnniversary()
                showDeleteDialog = false
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        uiState.anniversary?.let {
                            navController.navigate(Screen.EditAnniversary.createRoute(it.id))
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        uiState.anniversary?.let { anniversary ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnniversaryHeader(anniversary = anniversary)
                }

                if (anniversary.contactId > 0 && anniversary.contactName != null) {
                    item {
                        ContactCard(
                            contactId = anniversary.contactId,
                            contactName = anniversary.contactName ?: "",
                            contactAvatar = anniversary.contactAvatar,
                            onClick = {
                                navController.navigate(Screen.ContactDetail.createRoute(anniversary.contactId))
                            }
                        )
                    }
                }

                item {
                    DateInfoSection(anniversary = anniversary)
                }

                if (!anniversary.remarks.isNullOrBlank()) {
                    item {
                        RemarksSection(remarks = anniversary.remarks ?: "")
                    }
                }
            }
        } ?: run {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}

@Composable
private fun AnniversaryHeader(anniversary: Anniversary) {
    val daysInfo = if (anniversary.isLunar) {
        DateUtils.calculateLunarDaysInfo(anniversary.date)
    } else {
        DateUtils.calculateDaysInfo(anniversary.date)
    }
    val iconName = anniversary.icon ?: "Cake"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(getAnniversaryIconBackground(iconName)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getAnniversaryIcon(iconName),
                    contentDescription = null,
                    tint = getAnniversaryIconTint(iconName),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = anniversary.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
            ) {
                Text(
                    text = anniversary.type.displayName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = SignalAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (daysInfo.isPast) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "已过",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (daysInfo.isPast) MaterialTheme.colorScheme.outline else TextGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${daysInfo.daysPassed}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (daysInfo.isPast) MaterialTheme.colorScheme.outline else Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (daysInfo.isPast) MaterialTheme.colorScheme.outline else TextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "倒数",
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${daysInfo.daysUntil}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contactId: Long,
    contactName: String,
    contactAvatar: String?,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(Dimens.paddingCard),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarGradients = listOf(
                    Brush.linearGradient(colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEC89A))),
                    Brush.linearGradient(colors = listOf(Color(0xFF95E1D3), Color(0xFFF38181))),
                    Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
                    Brush.linearGradient(colors = listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
                    Brush.linearGradient(colors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)))
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(avatarGradients[contactId.toInt() % avatarGradients.size]),
                    contentAlignment = Alignment.Center
                ) {
                    if (contactAvatar != null && contactAvatar.isNotBlank()) {
                        AsyncImage(
                            model = contactAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = contactName.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "关联人物",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextGray
                )
            }
        }
    }
}

@Composable
private fun DateInfoSection(anniversary: Anniversary) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "纪念日期",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                    Text(
                        text = DateUtils.formatYearMonthDayChineseFull(anniversary.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (anniversary.isLunar) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "农历日期",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        Text(
                            text = DateUtils.formatLunarDateShort(anniversary.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            if (anniversary.isRepeat) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            tint = SignalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "重复设置",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        Text(
                            text = if (anniversary.isLunar) "每年农历重复" else "每年重复",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SignalGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemarksSection(remarks: String) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = SignalPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "备注",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = remarks,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(Dimens.paddingCard),
                    lineHeight = 24.sp
                )
            }
        }
    }
}
