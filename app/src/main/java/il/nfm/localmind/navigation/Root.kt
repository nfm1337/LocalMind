package il.nfm.localmind.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import il.nfm.localmind.feature.notes.presentation.list.NotesListRoute

@Composable
fun Root(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Route.NotesList)
    val currentRoute = backStack.lastOrNull().asRoute()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute.shouldShowBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onRouteClick = { backStack.add(it) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier.padding(paddingValues),
            backStack = backStack,
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<Route.NotesList> {
                        NotesListRoute(
                            onNoteClick = {
                                backStack.add(Route.NoteDetails(it))
                            },
                        )
                    }
                    entry<Route.Ask> {
                        Text("Ask")
                    }
                    entry<Route.Diagnostics> {
                        Text("Diagnostics")
                    }
                    entry<Route.NoteDetails> { key ->
                        Text(
                            text = "Note ${key.id}",
                        )
                    }
                },
        )
    }
}
