package com.homebudget.monthly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homebudget.monthly.ui.navigation.Screen
import com.homebudget.monthly.ui.theme.*

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val drawerItems = listOf(
        DrawerItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard.route),
        DrawerItem("Expenses", Icons.Default.Receipt, Screen.Expenses.route),
        DrawerItem("Income", Icons.Default.AccountBalance, Screen.Incomes.route),
        DrawerItem("Bills", Icons.Default.CreditCard, Screen.Bills.route),
        DrawerItem("Reports", Icons.Default.BarChart, Screen.Reports.route),
        DrawerItem("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardGradientStart, CardGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💰", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "HomeBudget",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Monthly Budget Tracker",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        drawerItems.forEach { item ->
            val isSelected = currentRoute == item.route
            DrawerItemRow(
                item = item,
                isSelected = isSelected,
                onClick = {
                    onNavigate(item.route)
                    onClose()
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider(modifier = Modifier.padding(horizontal = 24.dp))

        // App version
        Text(
            "HomeBudget Monthly v1.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DrawerItemRow(
    item: DrawerItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        Color.Transparent

    val iconColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
        if (item.badgeCount > 0) {
            Spacer(modifier = Modifier.weight(1f))
            Badge { Text("${item.badgeCount}") }
        }
    }
}
