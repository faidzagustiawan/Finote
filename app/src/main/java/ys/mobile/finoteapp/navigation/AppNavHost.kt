package ys.mobile.finoteapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ys.mobile.finoteapp.ui.camera.CameraScreen
import ys.mobile.finoteapp.ui.home.HomeScreen
import ys.mobile.finoteapp.ui.common.PlaceholderScreen
import ys.mobile.finoteapp.ui.transaction.AddTransactionScreen
import ys.mobile.finoteapp.ui.transaction.TransactionListScreen
import ys.mobile.finoteapp.viewmodel.TransactionListViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object TransactionListNav : Screen("transaction_list_nav", "Riwayat", Icons.Filled.History)
    data object Insight : Screen("insight", "Insight", Icons.Filled.Assessment)
    data object GoalTracker : Screen("goal_tracker", "Goal", Icons.Filled.Flag)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    data object AddTransaction : Screen("add_transaction", "Add Transaction", Icons.Filled.Home)
    data object CameraPlaceholder : Screen("camera_placeholder", "Camera", Icons.Filled.Home)
    data object TransactionList : Screen("transaction_list", "Transaction List", Icons.Filled.Home)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.TransactionListNav,
    Screen.Insight,
    Screen.GoalTracker
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    transactionViewModel: TransactionListViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        // HOME SCREEN (pakai shared ViewModel)
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = transactionViewModel,
                onAddTransactionClick = { navController.navigate(Screen.AddTransaction.route) },
                onHistoryClick = { navController.navigate(Screen.TransactionList.route) }
            )
        }

        // TAB Riwayat (BOTTOM NAVBAR)
        composable(Screen.TransactionListNav.route) {
            TransactionListScreen(
                viewModel = transactionViewModel,
                onBack = { /* No need back for bottom nav */ },
                onNavigateToEdit = { id ->
                    navController.navigate("transaction/edit/$id")
                }
            )
        }

        // INSIGHT
        composable(Screen.Insight.route) {
            PlaceholderScreen(title = "Insight")
        }

        // GOAL
        composable(Screen.GoalTracker.route) {
            PlaceholderScreen(title = "Goal Tracker")
        }

        // PROFILE
        composable(Screen.Profile.route) {
            PlaceholderScreen(title = "Profile")
        }

        // ADD TRANSACTION
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                navController = navController,
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() },
                onScanReceipt = { navController.navigate(Screen.CameraPlaceholder.route) }
            )
        }

        // CAMERA / RECEIPT SCAN
        composable(Screen.CameraPlaceholder.route) {
            CameraScreen(
                onResult = { amount, isIncome ->
                    // Set result to previous back stack (AddTransaction)
                    navController.previousBackStackEntry?.savedStateHandle?.set("ocr_amount", amount)
                    navController.previousBackStackEntry?.savedStateHandle?.set("ocr_is_income", isIncome)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // HISTORY FROM HOMESCREEN BUTTON
        composable(Screen.TransactionList.route) {
            TransactionListScreen(
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("transaction/edit/$id")
                }
            )
        }

        // EDIT TRANSACTION
        composable(
            route = "transaction/edit/{transactionId}",
            arguments = listOf(androidx.navigation.navArgument("transactionId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            AddTransactionScreen(
                navController = navController,
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() },
                onScanReceipt = { navController.navigate(Screen.CameraPlaceholder.route) },
                transactionId = transactionId
            )
        }
    }
}