package ys.mobile.finoteapp.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import ys.mobile.finoteapp.data.remote.SupabaseClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// --- IMPORT GOOGLE AUTH (Wajib Ada) ---
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

// --- IMPORT SUPABASE AUTH (Wajib Ada) ---
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selamat Datang di Finote", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Kelola keuangan & impianmu sekarang.", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                scope.launch {
                    signInWithGoogle(context, onLoginSuccess)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Masuk dengan Google", fontSize = 18.sp, color = Color.White)
        }
    }
}

private suspend fun signInWithGoogle(context: Context, onSuccess: () -> Unit) {
    val credentialManager = CredentialManager.create(context)

    // GANTI CLIENT ID DENGAN MILIK ANDA
    val webClientId = "484296721788-3gm2pmnsggcrg1ntj3bftiimltt9jf3d.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            // Mengambil token dari Google Object
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Simpan ke variabel dengan nama beda agar tidak bentrok
            val googleToken = googleIdTokenCredential.idToken

            // Login ke Supabase
            SupabaseClient.client.auth.signInWith(IDToken) {
                // 'idToken' (kiri) adalah properti Supabase
                // 'googleToken' (kanan) adalah variabel string dari Google
                this.idToken = googleToken
                this.provider = Google
            }


            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
            onSuccess()

        }
    } catch (e: Exception) {
        Log.e("LOGIN", "Error: ${e.message}", e)
        Toast.makeText(context, "Login Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
