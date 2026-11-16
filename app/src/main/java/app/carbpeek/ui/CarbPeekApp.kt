package app.carbpeek.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import app.carbpeek.core.designsystem.component.LocalBottomBarOverlap
import app.carbpeek.core.designsystem.component.SnackbarHost
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.rememberBottomBarOverlap
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.navigation.AppNavHost
import app.carbpeek.ui.component.AppBottomBar
import app.carbpeek.ui.component.AppEventsEffect
import app.carbpeek.ui.component.AppNavigationEffect
import app.carbpeek.ui.component.AppStartPlaceholder
import app.carbpeek.ui.component.DestinationEffect
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CarbPeekApp() {
    val viewModel: AppViewModel = koinViewModel()
    val koin = getKoin()
    setSingletonImageLoaderFactory { koin.get<ImageLoader>() }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    AppEventsEffect(events = viewModel.events, snackbarHostState = snackbarHostState)
    AppNavigationEffect(navigationEvents = viewModel.navigationEvents, navController = navController)
    DestinationEffect(navController = navController, onAction = viewModel::onAction)

    val bottomBar by viewModel.bottomBar.collectAsStateWithLifecycle()
    val start by viewModel.start.collectAsStateWithLifecycle()
    val appStart = start
    if (appStart == null) {
        AppStartPlaceholder()
        return
    }

    val bottomBarOverlap = rememberBottomBarOverlap(visible = bottomBar.isVisible)

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = AppTheme.colors.background.default,
        contentWindowInsets = if (bottomBar.isVisible) {
            WindowInsets(0)
        } else {
            WindowInsets.navigationBars
        },
        snackbarHost = {
            Ui.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = bottomBarOverlap),
            )
        },
        bottomBar = {
            AppBottomBar(
                state = bottomBar,
                onAction = viewModel::onAction,
            )
        },
    ) { innerPadding ->
        CompositionLocalProvider(LocalBottomBarOverlap provides bottomBarOverlap) {
            AppNavHost(
                navController = navController,
                start = appStart,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        }
    }
}
