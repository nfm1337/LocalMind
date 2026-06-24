package il.nfm.localmind.ml

import android.util.Log
import il.nfm.localmind.data.model.Note
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RetrieverImpl(
    private val embedder: Embedder,
) : Retriever {
    private val _state = MutableStateFlow<Retriever.State>(Retriever.State.Idle)
    val state = _state.asStateFlow()

    @Volatile
    private var index: List<EmbeddedNote> = emptyList()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun build(notes: List<Note>) {
        _state.value = Retriever.State.Indexing
        try {
            embedder.initialize()
            index = notes.map { EmbeddedNote(it, embedder.embedPassage(it.content)) }
            _state.value = Retriever.State.Ready
        } catch (e: CancellationException) {
            _state.value = Retriever.State.Idle
            throw e
        } catch (e: Exception) {
            _state.value = Retriever.State.Error(e)
            throw e
        }
    }

    override suspend fun topK(
        query: String,
        k: Int,
    ): List<Note> {
        val q = embedder.embedQuery(query)
        index
            .map { it.note.title to dot(q, it.vector) }
            .sortedByDescending { it.second }
            .forEach { Log.d("Retriever", "%.3f  %s".format(it.second, it.first)) }
        return index
            .sortedByDescending { dot(q, it.vector) }
            .take(k)
            .map { it.note }
    }

    private class EmbeddedNote(
        val note: Note,
        val vector: FloatArray,
    )
}
