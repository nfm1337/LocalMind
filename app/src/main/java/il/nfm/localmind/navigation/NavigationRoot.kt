package il.nfm.localmind.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import il.nfm.localmind.core.presentation.asString
import il.nfm.localmind.feature.notes.presentation.list.NotesListRoute
import kotlinx.coroutines.launch

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Route.NotesList)
    val currentRoute = backStack.lastOrNull().asRoute()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute.shouldShowBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onRouteClick = { route ->
                        if (route != currentRoute) {
                            backStack.add(route)
                        }
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        NavDisplay(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
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
                            onNewNoteClick = {
                                backStack.add(Route.NoteDetails(null))
                            },
                            onSnackBarMessage = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message.asString(context))
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
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

private val Route?.shouldShowBottomBar: Boolean
    get() =
        when (this) {
            Route.NotesList,
            Route.Ask,
            Route.Diagnostics,
            -> true

            is Route.NoteDetails,
            null,
            -> false
        }
