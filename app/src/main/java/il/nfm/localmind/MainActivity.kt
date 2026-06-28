package il.nfm.localmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import il.nfm.localmind.presentation.QuestionScreen
import il.nfm.localmind.presentation.QuestionViewModel
import il.nfm.localmind.ui.theme.LocalMindTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: QuestionViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalMindTheme {
                QuestionScreen(viewModel = viewModel)
            }
        }
    }
}
