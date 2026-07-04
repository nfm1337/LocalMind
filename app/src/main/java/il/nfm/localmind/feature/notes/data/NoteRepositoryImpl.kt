package il.nfm.localmind.feature.notes.data

import il.nfm.localmind.core.model.UserNote
import il.nfm.localmind.feature.notes.domain.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl
    @Inject
    constructor(
        private val noteDao: NoteDao,
    ) : NoteRepository {
        override fun observeNotes(): Flow<List<UserNote>> =
            noteDao.observeNotes().map {
                it.map { dbModel ->
                    dbModel.toDomainModel()
                }
            }

        override fun getNoteById(id: String): Flow<UserNote?> =
            noteDao.observeNoteById(id).map { dbModel -> dbModel?.toDomainModel() }

        override suspend fun upsertNote(note: UserNote) {
            val dbModel = note.toDbModel()
            noteDao.upsertNote(dbModel)
        }

        override suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)
    }
