package il.nfm.localmind.feature.notes.presentation

import il.nfm.localmind.core.model.UserNote
import kotlin.time.Instant

fun userNote(
    id: String,
    title: String = "Title",
    content: String = "Content",
): UserNote =
    UserNote(
        id = id,
        title = title,
        content = content,
        createdAt = Instant.parse("2026-07-10T10:00:00Z"),
        updatedAt = Instant.parse("2026-07-10T10:00:00Z"),
    )
