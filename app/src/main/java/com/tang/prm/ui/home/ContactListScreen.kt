@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.home.card.FullscreenCardOverlay
import com.tang.prm.ui.home.card.TerminalDossier
import com.tang.prm.ui.navigation.ContactDetailRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.*

@Composable
fun ContactListScreen(
    navController: NavController,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

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
                            if (searchState.isActive) Icons.Default.Close else Icons.Default.Search,
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
                AnimatedVisibility(visible = searchState.isActive) {
                    TerminalSearchBar(
                        query = searchState.query,
                        onQueryChange = viewModel::onSearchQueryChange
                    )
                }

                if (uiState.data.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TerminalLoading()
                    }
                } else if (uiState.data.circles.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Group,
                        title = "还没有联系人",
                        actionLabel = "添加",
                        onAction = { viewModel.showCreateDialog() }
                    )
                } else {
                    val filteredCircles = uiState.data.circles.filter { hologramCircle ->
                        val query = searchState.query
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
                                    circles = uiState.data.circles,
                                    contacts = uiState.data.contacts
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
                                        navController.navigate(ContactDetailRoute(contactId))
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(96.dp)) }
                        }
                }
            }
        }
    }

    if (uiState.dialog.showCreate) {
        TerminalCreateDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, desc, color, waveform -> viewModel.createCircle(name, desc, color, waveform) }
        )
    }

    uiState.dialog.showEdit?.let { circle ->
        TerminalEditDialog(
            circle = circle,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { name, desc, color, waveform -> viewModel.updateCircle(circle, name, desc, color, waveform) }
        )
    }

    uiState.dialog.showAddMember?.let { circleId ->
        TerminalAddMemberDialog(
            availableContacts = viewModel.getAvailableContacts(circleId),
            onDismiss = { viewModel.hideAddMemberDialog() },
            onAdd = { contactId -> viewModel.addMemberToCircle(circleId, contactId) }
        )
    }

    uiState.dialog.showDeleteConfirm?.let { circleId ->
        TerminalDeleteDialog(
            onDismiss = { viewModel.hideDeleteConfirm() },
            onConfirm = { viewModel.deleteCircle(circleId) }
        )
    }

    val selectedHologram = uiState.data.circles.find { it.selectedMemberId != null }
    val selectedMember = selectedHologram?.members?.find { it.id == selectedHologram.selectedMemberId }
    val selectedWaveform = selectedHologram?.circle?.waveform ?: "sine"

    if (selectedMember != null) {
        FullscreenCardOverlay(
            contact = selectedMember,
            waveformType = selectedWaveform,
            isFlipped = uiState.flippedCardId == selectedMember.id,
            onFlip = { viewModel.toggleCardFlip(selectedMember.id) },
            onClose = {
                val circleId = uiState.data.circles.find { it.selectedMemberId == selectedMember.id }?.circle?.id
                circleId?.let { viewModel.selectMember(it, null) }
            },
            onRemove = {
                val circleId = uiState.data.circles.find { it.selectedMemberId == selectedMember.id }?.circle?.id
                circleId?.let {
                    viewModel.removeMemberFromCircle(it, selectedMember.id)
                    viewModel.selectMember(it, null)
                }
            },
            onContactClick = {
                navController.navigate(ContactDetailRoute(selectedMember.id))
            }
        )
    }
}
