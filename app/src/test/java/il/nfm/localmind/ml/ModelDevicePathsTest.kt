package il.nfm.localmind.ml

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ModelDevicePathsTest {
    @Test
    fun debugModelPathsMatchManifestPaths() {
        val manifest = Json.parseToJsonElement(modelManifest.readText()).jsonObject
        val llm = manifest.getValue("llm").jsonObject
        val embedder = manifest.getValue("embedder").jsonObject
        val tokenizer = embedder.getValue("tokenizer").jsonObject

        assertEquals(
            "models/llm/gemma-4-E2B-it.litertlm",
            llm.getValue("localPath").jsonPrimitive.content,
        )
        assertEquals(
            llm.getValue("devicePath").jsonPrimitive.content,
            ModelDevicePaths.LLM,
        )
        assertEquals(
            "models/embedding/multilingual-e5-small.onnx",
            embedder.getValue("localPath").jsonPrimitive.content,
        )
        assertEquals(
            embedder.getValue("devicePath").jsonPrimitive.content,
            ModelDevicePaths.EMBEDDER,
        )
        assertEquals(
            "models/embedding/tokenizer.json",
            tokenizer.getValue("localPath").jsonPrimitive.content,
        )
        assertEquals(
            tokenizer.getValue("devicePath").jsonPrimitive.content,
            ModelDevicePaths.EMBEDDER_TOKENIZER,
        )
    }

    private val modelManifest: File
        get() {
            val candidates =
                listOf(
                    File("models/models.manifest.json"),
                    File("../models/models.manifest.json"),
                )
            return candidates.first { it.isFile }
        }
}
