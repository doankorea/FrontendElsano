package com.vn.elsanobooking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.models.Artist

@Composable
fun NearbyMakeupArtistCard(
    artist: Artist,
    navController: NavController,
    userId: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("artist_detail/${artist.id}/$userId")
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Gray, RoundedCornerShape(24.dp))
            ){
                artist.avatar?.let { avatarUrl ->
                    AsyncImage(
                        model = "${Constants.BASE_URL}${avatarUrl}?t=${System.currentTimeMillis()}",
                        contentDescription = "${artist.fullName}'s avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = artist.fullName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Rating: ${artist.rating} (${artist.reviewsCount} reviews)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = artist.address?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}