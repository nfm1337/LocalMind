package il.nfm.localmind.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class ProbeRetrievalTest {
    @Test
    fun fixedNoteRetrievalReturnsExpectedProbeNote() {
        val project = ProbeNotes.notes.first { it.id == "note_project" }
        val dentist = ProbeNotes.notes.first { it.id == "note_dentist" }
        val birthday = ProbeNotes.notes.first { it.id == "note_birthday" }
        val index =
            listOf(
                EmbeddedProbeNote(project, floatArrayOf(1f, 0f, 0f)),
                EmbeddedProbeNote(dentist, floatArrayOf(0f, 1f, 0f)),
                EmbeddedProbeNote(birthday, floatArrayOf(0f, 0f, 1f)),
            )

        val result = retrieveTopProbeNote(floatArrayOf(0.9f, 0.1f, 0f), index)

        assertEquals("note_project", result.id)
    }

    @Test
    fun fixedDatasetMatchesSpec() {
        assertEquals(10, ProbeNotes.notes.size)
        assertEquals(
            "note_project",
            ProbeNotes.expectedNoteIdFor("What do I need to prove for LocalMind on the phone?"),
        )
        assertEquals("note_dentist", ProbeNotes.expectedNoteIdFor("When is my dentist appointment?"))
        assertEquals("note_birthday", ProbeNotes.expectedNoteIdFor("What should I buy for Maya?"))
    }
}
