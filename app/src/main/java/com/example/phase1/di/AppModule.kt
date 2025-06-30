package com.example.phase1.di

import com.example.phase1.repository.NoteRepository
import com.example.phase1.viewmodel.NoteViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Provide FirebaseFirestore
    single { FirebaseFirestore.getInstance() }

    // Provide NoteRepository
    single { NoteRepository(get()) }

    // Provide ViewModel
    viewModel { NoteViewModel(get()) }
}