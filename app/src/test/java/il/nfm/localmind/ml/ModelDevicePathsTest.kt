package il.nfm.localmind.ml

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ModelDevicePathsTest {
    @Test
    fun debugModelPathsMatchManifestDevicePaths() {
        val manifest = Json.parseToJsonElement(modelManifest.readText()).jsonObject

        assertEquals(
            manifest
                .getValue("llm")
                .jsonObject
                .getValue("devicePath")
                .jsonPrimitive
                .content,
            ModelDevicePaths.LLM,
        )
        assertEquals(
            manifest
                .getValue("embedder")
                .jsonObject
                .getValue("devicePath")
                .jsonPrimitive
                .content,
            ModelDevicePaths.EMBEDDER,
        )
        assertEquals(
            manifest
                .getValue("embedder")
                .jsonObject
                .getValue("tokenizer")
                .jsonObject
                .getValue("devicePath")
                .jsonPrimitive
                .content,
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
