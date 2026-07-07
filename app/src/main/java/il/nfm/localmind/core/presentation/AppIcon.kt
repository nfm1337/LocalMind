package il.nfm.localmind.core.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import il.nfm.localmind.R
import il.nfm.localmind.ui.theme.Dimens
import il.nfm.localmind.ui.theme.LocalLocalMindColors

@Composable
fun AppIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.brain),
            contentDescription = null,
            tint = LocalLocalMindColors.current.onBrandLogo,
            modifier = Modifier.size(Dimens.iconMd),
        )
    }
}
