package il.nfm.localmind.core.ext

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun Instant.toUpdatedAtLabel(): String =
    DateTimeFormatter
        .ofPattern("MMM d, HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(this.toJavaInstant())
