    package com.vn.elsanobooking.presentation.components

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.ColorFilter
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.vn.elsanobooking.R
    import com.vn.elsanobooking.ui.theme.BluePrimary
    import com.vn.elsanobooking.ui.theme.PurpleGrey
    import com.vn.elsanobooking.ui.theme.WhiteBackground
    import com.vn.elsanobooking.ui.theme.poppinsFontFamily
    import coil.compose.AsyncImage

    @Composable
    fun MenuHome(modifier: Modifier = Modifier, icon: Int, title: String) {
        Column(
            modifier = modifier.width(100.dp), // Increased width to accommodate larger icon
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier.size(80.dp), // Larger button size for bigger icon
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhiteBackground),
                onClick = { /*TODO*/ }
            ) {
                Image(
                    modifier = Modifier.size(48.dp), // Increased icon size from 32.dp to 48.dp
                    painter = painterResource(id = icon),
                    contentDescription = title,
                )
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.W400,
                color = PurpleGrey,
                fontSize = 20.sp, // Increased font size from 16.sp to 20.sp for larger text
                textAlign = TextAlign.Center,
                maxLines = 2, // Allow wrapping to 2 lines if needed
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Preview
    @Composable
    private fun MenuHomePreview() {
        MenuHome(icon = R.drawable.makeup, title = "Trang điểm")
    }

    @Composable
    fun MenuService(
        modifier: Modifier = Modifier,
        imageUrl: String,
        title: String,
        onClick: () -> Unit = {}
    ) {
        Column(
            modifier = modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhiteBackground),
                onClick = onClick
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.W400,
                color = PurpleGrey,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }