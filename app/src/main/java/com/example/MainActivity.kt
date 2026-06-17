package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.PropertyRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Enable fully responsive edge-to-edge drawing
        enableEdgeToEdge()

        // 2. Instantiate Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PropertyRepository(database.listingsDao(), applicationContext)

        // 3. Instantiate ViewModel via Factory
        val factory = DealPinViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[DealPinViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainScreenContainer(viewModel)
            }
        }
    }
}

@Composable
fun MainScreenContainer(viewModel: DealPinViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notificationState by viewModel.notification.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    Scaffold(
        bottomBar = {
            // Standard NavigationBar only visible on primary views, hiding in single chat screen details or onboarding splash/welcome screens
            if (currentScreen !is AppScreen.ActiveChat && currentScreen !is AppScreen.Splash && currentScreen !is AppScreen.Welcome) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar"),
                    tonalElevation = 8.dp
                ) {
                    if (userRole == "Broker") {
                        // Broker Stakeholder Portal
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Chats || currentScreen is AppScreen.ActiveChat,
                            onClick = { viewModel.navigateTo(AppScreen.Chats) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.Chats) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Contacted Clients"
                                )
                            },
                            label = { Text("Client Chats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_chats")
                        )

                        NavigationBarItem(
                            selected = currentScreen is AppScreen.BrokerDashboard,
                            onClick = { viewModel.navigateTo(AppScreen.BrokerDashboard) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.BrokerDashboard) Icons.Filled.Business else Icons.Outlined.Business,
                                    contentDescription = "Broker Partner Console"
                                )
                            },
                            label = { Text("My Desk", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_broker")
                        )
                    } else {
                        // Elite Client/Buyer/Guest Stakeholder Portal
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Reels,
                            onClick = { viewModel.navigateTo(AppScreen.Reels) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.Reels) Icons.Filled.Explore else Icons.Outlined.Explore,
                                    contentDescription = "Featured Listings Reels"
                                )
                            },
                            label = { Text("Reels", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_reels")
                        )

                        NavigationBarItem(
                            selected = currentScreen is AppScreen.CameraPin,
                            onClick = { viewModel.navigateTo(AppScreen.CameraPin) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.CameraPin) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                                    contentDescription = "Pin Visual Street Deal"
                                )
                            },
                            label = { Text("Pin Deal", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_camera")
                        )

                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Chats || currentScreen is AppScreen.ActiveChat,
                            onClick = { viewModel.navigateTo(AppScreen.Chats) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.Chats) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Broker Chat Messages"
                                )
                            },
                            label = { Text("Chats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_chats")
                        )

                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Matches,
                            onClick = { viewModel.navigateTo(AppScreen.Matches) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.Matches) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                    contentDescription = "AI Property Concierge Suggestions"
                                )
                            },
                            label = { Text("AI Match", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_ai_match")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (currentScreen is AppScreen.ActiveChat || currentScreen is AppScreen.Splash || currentScreen is AppScreen.Welcome) 
                        0.dp 
                    else 
                        innerPadding.calculateBottomPadding()
                )
        ) {
            // Render active screen state layout
            when (currentScreen) {
                is AppScreen.Splash -> SplashScreen(viewModel = viewModel)
                is AppScreen.Welcome -> WelcomeScreen(viewModel = viewModel)
                is AppScreen.Reels -> ReelsScreen(viewModel = viewModel)
                is AppScreen.CameraPin -> CameraPinScreen(viewModel = viewModel)
                is AppScreen.Chats, is AppScreen.ActiveChat -> ChatsScreen(viewModel = viewModel)
                is AppScreen.Matches -> MatchesScreen(viewModel = viewModel)
                is AppScreen.BrokerDashboard -> BrokerDashboardScreen(viewModel = viewModel)
            }

            // Real-Time Agent Float-In Notification Overlay Popup
            AnimatedVisibility(
                visible = notificationState != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                notificationState?.let { text ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("instant_notification_popup")
                            .clickable { viewModel.navigateTo(AppScreen.Chats) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Agent Sparkle Indicator Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Broker Instant Response",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Dismiss control
                            IconButton(
                                onClick = { viewModel.dismissNotification() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Alert",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
