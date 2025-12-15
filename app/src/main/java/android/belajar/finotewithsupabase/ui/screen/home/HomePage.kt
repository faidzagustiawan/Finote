package android.belajar.finotewithsupabase.ui.screen.home

import android.belajar.finotewithsupabase.data.remote.SupabaseClient
import android.belajar.finotewithsupabase.data.repository.GoalRepository
import android.belajar.finotewithsupabase.utils.CurrencyUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.auth.auth

@Composable
fun HomePage(
    onNavigateTo: (String) -> Unit
) {
    // Ambil data user dari sesi Auth Supabase
    val user = SupabaseClient.client.auth.currentUserOrNull()
    val userName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User"
    val avatarUrl = user?.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")

    // Ambil saldo dari Repository
    val balance = GoalRepository.mainBalance.doubleValue

    // Fetch saldo saat halaman dibuka
    LaunchedEffect(Unit) {
        GoalRepository.fetchMainBalance()
        // GoalRepository.fetchGoals() // Optional: Fetch goals juga buat preview
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. HEADER (Salam & Profil)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Selamat Datang,", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Foto Profil
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. KARTU SALDO UTAMA
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text("Total Saldo Anda", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyUtils.toRupiah(balance),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Icon Wallet hiasan
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(80.dp)
                            .offset(x = 10.dp, y = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. MENU GRID
            Text("Menu Akses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MenuButton("Goal", Icons.Default.Flag, Color(0xFFE3F2FD), Color(0xFF2196F3)) {
                    onNavigateTo("goal")
                }
                MenuButton("Riwayat", Icons.Default.History, Color(0xFFFBE9E7), Color(0xFFFF5722)) {
                    onNavigateTo("history") // Pastikan rute ini ada di NavHost
                }
                MenuButton("Insight", Icons.Default.PieChart, Color(0xFFE8F5E9), Color(0xFF4CAF50)) {
                    onNavigateTo("insight") // Pastikan rute ini ada di NavHost
                }
            }

            // Tambahan preview goals atau info lain di bawah sini...
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = iconColor, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}