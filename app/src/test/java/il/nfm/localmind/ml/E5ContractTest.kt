package il.nfm.localmind.ml

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class E5ContractTest {
    @Test
    fun e5PrefixesAreApplied() {
        assertEquals("query: When is my dentist appointment?", e5QueryText("When is my dentist appointment?"))
        assertEquals("passage: Dentist appointment is scheduled.", e5PassageText("Dentist appointment is scheduled."))
    }

    @Test
    fun embeddingsAreMeanPooledAndL2Normalized() {
        val hidden =
            arrayOf(
                floatArrayOf(1f, 2f),
                floatArrayOf(3f, 4f),
                floatArrayOf(100f, 100f),
            )
        val pooled = meanPool(hidden, intArrayOf(1, 1, 0))
        val normalized = l2Normalize(pooled)
        val norm = sqrt(normalized.sumOf { (it * it).toDouble() }).toFloat()

        assertArrayEquals(floatArrayOf(2f, 3f), pooled, FLOAT_DELTA)
        assertEquals(1f, norm, FLOAT_DELTA)
    }

    private companion object {
        const val FLOAT_DELTA = 0.0001f
    }
}
