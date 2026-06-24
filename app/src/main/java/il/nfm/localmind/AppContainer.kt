package il.nfm.localmind

import android.content.Context
import android.util.Log
import il.nfm.localmind.data.model.loadNotes
import il.nfm.localmind.ml.E5SmallEmbedder
import il.nfm.localmind.ml.Embedder
import il.nfm.localmind.ml.LLMEngine
import il.nfm.localmind.ml.LiteRtLLMEngine
import il.nfm.localmind.ml.RetrieverImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class AppContainer(
    context: Context,
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val assets = context.assets

    // Large model weights are adb-pushed here (no APK rebuild to swap quants).
    private val modelDir =
        requireNotNull(context.getExternalFilesDir(null)) { "external storage unavailable" }

    // tokenizer.json ships in the APK (assets); native code needs a real path, so copy it once.
    private val tokenizerPath = context.copyAssetToFiles("tokenizer.json").absolutePath

    val llmEngine: LLMEngine by lazy {
        LiteRtLLMEngine(File(modelDir, "gemma-4-E2B-it.litertlm").absolutePath)
    }
    val embedder: Embedder by lazy {
        E5SmallEmbedder(
            modelPath = File(modelDir, "multilingual-e5-small.onnx").absolutePath,
            tokenizerPath = tokenizerPath,
        )
    }
    val retriever = RetrieverImpl(embedder)

    init {
        appScope.launch {
            retriever.state.collect { Log.d("Retriever", "state: $it") }
        }
        appScope.launch {
            retriever.build(assets.loadNotes())
        }
    }
}

private fun Context.copyAssetToFiles(name: String): File {
    val out = File(filesDir, name)
    if (out.exists() && out.length() > 0) return out
    assets.open(name).use { input ->
        out.outputStream().use { input.copyTo(it) }
    }
    return out
}
