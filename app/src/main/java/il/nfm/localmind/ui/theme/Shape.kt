package il.nfm.localmind.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * LocalMind — Material 3 shape scale.
 * The standard M3 five-step scale, tuned to the radii used in the design,
 * plus named app-specific shapes for components whose corner radius is a
 * deliberate brand choice rather than a scale step.
 */
val LocalMindShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp), // category tags, small labels
        small = RoundedCornerShape(12.dp), // status strips, metric cards
        medium = RoundedCornerShape(16.dp), // standard cards, dialogs
        large = RoundedCornerShape(18.dp), // note cards, FAB, source cards
        extraLarge = RoundedCornerShape(28.dp), // bottom sheets, large containers
    )

/*
 * Component-specific shapes — reference these directly where the M3 scale
 * step doesn't capture intent (fully-rounded fields, pills, the app icon).
 */
object LocalMindCorner {
    val tag = RoundedCornerShape(8.dp)
    val chip = RoundedCornerShape(9.dp)
    val statusStrip = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(18.dp) // note & source cards
    val fab = RoundedCornerShape(18.dp) // extended FAB
    val appIcon = RoundedCornerShape(13.dp)
    val searchBar = RoundedCornerShape(27.dp) // full-height pill (h=54)
    val inputBar = RoundedCornerShape(26.dp) // ask input pill
    val pill = RoundedCornerShape(percent = 50)
    val sendButton = RoundedCornerShape(percent = 50) // 44dp circle
}
