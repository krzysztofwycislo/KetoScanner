package app.carbpeek.feature.about.ui

import app.carbpeek.feature.about.ui.event.AboutAction
import app.carbpeek.feature.about.ui.event.AboutEvent.AboutNavigation.NavigateBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when back is clicked then navigate back is emitted`() = runTest {
        val viewModel = AboutViewModel()

        viewModel.onAction(AboutAction.OnBackClicked)

        assertEquals(NavigateBack, viewModel.events.first())
    }
}
