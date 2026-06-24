package com.elang.flaghive.ui.writeup.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elang.flaghive.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWriteupScreen(
    navController: NavController,
    writeupId: String,
    viewModel: EditWriteupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(writeupId) {
        viewModel.loadWriteup(writeupId)
    }

    var title by remember { mutableStateOf("") }
    var eventName by remember { mutableStateOf("") }
    var challengeName by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.writeup) {
        if (!initialized && uiState.writeup != null) {
            title = uiState.writeup!!.title
            eventName = uiState.writeup!!.eventName
            challengeName = uiState.writeup!!.challengeName
            difficulty = uiState.writeup!!.difficulty
            content = uiState.writeup!!.content
            selectedCategory = uiState.writeup!!.categoryId
            initialized = true
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Screen.WriteupDetail.createRoute(writeupId)) {
                popUpTo(Screen.EditWriteup.createRoute(writeupId)) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Writeup") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event / CTF Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = challengeName,
                    onValueChange = { challengeName = it },
                    label = { Text("Challenge Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text("Difficulty (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.categories.find { it.id == selectedCategory }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.id
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (Markdown)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                    minLines = 10
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val category = uiState.categories.find { it.id == selectedCategory }
                        val updatedWriteup = uiState.writeup!!.copy(
                            title = title,
                            content = content,
                            categoryId = selectedCategory,
                            categoryName = category?.name ?: "",
                            eventName = eventName,
                            challengeName = challengeName,
                            difficulty = difficulty
                        )
                        viewModel.updateWriteup(writeupId, updatedWriteup)
                    },
                    enabled = !uiState.isSaving && title.isNotBlank() &&
                        content.isNotBlank() && selectedCategory.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
