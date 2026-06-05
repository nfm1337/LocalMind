package il.nfm.localmind.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import java.nio.LongBuffer
import kotlin.math.sqrt

class EmbeddingModel(
    modelPath: String,
    private val tokenizer: Tokenizer,
) {
    private val env = OrtEnvironment.getEnvironment()
    private val session = env.createSession(modelPath, OrtSession.SessionOptions())

    init {
        Log.d("EmbeddingModel", "inputs:  ${session.inputNames.toList()}")
        Log.d("EmbeddingModel", "outputs: ${session.outputNames.toList()}")
    }

    fun embedQuery(text: String): FloatArray = embed(tokenizer.encode("query: $text"))

    fun embedPassage(text: String): FloatArray = embed(tokenizer.encode("passage: $text"))

    private fun embed(encoding: Encoding): FloatArray {
        val seqLen = encoding.inputIds.size.toLong()
        val shape = longArrayOf(1, seqLen)

        val inputIdsBuf = LongBuffer.wrap(encoding.inputIds.map { it.toLong() }.toLongArray())
        val maskBuf = LongBuffer.wrap(encoding.attentionMask.map { it.toLong() }.toLongArray())

        val tokenTypesBuf = LongBuffer.wrap(LongArray(encoding.inputIds.size))

        val inputs =
            mapOf(
                "input_ids" to OnnxTensor.createTensor(env, inputIdsBuf, shape),
                "attention_mask" to OnnxTensor.createTensor(env, maskBuf, shape),
                "token_type_ids" to OnnxTensor.createTensor(env, tokenTypesBuf, shape),
            )

        val output = session.run(inputs)

        @Suppress("UNCHECKED_CAST")
        val hidden = (output[0].value as Array<Array<FloatArray>>)[0]

        val result = l2Normalize(meanPool(hidden, encoding.attentionMask))

        output.close()
        inputs.values.forEach { it.close() }

        return result
    }

    private fun meanPool(
        hidden: Array<FloatArray>,
        mask: IntArray,
    ): FloatArray {
        val dim = hidden[0].size
        val result = FloatArray(dim)
        var count = 0f
        for (i in hidden.indices) {
            if (mask[i] == 1) {
                for (j in 0 until dim) result[j] += hidden[i][j]
                count++
            }
        }
        if (count > 0) for (j in 0 until dim) result[j] /= count
        return result
    }

    private fun l2Normalize(v: FloatArray): FloatArray {
        val norm = sqrt(v.sumOf { (it * it).toDouble() }).toFloat()
        return if (norm > 0f) FloatArray(v.size) { v[it] / norm } else v
    }

    fun cosine(
        a: FloatArray,
        b: FloatArray,
    ): Float {
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }

    fun close() = session.close()
}
