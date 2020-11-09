package pl.handsome.club.ketoscanner.viewmodel

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.repository.AnalysisHistoryRepository


@RunWith(MockitoJUnitRunner::class)
class AnalysisHistoryViewModelTest {

    private lateinit var viewModel: AnalysisHistoryViewModel

    @Mock
    private lateinit var analysisHistoryRepository: AnalysisHistoryRepository


    @Before
    fun init() {
        viewModel = AnalysisHistoryViewModel(analysisHistoryRepository)
    }

    @Test
    fun `when we want to get last products analysis then we should get them`() {
        val analysisResultsToReturn = listOf<ProductAnalysisResult>()
        val anyAmountToReturn = anyInt()
        `when`(analysisHistoryRepository.getLastEntries(anyAmountToReturn)).thenReturn(analysisResultsToReturn)

        val lastEntries = viewModel.getLastEntries(anyAmountToReturn)

        assertEquals(analysisResultsToReturn, lastEntries)
    }

}