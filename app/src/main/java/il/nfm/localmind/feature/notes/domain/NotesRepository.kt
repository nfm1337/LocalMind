package il.nfm.localmind.feature.notes.domain

import il.nfm.localmind.core.model.UserNote
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<UserNote>>

    fun getNoteById(id: String): Flow<UserNote?>

    suspend fun createNote(
        title: String,
        content: String,
    ): UserNote

    suspend fun upsertNote(note: UserNote)

    suspend fun deleteNoteById(id: String)

    suspend fun deleteNotesByIds(noteIds: Collection<String>)
}
