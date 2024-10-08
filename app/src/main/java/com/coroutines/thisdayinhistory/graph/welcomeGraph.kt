package com.coroutines.thisdayinhistory.graph

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coroutines.thisdayinhistory.ui.screens.language.LanguageScreen
import com.coroutines.thisdayinhistory.ui.screens.welcome.WelcomeScreen
import com.coroutines.thisdayinhistory.ui.transitions.enterHorizontallyTransition
import com.coroutines.thisdayinhistory.ui.viewmodels.ISettingsViewModel
import com.coroutines.thisdayinhistory.ui.viewmodels.WelcomeViewModel
import com.coroutines.thisdayinhistory.ui.viewmodels.WelcomeViewModelMock

const val langPrompt = "PROMPT"
fun NavGraphBuilder.introGraph(navController: NavController, settingsViewModel: ISettingsViewModel) {
    navigation(startDestination = IntroNavOption.WelcomeScreen.name, route = NavRoutes.IntroRoute.name) {
        composable(IntroNavOption.WelcomeScreen.name){
            val appConfigState = settingsViewModel.appConfigurationState.collectAsStateWithLifecycle().value
            WelcomeScreen(
                navController,
                appConfigState,
                hiltViewModel<WelcomeViewModel>()
            )
        }

        composable(IntroNavOption.LanguagesScreen.name + "/{$langPrompt}",
            arguments = listOf(navArgument(langPrompt) { type = NavType.StringType }),
            enterTransition = { enterHorizontallyTransition() },
        ){ backStackEntry ->
            val prompt = backStackEntry.arguments?.getString(langPrompt) ?: "Please choose your language"
            LanguageScreen(
                navController= navController,
                viewModel = settingsViewModel,
                languagePrompt =  prompt
            )
        }
    }
}

