package android.belajar.finotewithsupabase.data.remote

// Perhatikan underscore (_) pada 'jan_tennert'
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    // Masukkan URL & Key Project Anda di sini
    private const val SUPABASE_URL = "https://mzpexaqfdwyiflykqjne.supabase.co"
    private const val SUPABASE_KEY = "sb_secret_xNj40Fx4UhLaJcYN_0zuKw_vFVUVQks"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true // TAMBAHKAN INI
            coerceInputValues = true
        })

        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}