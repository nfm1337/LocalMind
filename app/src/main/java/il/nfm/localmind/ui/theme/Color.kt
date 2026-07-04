package il.nfm.localmind.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * LocalMind — Material 3 color tokens
 * Derived 1:1 from the HTML design. Seed / key color: #38614F (calm green).
 * Roles map to Material 3 ColorScheme slots so they plug straight into
 * lightColorScheme() / darkColorScheme() in Theme.kt.
 */

// ---------------------------------------------------------------------------
// LIGHT
// ---------------------------------------------------------------------------
val md_light_primary = Color(0xFF38614F) // brand green, FAB, save button, accents
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFCFE8DC) // privacy chip, nav pill, question bubble bg
val md_light_onPrimaryContainer = Color(0xFF00201A) // text on chips/containers

// Secondary reuses the muted green family (category chips, subtle fills)
val md_light_secondary = Color(0xFF4F6357)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFD3E8DD) // "Product"/"Sales" category tags
val md_light_onSecondaryContainer = Color(0xFF00201A)

// Tertiary = the amber "indexing / attention" accent
val md_light_tertiary = Color(0xFF8A6D00)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFFFE08A)
val md_light_onTertiaryContainer = Color(0xFF2A2000)

val md_light_error = Color(0xFFBA1A1A)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFFFDAD6)
val md_light_onErrorContainer = Color(0xFF410002)

// Neutral surfaces (M3 tonal surfaces, warm-green tinted neutrals)
val md_light_background = Color(0xFFF7FAF6) // screen bg
val md_light_onBackground = Color(0xFF171D1A)
val md_light_surface = Color(0xFFF7FAF6)
val md_light_onSurface = Color(0xFF171D1A) // primary text
val md_light_surfaceVariant = Color(0xFFEBEFE8) // search field fill
val md_light_onSurfaceVariant = Color(0xFF3F4945) // secondary text / icons
val md_light_outline = Color(0xFF6C766F) // meta text, dividers-strong
val md_light_outlineVariant = Color(0xFFC2CCC6) // card borders, hairlines, device bezel

// M3 tonal surface containers (elevation without shadows)
val md_light_surfaceDim = Color(0xFFDBE0DC) // board / desk bg
val md_light_surfaceBright = Color(0xFFFBFEFB) // key caps, raised sheets
val md_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_light_surfaceContainerLow = Color(0xFFF1F5EF)
val md_light_surfaceContainer = Color(0xFFEEF3EC) // note cards, metric cards
val md_light_surfaceContainerHigh = Color(0xFFEBEFE8)
val md_light_surfaceContainerHighest = Color(0xFFE6ECE6)

val md_light_inverseSurface = Color(0xFF2B322E)
val md_light_inverseOnSurface = Color(0xFFEEF3EC)
val md_light_inversePrimary = Color(0xFF9FD5BF)
val md_light_scrim = Color(0xFF000000)
val md_light_surfaceTint = md_light_primary

// ---------------------------------------------------------------------------
// DARK
// ---------------------------------------------------------------------------
val md_dark_primary = Color(0xFF9FD5BF) // brand green (light-on-dark)
val md_dark_onPrimary = Color(0xFF00382A)
val md_dark_primaryContainer = Color(0xFF1F4A3C) // privacy chip, nav pill, question bubble
val md_dark_onPrimaryContainer = Color(0xFFBBF0DB)

val md_dark_secondary = Color(0xFFB6CCBF)
val md_dark_onSecondary = Color(0xFF21352C)
val md_dark_secondaryContainer = Color(0xFF26473B) // category tags
val md_dark_onSecondaryContainer = Color(0xFFBBF0DB)

val md_dark_tertiary = Color(0xFFE0C05A) // amber indexing accent
val md_dark_onTertiary = Color(0xFF3B2F00)
val md_dark_tertiaryContainer = Color(0xFF554500)
val md_dark_onTertiaryContainer = Color(0xFFFFE08A)

val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_dark_background = Color(0xFF0F1512)
val md_dark_onBackground = Color(0xFFDFE4DF)
val md_dark_surface = Color(0xFF0F1512)
val md_dark_onSurface = Color(0xFFDFE4DF)
val md_dark_surfaceVariant = Color(0xFF1B211E) // search field fill
val md_dark_onSurfaceVariant = Color(0xFFBEC9C2)
val md_dark_outline = Color(0xFF8A938D) // meta text
val md_dark_outlineVariant = Color(0xFF2B322E) // card borders, hairlines, bezel

val md_dark_surfaceDim = Color(0xFF0F1512)
val md_dark_surfaceBright = Color(0xFF353B37)
val md_dark_surfaceContainerLowest = Color(0xFF0A0F0D)
val md_dark_surfaceContainerLow = Color(0xFF151B18) // bottom nav bg
val md_dark_surfaceContainer = Color(0xFF181E1B) // note cards, metric cards
val md_dark_surfaceContainerHigh = Color(0xFF1B211E)
val md_dark_surfaceContainerHighest = Color(0xFF242B27)

val md_dark_inverseSurface = Color(0xFFDFE4DF)
val md_dark_inverseOnSurface = Color(0xFF2B322E)
val md_dark_inversePrimary = Color(0xFF38614F)
val md_dark_scrim = Color(0xFF000000)
val md_dark_surfaceTint = md_dark_primary
