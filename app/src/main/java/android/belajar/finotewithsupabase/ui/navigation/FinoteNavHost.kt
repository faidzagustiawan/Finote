package android.belajar.finotewithsupabase.ui.navigation

import android.belajar.finotewithsupabase.ui.screen.auth.LoginScreen
import android.belajar.finotewithsupabase.ui.screen.goal.GoalDetailScreen
import android.belajar.finotewithsupabase.ui.screen.goal.GoalPage
import android.belajar.finotewithsupabase.data.remote.SupabaseClient
import android.belajar.finotewithsupabase.ui.screen.home.HomePage
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import io.github.jan.supabase.auth.auth

@Composable
fun FinoteApp() {
    val navController = rememberNavController()
    val session = SupabaseClient.client.auth.currentSessionOrNull()
    val startDestination = if (session != null) "goal" else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tampilkan BottomBar hanya di halaman utama
    val showBottomBar = currentRoute in listOf("home","goal", "history", "insight")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("goal") { popUpTo("login") { inclusive = true } }
                })
            }

            composable("home") {
                HomePage(
                    onNavigateTo = { route -> navController.navigate(route) }
                )
            }

            composable("goal") {
                GoalPage(navController = navController, onGoalClick = { id -> navController.navigate("goal_detail/$id") })
            }

            composable("history") {
                // Placeholder
            }

            composable("insight") {
                // Placeholder
            }

            composable(
                route = "goal_detail/{goalId}",
                arguments = listOf(navArgument("goalId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("goalId") ?: ""
                GoalDetailScreen(goalId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}