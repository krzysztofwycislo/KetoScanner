package app.carbpeek.feature.about.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.TopBar
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.about.ui.event.AboutAction
import app.carbpeek.feature.about.ui.event.AboutAction.OnBackClicked
import app.carbpeek.feature.about.ui.event.AboutEvent.AboutNavigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AboutScreen(onNavigation: (AboutNavigation) -> Unit) {
    val viewModel: AboutViewModel = koinViewModel()

    ObserveEvents(viewModel.events) { event ->
        if (event is AboutNavigation) onNavigation(event)
    }

    ScreenContent(viewModel::onAction)
}

@Composable
private fun ScreenContent(onAction: (AboutAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Ui.TopBar(
                title = stringResource(R.string.about_title),
                onBackClick = { onAction(OnBackClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            Text(
                text = stringResource(R.string.about_description),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text.default,
            )
            Text(
                text = stringResource(R.string.about_data_source),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text.default,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutPreview() {
    AppTheme {
        ScreenContent {}
    }
}
