package il.nfm.localmind.ml

class Tokenizer(
    modelPath: String,
) {
    init {
        System.loadLibrary("tokenizer_jni")
        check(nativeLoad(modelPath)) { "Failed to load tokenizer: $modelPath" }
    }

    fun encode(
        text: String,
        maxLen: Int = 512,
    ): Encoding {
        val flat = nativeEncode(text, maxLen)
        return Encoding(
            inputIds = flat.sliceArray(0 until maxLen),
            attentionMask = flat.sliceArray(maxLen until maxLen * 2),
        )
    }

    fun decode(ids: IntArray): String = nativeDecode(ids)

    private external fun nativeLoad(modelPath: String): Boolean

    private external fun nativeEncode(
        text: String,
        maxLen: Int,
    ): IntArray

    private external fun nativeDecode(ids: IntArray): String
}

data class Encoding(
    val inputIds: IntArray,
    val attentionMask: IntArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Encoding

        if (!inputIds.contentEquals(other.inputIds)) return false
        if (!attentionMask.contentEquals(other.attentionMask)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputIds.contentHashCode()
        result = 31 * result + attentionMask.contentHashCode()
        return result
    }
}
