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
import java.util.Locale

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
        QuestionOutput(
            result = uiState.result,
            retrieved = uiState.retrieved,
            errorMessage = uiState.errorMessage,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
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
    retrieved: List<RetrievedNoteUi> = emptyList(),
    errorMessage: String? = null,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(text = retrieved.formatDebugText())
        errorMessage?.let { Text(text = it) }
        Text(text = result)
    }
}

private fun Float.formatScore(): String = "%.3f".format(Locale.US, this)

private fun RetrievedNoteUi.formatDebugLine(): String = "$id ${score.formatScore()} $title"

private fun List<RetrievedNoteUi>.formatDebugText(): String =
    if (isEmpty()) {
        "Retrieved: empty"
    } else {
        joinToString(
            separator = "\n",
            prefix = "Retrieved:\n",
            transform = RetrievedNoteUi::formatDebugLine,
        )
    }
