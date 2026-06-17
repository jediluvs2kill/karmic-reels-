package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.BrokerListing
import com.example.data.ChatSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    viewModel: DealPinViewModel,
    modifier: Modifier = Modifier
) {
    val listings by viewModel.allListings.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (listings.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Using modern VerticalPager for absolute frame snapping (native TikTok-like scrolling)
    val pagerState = rememberPagerState(pageCount = { listings.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val listing = listings[page]
            ReelPlayerItem(
                listing = listing,
                onLikeToggled = { viewModel.toggleLikeListing(listing) },
                onChatBroker = {
                    coroutineScope.launch {
                        // Create chat session representing interest in this property
                        viewModel.submitDealPin(
                            title = listing.title,
                            price = listing.priceDescription,
                            location = listing.locationName,
                            notes = "Interested in this property featured in Reels",
                            imagePath = "res_fallback_${listing.imageResName}"
                        )
                    }
                }
            )
        }

        // Overlay Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Reels",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(color = Color.Black, offset = Offset(0f, 4f), blurRadius = 8f)
                )
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return to welcome/onboarding logins
                IconButton(
                    onClick = { viewModel.logoutUser() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("logout_reels_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out & Switch Account",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.refreshAIMatches() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Swipe Up Discovery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ReelPlayerItem(
    listing: BrokerListing,
    onLikeToggled: () -> Unit,
    onChatBroker: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var showBigHeart by remember { mutableStateOf(false) }
    var heartOffset by remember { mutableStateOf(Offset.Zero) }

    // Color gradient overlay to make text highly legible
    val feedScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.4f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.8f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        heartOffset = tapOffset
                        showBigHeart = true
                        if (!listing.isLiked) {
                            onLikeToggled()
                        }
                    },
                    onTap = {
                        // Single tap pauses/resumes or shows details
                    }
                )
            }
    ) {
        // Fallback Gorgeous Procedural Canvas Visual background in case Coil doesn't resolve generated image yet
        val gradientBrush = remember(listing.id) {
            val colors = when (listing.id % 5) {
                0 -> listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)) // Night loft
                1 -> listOf(Color(0xFF14532D), Color(0xFF022C22)) // Forest redwood
                2 -> listOf(Color(0xFF701A75), Color(0xFF4A044E)) // Classic parisian
                3 -> listOf(Color(0xFF7C2D12), Color(0xFF431407)) // Industrial warehouse
                else -> listOf(Color(0xFF1E293B), Color(0xFF0F172A)) // Minimal organic
            }
            Brush.verticalGradient(colors)
        }

        val designPhotoUrl = remember(listing.title) {
            when (listing.title) {
                "DLF The Camellias" -> "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
                "M3M Golfestate" -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80"
                "Max Estates 128" -> "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80"
                "Godrej Connaught One" -> "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80"
                "Elan The Presidential" -> "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80"
                else -> {
                    if (listing.videoUrl.startsWith("http") && !listing.videoUrl.contains("instagram")) {
                        listing.videoUrl
                    } else {
                        "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80"
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            // Coil async image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(designPhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = listing.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Legibility Scrim Overlay
        Box(modifier = Modifier.fillMaxSize().background(feedScrim))

        // Right-Side Interaction Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Broker Profile Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onChatBroker() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = listing.brokerName.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Like Action
            ReelActionButton(
                icon = if (listing.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = listing.likesCount.toString(),
                iconColor = if (listing.isLiked) Color(0xFFFF2D55) else Color.White,
                onClick = onLikeToggled,
                testTag = "like_button_${listing.id}"
            )

            // Chat with Broker instantly
            ReelActionButton(
                icon = Icons.Outlined.Forum,
                label = "Chat",
                iconColor = Color.White,
                onClick = onChatBroker,
                testTag = "chat_broker_button_${listing.id}"
            )

            // Share listing
            ReelActionButton(
                icon = Icons.Outlined.Share,
                label = "Share",
                iconColor = Color.White,
                onClick = {}
            )
        }

        // Left-Side Text Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(bottom = 80.dp, start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Price Tag Pill
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = listing.priceDescription,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Title
            Text(
                text = listing.title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(color = Color.Black, offset = Offset(0f, 2f), blurRadius = 4f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = listing.locationName,
                    color = Color.White.copy(alpha = 0.9f),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        shadow = Shadow(color = Color.Black, offset = Offset(0f, 1f), blurRadius = 2f)
                    )
                )
            }

            // Description
            Text(
                text = listing.description,
                color = Color.White.copy(alpha = 0.8f),
                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    shadow = Shadow(color = Color.Black, offset = Offset(0f, 1f), blurRadius = 2f)
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listing.tagPreferences.split(",").take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }

            // Watch Reels on Instagram Active Button Redirection
            if (listing.videoUrl.isNotEmpty()) {
                Button(
                    onClick = {
                        try {
                            uriHandler.openUri(listing.videoUrl)
                        } catch (e: Exception) {
                            // Safe fallback
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE1306C) // Instagram Brand Pink-Red
                    ),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .testTag("watch_on_instagram_btn_" + listing.id),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Watch on Instagram",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WATCH ON INSTAGRAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Floating double tap red heart effect
        AnimatedVisibility(
            visible = showBigHeart,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                )
            }
            LaunchedEffect(showBigHeart) {
                if (showBigHeart) {
                    delay(800)
                    showBigHeart = false
                }
            }
        }
    }
}

@Composable
fun ReelActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        val modifierBase = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(8.dp)
            
        Box(
            modifier = if (testTag != null) modifierBase.testTag(testTag) else modifierBase,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
            style = TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(0f, 1f), blurRadius = 2f)
            )
        )
    }
}
