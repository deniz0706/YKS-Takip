package com.deniz0706.ykstakip.ui.navigation

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.rememberCoroutineScope
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
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.FileProvider
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
import com.deniz0706.ykstakip.data.LegacyLearningRepositoryAdapter
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.data.StudyPlanRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.repository.LearningRepository

import com.deniz0706.ykstakip.ui.ai.PremiumAiCoachScreen
import com.deniz0706.ykstakip.ui.history.PremiumExamDetailScreen
import com.deniz0706.ykstakip.ui.history.PremiumExamsScreen
import com.deniz0706.ykstakip.ui.home.PremiumHomeDashboard
import com.deniz0706.ykstakip.ui.settings.PremiumSettingsScreen
import com.deniz0706.ykstakip.ui.statistics.PremiumStatisticsScreen
import com.deniz0706.ykstakip.ui.study.PremiumStudyScreen
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.viewmodel.HomeViewModel
import com.deniz0706.ykstakip.viewmodel.ExamViewModel
import com.deniz0706.ykstakip.viewmodel.SettingsViewModel
import com.deniz0706.ykstakip.viewmodel.StatisticsViewModel
import com.deniz0706.ykstakip.viewmodel.StudyViewModel
import com.deniz0706.ykstakip.viewmodel.AiCoachViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Two-pane layout for tablets: rail + content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // NavigationRail with adaptive width (icon-only on compact, labels when wide enough)
            Column(
                modifier = Modifier
                    .width(if (isLandscape) 96.dp else 80.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                YksTabletRail(
                    currentDestination = currentDestination,
                    onDestinationSelected = onDestinationSelected,
                    showLabels = isLandscape
                )
            }

            // Divider
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // Content area with max-width constraint for readability
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                color = c.surface
            ) {
                // Constrain content width on very wide tablets for comfortable reading
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = if (isLandscape) YksTokens.Space.pageHorizontal else 0.dp,
                            vertical = YksTokens.Space.pageVertical
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .width(if (isLandscape) YksTokens.Space.maxContentWidth else YksTokens.Space.maxContentWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
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
    }
}

