package il.nfm.localmind.ml

data class EmbeddedProbeNote(
    val note: ProbeNote,
    val vector: FloatArray,
)

fun retrieveTopProbeNote(
    queryVector: FloatArray,
    index: List<EmbeddedProbeNote>,
): ProbeNote =
    checkNotNull(index.maxByOrNull { dot(queryVector, it.vector) }) {
        "Probe index is empty"
    }.note
