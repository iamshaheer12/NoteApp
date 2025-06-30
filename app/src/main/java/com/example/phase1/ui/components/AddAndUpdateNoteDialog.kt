import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.phase1.R
import com.example.phase1.data.NoteData
import com.example.phase1.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNote(
    isUpdate: Boolean = false,
    noteId: String = "",
    navController: NavController
) {
    val viewModel: NoteViewModel = koinViewModel()
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val text = if (isUpdate) "Update Note" else "Add Note"
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (isUpdate && noteId.isNotBlank()) {
            viewModel.getNoteById(noteId).collect { note ->
                title = note?.title ?: ""
                content = note?.description ?: ""
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is NoteViewModel.NoteEvent.ShowSuccess -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    // Clear fields
                    title = ""
                    content = ""
                    // Optionally go back
                    // navController.popBackStack()
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = "Back"
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                singleLine = false,
                maxLines = 5
            )
            Button(
                onClick = {
                    if (isUpdate){
                        viewModel.updateNote(NoteData(title = title, description = content, id = noteId))

                    }
                    else{
                        viewModel.addNote(NoteData(title = title, description = content))

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text(text)
            }
        }
    }
}
