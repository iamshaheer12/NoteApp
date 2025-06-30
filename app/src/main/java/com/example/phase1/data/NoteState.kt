package com.example.phase1.data


sealed class NoteState<out T> {
    object Loading : NoteState<Nothing>()
    data class Success<T>(val data: T) : NoteState<T>()
    data class Error(val message: String) : NoteState<Nothing>()
}