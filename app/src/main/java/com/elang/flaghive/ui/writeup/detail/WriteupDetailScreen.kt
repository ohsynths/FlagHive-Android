package com.elang.flaghive.ui.writeup.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elang.flaghive.ui.navigation.Screen
import com.elang.flaghive.ui.components.MarkdownRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteupDetailScreen(
    navController: NavController,
    writeupId: String,
    viewModel: WriteupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(writeupId) {
        viewModel.loadWriteup(writeupId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Writeup") },
            text = { Text("Are you sure you want to delete this writeup?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteWriteup {
                        navController.popBackStack()
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.writeup?.title ?: "Writeup",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.writeup != null) {
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (uiState.isBookmarked)
                                    Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (uiState.isBookmarked)
                                    "Remove Bookmark" else "Add Bookmark"
                            )
                        }
                        if (uiState.isAuthor) {
                            IconButton(onClick = {
                                navController.navigate(
                                    Screen.EditWriteup.createRoute(writeupId)
                                )
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val writeup = uiState.writeup ?: return@Scaffold

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = writeup.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(writeup.categoryName) }
                    )
                    if (writeup.difficulty.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(writeup.difficulty) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${writeup.eventName} | ${writeup.challengeName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "by ${writeup.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                MarkdownRenderer(markdown = writeup.content)
            }
        }
    }
}
