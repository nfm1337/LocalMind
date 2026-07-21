package il.nfm.localmind.feature.notes

import il.nfm.localmind.core.model.UserNote
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

class FakeNotesRepository : NotesRepository {
    val notes = MutableStateFlow<List<UserNote>>(emptyList())
    val upsertedNotes = mutableListOf<UserNote>()
    val deletedNoteIds = mutableListOf<String>()

    var createCalls = 0
        private set
    var upsertCalls = 0
        private set

    var shouldFailDelete = false
    var shouldFailUpsert = false
    var shouldFailCreate = false

    override fun observeNotes(): Flow<List<UserNote>> = notes

    override fun getNoteById(id: String): Flow<UserNote?> = MutableStateFlow(notes.value.firstOrNull { it.id == id })

    override suspend fun createNote(
        title: String,
        content: String,
    ): UserNote {
        if (shouldFailCreate) {
            error("Create failed")
        }

        createCalls++

        val now = Clock.System.now()
        val note =
            UserNote(
                id = "created-note-$createCalls",
                title = title,
                content = content,
                createdAt = now,
                updatedAt = now,
            )

        notes.update { currentNotes ->
            currentNotes + note
        }

        return note
    }

    override suspend fun upsertNote(note: UserNote) {
        if (shouldFailUpsert) {
            error("Upsert failed")
        }

        upsertCalls++
        upsertedNotes += note

        notes.update { currentNotes ->
            currentNotes.filterNot { it.id == note.id } + note
        }
    }

    override suspend fun deleteNoteById(id: String) {
        if (shouldFailDelete) error("Delete failed")

        deletedNoteIds += id
        notes.update { currentNotes ->
            currentNotes.filterNot { it.id == id }
        }
    }

    override suspend fun deleteNotesByIds(noteIds: Collection<String>) {
        if (shouldFailDelete) error("Delete failed")
        deletedNoteIds += noteIds
        notes.update { currentNotes ->
            currentNotes.filterNot { it.id in noteIds }
        }
    }
}
