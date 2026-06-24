package il.nfm.localmind.data.model

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
)

suspend fun AssetManager.loadNotes(name: String = "dataset.json"): List<Note> =
    withContext(Dispatchers.IO) {
        val text = open(name).bufferedReader().use { it.readText() }
        Json.decodeFromString(text)
    }
