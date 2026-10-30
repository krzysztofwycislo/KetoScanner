package pl.handsome.club.ketoscanner.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
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
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.rememberUiTextResolver
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.navigation.AppNavHost
import pl.handsome.club.ketoscanner.navigation.TopLevelTab
import pl.handsome.club.ketoscanner.navigation.navigateToTab
import pl.handsome.club.ketoscanner.navigation.topLevelTab
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked

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
                snackbarHostState.showSnackbar(resolver.resolve(event.text), duration = SnackbarDuration.Long)
            }

            AppEvent.HideKeyboard -> hideKeyboard()
        }
    }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentTab = currentEntry?.destination?.topLevelTab()
    LaunchedEffect(currentEntry?.destination?.id) { hideKeyboard() }
    LaunchedEffect(currentTab) { viewModel.onAction(OnDestinationChanged(currentTab)) }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = AppTheme.colors.background.default,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(snackbarHostState, Modifier.padding(bottom = AppTheme.sizes.scanButtonLift))
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabClick = { tab ->
                    val reselected = tab == selectedTab
                    viewModel.onAction(OnTabClicked(tab))
                    if (reselected) navController.popBackStack(tab.route, inclusive = false)
                    else navController.navigateToTab(tab)
                },
            )
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun AppBottomBar(selectedTab: TopLevelTab, onTabClick: (TopLevelTab) -> Unit) {
    val items = TopLevelTab.entries
        .map { BottomBarItem(stringResource(it.label), it.icon, it.selectedIcon, it.isProminent) }
        .toImmutableList()

    Ui.BottomBar(
        items = items,
        selectedIndex = selectedTab.ordinal,
        onItemClick = { index -> onTabClick(TopLevelTab.entries[index]) },
    )
}
