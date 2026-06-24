package il.nfm.localmind.ml

fun buildPrompt(
    query: String,
    retrievedNotes: List<String>,
): String {
    val context = retrievedNotes.joinToString("\n\n")
    return """
        Answer the questions using ONLY the notes below.
        If the notes do not contain the answer, say "I don't know" in the language notes are.
        ALWAYS answer in the same language as the question.

        Notes:
        $context

        Question: $query
        """.trimIndent()
}
