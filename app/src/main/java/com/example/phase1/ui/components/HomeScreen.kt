package com.example.phase1.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.phase1.data.NoteData
import com.example.phase1.data.NoteState
import com.example.phase1.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    navController: NavController,
) {
    val viewModel: NoteViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is NoteViewModel.NoteEvent.ShowSuccess -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is NoteViewModel.NoteEvent.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_note_screen")
            }) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "My Notes",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            when (uiState) {
                is NoteState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp, 50.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                is NoteState.Success<*> -> {
                    val notes = (uiState as NoteState.Success<List<NoteData>>).data
                    AllNotes(
                        notes = notes,
                        viewModel = viewModel,
                        onItemClick = { note ->
                            if (note.id.isNullOrBlank()) {
                                Log.e("MainScreen", "Invalid note ID: $note")
                                return@AllNotes
                            }
                            Log.d("MainScreen", "Navigating to note_details_screen with ID: ${note.id}")
                            navController.navigate("note_details_screen/${note.id}")
                        },
                        modifier = Modifier
                    )
                }
                is NoteState.Error -> {
                    Text(
                        text = (uiState as NoteState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotes(
    notes: List<NoteData>,
    viewModel: NoteViewModel,
    onItemClick: (NoteData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id ?: "" }) { note ->
            if (note.id.isNullOrBlank()) {
                Log.e("AllNotes", "Invalid note ID for note: $note")
                return@items
            }
            var showDeleteDialog by remember { mutableStateOf(false) }
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        showDeleteDialog = true
                        false // Wait for dialog confirmation
                    } else {
                        false
                    }
                },
                positionalThreshold = { totalWidth -> totalWidth * 0.5f } // 50% threshold
            )

            // Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Note") },
                    text = { Text("Are you sure you want to delete '${note.title ?: "this note"}'?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteNote(note.id)
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                backgroundContent = {
                    val color = when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp)
                        ,

                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                enableDismissFromStartToEnd = false, // Disable left-to-right swipe
                enableDismissFromEndToStart = true,  // Enable right-to-left swipe
                content = {
                    NoteItemCard(
                        item = note,
                        onItemClick = { onItemClick(note) }
                    )
                }
            )
        }
    }
}

