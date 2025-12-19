package ys.mobile.finoteapp.supabase

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val SUPABASE_URL = "https://mzpexaqfdwyiflykqjne.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_YsYP7qsufbCiu6PHNW-FNg_qW7RNpQ9"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        }
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("$SUPABASE_URL/rest/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
