package com.example.phase1.repository

import android.util.Log
import com.example.phase1.core.Constant
import com.example.phase1.data.NoteData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteRepository(private val db: FirebaseFirestore) {

    private val notesCollection = db.collection(Constant.COLLECTION_NAME)

    suspend fun addNoteWithAutoId(note: NoteData): Result<Unit> = try {
        val docRef = notesCollection.add(note.copy(id = "")).await()
        val noteWithId = note.copy(id = docRef.id)
        docRef.set(noteWithId).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("NoteRepository", "Error adding note: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun updateNote(note: NoteData): Result<Unit> = try {
        notesCollection.document(note.id ?: "").set(note).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("NoteRepository", "Error updating note: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = try {
        notesCollection.document(noteId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("NoteRepository", "Error deleting note $noteId: ${e.message}", e)
        Result.failure(e)
    }

    fun getNotesFlow(): Flow<List<NoteData>> = callbackFlow {
        try {
            val listener = FirebaseFirestore.getInstance()
                .collection("notes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NoteRepository", "Error fetching notes: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    val notes = snapshot?.toObjects(NoteData::class.java).orEmpty()
                    trySend(notes).isSuccess
                }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error setting up notes listener: ${e.message}", e)
            close(e)
        }
    }

    fun getNoteById(noteId: String): Flow<NoteData?> = callbackFlow {
        if (noteId.isBlank()) {
            Log.e("NoteRepository", "Invalid noteId: '$noteId'")
            trySend(null).isSuccess
            close()
            return@callbackFlow
        }

        try {
            val docRef = notesCollection.document(noteId)
            Log.d("NoteRepository", "Fetching note with ID: $noteId, path: ${docRef.path}")
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NoteRepository", "Error fetching note $noteId: ${error.message}", error)
                    trySend(null).isSuccess
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    Log.w("NoteRepository", "Note $noteId does not exist")
                    trySend(null).isSuccess
                    return@addSnapshotListener
                }
                val note = snapshot.toObject(NoteData::class.java)?.copy(id = snapshot.id)
                Log.d("NoteRepository", "Fetched note: $note")
                trySend(note).isSuccess
            }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error setting up note listener for noteId: $noteId", e)
            trySend(null).isSuccess
            close(e)
        }
    }
}