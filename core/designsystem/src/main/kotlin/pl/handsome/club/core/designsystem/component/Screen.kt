package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

val LocalBottomBarOverlap = compositionLocalOf { 0.dp }

@Composable
fun Ui.Screen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    snackbarHostState: SnackbarHostState? = null,
    containerColor: Color = AppTheme.colors.background.default,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        topBar = {
            Box(
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
            ) {
                topBar()
            }
        },
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(it)
            }
        },
        containerColor = containerColor,
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            .add(WindowInsets(bottom = LocalBottomBarOverlap.current)),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    AppTheme {
        Ui.Screen(
            topBar = {
                Text(
                    text = UiText.Res(R.string.designsystem_preview_title).resolve(),
                    style = AppTheme.typography.title,
                    modifier = Modifier.padding(AppTheme.spacing.m),
                )
            },
        ) { innerPadding ->
            Text(
                text = UiText.Res(R.string.designsystem_preview_body).resolve(),
                style = AppTheme.typography.body,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = AppTheme.spacing.m),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenWithBottomBarPaddingPreview() {
    AppTheme {
        Ui.Screen(contentPadding = PaddingValues(bottom = 56.dp)) { innerPadding ->
            Text(
                text = UiText.Res(R.string.designsystem_preview_body).resolve(),
                style = AppTheme.typography.body,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(AppTheme.spacing.m),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenWithSnackbarPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = UiText.Res(R.string.designsystem_preview_snackbar).resolve()
    LaunchedEffect(snackbarHostState, message) { snackbarHostState.showSnackbar(message) }
    AppTheme {
        Ui.Screen(snackbarHostState = snackbarHostState) { innerPadding ->
            Text(
                text = UiText.Res(R.string.designsystem_preview_body).resolve(),
                style = AppTheme.typography.body,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(AppTheme.spacing.m),
            )
        }
    }
}
