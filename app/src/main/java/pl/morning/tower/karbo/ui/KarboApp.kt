package pl.handsome.club.ketoscanner.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.designsystem.component.BottomBar
import pl.handsome.club.core.designsystem.component.BottomBarItem
import pl.handsome.club.core.designsystem.component.LocalBottomBarOverlap
import pl.handsome.club.core.designsystem.component.SnackbarHost
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.showAppMessage
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.rememberUiTextResolver
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.navigation.AppNavHost
import pl.handsome.club.ketoscanner.navigation.BottomNavSection
import pl.handsome.club.ketoscanner.navigation.navigateToTab
import pl.handsome.club.ketoscanner.navigation.toBottomNavSection
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked
import pl.handsome.club.ketoscanner.ui.AppNavigation.NavigateToTab
import pl.handsome.club.ketoscanner.ui.AppNavigation.PopToTabRoot

@Composable
internal fun KetoScannerApp() {
    val viewModel: AppViewModel = koinViewModel()
    val koin = getKoin()
    setSingletonImageLoaderFactory { koin.get<ImageLoader>() }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resolver = rememberUiTextResolver()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val hideKeyboard = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is AppEvent.ShowMessage -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showAppMessage(resolver.resolve(event.text), event.tone)
            }

            AppEvent.HideKeyboard -> hideKeyboard()
        }
    }

    ObserveEvents(viewModel.navigationEvents) { navigation ->
        when (navigation) {
            is NavigateToTab -> navController.navigateToTab(navigation.tab)
            is PopToTabRoot -> navController.popBackStack(navigation.tab.route, inclusive = false)
        }
    }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentTab = currentEntry?.destination?.toBottomNavSection()

    LaunchedEffect(currentEntry?.destination?.id) { hideKeyboard() }
    LaunchedEffect(currentTab) { viewModel.onAction(OnDestinationChanged(currentTab)) }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = AppTheme.colors.background.default,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            Ui.SnackbarHost(snackbarHostState, Modifier.padding(bottom = AppTheme.sizes.scanButtonLift))
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onAction = viewModel::onAction,
            )
        },
    ) { innerPadding ->
        CompositionLocalProvider(LocalBottomBarOverlap provides AppTheme.sizes.scanButtonLift) {
            AppNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        }
    }
}

@Composable
private fun AppBottomBar(selectedTab: BottomNavSection, onAction: (AppAction) -> Unit) {
    val items = BottomNavSection.entries
        .map { BottomBarItem(stringResource(it.label), it.icon, it.selectedIcon, it.isProminent) }
        .toImmutableList()

    Ui.BottomBar(
        items = items,
        selectedIndex = selectedTab.ordinal,
        onItemClick = { index -> onAction(OnTabClicked(BottomNavSection.entries[index])) },
    )
}
