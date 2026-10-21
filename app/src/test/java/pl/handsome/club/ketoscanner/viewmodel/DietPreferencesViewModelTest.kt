package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.preferences.DietPreferencesViewModel


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DietPreferencesViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var dietPreferencesRepository: DietPreferencesRepository

    @Mock
    private lateinit var maxCarbsObserver: Observer<Int>

    private lateinit var viewModel: DietPreferencesViewModel


    @Before
    fun setUp() {
        `when`(dietPreferencesRepository.observeDietPreferences()).thenReturn(flowOf(DietPreferences(40)))
        viewModel = DietPreferencesViewModel(dietPreferencesRepository)
    }


    @Test
    fun `when observing max carbs amount then saved value is emitted`() {
        viewModel.getMaxCarbsAmount().observeForever(maxCarbsObserver)

        verify(maxCarbsObserver).onChanged(40)
    }

    @Test
    fun `when valid amount is saved then repository is updated and input is valid`() = coroutinesTestRule.runTest {
        viewModel.saveMaxCarbsAmount("30")

        verify(dietPreferencesRepository).setMaxDailyCarbsAmount(30)
        assertThat(viewModel.getInvalidInput().value).isFalse()
    }

    @Test
    fun `when amount has surrounding whitespace then it is trimmed and saved`() = coroutinesTestRule.runTest {
        viewModel.saveMaxCarbsAmount(" 30 ")

        verify(dietPreferencesRepository).setMaxDailyCarbsAmount(30)
        assertThat(viewModel.getInvalidInput().value).isFalse()
    }

    @Test
    fun `when amount is invalid then input is marked invalid and nothing is saved`() = coroutinesTestRule.runTest {
        listOf("", "abc", "0", "1000").forEach { input ->
            viewModel.saveMaxCarbsAmount(input)

            assertThat(viewModel.getInvalidInput().value).isTrue()
        }

        verify(dietPreferencesRepository, never()).setMaxDailyCarbsAmount(anyInt())
    }

}
