package il.nfm.localmind.ml

interface ModelPathProvider {
    val llmModelPath: String
    val embeddingModelPath: String
    val embeddingTokenizerPath: String
}
