package ys.mobile.finoteapp.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import ys.mobile.finoteapp.data.remote.SupabaseClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Masuk ke Finote", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Login Email
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        SupabaseClient.client.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        onLoginSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Login Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isLoading) "Memproses..." else "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Atau", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Login Google
        OutlinedButton(
            onClick = {
                scope.launch { signInWithGoogle(context, onLoginSuccess) }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Masuk dengan Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Belum punya akun? Daftar di sini")
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
