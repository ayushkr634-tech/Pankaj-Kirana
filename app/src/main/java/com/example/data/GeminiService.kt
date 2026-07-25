package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request & Response Models (Moshi Adaptable) ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiService {
    private val systemInstructionText = """
        You are the AI Shopping Assistant for 'Pankaj Kirana', a friendly local grocery store in Ranchi, Jharkhand.
        Contact phone number: 8235091376.
        Our store sells essentials across categories including Rice & Flour, Cooking Oil, Spices, Tea & Coffee, Snacks, Instant Food, Dairy Products, Dry Fruits, and Personal Care.
        We offer: Home Delivery, Express Delivery, and Store Pickup.
        
        Guidelines:
        1. Keep responses warm, professional, concise, and helpful.
        2. Help customers search and recommend products from the store.
        3. Suggest recipes using our key items like Aashirvaad Atta, India Gate Basmati Rice, Fortune Mustard Oil, Tata Tea Premium, Haldiram's Bhujia, Amul Butter, Amul Milk, Premium California Almonds, etc.
        4. If a customer is asking what to cook, give a brief Indian recipe and list the ingredients they can buy from us.
        5. You can reply in a friendly mixture of Hindi and English (Hinglish) or pure English to fit a neighborhood grocery store vibe.
    """.trimIndent()

    suspend fun askAssistant(userPrompt: String, history: List<Pair<String, String>> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if ((apiKey.isEmpty()) || (apiKey == "MY_GEMINI_API_KEY")) {
            return@withContext "I'm sorry, the Gemini API key is currently not configured in the Secrets panel. Please contact support or enter your key. In the meantime, I can assist you with local info! Contact Pankaj Kirana: 8235091376."
        }

        // Build content list including chat history
        val contentsList = mutableListOf<Content>()
        for (turn in history) {
            contentsList.add(Content(parts = listOf(Part(text = turn.first)))) // user
            contentsList.add(Content(parts = listOf(Part(text = turn.second)))) // model
        }
        contentsList.add(Content(parts = listOf(Part(text = userPrompt)))) // current user prompt

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            result ?: "I couldn't process that response. How else can I help you today?"
        } catch (e: Exception) {
            e.printStackTrace()
            "Oops! I had some trouble connecting. Please try again. Or, feel free to call our store directly at 8235091376!"
        }
    }
}
