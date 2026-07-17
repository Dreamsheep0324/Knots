@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.IngredientGroupType
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.SectionCard
import com.tang.prm.ui.components.photo.ManagedPhotoPickerLauncher
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.PhotoSelectionArea
import com.tang.prm.ui.components.photo.PhotoSlotMode
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import kotlinx.coroutines.launch

@Composable
fun AddRecipeScreen(
    navController: NavController,
    recipeId: Long = 0L,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showContactPicker by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        if (recipeId != 0L) viewModel.loadForEdit(recipeId)
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    val photoPicker = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = AddRecipeViewModel.MAX_PHOTOS, prefix = "recipe")
    ) { result ->
        if (result.localPaths.isNotEmpty()) {
            viewModel.addPhotos(result.localPaths)
        }
    }

    if (showContactPicker) {
        val selectedContacts = uiState.availableContacts.filter { it.id in uiState.selectedContactIds }
        ContactPickerDialog(
            contacts = uiState.availableContacts,
            multiSelect = true,
            selectedContacts = selectedContacts,
            title = "关联人物",
            subtitle = "选择喜欢这道菜的人",
            onContactSelected = { viewModel.toggleContact(it.id) },
            onDismiss = { showContactPicker = false }
        )
    }

    FormScreenScaffold(
        title = if (uiState.isEditing) "编辑菜谱" else "新建菜谱",
        onSaveClick = {
            viewModel.save(
                onSuccess = { navController.popBackStack() },
                onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        },
        saveEnabled = uiState.title.isNotBlank(),
        onBackClick = { navController.popBackStack() },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "title") { TitleSection(uiState, viewModel) }
            item(key = "description") { DescriptionSection(uiState, viewModel) }
            item(key = "cuisine_difficulty") { CuisineDifficultySection(uiState, viewModel) }
            item(key = "time_servings") { TimeServingsSection(uiState, viewModel) }
            item(key = "photos") { PhotosSection(uiState, viewModel, photoPicker) }
            item(key = "ingredients") { IngredientsSection(uiState, viewModel) }
            item(key = "steps") { StepsSection(uiState, viewModel) }
            item(key = "contacts") {
                ContactsSection(uiState, viewModel, onShowContactPicker = { showContactPicker = true })
            }
            item(key = "tags") { TagsSection(uiState, viewModel) }
            item(key = "rating") { RatingSection(uiState, viewModel) }
            item(key = "notes") { NotesSection(uiState, viewModel) }
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 表单分区
// ─────────────────────────────────────────────────────────────────

@Composable
private fun TitleSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "菜名", icon = Icons.Default.LocalDining, iconTint = SignalElectric) {
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::updateTitle,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("给这道菜起个名字", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            singleLine = true,
            isError = state.titleError != null,
            supportingText = {
                state.titleError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = recipeFieldColors()
        )
    }
}

@Composable
private fun DescriptionSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "简介", icon = Icons.Default.Edit, iconTint = SignalElectric) {
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::updateDescription,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("简单描述这道菜的特色...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = recipeFieldColors()
        )
    }
}

@Composable
private fun CuisineDifficultySection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "菜系与难度", icon = Icons.Default.RestaurantMenu, iconTint = SignalAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // 菜系：输入框 + 常见菜系快捷选择
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "菜系",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.cuisine,
                    onValueChange = viewModel::updateCuisine,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入菜系", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = recipeFieldColors()
                )
                CuisineQuickSelector(
                    selected = state.cuisine,
                    onSelect = viewModel::updateCuisine
                )
            }
            // 难度：三段式分段选择器
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "难度",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DifficultySelector(
                    selected = state.difficulty,
                    onSelect = viewModel::updateDifficulty
                )
            }
        }
    }
}

@Composable
private fun TimeServingsSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "时间与份数", icon = Icons.Default.Timer, iconTint = SignalGreen) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.cookingTime,
                onValueChange = viewModel::updateCookingTime,
                modifier = Modifier.weight(1f),
                placeholder = { Text("时间(分钟)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = recipeFieldColors()
            )
            OutlinedTextField(
                value = state.servings,
                onValueChange = viewModel::updateServings,
                modifier = Modifier.weight(1f),
                placeholder = { Text("份数", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = recipeFieldColors()
            )
        }
    }
}

