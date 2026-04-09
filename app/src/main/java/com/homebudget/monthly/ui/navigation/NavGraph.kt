package com.homebudget.monthly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.screens.bills.AddEditBillScreen
import com.homebudget.monthly.ui.screens.bills.BillsScreen
import com.homebudget.monthly.ui.screens.dashboard.DashboardScreen
import com.homebudget.monthly.ui.screens.expense.AddEditExpenseScreen
import com.homebudget.monthly.ui.screens.expense.ExpenseScreen
import com.homebudget.monthly.ui.screens.income.AddEditIncomeScreen
import com.homebudget.monthly.ui.screens.income.IncomeScreen
import com.homebudget.monthly.ui.screens.reports.ReportsScreen
import com.homebudget.monthly.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Expenses : Screen("expenses")
    object AddExpense : Screen("add_expense?id={id}") {
        fun createRoute(id: Long = -1L) = "add_expense?id=$id"
    }
    object Incomes : Screen("incomes")
    object AddIncome : Screen("add_income?id={id}") {
        fun createRoute(id: Long = -1L) = "add_income?id=$id"
    }
    object Bills : Screen("bills")
    object AddBill : Screen("add_bill?id={id}") {
        fun createRoute(id: Long = -1L) = "add_bill?id=$id"
    }
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: BudgetRepository,
    onDrawerOpen: () -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                repository = repository,
                onNavigate = { navController.navigate(it) },
                onDrawerOpen = onDrawerOpen
            )
        }
        composable(Screen.Expenses.route) {
            ExpenseScreen(
                repository = repository,
                onAddExpense = { navController.navigate(Screen.AddExpense.createRoute()) },
                onEditExpense = { navController.navigate(Screen.AddExpense.createRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            AddEditExpenseScreen(
                repository = repository,
                expenseId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Incomes.route) {
            IncomeScreen(
                repository = repository,
                onAddIncome = { navController.navigate(Screen.AddIncome.createRoute()) },
                onEditIncome = { navController.navigate(Screen.AddIncome.createRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddIncome.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            AddEditIncomeScreen(
                repository = repository,
                incomeId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bills.route) {
            BillsScreen(
                repository = repository,
                onAddBill = { navController.navigate(Screen.AddBill.createRoute()) },
                onEditBill = { navController.navigate(Screen.AddBill.createRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddBill.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            AddEditBillScreen(
                repository = repository,
                billId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
