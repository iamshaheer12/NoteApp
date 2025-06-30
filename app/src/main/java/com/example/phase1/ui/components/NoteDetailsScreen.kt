package com.example.phase1.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.phase1.data.NoteData
import com.example.phase1.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(
    noteId: String,
    navController: NavController
) {
    val viewModel: NoteViewModel = koinViewModel()
    var note by remember { mutableStateOf<NoteData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Log.d("NoteDetailsScreen", "ViewModel instance: ${viewModel.hashCode()}, noteId: $noteId")

    // Collect the note from Flow
    LaunchedEffect(noteId) {
        if (noteId.isBlank()) {
            Log.e("NoteDetailsScreen", "Invalid noteId: '$noteId'")
            errorMessage = "Invalid note ID"
            isLoading = false
            return@LaunchedEffect
        }
        viewModel.getNoteById(noteId).collectLatest { result ->
            isLoading = false
            if (result == null) {
                errorMessage = "Note not found or failed to load."
            } else {
                note = result
                errorMessage = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    note?.let { noteData ->
                        if (noteData.id.isNullOrBlank()) {
                            Log.e("NoteDetailsScreen", "Invalid note ID for note: $noteData")
                            return@let
                        }
                        IconButton(onClick = {
                            Log.d("NoteDetailsScreen", "Setting selectedNote: $noteData")
                            viewModel.selectNote(noteData)
                            navController.navigate("add_note_screen/${noteData.id}/${true}")
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp
                    )
                }
                note != null -> {
                    Text(
                        text = note?.title ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = note?.description ?: "",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}