package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingsDao {

    // Broker Listings
    @Query("SELECT * FROM broker_listings ORDER BY id DESC")
    fun getAllListings(): Flow<List<BrokerListing>>

    @Query("SELECT * FROM broker_listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: Int): BrokerListing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<BrokerListing>)

    @Update
    suspend fun updateListing(listing: BrokerListing)

    @Delete
    suspend fun deleteListing(listing: BrokerListing)

    @Query("DELETE FROM broker_listings")
    suspend fun clearAllListings()

    // Deal Pins (User clicked deals)
    @Query("SELECT * FROM deal_pins ORDER BY createdAt DESC")
    fun getAllDealPins(): Flow<List<DealPin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDealPin(dealPin: DealPin): Long

    @Query("DELETE FROM deal_pins WHERE id = :id")
    suspend fun deleteDealPin(id: Int)

    // Chat Sessions
    @Query("SELECT * FROM chat_sessions ORDER BY lastMessageTime DESC")
    fun getAllChatSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE brokerId = :brokerId AND propertyTitle = :propertyTitle LIMIT 1")
    suspend fun getSessionByBrokerAndProperty(brokerId: String, propertyTitle: String): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSession): Long

    @Query("UPDATE chat_sessions SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime WHERE id = :id")
    suspend fun updateSessionLastMessage(id: Int, lastMessage: String, lastMessageTime: Long)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long
}
