package com.deniz0706.ykstakip.ui.navigation

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.AiMemoryRepository
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.data.repository.SqlLearningRepository
import com.deniz0706.ykstakip.data.repository.SqlStudyRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.ai.CoachLine
import com.deniz0706.ykstakip.ui.ai.NewAiCoachScreen
import com.deniz0706.ykstakip.ui.history.ExamDetailScreen
import com.deniz0706.ykstakip.ui.history.NewExamsScreen
import com.deniz0706.ykstakip.ui.home.NewHomeScreen
import com.deniz0706.ykstakip.ui.settings.NewSettingsScreen
import com.deniz0706.ykstakip.ui.study.NewStudyScreen
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.viewmodel.HomeViewModel
import com.deniz0706.ykstakip.viewmodel.ExamViewModel
import com.deniz0706.ykstakip.viewmodel.SettingsViewModel
import com.deniz0706.ykstakip.viewmodel.StatisticsViewModel
import com.deniz0706.ykstakip.viewmodel.StudyViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

/** Adaptive shell that chooses NavigationBar (phone) or NavigationRail (tablet). */
@Composable
fun YksShell(
    context: Context,
    onDestinationChange: ((YksNavDestination) -> Unit)? = null
) {
    var currentDestination by remember { mutableStateOf<YksNavDestination>(YksNavDestination.Home) }
    val navController = rememberNavController()

    onDestinationChange?.invoke(currentDestination)

    val contextState = remember(context) { ContextStateHolder(context) }

    YksTheme {
        val configuration = LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600
        if (isTablet) {
            YksTabletShell(
                navController = navController,
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    if (destination !is YksNavDestination.ExamDetail) {
                        navController.navigate(destination.route) {
                            popUpTo(YksNavDestination.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                context = contextState.applicationContext,
                onDestinationChange = onDestinationChange
            )
        } else {
            YksPhoneShell(
                navController = navController,
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    if (destination !is YksNavDestination.ExamDetail) {
                        navController.navigate(destination.route) {
                            popUpTo(YksNavDestination.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                context = contextState.applicationContext,
                onDestinationChange = onDestinationChange
            )
        }
    }
}

/** Holds the application context for ViewModel creation across recompositions. */
private class ContextStateHolder(private val context: Context) {
    val applicationContext: Context = context.applicationContext
}

/** Phone shell with bottom NavigationBar. */
@Composable
private fun YksPhoneShell(
    navController: NavHostController,
    currentDestination: YksNavDestination,
    onDestinationSelected: (YksNavDestination) -> Unit,
    context: Context,
    onDestinationChange: ((YksNavDestination) -> Unit)?
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            YksBottomNavigation(
                currentDestination = currentDestination,
                navController = navController,
                onDestinationSelected = onDestinationSelected
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            YksNavHost(
                navController = navController,
                onDestinationChange = onDestinationChange,
                context = context
            )
        }
    }
}

/** Tablet shell with left NavigationRail and content area. */
@Composable
private fun YksTabletShell(
    navController: NavHostController,
    currentDestination: YksNavDestination,
    onDestinationSelected: (YksNavDestination) -> Unit,
    context: Context,
    onDestinationChange: ((YksNavDestination) -> Unit)?
) {
    val c = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // NavigationRail column
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                YksTabletRail(
                    currentDestination = currentDestination,
                    onDestinationSelected = onDestinationSelected
                )
            }

            // Divider
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // Content area
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                color = c.surface
            ) {
                YksNavHost(
                    navController = navController,
                    onDestinationChange = onDestinationChange,
                    context = context
                )
            }
        }
    }
}

/** Tablet navigation rail (compact icon-only). */
@Composable
fun YksTabletRail(
    currentDestination: YksNavDestination,
    onDestinationSelected: (YksNavDestination) -> Unit
) {
    val c = MaterialTheme.colorScheme

    NavigationRail(
        modifier = Modifier.fillMaxHeight()
    ) {
        BOTTOM_NAV_DESTINATIONS.forEach { destination ->
            val selected = currentDestination == destination ||
                (currentDestination is YksNavDestination.ExamDetail && destination == YksNavDestination.Exams)
            NavigationRailItem(
                icon = {
                    Text(
                        text = destinationIcon(destination),
                        fontSize = 24.sp,
                        color = if (selected) c.primary else c.onSurfaceVariant
                    )
                },
                selected = selected,
                onClick = { onDestinationSelected(destination) }
            )
        }
    }
}

/** Navigation host with all typed destinations wired to real ViewModels and repositories. */
@Composable
fun YksNavHost(
    navController: NavHostController,
    onDestinationChange: ((YksNavDestination) -> Unit)? = null,
    context: Context
) {
    val appContext = context.applicationContext

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(appContext as android.app.Application)
    )
    val homeUiState by homeViewModel.uiState.collectAsState()

    val examViewModel: ExamViewModel = viewModel(
        factory = ExamViewModelFactory(appContext as android.app.Application)
    )
    val exams by examViewModel.exams.asFlow().collectAsState(initial = emptyList())

    val studyViewModel: StudyViewModel = viewModel(
        factory = StudyViewModelFactory(appContext as android.app.Application)
    )
    val studyDays by studyViewModel.days.asFlow().collectAsState(initial = emptyList())

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(appContext as android.app.Application)
    )
    val theme by settingsViewModel.theme.asFlow().collectAsState(initial = "light")

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(appContext as android.app.Application)
    )
    val statsExams by statisticsViewModel.exams.asFlow().collectAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = YksNavDestination.Home.route
    ) {
        composable(
            route = "home",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://home" }
            )
        ) { backStackEntry ->
            val aiCoachAdvice = homeUiState.aiCoachAdvice
            val aiCoachDate = homeUiState.aiCoachDate

            NewHomeScreen(
                exams = homeUiState.exams,
                onNewExam = { examType ->
                    navController.navigate("exams")
                },
                onOpenStudy = { navController.navigate("study") },
                onOpenAiCoach = { navController.navigate("ai-coach") },
                onOpenLearning = { _, _ -> navController.navigate("study") },
                aiCoachAdvice = aiCoachAdvice,
                aiCoachDate = aiCoachDate,
                reviewAlertCount = 0,
                learningDoneCount = homeUiState.learningDoneCount,
                learningTotalCount = homeUiState.learningTotalCount,
                hasFocusSession = false,
                daysLeft = homeUiState.daysLeft,
                todayQuestions = homeUiState.todayQuestions,
                dailyGoal = homeUiState.dailyGoal,
                targetTyt = homeUiState.targetTyt,
                targetAyt = homeUiState.targetAyt,
                daysLeftAyt = null
            )
            onDestinationChange?.invoke(YksNavDestination.Home)
        }

        composable(
            route = "exams",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://exams" }
            )
        ) { backStackEntry ->
            NewExamsScreen(
                exams = exams,
                onExamClick = { examId ->
                    navController.navigate("exams/$examId")
                },
                onNewExam = { /* handled by shell */ }
            )
            onDestinationChange?.invoke(YksNavDestination.Exams)
        }

        composable(
            route = "exams/{examId}",
            arguments = listOf(navArgument("examId") { type = androidx.navigation.NavType.LongType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://exams/{examId}" }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: -1L
            val exam = exams.firstOrNull { it.id == examId }
                ?: Exam(
                    id = examId,
                    title = "",
                    date = "",
                    type = ExamType.TYT,
                    subjects = emptyList()
                )

            ExamDetailScreen(
                exam = exam,
                onBack = { navController.popBackStack() },
                onEdit = {},
                onDelete = {},
                onOpenLearning = { _, _, _ -> }
            )
            onDestinationChange?.invoke(YksNavDestination.ExamDetail(examId))
        }

        composable(
            route = "study",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://study" }
            )
        ) { backStackEntry ->
            NewStudyScreen(
                days = studyDays,
                plan = emptyList(),
                goal = null,
                exams = statsExams,
                learningOutcomes = emptyList(),
                learningQuestions = emptyList(),
                learningField = AppSettings.getLearningField(appContext),
                onLearningFieldChange = { field ->
                    AppSettings.setLearningField(appContext, field)
                },
                onLearningComplete = { _, _ -> },
                onLearningSubtopicComplete = { _, _, _ -> },
                onLearningWrong = {},
                onLearningRequestReview = {},
                onLearningReviewed = {},
                onLearningPostpone = {},
                onLearningCreate = { _, _, _, _ -> },
                onLearningDelete = {},
                onQuestionCreate = { _, _, _, _ -> "" },
                onQuestionDelete = {},
                onQuestionReview = { _, _ -> },
                onQuestionUnsolved = {},
                onQuestionCapture = {},
                onQuestionGallery = {},
                onQuestionPhotoRemove = { _, _ -> },
                onRequestReviewNotifications = {},
                onEditToday = {},
                onFocusSaved = {},
                onEditDay = {},
                onPickDay = {},
                onAddPlan = {},
                onAddSuggestedPlan = { _, _, _ -> },
                onEditPlan = {},
                onTogglePlan = {},
                onOpenLearningFromPlan = {},
                onDeletePlan = {}
            )
            onDestinationChange?.invoke(YksNavDestination.Study)
        }

        composable(
            route = "ai-coach",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://ai-coach" }
            )
        ) { backStackEntry ->
            NewAiCoachScreen(
                lines = listOf(CoachLine("assistant", "Merhaba! Size nasıl yardımcı olabilir?")),
                draft = "",
                onDraftChange = {},
                onSend = {},
                sending = false,
                hasApiKey = AiSecretStoreHasApiKey(appContext),
                onApiKeyMissing = { navController.navigate("settings") }
            )
            onDestinationChange?.invoke(YksNavDestination.AiCoach)
        }

        composable(
            route = "settings",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://settings" }
            )
        ) { backStackEntry ->
            NewSettingsScreen(
                selectedTheme = theme,
                onThemeChange = { mode ->
                    settingsViewModel.setTheme(mode)
                },
                tyt = "",
                ayt = "",
                obp = "",
                daily = "",
                onSave = { _, _, _, _ -> null },
                aiBaseUrl = AppSettings.getAiBaseUrl(appContext),
                aiModel = AppSettings.getAiModel(appContext),
                hasAiKey = AiSecretStoreHasApiKey(appContext),
                onSaveAi = { _, _, _ -> null },
                onTestAi = {},
                onExport = {},
                onImport = {},
                onImportStudy = {},
                onExportStudy = {},
                onUndoStudyImport = {},
                learningZipBusy = false,
                onExportLearningZip = {},
                onImportLearningZip = {},
                fullBackupBusy = false,
                onExportFullBackup = {},
                onImportFullBackup = {}
            )
            onDestinationChange?.invoke(YksNavDestination.Settings)
        }
    }
}

