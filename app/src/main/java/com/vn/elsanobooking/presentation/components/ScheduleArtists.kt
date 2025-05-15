package com.vn.elsanobooking.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vn.elsanobooking.R
import com.vn.elsanobooking.ui.theme.BluePrimary
import com.vn.elsanobooking.ui.theme.PurpleGrey
import com.vn.elsanobooking.ui.theme.TextColorTitle
import com.vn.elsanobooking.ui.theme.poppinsFontFamily

@Composable
fun ScheduleMakeupArtistCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.5.dp,
        shadowElevation = 0.2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Row {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = R.drawable.user), // Thay bằng hình ảnh thợ trang điểm
                    contentDescription = "Hình ảnh thợ trang điểm"
                )

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                ) {
                    Text(
                        text = "Anna Nguyễn",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextColorTitle
                    )

                    Text(
                        text = "Thợ trang điểm chuyên nghiệp",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Light,
                        color = PurpleGrey
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .alpha(0.1f),
                color = Color.Gray
            )

            ScheduleTimeContent(contentColor = PurpleGrey)

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF63B4FF).copy(alpha = 0.1f)),
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = "Chi tiết",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
            }
        }
    }
}

@Preview
@Composable
private fun ScheduleMakeupArtistCardPreview() {
    ScheduleMakeupArtistCard()
}