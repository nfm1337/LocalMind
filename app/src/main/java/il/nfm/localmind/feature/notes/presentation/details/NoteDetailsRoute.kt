package il.nfm.localmind.feature.notes.presentation.details

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.core.presentation.stringResourceUiText
import kotlinx.coroutines.launch

@Composable
fun NoteDetailsRoute(
    noteId: String?,
    onBackClick: () -> Unit,
    onSnackBarMessage: (UiText) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isLeaving by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(noteId) {
        viewModel.load(noteId)
    }

    val saveAndNavigateBack = {
        if (!isLeaving) {
            isLeaving = true

            scope.launch {
                val savedSuccessfully = viewModel.flush()

                if (savedSuccessfully) {
                    onBackClick()
                } else {
                    isLeaving = false

                    onSnackBarMessage(stringResourceUiText(R.string.error_failed_to_save_note))
                }
            }
        }
    }

    BackHandler(
        enabled = !isLeaving,
        onBack = saveAndNavigateBack,
    )

    NoteDetailsScreen(modifier = modifier)
}
