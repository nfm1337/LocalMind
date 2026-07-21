package il.nfm.localmind.feature.notes.presentation.details

import app.cash.turbine.test
import il.nfm.localmind.MainDispatcherRule
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.feature.notes.FakeNotesRepository
import il.nfm.localmind.feature.notes.presentation.userNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNotesRepository
    private lateinit var viewModel: NoteDetailsViewModel

    @Before
    fun setUp() {
        repository = FakeNotesRepository()
        viewModel = NoteDetailsViewModel(repository)
    }

    @Test
    fun `creates one note with latest content`() =
        runTest {
            viewModel.load(noteId = null)

            viewModel.onContentChange("First")
            viewModel.onContentChange("Second")
            viewModel.onContentChange("Last")

            advanceUntilIdle()

            assertEquals(1, repository.notes.value.size)
            assertEquals(1, repository.createCalls)
            assertEquals(
                "Last",
                repository.notes.value
                    .single()
                    .content,
            )
        }

    @Test
    fun `saves latest content after debounce`() =
        runTest {
            viewModel.load(noteId = null)

            viewModel.onContentChange("First")
            viewModel.onContentChange("Second")
            viewModel.onContentChange("Last")

            advanceUntilIdle()

            assertEquals(1, repository.notes.value.size)
            assertEquals(
                "Last",
                repository.notes.value
                    .single()
                    .content,
            )
        }

    @Test
    fun `updates existing note with latest content`() =
        runTest {
            repository.notes.value = listOf(userNote(id = "note-1", content = "Original"))

            viewModel.load(noteId = "note-1")
            advanceUntilIdle()

            viewModel.onContentChange("First")
            viewModel.onContentChange("Last")

            advanceUntilIdle()

            assertEquals(1, repository.notes.value.size)
            assertEquals(
                "Last",
                repository.notes.value
                    .single()
                    .content,
            )
        }

    @Test
    fun `flush saves latest content immediately`() =
        runTest {
            viewModel.load(noteId = null)

            viewModel.onContentChange("Not yet autosaved")

            val result = viewModel.flush()

            assertTrue(result)
            assertEquals(
                "Not yet autosaved",
                repository.notes.value
                    .single()
                    .content,
            )
        }

    @Test
    fun `empty draft is not saved`() =
        runTest {
            viewModel.load(noteId = null)

            viewModel.onContentChange(" ")

            val result = viewModel.flush()

            assertTrue(result)
            assertEquals(0, repository.createCalls)
            assertEquals(0, repository.notes.value.size)
        }

    @Test
    fun `failed save reports failure`() =
        runTest {
            repository.notes.value = listOf(userNote("note-1"))
            repository.shouldFailUpsert = true

            viewModel.events.test {
                viewModel.load("note-1")
                advanceUntilIdle()

                assertEquals(NoteLoadState.Ready, viewModel.uiState.value.loadState)

                viewModel.onContentChange("Content")
                viewModel.flush()

                val event = awaitItem() as NoteDetailsEvent.ShowSnackbar
                val message = event.message as UiText.StringResource
                assertEquals(R.string.error_failed_to_save_note, message.id)
            }
        }
}
