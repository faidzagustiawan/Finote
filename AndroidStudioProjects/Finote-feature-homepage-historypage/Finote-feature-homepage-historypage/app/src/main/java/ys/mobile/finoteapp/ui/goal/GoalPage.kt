package ys.mobile.finoteapp.ui.goal

import ys.mobile.finoteapp.data.repository.GoalRepository
import ys.mobile.finoteapp.data.remote.SupabaseClient
import ys.mobile.finoteapp.ui.components.GoalCard
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalPage(onGoalClick: (String) -> Unit, navController: NavController) {
    val goals = GoalRepository.goals
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    // Fetch data Supabase
    LaunchedEffect(Unit) {
        GoalRepository.fetchGoals()
    }
    val token = SupabaseClient.client.auth.currentSessionOrNull()?.accessToken
    Log.d("TOKEN_SUPABASE", "Bearer $token")
    if (showDialog) {
        AddGoalDialog(
            onDismiss = { showDialog = false },
            onConfirm = { newGoal ->
                // Jika user memilih gambar (uri string bukan null), kita convert ke Uri dulu
                val uri = if(newGoal.imageUri != null) Uri.parse(newGoal.imageUri) else null

                // Panggil repository untuk upload dan simpan
                GoalRepository.addGoal(newGoal, uri, context)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GOAL TRACKER", fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                actions = {
                    IconButton(onClick = {
                        showLogoutDialog = true // Pemicu dialog
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Black)
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text("Konfirmasi Logout") },
                            text = { Text("Yakin ingin keluar?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showLogoutDialog = false
                                        scope.launch {
                                            try {
                                                // Mencoba logout (meskipun session null, ini aman)
                                                SupabaseClient.client.auth.signOut()
                                            } catch (e: Exception) {
                                                Log.e("Logout", "Error: ${e.message}")
                                            } finally {
                                                // --- BAGIAN INI WAJIB DIISI ---
                                                // Ganti "login" dengan nama route halaman login Anda yang sebenarnya!
                                                navController.navigate("login") {
                                                    // Hapus history agar user tidak bisa tekan Back kembali ke Home
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Ya", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                    Text("Batal")
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(goals, key = { it.id }) { goal ->
                GoalCard(goal = goal, onClick = { onGoalClick(goal.id) })
            }
        }
    }
}
