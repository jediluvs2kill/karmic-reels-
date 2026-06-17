package com.example.data

import android.util.Log
import com.example.data.ai.GeminiClient
import com.example.data.ai.PropertyMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

import android.content.Context

class PropertyRepository(private val dao: ListingsDao, private val context: Context) {

    // SharedPreferences for Broker Profile
    private val prefs = context.getSharedPreferences("broker_profile_prefs", Context.MODE_PRIVATE)

    fun getBrokerProfileName(): String = prefs.getString("broker_name", "Rajesh Malhotra") ?: "Rajesh Malhotra"
    fun getBrokerProfileAgency(): String = prefs.getString("broker_agency", "Malhotra Luxury Estates") ?: "Malhotra Luxury Estates"
    fun getBrokerProfilePhone(): String = prefs.getString("broker_phone", "+91 98110 50321") ?: "+91 98110 50321"
    fun getBrokerProfileBio(): String = prefs.getString("broker_bio", "Premier real estate advisor in Delhi. Specializes in luxury independent floors & grand residences across GK, South Ext & Vasant Vihar.") ?: "Premier real estate advisor in Delhi. Specializes in luxury independent floors & grand residences across GK, South Ext & Vasant Vihar."
    fun getBrokerProfileZone(): String = prefs.getString("broker_zone", "Greater Kailash II, Delhi") ?: "Greater Kailash II, Delhi"
    fun getBrokerProfileAvatar(): String = prefs.getString("broker_avatar", "broker_sarah") ?: "broker_sarah"

    fun saveBrokerProfile(
        name: String,
        agency: String,
        phone: String,
        bio: String,
        zone: String,
        avatar: String
    ) {
        prefs.edit().apply {
            putString("broker_name", name)
            putString("broker_agency", agency)
            putString("broker_phone", phone)
            putString("broker_bio", bio)
            putString("broker_zone", zone)
            putString("broker_avatar", avatar)
            apply()
        }
    }

    // User login/role info persistence
    fun getUserLoginEmail(): String? = prefs.getString("user_login_email", null)
    fun getUserLoginRole(): String = prefs.getString("user_login_role", "Buyer") ?: "Buyer"
    fun saveUserLogin(email: String, role: String) {
        prefs.edit()
            .putString("user_login_email", email)
            .putString("user_login_role", role)
            .apply()
    }
    fun clearUserLogin() {
        prefs.edit()
            .remove("user_login_email")
            .remove("user_login_role")
            .apply()
    }

    suspend fun insertNewListing(listing: BrokerListing) = withContext(Dispatchers.IO) {
        dao.insertListings(listOf(listing))
    }

    suspend fun deleteListingDirect(listing: BrokerListing) = withContext(Dispatchers.IO) {
        dao.deleteListing(listing)
    }

    companion object {
        private const val TAG = "PropertyRepository"
    }

    // Expose all listings, checking if they are empty and pre-populating them
    val allListings: Flow<List<BrokerListing>> = dao.getAllListings()
        .onStart {
            ensureInitialListings()
        }
        .flowOn(Dispatchers.IO)

    // Expose pinning deals
    val allDealPins: Flow<List<DealPin>> = dao.getAllDealPins()
        .flowOn(Dispatchers.IO)

    // Expose active chat sessions
    val allChatSessions: Flow<List<ChatSession>> = dao.getAllChatSessions()
        .flowOn(Dispatchers.IO)

    // Expose specific chat messages
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> {
        return dao.getMessagesForSession(sessionId).flowOn(Dispatchers.IO)
    }

