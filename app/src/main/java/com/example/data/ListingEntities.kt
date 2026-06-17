package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "broker_listings")
data class BrokerListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priceDescription: String,
    val brokerName: String,
    val brokerAvatarUrl: String = "",
    val locationName: String,
    val description: String,
    val imageResName: String, // Reference name in drawables (e.g., "property_loft")
    val videoUrl: String = "", // Simulated reel background
    val likesCount: Int = 124,
    val commentsCount: Int = 45,
    val isLiked: Boolean = false,
    val tagPreferences: String = "luxury,penthouse,park view" // Comma-separated for match matching
)

@Entity(tableName = "deal_pins")
data class DealPin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val estimatedPrice: String,
    val locationName: String,
    val notes: String,
    val imagePath: String = "", // Can be custom base64 or drawn canvas representation
    val createdAt: Long = System.currentTimeMillis(),
    val buyerNotes: String = ""
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brokerName: String,
    val brokerId: String, // String identifier for automated agent broker
    val propertyTitle: String,
    val propertyImageUrl: String = "", // Refers to the drawable name
    val lastMessage: String,
    val lastMessageTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val senderType: String, // "buyer" or "broker"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
