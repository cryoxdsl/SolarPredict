package com.example.solarpredict.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.solarpredict.AppContainer
import com.example.solarpredict.presentation.components.AppScaffold
import com.example.solarpredict.presentation.ui.detail.DetailScreen
import com.example.solarpredict.presentation.ui.history.HistoryScreen
import com.example.solarpredict.presentation.ui.home.HomeScreen
import com.example.solarpredict.presentation.ui.settings.SettingsScreen
import com.example.solarpredict.presentation.viewmodel.DetailViewModel
import com.example.solarpredict.presentation.viewmodel.HistoryViewModel
import com.example.solarpredict.presentation.viewmodel.HomeViewModel
import com.example.solarpredict.presentation.viewmodel.SettingsViewModel
import com.example.solarpredict.presentation.viewmodel.SimpleFactory

enum class Screen(val route: String, val label: String) {
    HOME("home", "Home"),
    DETAIL("detail", "Détail"),
    HISTORY("history", "Historique"),
    SETTINGS("settings", "Paramètres")
}

@Composable
fun SolarNavGraph(container: AppContainer, navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val selected = Screen.entries.firstOrNull { it.route == backStack?.destination?.route } ?: Screen.HOME

    AppScaffold(
        selected = selected,
        onNavigate = { navController.navigate(it.route) }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.HOME.route) {
            composable(Screen.HOME.route) {
                val vm = viewModel<HomeViewModel>(factory = SimpleFactory { HomeViewModel(container) })
                HomeScreen(vm, padding)
            }
            composable(Screen.DETAIL.route) {
                val vm = viewModel<DetailViewModel>(factory = SimpleFactory { DetailViewModel(container) })
                DetailScreen(vm, padding)
            }
            composable(Screen.HISTORY.route) {
                val vm = viewModel<HistoryViewModel>(factory = SimpleFactory { HistoryViewModel(container) })
                HistoryScreen(vm, padding)
            }
            composable(Screen.SETTINGS.route) {
                val vm = viewModel<SettingsViewModel>(factory = SimpleFactory { SettingsViewModel(container) })
                SettingsScreen(vm, padding)
            }
        }
    }
}
