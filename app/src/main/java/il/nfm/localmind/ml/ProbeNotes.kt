package il.nfm.localmind.ml

data class ProbeNote(
    val id: String,
    val content: String,
)

data class ProbeCase(
    val query: String,
    val expectedNoteId: String,
)

object ProbeNotes {
    val notes =
        listOf(
            ProbeNote(
                id = "note_groceries",
                content = "Bought milk, eggs, tomatoes, and coffee from the neighborhood grocery store.",
            ),
            ProbeNote(
                id = "note_tax",
                content = "Paid quarterly taxes on Tuesday and saved the confirmation number in email.",
            ),
            ProbeNote(
                id = "note_flight",
                content = "Flight to Berlin leaves Friday morning from terminal 3.",
            ),
            ProbeNote(
                id = "note_book",
                content = "Recommended book: The Design of Everyday Things, especially the chapter about affordances.",
            ),
            ProbeNote(
                id = "note_gym",
                content = "Gym session focused on squats, rowing, and stretching.",
            ),
            ProbeNote(
                id = "note_dentist",
                content = "Dentist appointment is scheduled for next Monday at 09:30.",
            ),
            ProbeNote(
                id = "note_project",
                content = "LocalMind needs the LLM and embedding model to fit in memory on a mid-range phone.",
            ),
            ProbeNote(
                id = "note_recipe",
                content = "Pasta sauce recipe uses tomatoes, garlic, olive oil, basil, and parmesan.",
            ),
            ProbeNote(
                id = "note_birthday",
                content = "Maya's birthday gift idea: noise-canceling headphones.",
            ),
            ProbeNote(
                id = "note_wifi",
                content = "Home Wi-Fi router admin page is at 192.168.1.1.",
            ),
        )

    val cases =
        listOf(
            ProbeCase(
                query = "What do I need to prove for LocalMind on the phone?",
                expectedNoteId = "note_project",
            ),
            ProbeCase(
                query = "When is my dentist appointment?",
                expectedNoteId = "note_dentist",
            ),
            ProbeCase(
                query = "What should I buy for Maya?",
                expectedNoteId = "note_birthday",
            ),
        )

    fun expectedNoteIdFor(query: String): String? = cases.firstOrNull { it.query == query }?.expectedNoteId
}
