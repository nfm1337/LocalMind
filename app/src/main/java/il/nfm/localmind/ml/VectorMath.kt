package il.nfm.localmind.ml

import kotlin.math.sqrt

/**
 * Dot product of two equal-length vectors.
 *
 * For **L2-normalized** (unit-length) inputs this equals their cosine similarity, which is why it
 * is used as the similarity score for the unit-length embeddings produced by [Embedder]. Passing
 * un-normalized vectors yields a magnitude-sensitive value that is **not** cosine — normalize first
 * with [l2Normalize].
 *
 * @param a first vector; must be L2-normalized for the result to equal cosine similarity.
 * @param b second vector; must have the same length as [a].
 * @return the dot product `Σ aᵢ·bᵢ`; falls in `[-1, 1]` when both inputs are unit-length.
 */
fun dot(
    a: FloatArray,
    b: FloatArray,
): Float {
    var dot = 0f
    for (i in a.indices) dot += a[i] * b[i]
    return dot
}

/**
 * Returns a unit-length copy of [v] (Euclidean / L2 norm).
 *
 * After normalization [dot] between two such vectors equals their cosine similarity. A zero vector
 * has no direction, so it is returned unchanged to avoid division by zero.
 *
 * @param v the vector to normalize; not mutated.
 * @return a new array scaled to length 1, or [v] itself when its norm is 0.
 */
fun l2Normalize(v: FloatArray): FloatArray {
    val norm = sqrt(v.sumOf { (it * it).toDouble() }).toFloat()
    return if (norm > 0f) FloatArray(v.size) { v[it] / norm } else v
}

/**
 * Masked mean pooling over a transformer's token hidden states.
 *
 * Averages the per-token vectors in [hidden], counting only positions whose [mask] value is 1, so
 * that padding tokens are excluded. This is the standard sentence-embedding pooling for encoder
 * models such as E5; the result is typically passed to [l2Normalize].
 *
 * @param hidden last-hidden-state, shape `[seqLen][hiddenDim]`; must be non-empty.
 * @param mask attention mask of length `seqLen`; `1` = real token, `0` = padding.
 * @return the pooled vector of size `hiddenDim`, or all-zeros if no position is unmasked.
 */
fun meanPool(
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
