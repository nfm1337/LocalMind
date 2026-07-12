package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.AppIcon
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.core.presentation.asString
import il.nfm.localmind.ui.theme.Dimens
import il.nfm.localmind.ui.theme.Elevations
import il.nfm.localmind.ui.theme.LocalLocalMindColors
import il.nfm.localmind.ui.theme.LocalMindCorner
import il.nfm.localmind.ui.theme.LocalMindShapes
import il.nfm.localmind.ui.theme.LocalMindTheme
import il.nfm.localmind.ui.theme.LocalSpacing
import kotlin.math.hypot

@Composable
fun NotesListScreen(
    uiState: NotesListUiState,
    onNoteClick: (String) -> Unit,
    onToggleNoteSelection: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) {
        NotesListTopBar(
            modifier = Modifier.fillMaxWidth(),
            selectedCount = uiState.selectedCount,
            onClearSelection = onClearSelection,
            onDeleteSelection = onDeleteSelection,
        )
        NotesListContent(
            uiState = uiState,
            onNoteClick = onNoteClick,
            onToggleNoteSelection = onToggleNoteSelection,
            onNewNoteClick = onNewNoteClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NotesListContent(
    uiState: NotesListUiState,
    onNoteClick: (String) -> Unit,
    onToggleNoteSelection: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    if (uiState.notes.isEmpty()) {
        EmptyNotesMessage(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(spacing.xxl),
            onNewNoteClick = onNewNoteClick,
        )
    } else {
        PopulatedNotesList(
            notes = uiState.notes,
            selectedNoteIds = uiState.selectedNoteIds,
            onNoteClick = onNoteClick,
            onToggleNoteSelection = onToggleNoteSelection,
            onNewNoteClick = onNewNoteClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PopulatedNotesList(
    notes: List<NoteUi>,
    selectedNoteIds: Set<String>,
    onNoteClick: (String) -> Unit,
    onToggleNoteSelection: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            items(
                items = notes,
                key = { it.id },
            ) { note ->
                NoteListItem(
                    note = note,
                    selected = note.id in selectedNoteIds,
                    onClick = {
                        if (selectedNoteIds.isNotEmpty()) {
                            onToggleNoteSelection(note.id)
                        } else {
                            onNoteClick(note.id)
                        }
                    },
                    onLongClick = { onToggleNoteSelection(note.id) },
                )
            }
        }

        NewNoteFloatingActionButton(
            onClick = onNewNoteClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spacing.lg),
        )
    }
}

@Composable
private fun NewNoteFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = LocalMindCorner.fab,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation =
            FloatingActionButtonDefaults.elevation(
                defaultElevation = Elevations.fab,
                pressedElevation = Elevations.level0,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.padding(horizontal = spacing.md))
            Icon(
                modifier = Modifier.size(Dimens.iconMd),
                painter = painterResource(R.drawable.outline_edit),
                contentDescription = null,
            )
            Spacer(Modifier.padding(horizontal = spacing.xxs))
            Text(text = stringResource(R.string.new_note))
            Spacer(Modifier.padding(horizontal = spacing.md))
        }
    }
}

@Composable
private fun EmptyNotesMessage(
    onNewNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StripedRoundedBox()
        EmptyNotesCopy()
        CreateNoteButton(
            onClick = onNewNoteClick,
            modifier = Modifier.padding(spacing.lg),
        )
        OfflineStatement()
    }
}

@Composable
private fun EmptyNotesCopy(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(vertical = spacing.lg),
            text = stringResource(R.string.notes_list_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier =
                Modifier
                    .padding(
                        start = spacing.xxl * 2,
                        end = spacing.xxl * 2,
                    ),
            text = stringResource(R.string.notes_list_empty_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W500,
        )
    }
}

@Composable
private fun CreateNoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Button(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        contentPadding =
            PaddingValues(
                vertical = spacing.md,
                horizontal = spacing.xl,
            ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.size(spacing.sm))
        Text(text = stringResource(R.string.notes_create_note))
    }
}

@Composable
private fun OfflineStatement(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(Dimens.iconXs),
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.padding(horizontal = LocalSpacing.current.xxs))
        Text(text = stringResource(R.string.no_connection_statement))
    }
}

@Composable
private fun NoteListItem(
    note: NoteUi,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onLongClickLabel = "Select note",
                ),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ),
        border =
            if (selected) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
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

@Composable
private fun StripedRoundedBox(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceContainer
    Box(
        modifier =
            modifier
                .size(Dimens.stripedBox)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .drawWithCache {
                    val stripeWidth = 8.dp.toPx()
                    val step = stripeWidth * 2
                    val diagonal = hypot(size.width, size.height)
                    onDrawBehind {
                        rotate(degrees = -45f) {
                            var x = -diagonal

                            while (x < diagonal * 2) {
                                drawRect(
                                    color = color,
                                    topLeft = Offset(x, -diagonal),
                                    size =
                                        Size(
                                            width = stripeWidth,
                                            height = diagonal * 3,
                                        ),
                                )

                                x += step
                            }
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(Dimens.scoreBarWidth),
            painter = painterResource(R.drawable.note_stack),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = selectedCount > 0,
        modifier = modifier,
        label = "Notes top bar state",
        transitionSpec = @Suppress("MagicNumber") {
            fadeIn() + slideInVertically { -it / 3 } togetherWith
                fadeOut() + slideOutVertically { -it / 3 }
        },
    ) { isSelectionMode ->
        if (isSelectionMode) {
            NotesSelectionTopBar(
                selectedCount = selectedCount,
                onClearSelection = onClearSelection,
                onDeleteSelection = onDeleteSelection,
            )
        } else {
            NotesDefaultTopBar()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text =
                    pluralStringResource(
                        id = R.plurals.notes_selected_count,
                        count = selectedCount,
                        selectedCount,
                    ),
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cancel_notes_selection),
                )
            }
        },
        actions = {
            IconButton(onClick = onDeleteSelection) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_notes_action),
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesDefaultTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    modifier =
                        Modifier
                            .size(Dimens.appIconSmall)
                            .clip(LocalMindShapes.extraSmall)
                            .background(LocalLocalMindColors.current.brandLogoContainer),
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
private fun NotesListScreenEmptyPreview() {
    LocalMindTheme(darkTheme = false) {
        NotesListScreen(
            uiState = NotesListUiState(),
            onNoteClick = {},
            onNewNoteClick = {},
            onDeleteSelection = {},
            onClearSelection = {},
            onToggleNoteSelection = {},
        )
    }
}

@Preview
@Composable
private fun NotesListScreenEmptyDarkPreview() {
    LocalMindTheme(darkTheme = true) {
        NotesListScreen(
            uiState = NotesListUiState(),
            onNoteClick = {},
            onNewNoteClick = {},
            onDeleteSelection = {},
            onClearSelection = {},
            onToggleNoteSelection = {},
        )
    }
}

@Preview
@Composable
private fun NotesListScreenPreview() {
    LocalMindTheme(darkTheme = false) {
        NotesListScreen(
            uiState = previewNotesListUiState(),
            onNoteClick = {},
            onNewNoteClick = {},
            onDeleteSelection = {},
            onClearSelection = {},
            onToggleNoteSelection = {},
        )
    }
}

@Preview
@Composable
private fun NotesListScreenDarkPreview() {
    LocalMindTheme(darkTheme = true) {
        NotesListScreen(
            uiState = previewNotesListUiState().copy(selectedNoteIds = setOf("1", "2")),
            onNoteClick = {},
            onNewNoteClick = {},
            onDeleteSelection = {},
            onClearSelection = {},
            onToggleNoteSelection = {},
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
