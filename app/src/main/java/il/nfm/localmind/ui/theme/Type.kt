package il.nfm.localmind.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * LocalMind — typography.
 * Primary UI face: Roboto (Material 3 default).
 * Monospace face: Roboto Mono — used for all technical / numeric content
 * (latency, scores, dimensions, dates, model IDs, status bar clock).
 *
 * If you rely on the platform Roboto you can drop the Font(...) references and
 * use FontFamily.Default / FontFamily.Monospace. The R.font.* entries assume
 * you've added the Roboto & Roboto Mono files to res/font.
 */
val Roboto = FontFamily.Default

val RobotoMono = FontFamily.Monospace

// Standard Material 3 type scale, sized to the design.
val LocalMindTypography =
    Typography(
        // Display — reserved for large numeric metrics if needed
        displaySmall =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 30.sp,
                lineHeight = 36.sp,
            ),
        // Headlines
        headlineMedium =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
            ),
        // board / brand title
        headlineSmall =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 25.sp,
                lineHeight = 30.sp,
            ),
        // note editor title
        // Titles
        titleLarge =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
        // top app bar title
        titleMedium =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        // note card title, question text
        titleSmall =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        // card section headers
        // Body
        bodyLarge =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 24.sp,
            ),
        // editor content, answer text, input placeholder
        bodyMedium =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        // card snippet / secondary body
        bodySmall =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
        // privacy chip text, captions
        // Labels
        labelLarge =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        // buttons
        labelMedium =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
        // nav labels, category tags, chips
        labelSmall =
            TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            ),
        // tiny tags / score labels
    )

/*
 * Monospace styles — not part of the Material Typography object because M3
 * has no "mono" slot. Reference these directly for technical readouts.
 */
object LocalMindMono {
    val metricLarge =
        TextStyle( // big diagnostics numbers: 42 ms, 1.9 s
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 26.sp,
        )
    val code =
        TextStyle( // model IDs, dims, backends
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    val meta =
        TextStyle( // dates, chunk counts, "Jun 28 · 4 chunks"
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    val score =
        TextStyle( // relevance scores 0.92
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    val statusBar =
        TextStyle( // clock
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 16.sp,
        )
}
