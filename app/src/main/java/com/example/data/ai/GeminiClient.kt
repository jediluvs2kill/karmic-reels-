package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.BrokerListing
import com.example.data.DealPin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Moshi Mapped Request/Response for Gemini ---
@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

// --- Mapped AI Output to feed UI suggestions ---
@JsonClass(generateAdapter = true)
data class PropertyMatch(
    val listingId: Int,
    val matchReason: String
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Recommends properties from inventory based on liked listings and pins.
     */
    suspend fun recommendProperties(
        likedListings: List<BrokerListing>,
        pinnedListings: List<DealPin>,
        inventory: List<BrokerListing>
    ): List<PropertyMatch> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or placeholder value.")
            return@withContext getFallbackRecommendations(inventory)
        }

        val likedText = likedListings.joinToString("\n") { 
            "- ${it.title} in ${it.locationName} (${it.tagPreferences})"
        }
        val pinnedText = pinnedListings.joinToString("\n") {
            "- Buyer Custom Pin: ${it.title} (${it.notes}) near ${it.locationName}, price: ${it.estimatedPrice}"
        }
        val inventoryText = inventory.joinToString("\n") {
            "- ID ${it.id}: ${it.title} in ${it.locationName} priced ${it.priceDescription}, tags: ${it.tagPreferences}"
        }

        val prompt = """
            We need to recommend featured listings from our inventory to a real estate prospect.
            Here are listings the prospect has LIKED:
            $likedText
            
            Here are custom estate deals the prospect HAS PINNED via camera:
            $pinnedText
            
            Here is the broker inventory of available featured listings:
            $inventoryText
            
            Based on this profile, select up to 3 non-liked featured listings from the inventory.
            For each selection, provide a tailored explanation showing why this matches their visual taste (e.g., matching a penthouse preference, brick styles, or woodland settings).
            
            Provide the output as a valid JSON array of objects without markdown headers or code block tags.
            Each object MUST have:
            - "listingId" (Int representing the ID from inventory)
            - "matchReason" (String explaining the match in a friendly but highly professional agent tone)
            
            Output format example:
            [{"listingId": 1, "matchReason": "Since you liked Midtown Skylines, this premium penthouse offers identical floor-to-ceiling glass exposure."}]
        """.trimIndent()

        val requestPayload = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f),
            systemInstruction = Content(parts = listOf(Part(text = "You are an elite, classy, and concise property matching AI concierge.")))
        )

        try {
            val jsonAdapter = moshi.adapter(GenerateContentRequest::class.java)
            val requestBodyJson = jsonAdapter.toJson(requestPayload)
            
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code} / ${response.message}")
                    return@withContext getFallbackRecommendations(inventory)
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext getFallbackRecommendations(inventory)
                }

                val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)
                val responseData = responseAdapter.fromJson(responseBodyStr)
                val rawText = responseData?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return@withContext getFallbackRecommendations(inventory)

                // Parse property matches list
                val listType = Types.newParameterizedType(List::class.java, PropertyMatch::class.java)
                val matchesAdapter = moshi.adapter<List<PropertyMatch>>(listType)
                
                // Clean markdown wraps if some model returned it despite instructions
                val cleanJson = rawText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                return@withContext matchesAdapter.fromJson(cleanJson) ?: getFallbackRecommendations(inventory)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in match recommendProperties", e)
            return@withContext getFallbackRecommendations(inventory)
        }
    }

    /**
     * Generates a realistic automated broker response based on the pinned property.
     */
    suspend fun generateBrokerReply(
        brokerName: String,
        propertyTitle: String,
        propertyLocation: String,
        propertyNotes: String,
        chatHistory: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hi there! I detected your pin near $propertyLocation for $propertyTitle. It's a gorgeous building. When would you like to schedule a tour?"
        }

        val prompt = """
            You are $brokerName, a highly professional real estate broker. A buyer has taken a visual snap of a property ($propertyTitle at $propertyLocation) and pinned it. 
            Buyer details/notes: "$propertyNotes"
            
            Here is the current chat log:
            $chatHistory
            
            Write your next dynamic, professional response. Keep it friendly, realistic, and focused on learning about their timeline or budget to help them secure the deal. Do not use placeholders or markdown formatting. Keep the message under 3 sentences.
        """.trimIndent()

        val requestPayload = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a warm, helpful, high-status real estate broker.")))
        )

        try {
            val jsonAdapter = moshi.adapter(GenerateContentRequest::class.java)
            val requestBodyJson = jsonAdapter.toJson(requestPayload)
            
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Hi there! This is $brokerName. Thanks for pinning $propertyTitle at $propertyLocation. Let me know when you'd like to chat details!"
                }

                val responseBodyStr = response.body?.string() ?: return@withContext "Hey! Feel free to ask any questions about $propertyTitle."
                val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)
                val responseData = responseAdapter.fromJson(responseBodyStr)
                return@withContext responseData?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    ?: "Thanks for pinning this! It is an amazing listing."
            }
        } catch (e: Exception) {
            return@withContext "Hi! Let me look into this listing for you right away. What properties do you usually look for?"
        }
    }

    private fun getFallbackRecommendations(inventory: List<BrokerListing>): List<PropertyMatch> {
        // Safe mock matching reasons in case API fails or key is missing
        return inventory.take(3).mapIndexed { index, listing ->
            val reason = when (index) {
                0 -> "Features custom mid-century concrete stylings matching your modern structural interests."
                1 -> "Features eye-catching organic wooden materials aligned with sustainable natural architecture."
                else -> "An exceptional deal situated in a prime cultural hub, reflecting your pinned location taste."
            }
            PropertyMatch(listingId = listing.id, matchReason = reason)
        }
    }
}