@Composable
private fun PhotosSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel,
    photoPicker: ManagedPhotoPickerLauncher
) {
    SectionCard(title = "成品照片", icon = Icons.Default.Image, iconTint = SignalAmber) {
        PhotoSelectionArea(
            photos = state.photos,
            mode = PhotoSlotMode.THUMBNAIL,
            maxCount = AddRecipeViewModel.MAX_PHOTOS,
            onAdd = { photoPicker.launch() },
            onRemove = { idx -> viewModel.removePhotoAt(idx) }
        )
    }
}

@Composable
private fun IngredientsSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "食材", icon = Icons.Default.BreakfastDining, iconTint = SignalAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IngredientGroupEditor(
                groupType = IngredientGroupType.MAIN,
                ingredients = state.mainIngredients,
                onAdd = { viewModel.addIngredient(IngredientGroupType.MAIN) },
                onUpdate = { idx, ing -> viewModel.updateIngredient(IngredientGroupType.MAIN, idx, ing) },
                onRemove = { idx -> viewModel.removeIngredient(IngredientGroupType.MAIN, idx) }
            )
            IngredientGroupEditor(
                groupType = IngredientGroupType.SUB,
                ingredients = state.subIngredients,
                onAdd = { viewModel.addIngredient(IngredientGroupType.SUB) },
                onUpdate = { idx, ing -> viewModel.updateIngredient(IngredientGroupType.SUB, idx, ing) },
                onRemove = { idx -> viewModel.removeIngredient(IngredientGroupType.SUB, idx) }
            )
            IngredientGroupEditor(
                groupType = IngredientGroupType.SEASONING,
                ingredients = state.seasoningIngredients,
                onAdd = { viewModel.addIngredient(IngredientGroupType.SEASONING) },
                onUpdate = { idx, ing -> viewModel.updateIngredient(IngredientGroupType.SEASONING, idx, ing) },
                onRemove = { idx -> viewModel.removeIngredient(IngredientGroupType.SEASONING, idx) }
            )
        }
    }
}

@Composable
private fun StepsSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "步骤", icon = Icons.Default.RestaurantMenu, iconTint = SignalGreen) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.steps.forEachIndexed { index, step ->
                StepEditor(
                    index = index,
                    step = step,
                    onUpdate = { viewModel.updateStep(index, it) },
                    onUpdateTimer = { viewModel.updateStepTimer(index, it) },
                    onRemove = { viewModel.removeStep(index) }
                )
            }
            AddStepButton(onClick = viewModel::addStep)
        }
    }
}

@Composable
private fun ContactsSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel,
    onShowContactPicker: () -> Unit
) {
    SectionCard(title = "关联人物", icon = Icons.Default.People, iconTint = SignalGreen) {
        PersonAvatarSelector(
            contacts = state.availableContacts,
            selectedIds = state.selectedContactIds,
            onToggle = viewModel::toggleContact,
            onAdd = onShowContactPicker
        )
    }
}

@Composable
private fun TagsSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "标签", icon = Icons.Default.Sell, iconTint = SignalPurple) {
        TagSelector(
            availableTags = state.availableTags,
            selectedTags = state.selectedTagNames,
            onToggle = viewModel::toggleTag,
            onAdd = viewModel::addNewTag,
            onDelete = viewModel::deleteTag
        )
    }
}

@Composable
private fun RatingSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "评分", icon = Icons.Default.StarRate, iconTint = SignalAmber) {
        StarRating(
            rating = state.rating,
            onRatingChange = viewModel::updateRating
        )
    }
}

@Composable
private fun NotesSection(
    state: AddRecipeUiState,
    viewModel: AddRecipeViewModel
) {
    SectionCard(title = "笔记", icon = Icons.Default.AutoAwesome, iconTint = SignalPurple) {
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::updateNotes,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("记录烹饪心得、注意事项...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = recipeFieldColors()
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 添加步骤按钮
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AddStepButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "添加步骤",
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 公共：表单字段颜色
// ─────────────────────────────────────────────────────────────────

@Composable
private fun recipeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorBorderColor = MaterialTheme.colorScheme.error
)
