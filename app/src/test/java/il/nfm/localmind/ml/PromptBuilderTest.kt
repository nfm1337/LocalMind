package il.nfm.localmind.ml

import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {
    @Test
    fun promptIncludesQuestionAndRetrievedNotes() {
        val prompt =
            buildPrompt(
                query = "When is my dentist appointment?",
                retrievedNotes =
                    listOf(
                        "Dentist appointment is scheduled for next Monday at 09:30.",
                        "Gym session focused on squats, rowing, and stretching.",
                    ),
            )

        assertTrue(prompt.contains("Question: When is my dentist appointment?"))
        assertTrue(prompt.contains("Dentist appointment is scheduled for next Monday at 09:30."))
        assertTrue(prompt.contains("Gym session focused on squats, rowing, and stretching."))
        assertTrue(prompt.contains("Answer the questions using ONLY the notes below."))
    }
}
