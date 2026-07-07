package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.AppIcon
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.core.presentation.asString
import il.nfm.localmind.ui.theme.Dimens
import il.nfm.localmind.ui.theme.LocalMindShapes
import il.nfm.localmind.ui.theme.LocalMindTheme
import il.nfm.localmind.ui.theme.LocalSpacing

@Composable
fun NotesListScreen(
    uiState: NotesListUiState,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        topBar = { NotesListTopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        NotesListContent(
            uiState = uiState,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun NotesListContent(
    uiState: NotesListUiState,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    if (uiState.notes.isEmpty()) {
        EmptyNotesMessage(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(spacing.xxl),
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        items(
            items = uiState.notes,
            key = { it.id },
        ) { note ->
            NoteListItem(
                note = note,
                onClick = { onNoteClick(note.id) },
            )
        }
    }
}

@Composable
private fun EmptyNotesMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.notes_list_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = LocalSpacing.current.xs),
            text = stringResource(R.string.notes_list_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoteListItem(
    note: NoteUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = LocalMindShapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(LocalSpacing.current.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = note.title.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = note.updatedAtLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                modifier = Modifier.padding(top = LocalSpacing.current.sm),
                text = note.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    modifier =
                        Modifier
                            .size(Dimens.appIconSmall)
                            .clip(LocalMindShapes.extraSmall)
                            .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    modifier = Modifier.padding(start = LocalSpacing.current.md),
                    text = stringResource(R.string.app_name),
                )
            }
        },
    )
}

@Preview
@Composable
private fun NotesListScreenPreview() {
    LocalMindTheme(darkTheme = false) {
        NotesListScreen(
            uiState = previewNotesListUiState(),
            onNoteClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun NotesListScreenDarkPreview() {
    LocalMindTheme(darkTheme = true) {
        NotesListScreen(
            uiState = previewNotesListUiState(),
            onNoteClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun previewNotesListUiState(): NotesListUiState =
    NotesListUiState(
        notes =
            listOf(
                NoteUi(
                    id = "1",
                    title = UiText.DynamicString("Android architecture notes"),
                    preview = "Keep the local RAG flow small until indexing and retrieval are stable.",
                    updatedAtLabel = "Jul 7, 11:20",
                ),
                NoteUi(
                    id = "2",
                    title = UiText.DynamicString("Embedding checklist"),
                    preview = "Use E5 prefixes, mean pooling, and normalized vectors.",
                    updatedAtLabel = "Jul 6, 18:45",
                ),
            ),
    )
