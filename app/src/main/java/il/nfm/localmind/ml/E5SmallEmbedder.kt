package il.nfm.localmind.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.LongBuffer

class E5SmallEmbedder(
    private val modelPath: String,
    private val tokenizerPath: String,
) : Embedder {
    private val env = OrtEnvironment.getEnvironment()
    private val mutex = Mutex()

    @Volatile
    private var loaded: Loaded? = null

    private val _state = MutableStateFlow<Embedder.State>(Embedder.State.Idle)
    override val state = _state.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun initialize() =
        mutex.withLock {
            if (loaded != null) return@withLock
            _state.value = Embedder.State.Initializing

            try {
                loaded =
                    withContext(Dispatchers.IO) {
                        Loaded(
                            session = env.createSession(modelPath, OrtSession.SessionOptions()),
                            tokenizer = Tokenizer(tokenizerPath),
                        )
                    }
                _state.value = Embedder.State.Ready
            } catch (e: CancellationException) {
                _state.value = Embedder.State.Idle
                throw e
            } catch (e: Exception) {
                _state.value = Embedder.State.Error(e)
                throw e
            }
        }

    override suspend fun embedQuery(text: String): FloatArray = embed("query: $text")

    override suspend fun embedPassage(text: String): FloatArray = embed("passage: $text")

    override suspend fun close() =
        mutex.withLock {
            runCatching { loaded?.session?.close() }
            loaded = null
            _state.value = Embedder.State.Idle
        }

    private suspend fun embed(text: String): FloatArray =
        withContext(Dispatchers.Default) {
            val loaded = checkNotNull(loaded) { "call initialize() first" }
            runInference(loaded.session, loaded.tokenizer.encode(text))
        }

    private fun runInference(
        session: OrtSession,
        encoding: Encoding,
    ): FloatArray {
        val shape = longArrayOf(1, encoding.inputIds.size.toLong())
        val inputs =
            mapOf(
                "input_ids" to encoding.inputIds.toLongTensor(shape),
                "attention_mask" to encoding.attentionMask.toLongTensor(shape),
                "token_type_ids" to IntArray(encoding.inputIds.size).toLongTensor(shape),
            )

        return try {
            session.run(inputs).use { output ->
                @Suppress("UNCHECKED_CAST")
                val hidden = (output[0].value as Array<Array<FloatArray>>)[0]
                l2Normalize(meanPool(hidden, encoding.attentionMask))
            }
        } finally {
            inputs.values.forEach { it.close() }
        }
    }

    private fun IntArray.toLongTensor(shape: LongArray): OnnxTensor =
        OnnxTensor.createTensor(env, LongBuffer.wrap(LongArray(size) { this[it].toLong() }), shape)

    private class Loaded(
        val session: OrtSession,
        val tokenizer: Tokenizer,
    )
}
