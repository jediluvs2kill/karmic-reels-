package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: DealPinViewModel) {
    var startAnim by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // Spring animation for logo scale and rotation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (startAnim) 360f else 0f,
        animationSpec = tween(
            durationMillis = 1800,
            easing = FastOutSlowInEasing
        ),
        label = "logoRotation"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 400),
        label = "contentAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        // Loading animation progress
        val steps = 40
        for (i in 1..steps) {
            delay(40)
            progress = i.toFloat() / steps
        }
        delay(300)

        // Route accordingly
        val currentEmail = viewModel.userEmail.value
        val currentRole = viewModel.userRole.value
        if (currentEmail != null) {
            if (currentRole == "Broker") {
                viewModel.navigateTo(AppScreen.BrokerDashboard)
            } else {
                viewModel.navigateTo(AppScreen.Reels)
            }
        } else {
            viewModel.navigateTo(AppScreen.Welcome)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4C2219), // Central glowing warm tint
                        Color(0xFF201A19)  // Deep slate background match
                    )
                )
            )
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Custom Logo Crest
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(
                        scaleX = logoScale,
                        scaleY = logoScale,
                        rotationZ = logoRotation
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFDBD1), Color(0xFF8F4C38))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Architectural crest overlay inside
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF201A19).copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = "WeBroker Premium Logo",
                        tint = Color(0xFFFFDBD1),
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Text Elements
            Text(
                text = "W E B R O K E R   R E E L S",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .testTag("splash_brand_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Delhi NCR Real Estate Excellence",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFFFDBD1).copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glowing Custom Progress Bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFDBD1), Color(0xFFE1306C))
                            )
                        )
                )
            }
        }

        // Tagline at the bottom
        Text(
            text = "PREMIUM PORTAL FOR ELITE LOCATIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}

@Composable
fun WelcomeScreen(viewModel: DealPinViewModel) {
    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Elite Client, 1 = Certified Broker

    // Input States
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientSector by remember { mutableStateOf("Gurugram Golf Course Rd") }

    var brokerEmail by remember { mutableStateOf("rajesh@webroker.com") }
    var brokerPassword by remember { mutableStateOf("broker123") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoggingIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val delhiSectors = listOf(
        "Gurugram Golf Course Rd",
        "GK II Private Floors",
        "Vasant Vihar Bungalows",
        "Noida Expressway High-rises",
        "Dwarka Skyline Estates"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // High-fidelity background illustration
        Image(
            painter = painterResource(id = R.drawable.img_login_hero),
            contentDescription = "Delhi Cybercity Lights Onboarding Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Custom rich dark gradient screen veil (Material 3 Bento aesthetic)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color(0xFF201A19).copy(alpha = 0.82f),
                            Color(0xFF201A19).copy(alpha = 0.98f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Scrollable input content sheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Upper mini branding logo badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFDBD1),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WeBroker Reels • Delhi NCR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand headlines
            Text(
                text = "Experience Properties\nLike Never Before",
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("welcome_heading")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Short video tours of elite developments, direct agent channels and smart matching.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom Segmented Control Tab
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                indicator = { Box(modifier = Modifier.background(Color.Transparent)) },
                divider = { Box(modifier = Modifier.background(Color.Transparent)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        loginError = null
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) Color(0xFF8F4C38) else Color.Transparent)
                        .testTag("tab_client_login"),
                    text = {
                        Text(
                            "Elite Client",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        loginError = null
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) Color(0xFF8F4C38) else Color.Transparent)
                        .testTag("tab_broker_login"),
                    text = {
                        Text(
                            "Certified Broker",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // High Fidelity Interactive Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_form_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF322320).copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, Color(0xFFFFDBD1).copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTab == 0) {
                        // CLIENT REGISTRATION FORM
                        Text(
                            text = "Step Into Luxury",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFDBD1)
                        )

                        // Client Name Input
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Your Dynamic Name", color = Color.White.copy(alpha = 0.6f)) },
                            placeholder = { Text("e.g. Shubham Malhotra", color = Color.White.copy(alpha = 0.3f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("client_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFDBD1),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFFFFDBD1)
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Client Email Input
                        OutlinedTextField(
                            value = clientEmail,
                            onValueChange = { clientEmail = it },
                            label = { Text("Secure Email Address", color = Color.White.copy(alpha = 0.6f)) },
                            placeholder = { Text("e.g. shubham@gmail.com", color = Color.White.copy(alpha = 0.3f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("client_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFDBD1),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFFFFDBD1)
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Location Preference Choice Header
                        Text(
                            text = "Target Sub-Region / Location Preference:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFDBD1).copy(alpha = 0.8f)
                        )

                        // Horizontal Choice Chip Row
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            delhiSectors.forEach { sector ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (clientSector == sector) Color(0xFF8F4C38).copy(alpha = 0.4f)
                                            else Color.White.copy(alpha = 0.04f)
                                        )
                                        .border(
                                            1.dp,
                                            if (clientSector == sector) Color(0xFFFFDBD1) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { clientSector = sector }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = clientSector == sector,
                                        onClick = { clientSector = sector },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFFFDBD1),
                                            unselectedColor = Color.White.copy(alpha = 0.5f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = sector,
                                        fontSize = 12.sp,
                                        color = if (clientSector == sector) Color.White else Color.White.copy(alpha = 0.7f),
                                        fontWeight = if (clientSector == sector) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                    } else {
                        // BROKER PARTNER SECURE SIGN-IN
                        Text(
                            text = "Professional Agent Console Access",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFDBD1)
                        )

                        // Email Field
                        OutlinedTextField(
                            value = brokerEmail,
                            onValueChange = { brokerEmail = it },
                            label = { Text("Certified Broker Email", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("broker_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFDBD1),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFFFFDBD1)
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Password Field
                        OutlinedTextField(
                            value = brokerPassword,
                            onValueChange = { brokerPassword = it },
                            label = { Text("Access PIN / Secret Password", color = Color.White.copy(alpha = 0.6f)) },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle PIN visibility",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("broker_pwd_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFDBD1),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFFFFDBD1)
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        
                        // Prefill instructions details
                        Text(
                            text = "Demo Key: Any secure credentials work! (e.g., mail: rajesh@webroker.com, pass: broker123)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }

                    // Render validation error alerts if any
                    loginError?.let { err ->
                        Text(
                            text = err,
                            color = Color(0xFFEF9A9A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Main execution login button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (selectedTab == 0) {
                                // Validate Client Name / Email
                                if (clientName.trim().isEmpty()) {
                                    loginError = "Please write your name to step inside."
                                    return@Button
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(clientEmail).matches()) {
                                    loginError = "Please enter a valid email address."
                                    return@Button
                                }
                                loginError = null
                                isLoggingIn = true
                            } else {
                                // Validate Broker Email / Password
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(brokerEmail).matches()) {
                                    loginError = "A valid certified broker email is required."
                                    return@Button
                                }
                                if (brokerPassword.length < 5) {
                                    loginError = "Partner access password must be 5+ characters."
                                    return@Button
                                }
                                loginError = null
                                isLoggingIn = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("primary_login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8F4C38)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoggingIn
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) "ENTER PRIVATE CLIENT PORTAL" else "AUTHORIZE BROKER ACCESS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Guest Entry Actions
            TextButton(
                onClick = {
                    isLoggingIn = true
                    loginError = null
                },
                modifier = Modifier.testTag("skip_guest_button"),
                enabled = !isLoggingIn
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Skip and enter instantly as Guest",
                        color = Color(0xFFFFDBD1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Skip Onboarding",
                        tint = Color(0xFFFFDBD1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Interactive animated loading sequence
    if (isLoggingIn) {
        LaunchedEffect(selectedTab) {
            delay(1200) // Simulating high-grade secure server handshakes
            isLoggingIn = false
            if (selectedTab == 0) {
                val emailOrFallback = clientEmail.ifEmpty { "guest_client@webroker.com" }
                val nameOrFallback = clientName.ifEmpty { "Elite Investor" }
                viewModel.loginUser(emailOrFallback, "Buyer")
                viewModel.showToastNotification("Bespoke welcome, $nameOrFallback!")
            } else {
                viewModel.loginUser(brokerEmail, "Broker")
                viewModel.showToastNotification("Certified Broker Console access granted!")
            }
        }
    }
}
