package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule


@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class AnalysisHistoryViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AnalysisHistoryViewModel

    @Mock
    private lateinit var analysisHistoryRepository: AnalysisHistoryRepository

    @Mock
    private lateinit var observer: Observer<List<ProductAnalysisHistoryEntry>>


    @Before
    fun init() {
        viewModel = AnalysisHistoryViewModel(analysisHistoryRepository)
        viewModel.getLastUniqueEntries().observeForever(observer)
    }

    @Test
    fun `when we want to get last products analysis then we should be able to observe them`() =
        coroutinesTestRule.runBlockingTest {
            val historyEntriesToReturn = listOf<ProductAnalysisHistoryEntry>()
            val anyAmountToReturn = anyInt()
            `when`(analysisHistoryRepository.getLastUniqueEntries(anyAmountToReturn))
                .thenReturn(historyEntriesToReturn)

            viewModel.loadLastUniqueEntries(anyAmountToReturn)

            observer.onChanged(historyEntriesToReturn)
        }

}