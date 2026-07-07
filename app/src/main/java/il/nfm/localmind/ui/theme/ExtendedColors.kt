package il.nfm.localmind.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Extended (non-Material) semantic colors used across LocalMind that don't map
 * to a standard ColorScheme role: the "indexed / synced / pending" status
 * signals, retrieval-score accents, source snippet highlight, and the storage
 * usage bar segments. Exposed via CompositionLocal so they theme automatically.
 *
 * Access in a composable: LocalMindTokens.extended.statusIndexed
 */
@Immutable
data class ExtendedColors(
    // Note / index status
    val statusIndexed: Color, // check_circle — chunk is embedded & ready
    val statusIndexing: Color, // sync — in progress (amber)
    val statusPending: Color, // queued, not yet embedded
    // Retrieval / grounding
    val scoreAccent: Color, // relevance score mono text (0.92 …)
    val scoreTrack: Color, // background track of the relevance bar
    val snippetHighlight: Color, // <mark> match background in source snippets
    val onSnippetHighlight: Color,
    // Storage usage bar segments (model / index / notes)
    val storageModel: Color,
    val storageIndex: Color,
    val storageNotes: Color,
    // Privacy emphasis (the "on-device / offline" green)
    val privacyAccent: Color,
    val privacyContainer: Color,
    val onPrivacyContainer: Color,
    val brandLogoContainer: Color,
    val onBrandLogo: Color,
)

val LightLocalMindColors =
    ExtendedColors(
        statusIndexed = Color(0xFF38614F),
        statusIndexing = Color(0xFFC9A227),
        statusPending = Color(0xFF6C766F),
        scoreAccent = Color(0xFF38614F),
        scoreTrack = Color(0xFFD7E3DC),
        snippetHighlight = Color(0xFFCFE8DC),
        onSnippetHighlight = Color(0xFF123A2C),
        storageModel = Color(0xFF38614F),
        storageIndex = Color(0xFF7FAE9A),
        storageNotes = Color(0xFFBCD8CC),
        privacyAccent = Color(0xFF38614F),
        privacyContainer = Color(0xFFCFE8DC),
        onPrivacyContainer = Color(0xFF00201A),
        brandLogoContainer = Color(0xFF38614F),
        onBrandLogo = Color(0xFFFFFFFF),
    )

val DarkLocalMindColors =
    ExtendedColors(
        statusIndexed = Color(0xFF9FD5BF),
        statusIndexing = Color(0xFFE0C05A),
        statusPending = Color(0xFF8A938D),
        scoreAccent = Color(0xFF9FD5BF),
        scoreTrack = Color(0xFF2B322E),
        snippetHighlight = Color(0xFF26473B),
        onSnippetHighlight = Color(0xFFBBF0DB),
        storageModel = Color(0xFF9FD5BF),
        storageIndex = Color(0xFF5B8F79),
        storageNotes = Color(0xFF37564A),
        privacyAccent = Color(0xFF9FD5BF),
        privacyContainer = Color(0xFF1F4A3C),
        onPrivacyContainer = Color(0xFFBBF0DB),
        brandLogoContainer = Color(0xFF38614F),
        onBrandLogo = Color(0xFFFFFFFF),
    )
val LocalLocalMindColors = staticCompositionLocalOf { LightLocalMindColors }
