@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.animation.core.rememberPausableInfiniteValue
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse

internal val TerminalTextDim: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurfaceVariant else Color(0xFF64748B)
internal val TerminalTextMuted: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurfaceVariant else Color(0xFF94A3B8)
private val TerminalGrid: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFE2E8F0)

@Composable
fun ContactListScreen(
    navController: NavController,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalPurple)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "圈子系统",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = TerminalTextDim
                        )
                    }
                    IconButton(onClick = { viewModel.showCreateDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "新建圈子", tint = SignalPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TerminalGridBackground()

            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = uiState.isSearchActive) {
                    TerminalSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange
                    )
                }

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TerminalLoading()
                    }
                } else if (uiState.circles.isEmpty()) {
                    EmptyTerminalState(onCreate = { viewModel.showCreateDialog() })
                } else {
                    val filteredCircles = uiState.circles.filter { hologramCircle ->
                        val query = uiState.searchQuery
                        if (query.isBlank()) true
                        else {
                            hologramCircle.circle.name.contains(query, ignoreCase = true) ||
                            hologramCircle.members.any { it.name.contains(query, ignoreCase = true) }
                        }
                    }
                    val sortedCircles = viewModel.getSortedCircles(filteredCircles)

                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                TerminalStatsPanel(
                                    circles = uiState.circles,
                                    contacts = uiState.contacts
                                )
                            }

                            items(
                                items = sortedCircles,
                                key = { it.circle.id }
                            ) { hologramCircle ->
                                TerminalDossier(
                                    hologramCircle = hologramCircle,
                                    isExpanded = uiState.expandedCircleId == hologramCircle.circle.id,
                                    selectedMemberId = hologramCircle.selectedMemberId,
                                    flippedCardId = uiState.flippedCardId,
                                    onExpand = { viewModel.toggleCircleExpand(hologramCircle.circle.id) },
                                    onSelectMember = { contactId ->
                                        viewModel.selectMember(hologramCircle.circle.id, contactId)
                                    },
                                    onFlipCard = { contactId ->
                                        viewModel.toggleCardFlip(contactId)
                                    },
                                    onAddMember = { viewModel.showAddMemberDialog(hologramCircle.circle.id) },
                                    onEditCircle = { viewModel.showEditDialog(hologramCircle.circle) },
                                    onDeleteCircle = { viewModel.showDeleteConfirm(hologramCircle.circle.id) },
                                    onRemoveMember = { contactId ->
                                        viewModel.removeMemberFromCircle(hologramCircle.circle.id, contactId)
                                    },
                                    onContactClick = { contactId ->
                                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(96.dp)) }
                        }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        TerminalCreateDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, desc, color, waveform -> viewModel.createCircle(name, desc, color, waveform) }
        )
    }

    uiState.showEditDialog?.let { circle ->
        TerminalEditDialog(
            circle = circle,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { name, desc, color, waveform -> viewModel.updateCircle(circle, name, desc, color, waveform) }
        )
    }

    uiState.showAddMemberDialog?.let { circleId ->
        TerminalAddMemberDialog(
            availableContacts = viewModel.getAvailableContacts(circleId),
            onDismiss = { viewModel.hideAddMemberDialog() },
            onAdd = { contactId -> viewModel.addMemberToCircle(circleId, contactId) }
        )
    }

    uiState.showDeleteConfirm?.let { circleId ->
        TerminalDeleteDialog(
            onDismiss = { viewModel.hideDeleteConfirm() },
            onConfirm = { viewModel.deleteCircle(circleId) }
        )
    }

    val selectedHologram = uiState.circles.find { it.selectedMemberId != null }
    val selectedMember = selectedHologram?.members?.find { it.id == selectedHologram.selectedMemberId }
    val selectedWaveform = selectedHologram?.circle?.waveform ?: "sine"

    if (selectedMember != null) {
        FullscreenCardOverlay(
            contact = selectedMember,
            waveformType = selectedWaveform,
            isFlipped = uiState.flippedCardId == selectedMember.id,
            onFlip = { viewModel.toggleCardFlip(selectedMember.id) },
            onClose = {
                val circleId = uiState.circles.find { it.selectedMemberId == selectedMember.id }?.circle?.id
                circleId?.let { viewModel.selectMember(it, null) }
            },
            onRemove = {
                val circleId = uiState.circles.find { it.selectedMemberId == selectedMember.id }?.circle?.id
                circleId?.let {
                    viewModel.removeMemberFromCircle(it, selectedMember.id)
                    viewModel.selectMember(it, null)
                }
            },
            onContactClick = {
                navController.navigate(Screen.ContactDetail.createRoute(selectedMember.id))
            }
        )
    }
}

@Composable
private fun TerminalPrompt() {
    val dotAlpha by rememberBreathingPulse(
        minAlpha = 0.4f, maxAlpha = 1f,
        cycleDuration = AnimationTokens.Duration.dramatic
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(SignalPurple.copy(alpha = dotAlpha))
    )
}

@Composable
private fun TerminalGridBackground() {
    val gridColor = TerminalGrid.copy(alpha = 0.3f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 40.dp.toPx()
        val width = size.width
        val height = size.height

        for (x in 0..(width / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(x * gridSize, 0f),
                end = Offset(x * gridSize, height),
                strokeWidth = 0.5f
            )
        }
        for (y in 0..(height / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y * gridSize),
                end = Offset(width, y * gridSize),
                strokeWidth = 0.5f
            )
        }
    }
}

@Composable
private fun TerminalSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ">",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = SignalPurple,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                        Text("搜索圈子或成员...", fontFamily = FontFamily.Monospace, color = TerminalTextMuted, fontSize = 13.sp)
                    },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(2.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = SignalPurple
            )
        )
    }
}

@Composable
internal fun TerminalDivider(color: Color = Color.Unspecified) {
    val dividerColor = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor.copy(alpha = 0.3f))
    )
}

@Composable
private fun TerminalLoading() {
    val dotCount by rememberPausableInfiniteValue(
        initialValue = 1, targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Restart),
        label = "dots"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "> 加载中${".".repeat(dotCount)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = TerminalTextDim
        )
    }
}

@Composable
private fun EmptyTerminalState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "[暂无数据]",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TerminalTextDim
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "目录为空",
            fontFamily = FontFamily.Monospace,
            color = TerminalTextMuted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(20.dp))
        TerminalActionButton(label = "新建圈子", onClick = onCreate)
    }
}
