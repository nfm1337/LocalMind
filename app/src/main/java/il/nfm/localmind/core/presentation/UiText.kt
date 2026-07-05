package il.nfm.localmind.core.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(
        val value: String,
    ) : UiText

    data class StringResource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(id, *args.toTypedArray())
    }