/** Bottom navigation bar for compact (phone) layouts. */
@Composable
fun YksBottomNavigation(
    currentDestination: YksNavDestination,
    navController: NavHostController,
    onDestinationSelected: (YksNavDestination) -> Unit
) {
    val c = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = c.surfaceContainer
    ) {
        BOTTOM_NAV_DESTINATIONS.forEach { destination ->
            val selected = currentDestination == destination ||
                (currentDestination is YksNavDestination.ExamDetail && destination == YksNavDestination.Exams)
            NavigationBarItem(
                icon = {
                    Text(
                        text = destinationIcon(destination),
                        fontSize = 24.sp,
                        color = if (selected) c.primary else c.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = destinationLabel(destination),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) c.primary else c.onSurfaceVariant
                    )
                },
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                alwaysShowLabel = true
            )
        }
    }
}

/** Backwards-compatible wrapper for the old YksNavigation API. */
@Deprecated(
    message = "Use YksShell / YksNavHost with Navigation Compose instead",
    level = DeprecationLevel.ERROR
)
@Composable
fun YksNavigation(
    currentScreen: Any,
    onScreenChange: (Any) -> Unit,
    content: @Composable (Any) -> Unit
) {
    error("YksNavigation is deprecated. Use YksShell and YksNavHost instead.")
}

/** Returns whether an AI API key is stored. */
private fun AiSecretStoreHasApiKey(context: Context): Boolean {
    return try {
        com.deniz0706.ykstakip.data.AiSecretStore.hasApiKey(context)
    } catch (_: Exception) {
        false
    }
}

/** ViewModel factory for HomeViewModel. */
private class HomeViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val examRepo: com.deniz0706.ykstakip.domain.repository.ExamRepository = SqlExamRepository(application)
        val learningRepo: com.deniz0706.ykstakip.domain.repository.LearningRepository = SqlLearningRepository(application)
        return HomeViewModel(application, examRepo, learningRepo) as T
    }
}

/** ViewModel factory for ExamViewModel. */
private class ExamViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExamViewModel(application) as T
    }
}

/** ViewModel factory for StudyViewModel. */
private class StudyViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StudyViewModel(application) as T
    }
}

/** ViewModel factory for SettingsViewModel. */
private class SettingsViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(application) as T
    }
}

/** ViewModel factory for StatisticsViewModel. */
private class StatisticsViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(application) as T
    }
}

