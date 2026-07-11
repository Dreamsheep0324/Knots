package com.tang.prm.feature.remember.anniversary

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditAnniversaryRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversaryDetailScreen(
    anniversaryId: Long,
    navController: NavController,
    viewModel: AnniversaryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                            navController.navigate(EditAnniversaryRoute(it.id))
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
        AnniversaryDetailBody(
            uiState = uiState,
            showActionsInBody = false,
            onEdit = {
                uiState.anniversary?.let {
                    navController.navigate(EditAnniversaryRoute(it.id))
                }
            },
            onDelete = { showDeleteDialog = true },
            onContactClick = { contactId ->
                navController.navigate(ContactDetailRoute(contactId))
            },
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * 平板双栏模式：纪念日详情内容（无 Scaffold/TopBar）。
 * 供 AnniversariesScreen 的右栏嵌入。
 */
@Composable
fun AnniversaryDetailContent(
    anniversaryId: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnniversaryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    AnniversaryDetailBody(
        uiState = uiState,
        showActionsInBody = true,
        onEdit = onEdit,
        onDelete = { showDeleteDialog = true },
        onContactClick = { /* tablet: could navigate or ignore */ },
        modifier = modifier
    )
}

@Composable
private fun AnniversaryDetailBody(
    uiState: AnniversaryDetailUiState,
    showActionsInBody: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    uiState.anniversary?.let { anniversary ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showActionsInBody) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                        }
                    }
                }
            }

            item {
                AnniversaryHeader(anniversary = anniversary)
            }

            val contactId = anniversary.contactId
            val contactName = anniversary.contactName
            if (contactId != null && contactId > 0 && contactName != null) {
                item {
                    ContactCard(
                        contactId = contactId,
                        contactName = contactName,
                        contactAvatar = anniversary.contactAvatar,
                        onClick = { onContactClick(contactId) }
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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
