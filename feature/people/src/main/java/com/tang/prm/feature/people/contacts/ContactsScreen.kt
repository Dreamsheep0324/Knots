@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.common.SearchState
import com.tang.prm.feature.people.contacts.overlay.ContactCardOverlay
import com.tang.prm.ui.navigation.AddContactRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.*

@Composable
fun ContactsScreen(
    navController: NavController,
    onOverlayVisibleChange: (Boolean) -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onViewModeChange(0)
    }

    DisposableEffect(uiState.dialog.selectedCardId) {
        onOverlayVisibleChange(uiState.dialog.selectedCardId != null)
        onDispose {
            onOverlayVisibleChange(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text("人物", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple(Icons.Default.Apps, "网格", 0),
                            Triple(Icons.AutoMirrored.Filled.List, "列表", 1),
                            Triple(Icons.Default.Style, "卡牌", 2)
                        ).forEach { (icon, _, mode) ->
                            val selected = uiState.data.viewMode == mode
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.onViewModeChange(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { navController.navigate(AddContactRoute) }) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "新建人物",
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            SearchBar(
                query = searchState.query,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingPage),
                placeholder = "搜索姓名、电话..."
            )

            Spacer(modifier = Modifier.height(4.dp))

            RelationshipFilterChips(
                relationships = uiState.data.relationships,
                selectedRelationship = uiState.data.selectedRelationship,
                onRelationshipSelected = { viewModel.onRelationshipSelected(it) }
            )

            if (uiState.data.contacts.isEmpty() && !uiState.data.isLoading) {
                EmptyState(
                    icon = Icons.Default.PersonAdd,
                    title = "还没有联系人",
                    description = "添加你的第一个联系人开始管理你的人际关系",
                    actionLabel = "添加联系人",
                    onAction = { navController.navigate(AddContactRoute) }
                )
            } else {
                ContactsContent(
                    contacts = uiState.data.contacts,
                    viewMode = uiState.data.viewMode,
                    isReorderMode = uiState.data.isReorderMode,
                    onContactClick = { contact ->
                        if (!uiState.data.isReorderMode) {
                            navController.navigate(ContactDetailRoute(contact.id))
                        }
                    },
                    onCardSelect = { contactId ->
                        viewModel.selectCard(contactId)
                    },
                    onToggleReorder = { viewModel.toggleReorderMode() },
                    onMoveContact = { from, to -> viewModel.moveContact(from, to) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val selectedCard = uiState.data.contacts.find { it.id == uiState.dialog.selectedCardId }
        if (selectedCard != null) {
            ContactCardOverlay(
                contact = selectedCard,
                isFlipped = uiState.dialog.flippedCardId == selectedCard.id,
                onFlip = { viewModel.toggleCardFlip(selectedCard.id) },
                onClose = { viewModel.selectCard(null) },
                onContactClick = {
                    viewModel.selectCard(null)
                    navController.navigate(ContactDetailRoute(selectedCard.id))
                }
            )
        }
    }
}
