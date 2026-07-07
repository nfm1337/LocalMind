package il.nfm.localmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import il.nfm.localmind.navigation.NavigationRoot
import il.nfm.localmind.ui.theme.LocalMindTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalMindTheme {
                NavigationRoot()
            }
        }
    }
}
