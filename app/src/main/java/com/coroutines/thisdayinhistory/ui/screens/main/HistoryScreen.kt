package com.coroutines.thisdayinhistory.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coroutines.api.translation.TranslationApiService
import com.coroutines.api.wiki.WikiMediaApiService
import com.coroutines.api.wiki.WikiMediaApiServiceImpl
import com.coroutines.data.converters.JsonConverterService
import com.coroutines.data.models.HistoricalEvent
import com.coroutines.data.models.LangEnum
import com.coroutines.thisdayinhistory.R
import com.coroutines.thisdayinhistory.drawer.AppNavigationDrawerWithContent
import com.coroutines.thisdayinhistory.ui.viewmodels.ISettingsViewModel
import com.coroutines.thisdayinhistory.components.NAV_ARGUMENT_HISTORY_EVENT
import com.coroutines.thisdayinhistory.ui.appbar.AppBar
import com.coroutines.thisdayinhistory.ui.components.BottomNavigationBarCalendar
import com.coroutines.thisdayinhistory.ui.state.DataRequestState
import com.coroutines.thisdayinhistory.ui.state.HistoryViewModelState
import com.coroutines.thisdayinhistory.ui.state.RequestCategory
import com.coroutines.thisdayinhistory.ui.viewmodels.HistoryViewModel
import com.coroutines.thisdayinhistory.ui.viewmodels.HistoryViewModelMock
import com.coroutines.thisdayinhistory.ui.viewmodels.IHistoryViewModel
import com.coroutines.thisdayinhistory.uimodels.HistoryCalendar
import com.coroutines.thisdayinhistory.uimodels.HistoryDataMap
import com.coroutines.usecase.HistoryDataStandardUseCase
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    settingsViewModel : ISettingsViewModel,
    viewModel: IHistoryViewModel
){
   val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
   AppNavigationDrawerWithContent(
       navController = navController,
       settingsViewModel = settingsViewModel,
       drawerState
   ) {

       val uiState by viewModel.uiState.collectAsStateWithLifecycle()
       val dataRequestState = uiState.dataRequestState
       val option = uiState.selectedCategory
       val categories = uiState.catsByLanguage.getCategories().values.toList()
       var isItemImageExpanded by remember { mutableStateOf(false) }

       AnimatedContent(targetState = dataRequestState,
           transitionSpec = {
               if (dataRequestState == DataRequestState.CompletedSuccessfully(RequestCategory.Option)) {
                   (slideInHorizontally (animationSpec = tween(durationMillis = 300))
                   { width -> width }
                           + fadeIn()).togetherWith(slideOutHorizontally
                   { width -> -width } + fadeOut())
               } else {
                   fadeIn(animationSpec = tween(200)) togetherWith
                           fadeOut(animationSpec = tween(200)) using
                           SizeTransform { initialSize, targetSize ->
                               if (targetState == DataRequestState.CompletedSuccessfully()) {
                                   keyFramesToTargetState(targetSize)
                               } else {
                                   keyFramesToNonTargetState(initialSize)
                               }
                           }
               }
           }, label = "") { targetState ->
           when {
               targetState is DataRequestState.Started
               -> HistoryScreenLoading(viewModel)
               targetState is DataRequestState.CompletedWithError
               -> HistoryScreenError(targetState.errorMessage)
               targetState is DataRequestState.CompletedSuccessfully
               -> {
                   val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                   Scaffold(
                       modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection ),
                       topBar = {
                           AppBar(
                               historyViewModel = viewModel,
                               drawerState = drawerState,
                               cancelButtonText = R.string.cancel,
                               scrollBehavior = scrollBehavior
                           )
                       },
                       bottomBar = {
                           BottomBar(
                               isItemImageExpanded,
                               viewModel,
                               uiState
                           )
                       }
                   ) { paddingValues ->
                       Column(
                           Modifier
                               .fillMaxSize()
                               .padding(top = paddingValues.calculateTopPadding())
                       ) {
                           Column(Modifier.fillMaxWidth()) {
                               HistoryViewCategoryTabs(
                                   categories = categories,
                                   option,
                                   onCategorySelected = viewModel::onCategoryChanged,
                                   tabItemsPadding = 10.dp,
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .background(MaterialTheme.colorScheme.background)
                               )


                               HistoryEventList(
                                   viewModel = viewModel,
                                   windowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
                                   navController = navController
                               ) { result ->
                                    isItemImageExpanded = result
                               }
                           }
                       }
                   }
               }
           }
       }
   }
}
@Composable
@Suppress("MagicNumber")
private fun BottomBar(
    isItemImageExpanded: Boolean,
    historyViewModel: IHistoryViewModel,
    historyViewModelState: HistoryViewModelState,
) {
    AnimatedVisibility(
        visible = (!isItemImageExpanded) && (!historyViewModel.isScrolled.value),
        enter = fadeIn() + slideInVertically(animationSpec = tween(10),
            initialOffsetY = { fullHeight -> fullHeight }),
        exit = fadeOut() + slideOutVertically(animationSpec = tween(10),
            targetOffsetY = { fullHeight -> fullHeight }),
    ) {
        BottomNavigationBarCalendar(
            historyViewModel = historyViewModel,
            historyViewModelState = historyViewModelState
        )
    }
}

@Suppress("MagicNumber")
private fun keyFramesToNonTargetState(initialSize: IntSize) = keyframes {
    IntSize(
        initialSize.width / 5,
        initialSize.height / 5
    ) at 60
    durationMillis = 70
    IntSize(
        initialSize.width / 3,
        initialSize.height / 3
    ) at 130
    durationMillis = 70
    IntSize(
        initialSize.width / 2,
        initialSize.height / 2
    ) at 150
    durationMillis = 70
}

@Suppress("MagicNumber")
private fun keyFramesToTargetState(targetSize: IntSize) = keyframes {
    // Expand horizontally first.
    IntSize(0, 0) at 0
    durationMillis = 50
    IntSize(
        targetSize.width / 5,
        targetSize.height / 5
    ) at 60
    durationMillis = 50
    IntSize(
        targetSize.width / 3,
        targetSize.height / 3
    ) at 100
    durationMillis = 50
    IntSize(
        targetSize.width / 2,
        targetSize.height / 2
    ) at 150
    durationMillis = 50
}
