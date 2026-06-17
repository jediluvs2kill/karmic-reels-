package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BrokerListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerDashboardScreen(viewModel: DealPinViewModel) {
    val brokerName by viewModel.brokerName.collectAsState()
    val brokerAgency by viewModel.brokerAgency.collectAsState()
    val brokerPhone by viewModel.brokerPhone.collectAsState()
    val brokerBio by viewModel.brokerBio.collectAsState()
    val brokerZone by viewModel.brokerZone.collectAsState()
    val brokerAvatar by viewModel.brokerAvatar.collectAsState()

    val listings by viewModel.allListings.collectAsState()
    
    // Filter listings posted by this active broker
    val myListings = listings.filter { 
        it.brokerName.trim().lowercase() == brokerName.trim().lowercase()
    }

    var isEditingProfile by remember { mutableStateOf(false) }

    // Forms temp state representing Profile Edit
    var editName by remember { mutableStateOf("") }
    var editAgency by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editZone by remember { mutableStateOf("") }

    // Forms temp state representing New Listing Upload
    var listTitle by remember { mutableStateOf("") }
    var listPrice by remember { mutableStateOf("") }
    var listLocation by remember { mutableStateOf("") }
    var listDescription by remember { mutableStateOf("") }
    var listTags by remember { mutableStateOf("") }
    var listImage by remember { mutableStateOf("property_loft") } // Default

    // Initialize editing forms
    LaunchedEffect(isEditingProfile) {
        if (isEditingProfile) {
            editName = brokerName
            editAgency = brokerAgency
            editPhone = brokerPhone
            editBio = brokerBio
            editZone = brokerZone
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WeBroker Reels",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Delhi NCR Partner Desk",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Reels) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Reels"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isEditingProfile = !isEditingProfile },
                        modifier = Modifier.testTag("toggle_profile_edit_btn")
                    ) {
                        Icon(
                            imageVector = if (isEditingProfile) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.logoutUser() },
                        modifier = Modifier.testTag("logout_broker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Sign Out & Return Home",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("broker_dashboard_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Integrated Profile Sheet editor OR Display state in a Bento Card
            item {
                AnimatedVisibility(
                    visible = isEditingProfile,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_form_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "EDIT BROKER PROFILE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Broker Name", color = Color.White.copy(alpha = 0.6f)) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFFFFDBD1)) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = editAgency,
                                onValueChange = { editAgency = it },
                                label = { Text("Agency / Company Name", color = Color.White.copy(alpha = 0.6f)) },
                                leadingIcon = { Icon(Icons.Filled.Business, null, tint = Color(0xFFFFDBD1)) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_agency"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Contact Phone", color = Color.White.copy(alpha = 0.6f)) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = Color(0xFFFFDBD1)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = editZone,
                                onValueChange = { editZone = it },
                                label = { Text("Delhi Operations Zone", color = Color.White.copy(alpha = 0.6f)) },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = Color(0xFFFFDBD1)) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_zone"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                label = { Text("Professional Advisory Biography", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(80.dp).testTag("edit_profile_bio"),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            Button(
                                onClick = {
                                    if (editName.isNotBlank() && editAgency.isNotBlank()) {
                                        viewModel.updateBrokerProfile(
                                            name = editName,
                                            agency = editAgency,
                                            phone = editPhone,
                                            bio = editBio,
                                            zone = editZone,
                                            avatar = brokerAvatar
                                        )
                                        isEditingProfile = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Check, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SAVE PROFILE CHANGES", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Display Broker Profile Card (Bento Card A: Rust Warm Background)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broker_info_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("broker_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Broker Avatar",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DELHI EXPERT PARTNER",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = brokerName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    text = brokerAgency,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Professional specialties & metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Zone",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = brokerZone,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Contact",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = brokerPhone,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = brokerBio,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Bento Card B: Create Property Listing Form (Sleek Dark Premium Box with borders)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_property_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF322320).copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFDBD1).copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "List New Property",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFDBD1)
                            )
                            Icon(
                                imageVector = Icons.Filled.AddHome,
                                contentDescription = null,
                                tint = Color(0xFFFFDBD1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = listTitle,
                                onValueChange = { listTitle = it },
                                label = { Text("Property Title (e.g. GK Luxury Flat)", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().testTag("input_list_title"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = listPrice,
                                    onValueChange = { listPrice = it },
                                    label = { Text("Price Description (e.g. ₹6.5 Cr)", color = Color.White.copy(alpha = 0.6f)) },
                                    modifier = Modifier.weight(1f).testTag("input_list_price"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        focusedBorderColor = Color(0xFFFFDBD1),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedLabelColor = Color(0xFFFFDBD1),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )

                                OutlinedTextField(
                                    value = listLocation,
                                    onValueChange = { listLocation = it },
                                    label = { Text("Location (Delhi Area)", color = Color.White.copy(alpha = 0.6f)) },
                                    modifier = Modifier.weight(1f).testTag("input_list_location"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        focusedBorderColor = Color(0xFFFFDBD1),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedLabelColor = Color(0xFFFFDBD1),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            OutlinedTextField(
                                value = listTags,
                                onValueChange = { listTags = it },
                                label = { Text("Tags (comma separated: e.g. luxury, park)", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().testTag("input_list_tags"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = listDescription,
                                onValueChange = { listDescription = it },
                                label = { Text("Full Property Description & Specifications", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(80.dp).testTag("input_list_description"),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFFFFDBD1),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFFFDBD1),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            // Select asset mockup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mock Visual Asset:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                listOf(
                                    "property_loft" to "Loft",
                                    "property_minimal" to "Modern",
                                    "property_redwood" to "Villa",
                                    "property_classic" to "Heritage"
                                ).forEach { (resValue, titleLabel) ->
                                    val isSelected = listImage == resValue
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { listImage = resValue }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = titleLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (listTitle.isNotBlank() && listPrice.isNotBlank() && listLocation.isNotBlank() && listDescription.isNotBlank()) {
                                        viewModel.addNewBrokerListing(
                                            title = listTitle,
                                            priceDescription = listPrice,
                                            locationName = listLocation,
                                            description = listDescription,
                                            tags = listTags,
                                            imageName = listImage
                                        )
                                        // Reset fields
                                        listTitle = ""
                                        listPrice = ""
                                        listLocation = ""
                                        listDescription = ""
                                        listTags = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("submit_property_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Publish, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PUBLISH REAL ESTATE LISTING", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Header for My Listings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MY ACTIVE PORTFOLIO (${myListings.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (myListings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No property listed yet under your name.\nUse 'List New Property' form above.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                items(myListings, key = { it.id }) { listing ->
                    BentoActiveListingItemCard(
                        listing = listing,
                        onDeleteClick = { viewModel.deleteBrokerListing(listing) }
                    )
                }
            }
        }
    }
}

@Composable
fun BentoActiveListingItemCard(
    listing: BrokerListing,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_listing_${listing.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HomeWork,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = listing.locationName,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = listing.priceDescription,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${listing.likesCount} Likes",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .testTag("delete_property_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Property",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
