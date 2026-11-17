package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.engine.LiveMatchUiState
import com.example.engine.TacticalDecisionOption
import com.example.model.CareerSaveState
import com.example.model.GraphicsQuality
import com.example.ui.screens.match.InteractiveMatchScreen

@Composable
fun MatchCenterScreen(
    career: CareerSaveState,
    liveMatch: LiveMatchUiState?,
    graphicsQuality: GraphicsQuality,
    onPrepareMatch: () -> Unit,
    onStartKickOff: () -> Unit,
    onResumeSecondHalf: () -> Unit,
    onChangeSpeed: (Int) -> Unit,
    onChooseDecisionOption: (TacticalDecisionOption) -> Unit,
    onFinishMatch: () -> Unit,
    onSkipToNextMoment: () -> Unit = {}
) {
    InteractiveMatchScreen(
        career = career,
        liveMatch = liveMatch,
        graphicsQuality = graphicsQuality,
        onPrepareMatch = onPrepareMatch,
        onStartKickOff = onStartKickOff,
        onResumeSecondHalf = onResumeSecondHalf,
        onChangeSpeed = onChangeSpeed,
        onChooseDecisionOption = onChooseDecisionOption,
        onFinishMatch = onFinishMatch,
        onSkipToNextMoment = onSkipToNextMoment
    )
}

@Composable
fun PostMatchLockerRoomScreen(
    career: CareerSaveState,
    onSubmitPressStatement: (String, String) -> Unit,
    onContinueToCareerHub: () -> Unit,
    onCompleteInterview: ((com.example.engine.interview.InterviewSessionResult) -> Unit)? = null,
    geminiApiKey: String? = null,
    geminiModel: String = "gemini-2.5-flash",
    aiEnabled: Boolean = true
) {
    com.example.ui.screens.interview.PostMatchInterviewScreen(
        career = career,
        onCompleteInterview = onCompleteInterview,
        onSubmitPressStatement = onSubmitPressStatement,
        geminiApiKey = geminiApiKey,
        geminiModel = geminiModel,
        aiEnabled = aiEnabled,
        onContinueToCareerHub = onContinueToCareerHub
    )
}
