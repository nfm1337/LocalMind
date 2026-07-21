package il.nfm.localmind.feature.notes.data

import il.nfm.localmind.core.model.UserNote
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Clock

class NotesRepositoryImpl
    @Inject
    constructor(
        private val notesDao: NotesDao,
    ) : NotesRepository {
        override fun observeNotes(): Flow<List<UserNote>> =
            notesDao.observeNotes().map {
                it.map { dbModel ->
                    dbModel.toDomainModel()
                }
            }

        override fun getNoteById(id: String): Flow<UserNote?> =
            notesDao.observeNoteById(id).map { dbModel -> dbModel?.toDomainModel() }

        override suspend fun createNote(
            title: String,
            content: String,
        ): UserNote {
            val now = Clock.System.now()
            val note =
                UserNote(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    createdAt = now,
                    updatedAt = now,
                )
            notesDao.upsertNote(note.toDbModel())

            return note
        }

        override suspend fun upsertNote(note: UserNote) {
            val dbModel = note.toDbModel()
            notesDao.upsertNote(dbModel)
        }

        override suspend fun deleteNoteById(id: String) = notesDao.deleteNoteById(id)

        override suspend fun deleteNotesByIds(noteIds: Collection<String>) {
            if (noteIds.isEmpty()) return
            notesDao.deleteNotesByIds(noteIds)
        }
    }
