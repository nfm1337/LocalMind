package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.runtime.Immutable
import il.nfm.localmind.R
import il.nfm.localmind.core.model.UserNote
import il.nfm.localmind.core.presentation.UiText
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Immutable
data class NoteUi(
    val id: String,
    val title: UiText,
    val preview: String,
    val updatedAtLabel: String,
)

fun UserNote.toUi(): NoteUi =
    NoteUi(
        id = id,
        title = title.toTitleUiText(),
        preview = content.take(NOTE_PREVIEW_MAX_LENGTH),
        updatedAtLabel = updatedAt.toUpdatedAtLabel(),
    )

private fun String.toTitleUiText(): UiText =
    if (isBlank()) {
        UiText.StringResource(R.string.note_untitled_title)
    } else {
        UiText.DynamicString(this)
    }

private fun Instant.toUpdatedAtLabel(): String =
    DateTimeFormatter
        .ofPattern("MMM d, HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(this.toJavaInstant())

private const val NOTE_PREVIEW_MAX_LENGTH = 120
