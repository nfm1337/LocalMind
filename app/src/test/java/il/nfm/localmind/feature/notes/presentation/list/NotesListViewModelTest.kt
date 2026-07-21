package il.nfm.localmind.feature.notes.presentation.list

import app.cash.turbine.test
import il.nfm.localmind.MainDispatcherRule
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.feature.notes.FakeNotesRepository
import il.nfm.localmind.feature.notes.presentation.userNote
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NotesListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNotesRepository

    @Before
    fun setUp() {
        repository = FakeNotesRepository()
    }

    @Test
    fun `loads notes from repository into ui state`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(),
                    notesRepository = repository,
                )

            viewModel.uiState.test {
                assertEquals(emptyList(), awaitItem().notes)

                repository.notes.value =
                    listOf(
                        userNote(id = "note-1", title = "First Note", content = "Hello world"),
                    )

                val state = awaitItem()
                assertEquals(1, state.notesCount)
                assertEquals("note-1", state.notes.single().id)
                assertEquals(UiText.DynamicString("First Note"), state.notes.single().title)
            }
        }

    @Test
    fun `loads empty notes from repository after non-empty notes`() =
        runTest {
            repository.notes.value = listOf(userNote("note-1"))

            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(),
                    notesRepository = repository,
                )

            viewModel.uiState.test {
                assertEquals(1, awaitItem().notesCount)

                repository.notes.value = emptyList()

                assertEquals(0, awaitItem().notesCount)
            }
        }

    @Test
    fun `keeps selection when repository updates notes`() =
        runTest {
            repository.notes.value = listOf(userNote("note-1"))

            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1")),
                    notesRepository = repository,
                )

            viewModel.uiState.test {
                assertEquals(setOf("note-1"), awaitItem().selectedNoteIds)

                repository.notes.value = listOf(userNote("note-2"))

                val state = awaitItem()
                assertEquals(setOf("note-1"), state.selectedNoteIds)
                assertEquals("note-2", state.notes.single().id)
            }
        }

    @Test
    fun `toggleNoteSelection selects note`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(),
                    notesRepository = repository,
                )

            viewModel.uiState.test {
                assertEquals(0, awaitItem().selectedNoteIds.size)
                viewModel.toggleNoteSelection("note-1")

                val state = awaitItem()
                assertEquals(1, state.selectedNoteIds.size)
                assertEquals("note-1", state.selectedNoteIds.single())
            }
        }

    @Test
    fun `toggleNoteSelection deselects note`() {
        val viewModel =
            NotesListViewModel(
                initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1")),
                notesRepository = repository,
            )

        viewModel.toggleNoteSelection("note-1")

        assertEquals(emptySet(), viewModel.uiState.value.selectedNoteIds)
    }

    @Test
    fun `clearSelection clears selected note ids`() {
        val viewModel =
            NotesListViewModel(
                initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1", "note-2")),
                notesRepository = repository,
            )

        viewModel.clearSelection()

        assertEquals(emptySet(), viewModel.uiState.value.selectedNoteIds)
    }

    @Test
    fun `deleteSelection deletes selected notes`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1", "note-2")),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                awaitItem()
                assertEquals(listOf("note-1", "note-2"), repository.deletedNoteIds)
            }
        }

    @Test
    fun `deleteSelection clears selection on success`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1")),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                awaitItem()
                assertEquals(emptySet(), viewModel.uiState.value.selectedNoteIds)
            }
        }

    @Test
    fun `deleteSelection emits success snackbar`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1")),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                val event = awaitItem() as NotesListEvent.ShowSnackbar
                val message = event.message as UiText.PluralResource

                assertEquals(R.plurals.notes_deleted_count, message.id)
            }
        }

    @Test
    fun `deleteSelection does nothing when selection is empty`() =
        runTest {
            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                assertEquals(emptyList(), repository.deletedNoteIds)
                expectNoEvents()
            }
        }

    @Test
    fun `deleteSelection keeps selection when delete fails`() =
        runTest {
            repository.shouldFailDelete = true

            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1", "note-2")),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                awaitItem()
                assertEquals(setOf("note-1", "note-2"), viewModel.uiState.value.selectedNoteIds)
            }
        }

    @Test
    fun `deleteSelection emits error snackbar when delete fails`() =
        runTest {
            repository.shouldFailDelete = true

            val viewModel =
                NotesListViewModel(
                    initialUiState = NotesListUiState(selectedNoteIds = setOf("note-1", "note-2")),
                    notesRepository = repository,
                )

            viewModel.events.test {
                viewModel.deleteSelection()

                val event = awaitItem() as NotesListEvent.ShowSnackbar
                val message = event.message as UiText.StringResource

                assertEquals(R.string.notes_deletion_error, message.id)
            }
        }
}
