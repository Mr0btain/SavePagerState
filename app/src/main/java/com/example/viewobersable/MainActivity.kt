@file:OptIn(ExperimentalFoundationApi::class)

package com.example.viewobersable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.viewobersable.ui.theme.ViewObersableTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewObersableTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = VariableViewModel()

                    // initialize a pagerState to control pager this could also be hoisted higher
                    val pagerState = rememberPagerState()

                    // This version makes use of State Flows
                    MainScreenViewModelExample(viewModel, pagerState)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreenViewModelExample(viewModel: VariableViewModel, pagerState: PagerState) {
    // Collect this as a State so we see the changes
    val screenToShow = viewModel.uiState.collectAsState()
    val savedPageNumber = viewModel.savedPageNum.collectAsState()

    Column {
        // Not sure what screenImage is for? to display the index of the
        // screens? you could just use the name of the ScreenName enums .name like below
        // Top text shows selected screen that you are on at all times, i like to do this
        // when testing so i know what is set at every step just in case

        // You can use AnimatedContent to smooth the transition between composable
        AnimatedContent(
            label = "PagerScreenAnimation",
            targetState = screenToShow.value // we have to specify value here
        ) { screenName ->
            when (screenName) {
                // First screen our HorizontalPager
                ScreenName.FIRST -> {
                    Column {
                        CurrentScreenText(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            screenName = screenToShow.value.name
                        )

                        ScreenFirstView(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            pagerState = pagerState,
                            savedPageNumber = savedPageNumber.value
                        )
                    }
                }

                // Second screen
                ScreenName.SECOND -> {
                    Column {
                        CurrentScreenText(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            screenName = screenToShow.value.name
                        )

                        ScreenSecondView(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            currentIndex = pagerState.currentPage
                        )
                    }
                }
            }
        }

        // You could pass the view model and have this code in the composable but
        // we can just add the logic to switch our selectedScreen right here like this:
        ChangeUiStateButton(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            // Update our selected Screen
            if (screenToShow.value == ScreenName.FIRST) {
                // We call the function from out view model to update the variable were observing
                viewModel.updateUiState(ScreenName.SECOND)
                // Same for page number
                viewModel.savePageNumber(pagerState.currentPage)
            } else {
                // update our composable
                viewModel.updateUiState(ScreenName.FIRST)
            }
        }
    }
}

// If you would like to still store these values in view model, I would set it up like this:
class VariableViewModel : ViewModel() {
    // Using a private val so it only can receive updates from updateUiState function
    private val _uiState = MutableStateFlow(ScreenName.FIRST)
    // This is the state flow we will "Observe" in our ui
    val uiState = _uiState.asStateFlow()

    fun updateUiState(screen: ScreenName) { _uiState.value = screen }

    private val _savedPageNum = MutableStateFlow(0)
    // This is the state flow we will "Observe" in our ui
    val savedPageNum = _savedPageNum.asStateFlow()

    fun savePageNumber(pageNumber: Int) { _savedPageNum.value = pageNumber }
}

enum class ScreenName {
    FIRST,
    SECOND
}

@Composable
fun ChangeUiStateButton(modifier: Modifier = Modifier,changeUiState: () -> Unit) {
    Button(
        onClick = changeUiState,
        modifier = modifier
    ) {
        Text(text = "Add")
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenFirstView(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    savedPageNumber: Int = 0
) {
    // Upon composing this view, we need a launched effect to set the page to the saved page
    // if no page is passed defaults to 0
    LaunchedEffect(
        key1 = Unit,
        block = { pagerState.scrollToPage(savedPageNumber) }
    )

    Column(
        modifier = modifier
            .wrapContentSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.CenterHorizontally),
            pageCount = 10,
            state = pagerState
        ) { page ->
            Text(
                text = "Page: $page",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun ScreenSecondView(modifier: Modifier = Modifier, currentIndex: Int) {
    Column(
        modifier = modifier
            .background(Color.Black)
            .fillMaxWidth()
    ) {
        Text(
            text = "Saved Page Number: $currentIndex",
            color = Color.White,
            style = TextStyle(
                fontSize = 20.sp
            ),
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun CurrentScreenText(modifier: Modifier = Modifier, screenName: String) {
    // This will display which screen is selected which i think is what you mean
    // by current index this would be the way i would test which screen is selected
    // as i would want to know exactly what it is at every transition
    Text(
        text = screenName,
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = modifier
            .padding(8.dp)
            .wrapContentSize()
    )
}