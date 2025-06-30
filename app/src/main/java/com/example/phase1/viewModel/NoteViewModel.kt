package com.example.phase1.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phase1.data.NoteData
import com.example.phase1.data.NoteState
import com.example.phase1.repository.NoteRepository
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.*

import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    val uiState: StateFlow<NoteState<List<NoteData>>> =
        repository.getNotesFlow()
            .map<List<NoteData>, NoteState<List<NoteData>>> { notes ->
                NoteState.Success(notes)
            }
            .catch { e ->
                emit(NoteState.Error("Failed to load notes: ${e.message}"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NoteState.Loading
            )

    private val _eventFlow = MutableSharedFlow<NoteEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun addNote(note: NoteData) {
        viewModelScope.launch {
            repository.addNoteWithAutoId(note)
                .onSuccess {
                    _eventFlow.emit(NoteEvent.ShowSuccess("Note added successfully"))
                }
                .onFailure { exception ->
                    _eventFlow.emit(NoteEvent.ShowError("Failed to add note: ${exception.message ?: "Unknown error"}"))
                }
        }
    }

    fun updateNote(note: NoteData) {
        viewModelScope.launch {
            repository.updateNote(note)
                .onSuccess {
                    _eventFlow.emit(NoteEvent.ShowSuccess("Note updated successfully"))
                }
                .onFailure { exception ->
                    _eventFlow.emit(NoteEvent.ShowError("Failed to update note: ${exception.message ?: "Unknown error"}"))
                }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
                .onSuccess {
                    _eventFlow.emit(NoteEvent.ShowSuccess("Note deleted successfully"))
                }
                .onFailure { exception ->
                    _eventFlow.emit(NoteEvent.ShowError("Failed to delete note: ${exception.message ?: "Unknown error"}"))
                }
        }
    }
    fun getNoteById(noteId: String): Flow<NoteData?> {
        return repository.getNoteById(noteId)
    }

    var selectedNote by mutableStateOf<NoteData?>(null)
        private set

    fun selectNote(note: NoteData) {
        selectedNote = note
    }

    sealed class NoteEvent {
        data class ShowSuccess(val message: String) : NoteEvent()
        data class ShowError(val message: String) : NoteEvent()
    }
}
