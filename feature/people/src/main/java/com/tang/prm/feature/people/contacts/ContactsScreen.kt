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
import com.tang.prm.ui.components.SegmentedOption
import com.tang.prm.ui.components.SegmentedToggleButton
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
    isTabletLayout: Boolean = false,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

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
        if (isTabletLayout) {
            // 平板全宽列表，点击卡片导航到画廊精装详情界面
            ContactsTabletScreen(
                onAddContact = { navController.navigate(AddContactRoute) },
                onContactClick = { id -> navController.navigate(ContactDetailRoute(id)) },
                viewModel = viewModel
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = {
                        Text("人物", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    },
                    actions = {
                        SegmentedToggleButton(
                            options = listOf(
                                SegmentedOption(0, Icons.Default.Apps, "网格"),
                                SegmentedOption(1, Icons.AutoMirrored.Filled.List, "列表"),
                                SegmentedOption(2, Icons.Default.Style, "卡牌")
                            ),
                            selectedKey = uiState.data.viewMode,
                            onSelectionChange = viewModel::onViewModeChange
                        )

                        IconButton(onClick = { navController.navigate(AddContactRoute) }) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "新建人物",
                                    tint = MaterialTheme.colorScheme.primary,
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
