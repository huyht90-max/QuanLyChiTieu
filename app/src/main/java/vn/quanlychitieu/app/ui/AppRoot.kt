package vn.quanlychitieu.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import vn.quanlychitieu.app.MainViewModel
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.screens.BudgetDialog
import vn.quanlychitieu.app.ui.screens.EditTransactionScreen
import vn.quanlychitieu.app.ui.screens.HomeScreen
import vn.quanlychitieu.app.ui.screens.SettingsScreen
import vn.quanlychitieu.app.ui.screens.StatsScreen
import vn.quanlychitieu.app.ui.screens.TransactionsScreen
import vn.quanlychitieu.app.ui.theme.AppTheme
import vn.quanlychitieu.app.ui.theme.QuanLyChiTieuTheme
import java.time.LocalDate

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab("home", "Tổng quan", Icons.Outlined.Home, Icons.Rounded.Home),
    Tab("transactions", "Giao dịch", Icons.AutoMirrored.Rounded.ReceiptLong, Icons.AutoMirrored.Rounded.ReceiptLong),
    Tab("stats", "Thống kê", Icons.Outlined.PieChart, Icons.Rounded.PieChart),
    Tab("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Rounded.Settings),
)

private fun NavHostController.openTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AppRoot(vm: MainViewModel) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    QuanLyChiTieuTheme(settings.themeMode) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route
        val onTab = tabs.any { it.route == route }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var budgetDialog by remember { mutableStateOf(false) }

        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) vm.exportCsv(uri) { ok ->
                Toast.makeText(
                    context,
                    if (ok) "Đã xuất file CSV thành công" else "Xuất file thất bại",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        fun deleteWithUndo(tx: Transaction) {
            vm.delete(tx.id)
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                val r = snackbar.showSnackbar(
                    "Đã xóa giao dịch ${Fmt.money(tx.amount)}",
                    actionLabel = "Hoàn tác",
                    duration = SnackbarDuration.Short,
                )
                if (r == SnackbarResult.ActionPerformed) vm.save(tx.copy(id = 0))
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                AnimatedVisibility(
                    onTab,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    NavigationBar(containerColor = AppTheme.finance.card, tonalElevation = 0.dp) {
                        tabs.forEach { tab ->
                            val selected = route == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { nav.openTab(tab.route) },
                                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    route == "home" || route == "transactions",
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    FloatingActionButton(
                        onClick = { nav.navigate("edit?id=0&type=${TxType.EXPENSE.name}") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(20.dp),
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    ) {
                        Icon(Icons.Rounded.Add, "Thêm giao dịch")
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(180)) },
            ) {
                composable("home") {
                    HomeScreen(
                        transactions = transactions,
                        settings = settings,
                        contentPadding = padding,
                        onToggleHide = { vm.updateSettings { it.copy(hideAmounts = !it.hideAmounts) } },
                        onOpen = { nav.navigate("edit?id=$it") },
                        onSeeAll = { nav.openTab("transactions") },
                        onSetBudget = { budgetDialog = true },
                        onOpenStats = { nav.openTab("stats") },
                    )
                }
                composable("transactions") {
                    TransactionsScreen(
                        transactions = transactions,
                        settings = settings,
                        contentPadding = padding,
                        onOpen = { nav.navigate("edit?id=$it") },
                        onDelete = ::deleteWithUndo,
                    )
                }
                composable("stats") {
                    StatsScreen(transactions, settings, padding)
                }
                composable("settings") {
                    SettingsScreen(
                        settings = settings,
                        transactionCount = transactions.size,
                        contentPadding = padding,
                        onUpdate = { vm.updateSettings(it) },
                        onEditBudget = { budgetDialog = true },
                        onExport = { exportLauncher.launch("chi-tieu-${LocalDate.now()}.csv") },
                        onDeleteAll = { vm.deleteAll() },
                    )
                }
                composable(
                    "edit?id={id}&type={type}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                        navArgument("type") { type = NavType.StringType; defaultValue = TxType.EXPENSE.name },
                    ),
                    enterTransition = { slideInVertically(tween(320)) { it / 3 } + fadeIn(tween(320)) },
                    exitTransition = { slideOutVertically(tween(260)) { it / 3 } + fadeOut(tween(260)) },
                    popExitTransition = { slideOutVertically(tween(260)) { it / 3 } + fadeOut(tween(260)) },
                ) { entry ->
                    val id = entry.arguments?.getLong("id") ?: 0L
                    val type = runCatching { TxType.valueOf(entry.arguments?.getString("type")!!) }
                        .getOrDefault(TxType.EXPENSE)
                    val existing = remember(id) { transactions.firstOrNull { it.id == id } }
                    Box(Modifier.fillMaxSize()) {
                        EditTransactionScreen(
                            existing = existing,
                            initialType = type,
                            onSave = {
                                vm.save(it)
                                nav.popBackStack()
                            },
                            onDelete = {
                                nav.popBackStack()
                                deleteWithUndo(it)
                            },
                            onBack = { nav.popBackStack() },
                        )
                    }
                }
            }
        }

        if (budgetDialog) {
            BudgetDialog(
                current = settings.monthlyBudget,
                onDismiss = { budgetDialog = false },
                onConfirm = { v ->
                    vm.updateSettings { it.copy(monthlyBudget = v) }
                    budgetDialog = false
                },
            )
        }
    }
}
