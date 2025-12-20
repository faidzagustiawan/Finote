package ys.mobile.finoteapp.ui.auth


import android.widget.Toast
import ys.mobile.finoteapp.data.remote.SupabaseClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
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
        Text("Buat Akun Baru", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        SupabaseClient.client.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        Toast.makeText(context, "Cek email untuk konfirmasi!", Toast.LENGTH_LONG).show()
                        onRegisterSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Daftar Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Daftar")
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Sudah punya akun? Login")
        }
    }
}
