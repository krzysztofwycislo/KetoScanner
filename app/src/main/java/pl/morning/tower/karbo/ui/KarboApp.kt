package pl.morning.tower.karbo.ui

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
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import pl.morning.tower.karbo.core.designsystem.component.LocalBottomBarOverlap
import pl.morning.tower.karbo.core.designsystem.component.SnackbarHost
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.component.rememberBottomBarOverlap
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.navigation.AppNavHost
import pl.morning.tower.karbo.ui.component.AppBottomBar
import pl.morning.tower.karbo.ui.component.AppEventsEffect
import pl.morning.tower.karbo.ui.component.AppNavigationEffect
import pl.morning.tower.karbo.ui.component.AppStartPlaceholder
import pl.morning.tower.karbo.ui.component.DestinationEffect

@Composable
internal fun KarboApp() {
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
