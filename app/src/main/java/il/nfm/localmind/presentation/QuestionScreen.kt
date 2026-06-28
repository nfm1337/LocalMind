package il.nfm.localmind.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun QuestionScreen(viewModel: QuestionViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    QuestionScreenContent(uiState = uiState.value, onQuery = viewModel::query)
}

@Composable
private fun QuestionScreenContent(
    uiState: QuestionUiState,
    onQuery: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
    ) {
        QuestionInput(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            onQuery = { onQuery(it) },
        )
        QuestionOutput(result = uiState.result, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun QuestionInput(
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val textFieldState = rememberTextFieldState()
        TextField(state = textFieldState)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onQuery(textFieldState.text.toString()) },
        ) {
            Text(text = "Ask LLM")
        }
    }
}

@Composable
private fun QuestionOutput(
    result: String,
    modifier: Modifier = Modifier,
    retrieved: List<String> = emptyList(),
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(text = retrieved.joinToString(", "))
        Text(text = result)
    }
}
