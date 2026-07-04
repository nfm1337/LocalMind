package il.nfm.localmind.core.model

import kotlin.time.Instant

data class UserNote(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
