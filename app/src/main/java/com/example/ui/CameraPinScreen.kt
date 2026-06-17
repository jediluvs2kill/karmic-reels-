package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPinScreen(
    viewModel: DealPinViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isCaptured by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisText by remember { mutableStateOf("") }

    // Inputs
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Floating laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserLine"
    )

    // Flash toggle simulation
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1216))
            .testTag("camera_pin_screen")
    ) {
        if (!isCaptured) {
            // VIEWPORT VIEWFINDER VIEW
            Box(modifier = Modifier.fillMaxSize()) {
                // Procedural Grid drawing on Canvas representing Camera viewfinder
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw stylized abstract grid layout
                    val gridAlpha = 0.15f
                    val columns = 5
                    val rows = 8
                    for (i in 1 until columns) {
                        val x = width * (i.toFloat() / columns)
                        drawLine(
                            color = Color.White,
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1f,
                            alpha = gridAlpha
                        )
                    }
                    for (i in 1 until rows) {
                        val y = height * (i.toFloat() / rows)
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            alpha = gridAlpha
                        )
                    }

                    // 2. Draw mock building vector on Canvas for an extremely satisfying graphic look
                    val buildingColor = Color(0xFF263238)
                    val windowColor = Color(0xFFFFEB3B)
                    
                    // Simple landscape skyline shapes
                    drawRect(
                        color = buildingColor,
                        topLeft = Offset(width * 0.15f, height * 0.4f),
                        size = androidx.compose.ui.geometry.Size(width * 0.3f, height * 0.6f)
                    )
                    drawRect(
                        color = Color(0xFF1E272C),
                        topLeft = Offset(width * 0.4f, height * 0.3f),
                        size = androidx.compose.ui.geometry.Size(width * 0.45f, height * 0.7f)
                    )

                    // Windows
                    for (wx in listOf(0.2f, 0.28f)) {
                        for (wy in listOf(0.45f, 0.55f, 0.65f)) {
                            drawCircle(
                                color = windowColor,
                                radius = 6f,
                                center = Offset(width * wx, height * wy),
                                alpha = 0.6f
                            )
                        }
                    }
                    for (wx in listOf(0.48f, 0.58f, 0.68f, 0.78f)) {
                        for (wy in listOf(0.35f, 0.45f, 0.55f, 0.65f, 0.75f)) {
                            drawRect(
                                color = windowColor,
                                topLeft = Offset(width * wx - 10f, height * wy - 10f),
                                size = androidx.compose.ui.geometry.Size(20f, 20f),
                                alpha = 0.8f
                            )
                        }
                    }

                    // 3. Draw viewfinder corner brackets
                    val bracketLen = 40.dp.toPx()
                    val margin = 32.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    val bColor = Color.Green

                    // Top Left
                    drawLine(bColor, Offset(margin, margin), Offset(margin + bracketLen, margin), strokeW)
                    drawLine(bColor, Offset(margin, margin), Offset(margin, margin + bracketLen), strokeW)

                    // Top Right
                    drawLine(bColor, Offset(width - margin, margin), Offset(width - margin - bracketLen, margin), strokeW)
                    drawLine(bColor, Offset(width - margin, margin), Offset(width - margin, margin + bracketLen), strokeW)

                    // Bottom Left
                    drawLine(bColor, Offset(margin, height - margin), Offset(margin + bracketLen, height - margin), strokeW)
                    drawLine(bColor, Offset(margin, height - margin), Offset(margin, height - margin - bracketLen), strokeW)

                    // Bottom Right
                    drawLine(bColor, Offset(width - margin, height - margin), Offset(width - margin - bracketLen, height - margin), strokeW)
                    drawLine(bColor, Offset(width - margin, height - margin), Offset(width - margin, height - margin - bracketLen), strokeW)

                    // 4. Draw moving green scanner line laser
                    val laserValY = height * laserYOffset
                    drawLine(
                        color = Color.Green,
                        start = Offset(margin, laserValY),
                        end = Offset(width - margin, laserValY),
                        strokeWidth = 3.dp.toPx(),
                        alpha = 0.8f
                    )
                }

                if (isFlashOn) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.4f))) {
                        LaunchedEffect(Unit) {
                            delay(100)
                            isFlashOn = false
                        }
                    }
                }

                // Shutter Camera control HUD
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Point at property and tap Shutter",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Simulate Flash",
                                tint = if (isFlashOn) Color.Yellow else Color.White
                            )
                        }

                        // Capture Shutter Trigger
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .border(3.dp, Color.White, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    isCaptured = true
                                    isAnalyzing = true
                                    coroutineScope.launch {
                                        analysisText = "Scanning façade details..."
                                        delay(800)
                                        analysisText = "Analyzing classic bricks & frame structures..."
                                        delay(1000)
                                        analysisText = "Matching coordinate database..."
                                        delay(600)
                                        analysisText = "Ready! Building parsed: 5-story brick flat near location grid."
                                        isAnalyzing = false
                                        // Set preloaded pre-populated outputs to help the buyer!
                                        title = "Colonial Heritage Bungalow"
                                        price = "₹12.5 Cr"
                                        locationName = "Connaught Place, Delhi"
                                    }
                                }
                                .testTag("shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capturing Button",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                // Simulate rotation or auto detection toggling
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Simulate Rotator",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // SNAP DETAILS SHEET (Buyer sets Pin Data details)
            Scaffold(
                topBar = {
                    OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { Text("Pin Estate Deal", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { isCaptured = false }) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Small thumbnail of Captured simulated drawing
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PINNED PHOTOGRAPH DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Miniature drawing representation
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Custom Visual Capture",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ID: PIN_LOCKED_${System.currentTimeMillis() % 10000}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            // AI Vision diagnostics feed
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = analysisText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Form Fields
                    Text(
                        text = "DEAL SPECIFICATIONS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Deal Title (or building name)") },
                        modifier = Modifier.fillMaxWidth().testTag("deal_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Estimated Offer/Value") },
                            modifier = Modifier.weight(1f).testTag("deal_price_input"),
                            placeholder = { Text("₹ Price") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = locationName,
                            onValueChange = { locationName = it },
                            label = { Text("Local Neighborhood") },
                            modifier = Modifier.weight(1.2f).testTag("deal_location_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    locationName = "Greater Kailash II, Delhi"
                                }) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = "GPS Lock")
                                }
                            }
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Pin Status Notes (What are you looking for?)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("deal_notes_input"),
                        placeholder = { Text("e.g. Seeking contact with Rajesh Malhotra, looking to buy premium floor in GK...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Pinning this custom deal notifies associated real estate brokers in the area instantly. They will receive coordinates, photo analytics, and will initiate an instant message conversation with you.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PIN & NOTIFY ACTION
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && price.isNotEmpty()) {
                                viewModel.submitDealPin(
                                    title = title,
                                    price = price,
                                    location = locationName.ifEmpty { "Pinned Coordinates" },
                                    notes = notes.ifEmpty { "Interested in securing this specific deal." },
                                    imagePath = "res_camera_capture_${System.currentTimeMillis()}"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pin_deal_button"),
                        enabled = title.isNotEmpty() && price.isNotEmpty() && !isAnalyzing,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PIN DEAL & START AGENT CHAT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