    /**
     * Pre-populates default real estate listings if database is empty.
     */
    private suspend fun ensureInitialListings() = withContext(Dispatchers.IO) {
        try {
            // Force reset to popular luxury developer reels if not already executed
            val isDbResetForDelhiSecueds = prefs.getBoolean("delhi_ncr_real_devs_seed_v4", false)
            if (!isDbResetForDelhiSecueds) {
                dao.clearAllListings()
                prefs.edit().putBoolean("delhi_ncr_real_devs_seed_v4", true).apply()
            }

            val currentListings = dao.getAllListings().first()
            if (currentListings.isEmpty()) {
                val defaultListings = listOf(
                    BrokerListing(
                        title = "DLF The Camellias",
                        priceDescription = "₹45 Cr - ₹85 Cr",
                        brokerName = "DLF Homes (@dlf.homes)",
                        brokerAvatarUrl = "broker_sarah",
                        locationName = "Sector 42, Golf Course Road, Gurugram",
                        description = "India's highest-valued super-luxury residence. Overlooking the Aravalli hills & DLF Golf Course. Pristine custom architecture, grand central lakes, and an majestic 160,000 sq.ft. wellness club.",
                        imageResName = "property_redwood",
                        videoUrl = "https://www.instagram.com/dlf.homes/",
                        likesCount = 1420,
                        commentsCount = 234,
                        isLiked = false,
                        tagPreferences = "dlf,camellias,super-luxury,gurugram,golf-course"
                    ),
                    BrokerListing(
                        title = "M3M Golfestate",
                        priceDescription = "₹12.5 Cr - ₹24 Cr",
                        brokerName = "M3M India (@m3mindia)",
                        brokerAvatarUrl = "broker_emma",
                        locationName = "Sector 65, Golf Course Ext Road, Gurugram",
                        description = "Premium duplex penthouse units designed inside a private 9-hole executive golf course. Floating balconies, custom automated woodwork, and bespoke deck pools.",
                        imageResName = "property_loft",
                        videoUrl = "https://www.instagram.com/m3mindia/",
                        likesCount = 980,
                        commentsCount = 189,
                        isLiked = false,
                        tagPreferences = "m3m,golfestate,duplex,gurugram,views"
                    ),
                    BrokerListing(
                        title = "Max Estates 128",
                        priceDescription = "₹7.5 Cr - ₹15 Cr",
                        brokerName = "Max Estates (@maxestates_official)",
                        brokerAvatarUrl = "broker_chloe",
                        locationName = "Sector 128, Noida Expressway",
                        description = "Award-winning bioclimatic architecture centering natural well-being. Double-height elevator lobbies with scenic sky pocket forests, low residential density, and green parks.",
                        imageResName = "property_minimal",
                        videoUrl = "https://www.instagram.com/maxestates_official/",
                        likesCount = 752,
                        commentsCount = 104,
                        isLiked = false,
                        tagPreferences = "max-estates,noida,bioclimatic,pocket-forest,sustainable"
                    ),
                    BrokerListing(
                        title = "Godrej Connaught One",
                        priceDescription = "₹18 Cr - ₹28 Cr",
                        brokerName = "Godrej Properties (@godrejpropertiesltd)",
                        brokerAvatarUrl = "broker_kenji",
                        locationName = "Connaught Place, New Delhi",
                        description = "Super-executive boutique luxury in the historic central core of Connaught Place. Beautiful classical red-brick exterior columns, private electronic automated control screens, and elite lounge lanes.",
                        imageResName = "property_classic",
                        videoUrl = "https://www.instagram.com/godrejpropertiesltd/",
                        likesCount = 642,
                        commentsCount = 88,
                        isLiked = false,
                        tagPreferences = "godrej,connaught-place,heritage,new-delhi,boutique"
                    ),
                    BrokerListing(
                        title = "Elan The Presidential",
                        priceDescription = "₹6.8 Cr - ₹11.5 Cr",
                        brokerName = "Elan Group (@elan__group)",
                        brokerAvatarUrl = "broker_liam",
                        locationName = "Sector 106, Dwarka Expressway, Gurugram",
                        description = "Spectacular modern high-rise architectural towers. Offers continuous wrap-around curved balconies overlooking Gurugram's skyline, customized pool decks, and dual-height grand lounge doors.",
                        imageResName = "property_warehouse",
                        videoUrl = "https://www.instagram.com/elan__group/",
                        likesCount = 890,
                        commentsCount = 145,
                        isLiked = false,
                        tagPreferences = "elan,gurugram,dwarka-expressway,glass,skyline"
                    )
                )
                dao.insertListings(defaultListings)
                Log.d(TAG, "Pre-populated database with 5 luxury Delhi NCR developer listings.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pre-populate mock listings", e)
        }
    }

    suspend fun updateListing(listing: BrokerListing) = withContext(Dispatchers.IO) {
        dao.updateListing(listing)
    }

    suspend fun getListingById(id: Int): BrokerListing? = withContext(Dispatchers.IO) {
        dao.getListingById(id)
    }

    /**
     * Handles adding a buyer custom visual deal pin, setting up the automated broker session,
     * and launching an initial greeting from the broker representing that area.
     */
    suspend fun pinNewDeal(
        title: String,
        estimatedPrice: String,
        locationName: String,
        buyerNotes: String,
        imagePath: String
    ): ChatSession = withContext(Dispatchers.IO) {
        // 1. Save local Pin entry
        val dealPin = DealPin(
            title = title,
            estimatedPrice = estimatedPrice,
            locationName = locationName,
            notes = buyerNotes,
            imagePath = imagePath
        )
        dao.insertDealPin(dealPin)

        // 2. Select a suitable broker based on spelling/matching of notes or randomly choose from main list
        val listings = dao.getAllListings().first()
        val representative = listings.firstOrNull { 
            locationName.contains(it.brokerName, ignoreCase = true) || 
            it.locationName.split(",").firstOrNull()?.let { city -> locationName.contains(city, ignoreCase = true) } == true
        } ?: listings.randomOrNull() ?: BrokerListing(
            title = "Exclusive Estate Partnership",
            priceDescription = "Varies",
            brokerName = "Broker Arthur",
            locationName = locationName,
            description = "General listing proxy",
            imageResName = "property_loft"
        )

        // 3. Create or find existing chat session
        var session = dao.getSessionByBrokerAndProperty(representative.brokerName, title)
        val sessionId: Int
        
        if (session == null) {
            val newSession = ChatSession(
                brokerName = representative.brokerName,
                brokerId = representative.brokerName.lowercase().replace(" ", "_"),
                propertyTitle = title,
                propertyImageUrl = representative.imageResName,
                lastMessage = "Awaiting first response...",
                lastMessageTime = System.currentTimeMillis()
            )
            sessionId = dao.insertChatSession(newSession).toInt()
            session = newSession.copy(id = sessionId)
        } else {
            sessionId = session.id
        }

        // 4. Generate first broker reply instantly via Gemini (or fallback)
        val greeting = GeminiClient.generateBrokerReply(
            brokerName = session.brokerName,
            propertyTitle = title,
            propertyLocation = locationName,
            propertyNotes = buyerNotes,
            chatHistory = "Buyer pinned building: $title\nBuyer notes: $buyerNotes"
        )

        val brokerMessage = ChatMessage(
            sessionId = sessionId,
            senderType = "broker",
            message = greeting,
            timestamp = System.currentTimeMillis()
        )
        dao.insertChatMessage(brokerMessage)
        dao.updateSessionLastMessage(sessionId, greeting, System.currentTimeMillis())

        return@withContext session.copy(lastMessage = greeting)
    }

    suspend fun deleteDealPin(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteDealPin(id)
    }

    /**
     * Sends buyer's message, updates session metadata, and requests matching agent reply from Gemini.
     */
    suspend fun sendBuyerMessage(sessionId: Int, messageText: String) = withContext(Dispatchers.IO) {
        // 1. Save buyer's message
        val buyerMsg = ChatMessage(
            sessionId = sessionId,
            senderType = "buyer",
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        dao.insertChatMessage(buyerMsg)
        dao.updateSessionLastMessage(sessionId, messageText, System.currentTimeMillis())

        // 2. Load recent full history to feed the broker reply model context
        val messages = dao.getMessagesForSession(sessionId).first().takeLast(10)
        val chatHistory = messages.joinToString("\n") { "${it.senderType}: ${it.message}" }

        // Get session detail to explain properties
        val sessionsList = dao.getAllChatSessions().first()
        val currentSession = sessionsList.find { it.id == sessionId } 
            ?: return@withContext

        // 3. Request broker reply
        val brokerReply = GeminiClient.generateBrokerReply(
            brokerName = currentSession.brokerName,
            propertyTitle = currentSession.propertyTitle,
            propertyLocation = "pinned area",
            propertyNotes = "Deal chat ID $sessionId",
            chatHistory = chatHistory
        )

        // 4. Save broker's reply
        val brokerMsg = ChatMessage(
            sessionId = sessionId,
            senderType = "broker",
            message = brokerReply,
            timestamp = System.currentTimeMillis()
        )
        dao.insertChatMessage(brokerMsg)
        dao.updateSessionLastMessage(sessionId, brokerReply, System.currentTimeMillis())
    }

    /**
     * AI suggestions pipeline
     */
    suspend fun getAIMatchedProperties(): List<PropertyMatch> = withContext(Dispatchers.IO) {
        val listings = dao.getAllListings().first()
        val liked = listings.filter { it.isLiked }
        val pins = dao.getAllDealPins().first()
        
        Log.d(TAG, "Getting AI suggestions. Liked: ${liked.size}, Pins: ${pins.size}")
        return@withContext GeminiClient.recommendProperties(
            likedListings = liked,
            pinnedListings = pins,
            inventory = listings
        )
    }
}
