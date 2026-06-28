package il.nfm.localmind.ml

import android.content.Context
import java.io.File

class DebugModelPathProvider(
    context: Context,
) : ModelPathProvider {
    private val modelRoot = context.filesDir

    override val llmModelPath: String =
        modelFile(ModelDevicePaths.LLM).absolutePath

    override val embeddingModelPath: String =
        modelFile(ModelDevicePaths.EMBEDDER).absolutePath

    override val embeddingTokenizerPath: String =
        modelFile(ModelDevicePaths.EMBEDDER_TOKENIZER).absolutePath

    private fun modelFile(devicePath: String): File = File(modelRoot, devicePath)
}

object ModelDevicePaths {
    const val LLM = "models/llm/gemma-4-E2B-it.litertlm"
    const val EMBEDDER = "models/embedding/multilingual-e5-small.onnx"
    const val EMBEDDER_TOKENIZER = "models/embedding/tokenizer.json"
}