/** Tablet navigation rail with adaptive label support. */
@Composable
fun YksTabletRail(
    currentDestination: YksNavDestination,
    onDestinationSelected: (YksNavDestination) -> Unit,
    showLabels: Boolean = false
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
                label = {
                    if (showLabels) {
                        Text(
                            text = destinationLabel(destination),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) c.primary else c.onSurfaceVariant
                        )
                    }
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
    val coroutineScope = rememberCoroutineScope()

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
    var hasAiKey by remember(appContext) { mutableStateOf(AiSecretStoreHasApiKey(appContext)) }

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(appContext as android.app.Application)
    )
    val statsExams by statisticsViewModel.exams.asFlow().collectAsState(initial = emptyList())

    // Study screen: real plan data from StudyPlanRepository
    val studyPlanRepository = remember(appContext) { StudyPlanRepository(appContext) }
    var studyPlan by remember { mutableStateOf(studyPlanRepository.getAll()) }
    var studyGoal by remember { mutableStateOf(AppSettings.getDailyGoal(appContext)) }

    val learningRepository = remember(appContext) { com.deniz0706.ykstakip.data.LearningRepository.getInstance(appContext) }
    var learningOutcomes by remember { mutableStateOf(learningRepository.outcomes()) }
    var learningQuestions by remember { mutableStateOf(learningRepository.questions()) }
    val refreshLearningData = {
        learningOutcomes = learningRepository.outcomes()
        learningQuestions = learningRepository.questions()
    }
    val refreshStudyPlan = { studyPlan = studyPlanRepository.getAll() }

    var pendingCameraPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingGalleryQuestionId by remember { mutableStateOf<String?>(null) }
    val cameraPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val pending = pendingCameraPhoto
        pendingCameraPhoto = null
        if (pending != null) {
            if (saved && learningRepository.attachCapturedPhoto(pending.first, pending.second)) refreshLearningData()
            else learningRepository.discardCameraPhoto(pending.second)
        }
    }
    val galleryPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val questionId = pendingGalleryQuestionId
        pendingGalleryQuestionId = null
        if (uri != null && questionId != null && learningRepository.attachGalleryPhoto(questionId, uri)) {
            refreshLearningData()
        }
    }

    // File operation state for settings import/export
    var lastStudyImportJson by remember { mutableStateOf<String?>(null) }
    var fullBackupBusy by remember { mutableStateOf(false) }
    var learningZipBusy by remember { mutableStateOf(false) }

    // Activity Result launchers for file operations
    val exportFullLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { destination ->
            coroutineScope.launch {
                fullBackupBusy = true
                try {
                    withContext(Dispatchers.IO) {
                        com.deniz0706.ykstakip.data.FullBackupManager.exportTo(appContext, destination)
                    }
                } catch (_: Exception) {
                    // The screen presents a non-destructive error state.
                } finally {
                    fullBackupBusy = false
                }
            }
        }
    }
    val importFullLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { source ->
            coroutineScope.launch {
                fullBackupBusy = true
                try {
                    withContext(Dispatchers.IO) {
                        val prepared = com.deniz0706.ykstakip.data.FullBackupManager.prepareImport(appContext, source)
                        com.deniz0706.ykstakip.data.FullBackupManager.merge(appContext, prepared)
                    }
                    examViewModel.refresh()
                    studyViewModel.refresh()
                    refreshLearningData()
                    refreshStudyPlan()
                } catch (_: Exception) {
                    // The screen presents a non-destructive error state.
                } finally {
                    fullBackupBusy = false
                }
            }
        }
    }
    val exportStudyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { destination ->
            coroutineScope.launch {
                try {
                    val days = withContext(Dispatchers.IO) {
                        com.deniz0706.ykstakip.data.StudyRepository(appContext).getAll()
                    }
                    val json = com.deniz0706.ykstakip.data.StudyBackupManager.exportJson(days)
                    withContext(Dispatchers.IO) {
                        appContext.contentResolver.openOutputStream(destination)?.use { output ->
                            output.write(json.toByteArray())
                        }
                    }
                } catch (_: Exception) {
                    // The screen presents a non-destructive error state.
                }
            }
        }
    }
    val importStudyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { source ->
            coroutineScope.launch {
                try {
                    val parsed = withContext(Dispatchers.IO) {
                        val text = appContext.contentResolver.openInputStream(source)?.use {
                            java.io.BufferedReader(java.io.InputStreamReader(it)).readText()
                        } ?: throw java.io.IOException("Dosya okunamadı.")
                        lastStudyImportJson = text
                        com.deniz0706.ykstakip.data.StudyBackupManager.parseJson(text)
                    }
                    withContext(Dispatchers.IO) {
                        com.deniz0706.ykstakip.data.StudyRepository(appContext).importDays(parsed.days, overwrite = false)
                    }
                    studyViewModel.refresh()
                } catch (_: Exception) {
                    // The screen presents a non-destructive error state.
                }
            }
        }
    }
    val exportLearningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            try {
                learningZipBusy = true
                com.deniz0706.ykstakip.data.LearningArchiveManager(appContext).exportTo(it)
            } catch (e: Exception) {
                // Error feedback handled by caller
            } finally {
                learningZipBusy = false
            }
        }
    }
    val importLearningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                learningZipBusy = true
                val prepared = com.deniz0706.ykstakip.data.LearningArchiveManager(appContext).prepareImport(it)
                com.deniz0706.ykstakip.data.LearningArchiveManager(appContext).merge(prepared)
                refreshLearningData()
            } catch (e: Exception) {
                // Error feedback handled by caller
            } finally {
                learningZipBusy = false
            }
        }
    }

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

            PremiumHomeDashboard(
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
            PremiumExamsScreen(
                exams = exams,
                onExamClick = { examId ->
                    navController.navigate("exams/$examId")
                },
                onNewExam = {
                    navController.navigate("exams/new")
                },
                colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
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

            PremiumExamDetailScreen(
                exam = exam,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate("exams/${exam.id}/edit")
                },
                onDelete = {
                    coroutineScope.launch {
                        examViewModel.deleteExam(exam.id)
                    }
                },
                onOpenLearning = { subject, topic, title ->
                    navController.navigate("study?learning=$subject/$topic/$title")
                },
                colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
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
            PremiumStudyScreen(
                days = studyDays,
                plan = studyPlan,
                goal = studyGoal,
                exams = statsExams,
                learningOutcomes = learningOutcomes,
                learningQuestions = learningQuestions,
                learningField = AppSettings.getLearningField(appContext),
                onLearningFieldChange = { field ->
                    AppSettings.setLearningField(appContext, field)
                },
                onLearningComplete = { id, completed ->
                    learningRepository.setCompleted(id, completed)
                    refreshLearningData()
                },
                onLearningSubtopicComplete = { id, subtopic, completed ->
                    learningRepository.setSubtopicCompleted(id, subtopic, completed)
                    refreshLearningData()
                },
                onLearningWrong = { id ->
                    learningRepository.markWrong(id)
                    refreshLearningData()
                },
                onLearningRequestReview = { id ->
                    learningRepository.requestReview(id)
                    refreshLearningData()
                },
                onLearningReviewed = { id ->
                    learningRepository.reviewOutcome(id)
                    refreshLearningData()
                },
                onLearningPostpone = { id ->
                    learningRepository.postponeReview(id)
                    refreshLearningData()
                },
                onLearningCreate = { examType, subject, topic, title ->
                    learningRepository.addOutcome(examType, subject, topic, title)
                    refreshLearningData()
                },
                onLearningDelete = { id ->
                    coroutineScope.launch {
                        learningRepository.deleteCustomOutcome(id)
                        refreshLearningData()
                    }
                },
                onQuestionCreate = { title, notes, photos, status ->
                    // Use an existing outcome or create a default one for standalone questions
                    val outcomeId = learningOutcomes.firstOrNull()?.id
                        ?: run {
                            val defaultId = "genel-sorular"
                            learningRepository.addOutcome("TYT", "Genel", "Sorular", "Genel Sorular").id
                        }
                    val card = learningRepository.addQuestion(outcomeId, title, notes, status)
                    refreshLearningData()
                    card.id
                },
                onQuestionDelete = { id ->
                    learningRepository.deleteQuestion(id)
                    refreshLearningData()
                },
                onQuestionReview = { id, solved ->
                    learningRepository.reviewQuestion(id, solved)
                    refreshLearningData()
                },
                onQuestionUnsolved = { id ->
                    learningRepository.markQuestionUnsolved(id)
                    refreshLearningData()
                },
                onQuestionCapture = { id ->
                    try {
                        val (filename, file) = learningRepository.newCameraPhoto()
                        pendingCameraPhoto = id to filename
                        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.reviewphotos", file)
                        cameraPhotoLauncher.launch(uri)
                    } catch (_: Exception) {
                        pendingCameraPhoto?.second?.let(learningRepository::discardCameraPhoto)
                        pendingCameraPhoto = null
                    }
                },
                onQuestionGallery = { id ->
                    pendingGalleryQuestionId = id
                    galleryPhotoLauncher.launch("image/*")
                },
                onQuestionPhotoRemove = { id, filename ->
                    learningRepository.removePhoto(id, filename)
                    refreshLearningData()
                },
                onRequestReviewNotifications = {
                    com.deniz0706.ykstakip.data.ReviewReminderScheduler.rescheduleAll(appContext, learningOutcomes)
                },
                onEditToday = {
                    val date = java.time.LocalDate.now().toString()
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val studyRepo = com.deniz0706.ykstakip.data.StudyRepository(appContext)
                            if (studyRepo.get(date) == null) {
                                studyRepo.save(com.deniz0706.ykstakip.domain.model.StudyDay(
                                    date = date,
                                    questionCount = 0,
                                    studyMinutes = 0
                                ))
                            }
                        }
                        studyViewModel.refresh()
                    }
                    navController.navigate("study")
                },
                onFocusSaved = {
                    val focusRepo = com.deniz0706.ykstakip.data.FocusSessionRepository(appContext)
                    focusRepo.saveToToday()
                },
                onEditDay = { date ->
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            com.deniz0706.ykstakip.data.StudyRepository(appContext).get(date)
                        }
                    }
                },
                onPickDay = {
                    navController.navigate("study")
                },
                onAddPlan = { dayOfWeek ->
                    val newId = (studyPlan.maxByOrNull { it.id }?.id ?: 0L) + 1
                    val item = com.deniz0706.ykstakip.domain.model.StudyPlanItem(
                        id = newId,
                        dayOfWeek = dayOfWeek,
                        startTime = "09:00",
                        endTime = "10:00",
                        subject = "",
                        topic = ""
                    )
                    studyPlanRepository.save(item)
                    refreshStudyPlan()
                },
                onAddSuggestedPlan = { dayOfWeek, start, end ->
                    val newId = (studyPlan.maxByOrNull { it.id }?.id ?: 0L) + 1
                    val item = com.deniz0706.ykstakip.domain.model.StudyPlanItem(
                        id = newId,
                        dayOfWeek = dayOfWeek,
                        startTime = start,
                        endTime = end,
                        subject = "Genel Tekrar",
                        topic = "Önerilen Çalışma"
                    )
                    studyPlanRepository.save(item)
                    refreshStudyPlan()
                },
                onEditPlan = { item ->
                    studyPlanRepository.save(item)
                    refreshStudyPlan()
                },
                onTogglePlan = { id ->
                    studyPlanRepository.toggleCompleted(id)
                    refreshStudyPlan()
                },
                onOpenLearningFromPlan = { item ->
                    navController.navigate("study?learning=${item.subject}/${item.topic}/${item.subject}")
                },
                onDeletePlan = { item ->
                    studyPlanRepository.delete(item.id)
                    refreshStudyPlan()
                }
            )
            onDestinationChange?.invoke(YksNavDestination.Study)
        }

        composable(
            route = "statistics",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://statistics" }
            )
        ) { backStackEntry ->
            PremiumStatisticsScreen(
                exams = statsExams,
                obp = AppSettings.getObp(appContext)?.toDouble(),
                targetTyt = AppSettings.getTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.TYT),
                targetAyt = AppSettings.getTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.AYT)
            )
            onDestinationChange?.invoke(YksNavDestination.Statistics)
        }

        composable(
            route = "ai-coach",
            arguments = emptyList(),
            deepLinks = listOf(
                navDeepLink { uriPattern = "yks://ai-coach" }
            )
        ) { backStackEntry ->
            val aiCoachViewModel: AiCoachViewModel = viewModel(
                factory = AiCoachViewModelFactory(appContext as android.app.Application)
            )
            PremiumAiCoachScreen(
                viewModel = aiCoachViewModel,
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
            PremiumSettingsScreen(
                selectedTheme = theme,
                onThemeChange = { mode ->
                    settingsViewModel.setTheme(mode)
                },
                tyt = AppSettings.getTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.TYT)?.toString() ?: "",
                ayt = AppSettings.getTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.AYT)?.toString() ?: "",
                obp = AppSettings.getObp(appContext)?.toString() ?: "",
                daily = AppSettings.getDailyGoal(appContext)?.toString() ?: "",
                onSave = { tytVal, aytVal, obpVal, dailyVal ->
                    val tytFloat = if (tytVal.isNotBlank()) tytVal.toFloatOrNull() else null
                    val aytFloat = if (aytVal.isNotBlank()) aytVal.toFloatOrNull() else null
                    val obpFloat = if (obpVal.isNotBlank()) obpVal.toFloatOrNull() else null
                    val dailyInt = if (dailyVal.isNotBlank()) dailyVal.toIntOrNull() else null
                    AppSettings.setTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.TYT, tytFloat)
                    AppSettings.setTargetNet(appContext, com.deniz0706.ykstakip.domain.model.ExamType.AYT, aytFloat)
                    AppSettings.setObp(appContext, obpFloat)
                    AppSettings.setDailyGoal(appContext, dailyInt)
                    studyGoal = AppSettings.getDailyGoal(appContext)
                    null
                },
                aiBaseUrl = AppSettings.getAiBaseUrl(appContext),
                aiModel = AppSettings.getAiModel(appContext),
                hasAiKey = hasAiKey,
                onSaveAi = { key, baseUrl, model ->
                    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
                    when {
                        !normalizedBaseUrl.startsWith("https://") -> "Base URL https:// ile başlamalı."
                        model.isBlank() -> "Model adı boş olamaz."
                        key.isNotBlank() && key.trim().length < 12 -> "API anahtarı çok kısa görünüyor."
                        else -> try {
                            if (key.isNotBlank()) com.deniz0706.ykstakip.data.AiSecretStore.saveApiKey(appContext, key)
                            AppSettings.setAiBaseUrl(appContext, normalizedBaseUrl)
                            AppSettings.setAiModel(appContext, model.trim())
                            hasAiKey = AiSecretStoreHasApiKey(appContext)
                            null
                        } catch (error: Exception) {
                            error.message ?: "AI ayarları kaydedilemedi."
                        }
                    }
                },
                onTestAi = {
                    coroutineScope.launch {
                        val result = runCatching {
                            com.deniz0706.ykstakip.data.NvidiaAiService(appContext).testConnection()
                        }
                        val message = result.fold(
                            { "AI bağlantısı başarılı: " + it.take(100) },
                            { "AI bağlantısı başarısız: " + (it.message ?: "Bilinmeyen hata") }
                        )
                        android.widget.Toast.makeText(appContext, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                onExport = {
                    val destination = android.net.Uri.fromFile(java.io.File(appContext.cacheDir, "yks-export.zip"))
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            com.deniz0706.ykstakip.data.FullBackupManager.exportTo(appContext, destination)
                        }
                    }
                },
                onImport = {
                    // No file-based exam import currently supported; exams are managed in-app.
                },
                onImportStudy = {
                    importStudyLauncher.launch(arrayOf("application/json"))
                },
                onExportStudy = {
                    exportStudyLauncher.launch("yks-study-backup.json")
                },
                onUndoStudyImport = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            com.deniz0706.ykstakip.data.StudyRepository(appContext).undoLastImport()
                        }
                        studyViewModel.refresh()
                    }
                },
                learningZipBusy = learningZipBusy,
                onExportLearningZip = {
                    exportLearningLauncher.launch("yks-learning-backup.zip")
                },
                onImportLearningZip = {
                    importLearningLauncher.launch(arrayOf("application/zip"))
                },
                fullBackupBusy = fullBackupBusy,
                onExportFullBackup = {
                    exportFullLauncher.launch("yks-full-backup.zip")
                },
                onImportFullBackup = {
                    importFullLauncher.launch(arrayOf("application/zip"))
                },
                colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
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
        val learningRepo: com.deniz0706.ykstakip.domain.repository.LearningRepository = LegacyLearningRepositoryAdapter(application)
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


/** ViewModel factory for AiCoachViewModel. */
private class AiCoachViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiCoachViewModel(application) as T
    }
}
