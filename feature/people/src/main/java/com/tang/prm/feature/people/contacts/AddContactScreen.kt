package com.tang.prm.feature.people.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import com.tang.prm.domain.util.DateUtils
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navController: androidx.navigation.NavController,
    onPickAvatar: (() -> Unit)? = null,
    viewModel: AddContactViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showKnowingDatePicker by remember { mutableStateOf(false) }
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { nameFocusRequester.requestFocus() }

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = uiState.hasUnsavedChanges) { showExitDialog = true }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    if (showExitDialog) {
        DiscardEditDialog(onDiscard = { showExitDialog = false; navController.popBackStack() }, onDismiss = { showExitDialog = false })
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { millis ->
                viewModel.updateBirthday(DateUtils.formatDate(millis))
            }
        )
    }

    if (showKnowingDatePicker) {
        AppDatePicker(
            show = showKnowingDatePicker,
            onDismiss = { showKnowingDatePicker = false },
            onDateSelected = { millis ->
                viewModel.updateKnowingDate(DateUtils.formatDate(millis))
            }
        )
    }

    FormScreenScaffold(
        title = if (uiState.isEditing) "编辑人物" else "新建人物",
        onSaveClick = { viewModel.saveContact() },
        saveEnabled = uiState.name.isNotBlank(),
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else navController.popBackStack() },
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddContactProfileHeader(
                    avatar = uiState.avatar,
                    onAvatarClick = { onPickAvatar?.invoke() }
                )
            }

            item {
                FormSection("基本信息") {
                    FormField("姓名", uiState.name, viewModel::updateName, placeholder = "请输入姓名", required = true, focusRequester = nameFocusRequester)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("昵称", uiState.nickname ?: "", viewModel::updateNickname, placeholder = "请输入昵称")
                    Spacer(modifier = Modifier.height(12.dp))
                    TagSelector(
                        mode = TagSelectorMode.SINGLE, title = "学校",
                        availableItems = uiState.educations, selectedItems = listOfNotNull(uiState.education),
                        onSelectionChange = { viewModel.updateEducation(it.firstOrNull() ?: "") },
                        onAddItem = { name, _, _ -> viewModel.addCustomType(CustomCategories.EDUCATION, name) },
                        onDeleteItem = { viewModel.deleteCustomType(it) }
                    )
                }
            }

            item {
                var showAddRelationshipDialog by remember { mutableStateOf(false) }
                FormSection(
                    title = "关系",
                    action = {
                        TextButton(onClick = { showAddRelationshipDialog = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("新增", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                ) {
                    TagSelector(
                        mode = TagSelectorMode.SINGLE, availableItems = uiState.relationships,
                        selectedItems = listOfNotNull(uiState.relationship),
                        onSelectionChange = { viewModel.updateRelationship(it.firstOrNull() ?: "") },
                        onAddItem = { name, color, icon -> viewModel.addCustomType(CustomCategories.RELATIONSHIP, name, color, icon) },
                        onDeleteItem = { viewModel.deleteCustomType(it) },
                        emptyText = "暂无关系标签，点击新增添加",
                        showHeader = false,
                        showAddDialog = showAddRelationshipDialog,
                        onAddDialogDismiss = { showAddRelationshipDialog = false }
                    )
                }
            }

            item {
                FormSection("重要日期") {
                    DatePickerField("生日", uiState.birthday, { viewModel.updateBirthday(it) }) { showDatePicker = true }
                    Spacer(modifier = Modifier.height(12.dp))
                    DatePickerField("相识日期", uiState.knowingDate, { viewModel.updateKnowingDate(it) }) { showKnowingDatePicker = true }
                }
            }

            item {
                FormSection("联系方式") {
                    FormField("电话", uiState.phone, viewModel::updatePhone, placeholder = "请输入电话")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("其他", uiState.email, viewModel::updateEmail, placeholder = "请输入其他联系方式")
                }
            }

            item {
                FormSection("位置信息") {
                    FormField("城市", uiState.city ?: "", viewModel::updateCity, placeholder = "请输入城市")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("详细地址", uiState.address ?: "", viewModel::updateAddress, placeholder = "请输入详细地址", maxLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("公司", uiState.company ?: "", viewModel::updateCompany, placeholder = "请输入公司")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("职务", uiState.jobTitle ?: "", viewModel::updateJobTitle, placeholder = "请输入职务")
                }
            }

            item {
                FormSection("个人特征") {
                    TagSelector(mode = TagSelectorMode.MULTI, title = "爱好", availableItems = uiState.hobbyOptions, selectedItems = uiState.hobbies, onSelectionChange = { viewModel.updateHobbies(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.HOBBY, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "习惯", availableItems = uiState.habitOptions, selectedItems = uiState.habits, onSelectionChange = { viewModel.updateHabits(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.HABIT, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "饮食", availableItems = uiState.dietOptions, selectedItems = uiState.diets, onSelectionChange = { viewModel.updateDiets(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.DIET, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "技能", availableItems = uiState.skillOptions, selectedItems = uiState.skills, onSelectionChange = { viewModel.updateSkills(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.SKILL, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                }
            }

            item {
                FormSection("简介") {
                    NotesField(value = uiState.notes ?: "", onValueChange = { viewModel.updateNotes(it) })
                }
            }

            item {
                FormSection("亲密度") {
                    IntimacySlider(value = uiState.intimacyScore, onValueChange = { viewModel.updateIntimacyScore(it) })
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
