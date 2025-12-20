package ys.mobile.finoteapp.supabase

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    private const val SUPABASE_URL = "https://mzpexaqfdwyiflykqjne.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_secret_xNj40Fx4UhLaJcYN_0zuKw_vFVUVQks"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Logging interceptor untuk melihat seluruh request/response
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("SUPABASE_HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()

            Log.d("SUPABASE_DEBUG", "REQUEST URL = ${original.url}")

            val request = original.newBuilder()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Prefer", "return=representation")
                .build()

            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/rest/v1/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Service generator
    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
