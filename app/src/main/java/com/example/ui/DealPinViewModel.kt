package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.ai.PropertyMatch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Splash : AppScreen()
    object Welcome : AppScreen()
    object Reels : AppScreen()
    object CameraPin : AppScreen()
    object Chats : AppScreen()
    object Matches : AppScreen()
    data class ActiveChat(val session: ChatSession) : AppScreen()
    object BrokerDashboard : AppScreen()
}

class DealPinViewModel(private val repository: PropertyRepository) : ViewModel() {

    companion object {
        private const val TAG = "DealPinViewModel"
    }

    // Broker Profile State Management
    private val _brokerName = MutableStateFlow(repository.getBrokerProfileName())
    val brokerName: StateFlow<String> = _brokerName.asStateFlow()

    private val _brokerAgency = MutableStateFlow(repository.getBrokerProfileAgency())
    val brokerAgency: StateFlow<String> = _brokerAgency.asStateFlow()

    private val _brokerPhone = MutableStateFlow(repository.getBrokerProfilePhone())
    val brokerPhone: StateFlow<String> = _brokerPhone.asStateFlow()

    private val _brokerBio = MutableStateFlow(repository.getBrokerProfileBio())
    val brokerBio: StateFlow<String> = _brokerBio.asStateFlow()

    private val _brokerZone = MutableStateFlow(repository.getBrokerProfileZone())
    val brokerZone: StateFlow<String> = _brokerZone.asStateFlow()

    private val _brokerAvatar = MutableStateFlow(repository.getBrokerProfileAvatar())
    val brokerAvatar: StateFlow<String> = _brokerAvatar.asStateFlow()

    fun updateBrokerProfile(name: String, agency: String, phone: String, bio: String, zone: String, avatar: String) {
        repository.saveBrokerProfile(name, agency, phone, bio, zone, avatar)
        _brokerName.value = name
        _brokerAgency.value = agency
        _brokerPhone.value = phone
        _brokerBio.value = bio
        _brokerZone.value = zone
        _brokerAvatar.value = avatar
        showToastNotification("Broker Profile updated successfully")
    }

    fun addNewBrokerListing(
        title: String,
        priceDescription: String,
        locationName: String,
        description: String,
        tags: String,
        imageName: String
    ) {
        viewModelScope.launch {
            try {
                _isMatchingLoading.value = true
                val newListing = BrokerListing(
                    title = title,
                    priceDescription = priceDescription,
                    brokerName = _brokerName.value,
                    brokerAvatarUrl = _brokerAvatar.value,
                    locationName = locationName,
                    description = description,
                    imageResName = imageName.ifEmpty { "property_loft" },
                    videoUrl = "",
                    likesCount = 0,
                    commentsCount = 0,
                    isLiked = false,
                    tagPreferences = tags
                )
                repository.insertNewListing(newListing)
                showToastNotification("Property '$title' listed in India, Delhi!")
                _isMatchingLoading.value = false
                refreshAIMatches()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding broker property", e)
                _isMatchingLoading.value = false
            }
        }
    }

    fun deleteBrokerListing(listing: BrokerListing) {
        viewModelScope.launch {
            try {
                repository.deleteListingDirect(listing)
                showToastNotification("Property '${listing.title}' unlisted from WeBroker Reels.")
                refreshAIMatches()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting property", e)
            }
        }
    }

    // Modern M3 Edge-to-Edge Navigation state
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Persistent User Session Management
    private val _userEmail = MutableStateFlow(repository.getUserLoginEmail())
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userRole = MutableStateFlow(repository.getUserLoginRole())
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    fun loginUser(email: String, role: String) {
        repository.saveUserLogin(email, role)
        _userEmail.value = email
        _userRole.value = role
        if (role == "Broker") {
            navigateTo(AppScreen.BrokerDashboard)
        } else {
            navigateTo(AppScreen.Reels)
        }
    }

    fun logoutUser() {
        repository.clearUserLogin()
        _userEmail.value = null
        _userRole.value = "Buyer"
        navigateTo(AppScreen.Welcome)
    }

    // Loaded flow states (observing database directly for real-time reactivity)
    val allListings: StateFlow<List<BrokerListing>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDealPins: StateFlow<List<DealPin>> = repository.allDealPins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChatSessions: StateFlow<List<ChatSession>> = repository.allChatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // active chat setup
    private val _selectedSession = MutableStateFlow<ChatSession?>(null)
    val selectedSession: StateFlow<ChatSession?> = _selectedSession.asStateFlow()

    val sessionMessages: StateFlow<List<ChatMessage>> = _selectedSession
        .flatMapLatest { session ->
            if (session != null) {
                repository.getMessagesForSession(session.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI suggestions state
    private val _aiMatches = MutableStateFlow<List<PropertyMatch>>(emptyList())
    val aiMatches: StateFlow<List<PropertyMatch>> = _aiMatches.asStateFlow()

    private val _isMatchingLoading = MutableStateFlow(false)
    val isMatchingLoading: StateFlow<Boolean> = _isMatchingLoading.asStateFlow()

    // Toast/Notification state for instant broker overlays
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    init {
        // Load initial match list
        refreshAIMatches()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen is AppScreen.ActiveChat) {
            _selectedSession.value = screen.session
        } else {
            _selectedSession.value = null
        }
    }

    fun showToastNotification(text: String) {
        _notification.value = text
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_notification.value == text) {
                _notification.value = null
            }
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun toggleLikeListing(listing: BrokerListing) {
        viewModelScope.launch {
            val updated = listing.copy(
                isLiked = !listing.isLiked,
                likesCount = if (listing.isLiked) listing.likesCount - 1 else listing.likesCount + 1
            )
            repository.updateListing(updated)
            // Liking/disliking triggers matching updates
            refreshAIMatches()
        }
    }

    fun submitDealPin(
        title: String,
        price: String,
        location: String,
        notes: String,
        imagePath: String
    ) {
        viewModelScope.launch {
            try {
                _isMatchingLoading.value = true
                val session = repository.pinNewDeal(
                    title = title,
                    estimatedPrice = price,
                    locationName = location,
                    buyerNotes = notes,
                    imagePath = imagePath
                )
                _isMatchingLoading.value = false
                
                // Show instant notification overlay from the broker!
                showToastNotification("Broker ${session.brokerName} received your pin for $title! Direct chat started.")
                
                // Promptly route to general list or active chat
                navigateTo(AppScreen.ActiveChat(session))
                refreshAIMatches()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit deal pin", e)
                _isMatchingLoading.value = false
            }
        }
    }

    fun selectSession(session: ChatSession) {
        _selectedSession.value = session
        _currentScreen.value = AppScreen.ActiveChat(session)
    }

    fun sendChatMessage(text: String) {
        val session = _selectedSession.value ?: return
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            try {
                repository.sendBuyerMessage(session.id, text)
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending chat message", e)
            }
        }
    }

    fun deletePin(dealId: Int) {
        viewModelScope.launch {
            repository.deleteDealPin(dealId)
            refreshAIMatches()
        }
    }

    fun refreshAIMatches() {
        viewModelScope.launch {
            try {
                _isMatchingLoading.value = true
                val suggestions = repository.getAIMatchedProperties()
                _aiMatches.value = suggestions
                _isMatchingLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading AI suggestions", e)
                _isMatchingLoading.value = false
            }
        }
    }
}

class DealPinViewModelFactory(private val repository: PropertyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealPinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DealPinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
