package pl.handsome.club.domain.analyze.general

// TODO success result verification
sealed class GeneralAnalysisResult {

    class Error(val throwable: Throwable) : GeneralAnalysisResult()
    class Success(
        isItTooCaloric: Boolean
    ) : GeneralAnalysisResult()

}