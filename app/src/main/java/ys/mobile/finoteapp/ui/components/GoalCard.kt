package ys.mobile.finoteapp.ui.components

import ys.mobile.finoteapp.data.model.FinoteGoal
import ys.mobile.finoteapp.utils.CurrencyUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Composable
fun GoalCard(goal: FinoteGoal, onClick: () -> Unit) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    val targetDisplay = CurrencyUtils.toRupiah(goal.targetAmount)

    val today = System.currentTimeMillis()
    val diff = (goal.endDateMillis ?: 0) - today
    val isOverdue = diff < 0

    val (statusText, statusBgColor, statusTextColor) = if (isOverdue && goal.endDateMillis != null) {
        val daysOverdue = TimeUnit.MILLISECONDS.toDays(abs(diff)) + 1
        Triple("Terlewat $daysOverdue Hari", Color(0xFFFFEBEE), Color.Red)
    } else if (goal.endDateMillis != null) {
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff) + 1
        if (daysLeft < 7) Triple("$daysLeft Hari Lagi", Color(0xFFFFF9C4), Color(0xFFF57F17))
        else Triple("$daysLeft Hari Lagi", Color(0xFFE3F2FD), Color.Blue)
    } else {
        Triple("No Deadline", Color(0xFFEEEEEE), Color.Gray)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            // IMAGE
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(if (goal.imageUri == null) Color.Black else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (goal.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(goal.imageUri).crossfade(true).build(),
                        contentDescription = "Goal Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Pakai 'title' bukan 'name'
                    Text(text = goal.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBgColor).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = statusText, fontSize = 10.sp, color = statusTextColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$targetDisplay", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = goal.dateRangeString, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)))
                    Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color.Black))
                    Text(text = "${(goal.progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (goal.progress > 0.5f) Color.White else Color.Black, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Terkumpul: ${CurrencyUtils.toRupiah(goal.currentAmount)}", fontSize = 11.sp, color = Color(0xFF4CAF50))
                    Text("Kurang: ${CurrencyUtils.toRupiah(goal.remainingAmount)}", fontSize = 11.sp, color = Color(0xFFE91E63))
                }
            }
        }
    }
}
