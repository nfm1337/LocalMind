package il.nfm.localmind.feature.notes.data

import il.nfm.localmind.core.model.UserNote
import kotlin.time.Instant

fun UserNote.toDbModel() =
    NoteEntity(
        id = this.id,
        title = this.title,
        content = this.content,
        createdAt = this.createdAt.toEpochMilliseconds(),
        updatedAt = this.updatedAt.toEpochMilliseconds(),
    )

fun NoteEntity.toDomainModel() =
    UserNote(
        id = this.id,
        title = this.title,
        content = this.content,
        createdAt = Instant.fromEpochMilliseconds(this.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
    )
