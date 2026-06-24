package il.nfm.localmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import il.nfm.localmind.presentation.QuestionScreen
import il.nfm.localmind.ui.theme.LocalMindTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = LocalContext.current.applicationContext as LocalMindApp
            LocalMindTheme {
                QuestionScreen(llmEngine = app.container.llmEngine, retriever = app.container.retriever)
            }
        }
    }
}
