package com.example.phase1.navigation

import AddNote
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.phase1.ui.components.MainScreen
import com.example.phase1.ui.components.NoteDetailsScreen
import com.example.phase1.viewmodel.NoteViewModel

object Routes {
    const val HOME_SCREEN = "home_screen"
    const val ADD_NOTE_SCREEN = "add_note_screen"
    const val ADD_NOTE_WITH_ARGS = "add_note_screen/{noteId}/{isUpdate}"
    const val  NOTE_DETAILS_SCREEN = "note_details_screen/{noteId}"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME_SCREEN) {

        composable(route = Routes.HOME_SCREEN) {
            MainScreen(navController = navController,)
        }

        composable(
            route = Routes.ADD_NOTE_WITH_ARGS,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType },
                navArgument("isUpdate") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            val isUpdate = backStackEntry.arguments?.getBoolean("isUpdate") ?: false
            AddNote(noteId = noteId, isUpdate = isUpdate, navController = navController)
        }
        composable(route = Routes.ADD_NOTE_SCREEN) {
            AddNote(noteId = "", isUpdate = false, navController = navController)
        }
        composable (route = Routes.NOTE_DETAILS_SCREEN) {
            val noteId = it.arguments?.getString("noteId") ?: ""
            NoteDetailsScreen(noteId = noteId, navController = navController)
        }
    }
}
