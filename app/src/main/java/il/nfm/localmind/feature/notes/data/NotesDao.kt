package il.nfm.localmind.feature.notes.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE id in (:noteIds)")
    suspend fun deleteNotesByIds(noteIds: Collection<String>)
}
