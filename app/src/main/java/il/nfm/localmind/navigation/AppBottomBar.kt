package il.nfm.localmind.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import il.nfm.localmind.R
import il.nfm.localmind.ui.theme.LocalMindTheme

@Composable
fun AppBottomBar(
    currentRoute: Route?,
    onRouteClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        NavigationBarItem(
            selected = currentRoute == Route.NotesList,
            onClick = { onRouteClick(Route.NotesList) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "Notes",
                )
            },
            label = {
                Text(stringResource(R.string.notes_list_navigation_label))
            },
            colors = barItemColors(),
        )

        NavigationBarItem(
            selected = currentRoute == Route.Ask,
            onClick = { onRouteClick(Route.Ask) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Forum,
                    contentDescription = "Ask AI",
                )
            },
            label = {
                Text(stringResource(R.string.ask_ai_navigation_label))
            },
            colors = barItemColors(),
        )

        NavigationBarItem(
            selected = currentRoute == Route.Diagnostics,
            onClick = { onRouteClick(Route.Diagnostics) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.monitoring),
                    contentDescription = "Analytics",
                )
            },
            label = { Text(stringResource(R.string.diagnostics_navigation_label)) },
            colors = barItemColors(),
        )
    }
}

@Composable
private fun barItemColors(): NavigationBarItemColors =
    NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
    )

@Preview
@Composable
private fun AppBottomBarLightPreview() {
    LocalMindTheme(darkTheme = false) {
        AppBottomBar(
            currentRoute = Route.NotesList,
            onRouteClick = {},
        )
    }
}

@Preview
@Composable
private fun AppBottomBarDarkPreview() {
    LocalMindTheme(darkTheme = true) {
        AppBottomBar(
            currentRoute = Route.NotesList,
            onRouteClick = {},
        )
    }
}
