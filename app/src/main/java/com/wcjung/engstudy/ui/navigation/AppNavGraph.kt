package com.wcjung.engstudy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wcjung.engstudy.ui.screen.bookmarks.BookmarksScreen
import com.wcjung.engstudy.ui.screen.challenge.DailyChallengeScreen
import com.wcjung.engstudy.ui.screen.edu.EduFlashCardScreen
import com.wcjung.engstudy.ui.screen.edu.EduHomeScreen
import com.wcjung.engstudy.ui.screen.edu.EduQuizScreen
import com.wcjung.engstudy.ui.screen.edu.EduWordListScreen
import com.wcjung.engstudy.ui.screen.flashcard.FlashCardScreen
import com.wcjung.engstudy.ui.screen.home.HomeScreen
import com.wcjung.engstudy.ui.screen.idiom.IdiomHomeScreen
import com.wcjung.engstudy.ui.screen.idiom.IdiomListScreen
import com.wcjung.engstudy.ui.screen.idiom.IdiomQuizScreen
import com.wcjung.engstudy.ui.screen.home.HomeViewModel
import com.wcjung.engstudy.ui.screen.placementtest.PlacementTestScreen
import com.wcjung.engstudy.ui.screen.quiz.QuizScreen
import com.wcjung.engstudy.ui.screen.review.ReviewScreen
import com.wcjung.engstudy.ui.screen.spelling.SpellingQuizScreen
import com.wcjung.engstudy.ui.screen.search.SearchScreen
import com.wcjung.engstudy.ui.screen.settings.SettingsScreen
import com.wcjung.engstudy.ui.screen.statistics.StatisticsScreen
import com.wcjung.engstudy.ui.screen.study.StudyScreen
import com.wcjung.engstudy.ui.screen.worddetail.WordDetailScreen
import com.wcjung.engstudy.ui.screen.wordlist.WordListScreen
import com.wcjung.engstudy.ui.screen.wronganswer.WrongAnswerScreen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun EngStudyNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val dueReviewCount by homeViewModel.dueReviewCount.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "홈", Icons.Default.Home),
        BottomNavItem(Screen.Study, "학습", Icons.AutoMirrored.Filled.MenuBook),
        BottomNavItem(Screen.Review, "복습", Icons.Default.Refresh),
        BottomNavItem(Screen.Profile, "프로필", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = bottomNavItems.any { item ->
                currentDestination?.hasRoute(item.screen::class) == true
            }

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hasRoute(item.screen::class) == true
                        NavigationBarItem(
                            icon = {
                                if (item.screen is Screen.Review && dueReviewCount > 0) {
                                    BadgedBox(badge = {
                                        Badge { Text(dueReviewCount.toString()) }
                                    }) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToWordList = { domain, stage ->
                        navController.navigate(Screen.WordList(domain, stage))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search) },
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail(wordId))
                    },
                    onNavigateToEdu = { navController.navigate(Screen.EduHome) },
                    onNavigateToPlacementTest = { navController.navigate(Screen.PlacementTest) },
                    onNavigateToDailyChallenge = { navController.navigate(Screen.DailyChallenge) },
                    onNavigateToIdiom = { navController.navigate(Screen.IdiomHome) }
                )
            }
            composable<Screen.Study> {
                StudyScreen(
                    onStartFlashCard = { domain, stage ->
                        navController.navigate(Screen.FlashCard(domain, stage))
                    },
                    onStartQuiz = { domain, stage ->
                        navController.navigate(Screen.Quiz(domain, stage))
                    },
                    onStartSpellingQuiz = { domain, stage ->
                        navController.navigate(Screen.SpellingQuiz(domain, stage))
                    },
                    onNavigateToWordList = { domain, stage ->
                        navController.navigate(Screen.WordList(domain, stage))
                    }
                )
            }
            composable<Screen.Review> {
                ReviewScreen(
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail(wordId))
                    }
                )
            }
            composable<Screen.Profile> {
                ProfileScreen(
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics) },
                    onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings) },
                    onNavigateToSearch = { navController.navigate(Screen.Search) },
                    onNavigateToWrongAnswers = { navController.navigate(Screen.WrongAnswers) }
                )
            }
            composable<Screen.WordList> {
                WordListScreen(
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail(wordId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.WordDetail> {
                WordDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.FlashCard> {
                FlashCardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Quiz> {
                QuizScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.SpellingQuiz> {
                SpellingQuizScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Bookmarks> {
                BookmarksScreen(
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail(wordId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Search> {
                SearchScreen(
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail(wordId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Statistics> {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.WrongAnswers> {
                WrongAnswerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.PlacementTest> {
                PlacementTestScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTestComplete = { navController.popBackStack() }
                )
            }
            composable<Screen.DailyChallenge> {
                DailyChallengeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.EduHome> {
                EduHomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWordList = { level ->
                        navController.navigate(Screen.EduWordList(level))
                    },
                    onNavigateToFlashCard = { level ->
                        navController.navigate(Screen.EduFlashCard(level))
                    },
                    onNavigateToQuiz = { level ->
                        navController.navigate(Screen.EduQuiz(level))
                    }
                )
            }
            composable<Screen.EduWordList> {
                EduWordListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.EduFlashCard> {
                EduFlashCardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.EduQuiz> {
                EduQuizScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.IdiomHome> {
                IdiomHomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToList = { type ->
                        navController.navigate(Screen.IdiomList(type))
                    },
                    onNavigateToQuiz = { type ->
                        navController.navigate(Screen.IdiomQuiz(type))
                    }
                )
            }
            composable<Screen.IdiomList> {
                IdiomListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.IdiomQuiz> {
                IdiomQuizScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onNavigateToStatistics: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWrongAnswers: () -> Unit
) {
    com.wcjung.engstudy.ui.screen.profile.ProfileScreen(
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToBookmarks = onNavigateToBookmarks,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToWrongAnswers = onNavigateToWrongAnswers
    )
}
