package il.nfm.localmind.ml

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LiteRtLLMEngine(
    private val modelPath: String,
    private val samplerConfig: SamplerConfig = SamplerConfig(topK = 10, topP = 0.95, temperature = 0.8),
) : LLMEngine {
    private val mutex = Mutex()
    private val _state = MutableStateFlow<LLMEngine.State>(LLMEngine.State.Idle)
    override val state = _state.asStateFlow()

    @Volatile
    private var engine: Engine? = null

    @Suppress("TooGenericExceptionCaught")
    override suspend fun initialize() =
        mutex.withLock {
            if (engine != null) return@withLock
            _state.value = LLMEngine.State.Initializing
            try {
                engine = withContext(Dispatchers.Default) { createAndInit() }
                _state.value = LLMEngine.State.Ready
            } catch (e: CancellationException) {
                _state.value = LLMEngine.State.Idle
                throw e
            } catch (e: Exception) {
                _state.value = LLMEngine.State.Error(e)
                throw e
            }
        }

    override fun askOnce(prompt: String): Flow<String> =
        flow {
            mutex.withLock {
                val engine = checkNotNull(engine) { "call initialize() first " }
                val conversation = engine.createConversation(ConversationConfig(samplerConfig = samplerConfig))
                try {
                    conversation.sendMessageAsync(prompt).collect { emit(it.toString()) }
                } finally {
                    runCatching { conversation.close() }
                }
            }
        }.flowOn(Dispatchers.Default)

    override suspend fun close() =
        mutex.withLock {
            runCatching { engine?.close() }
            engine = null
            _state.value = LLMEngine.State.Idle
        }

    private fun createAndInit(): Engine {
        val backends = listOf(Backend.GPU(), Backend.CPU())
        var last: Throwable? = null

        for (backend in backends) {
            val candidate = Engine(EngineConfig(modelPath, backend))
            runCatching { candidate.initialize() }
                .onSuccess {
                    return candidate
                }.onFailure { e -> last = e }
        }

        throw checkNotNull(last)
    }
}
