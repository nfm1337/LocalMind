package il.nfm.localmind.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * LocalMind — spacing, sizing & elevation tokens.
 * All spacing sits on a 4dp grid. Access via LocalSpacing.current in composables,
 * or reference the object constants directly for fixed component sizes.
 */

// -- Spacing scale (margins / padding / gaps) -------------------------------
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 4.dp, // tag padding, tight gaps
    val xs: Dp = 6.dp,
    val sm: Dp = 8.dp, // chip padding, small gaps
    val md: Dp = 12.dp, // card inner gap, list gap
    val lg: Dp = 16.dp, // screen horizontal margin (M3 default)
    val xl: Dp = 20.dp, // editor content margin
    val xxl: Dp = 24.dp, // section padding
    val section: Dp = 32.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

// -- Fixed component dimensions ---------------------------------------------
object Dimens {
    // Touch targets
    val minTouchTarget = 48.dp
    val iconButton = 44.dp
    val sendButton = 44.dp // circular ask/send

    // App structure
    val topAppBarHeight = 56.dp
    val brandAppBarHeight = 60.dp
    val bottomNavHeight = 80.dp
    val navPillWidth = 58.dp
    val navPillHeight = 30.dp

    // Search & input
    val searchBarHeight = 54.dp
    val inputBarMinHeight = 56.dp

    // Buttons
    val buttonHeight = 40.dp // save (in app bar)
    val ctaButtonHeight = 52.dp // create note CTA
    val extendedFabHeight = 56.dp

    // Icons
    val iconXs = 16.dp
    val iconSm = 18.dp
    val iconMd = 20.dp
    val iconLg = 22.dp
    val iconXl = 24.dp

    // Progress / meters
    val indexBarHeight = 6.dp
    val thinProgressHeight = 3.dp
    val storageBarHeight = 12.dp
    val scoreBarWidth = 52.dp
    val scoreBarHeight = 6.dp

    // Source rank badge
    val rankBadge = 22.dp

    // App icon
    val appIconLarge = 46.dp
    val appIconSmall = 30.dp

    val stripedBox = 112.dp
}

// -- Elevation --------------------------------------------------------------
object Elevations {
    val level0 = 0.dp
    val card = 0.dp // cards are tonal (surfaceContainer), no shadow — calm/flat
    val fab = 6.dp // extended FAB (design shadow ~8px blur)
    val bottomNav = 3.dp
    val bottomSheet = 8.dp
}
