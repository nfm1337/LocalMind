package il.nfm.localmind.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    val showBottomBar: Boolean
        get() = true

    @Serializable
    data object NotesList : Route

    @Serializable
    data object Ask : Route

    @Serializable
    data object Diagnostics : Route

    @Serializable
    data class NoteDetails(
        val id: String,
    ) : Route {
        override val showBottomBar: Boolean = false
    }
}

val Route?.shouldShowBottomBar: Boolean
    get() = this?.showBottomBar == true

fun NavKey?.asRoute(): Route? = this as? Route
