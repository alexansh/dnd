package com.grimoire.dnd.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.ui.theme.*

@Composable
fun VisualSceneCard(
    visualTag: String,
    locationTitle: String,
    speakerName: String? = null,
    modifier: Modifier = Modifier
) {
    val (bgDrawableRes, sceneIcon, ambientMood) = when (visualTag) {
        "tavern" -> Triple(
            R.drawable.bg_tavern,
            R.drawable.ic_fire,
            "Atmosphere: Roaring Hearth & Spiced Ale"
        )
        "graveyard" -> Triple(
            R.drawable.bg_crypt,
            R.drawable.ic_tombstone,
            "Atmosphere: Cold Fog & Chittering Ravens"
        )
        "crypt" -> Triple(
            R.drawable.bg_crypt,
            R.drawable.ic_key,
            "Atmosphere: Submerged Slabs & Murky Waters"
        )
        "forge" -> Triple(
            R.drawable.bg_forge,
            R.drawable.ic_fire,
            "Atmosphere: Volcanic Smelters & Molten Slag"
        )
        "sanctum" -> Triple(
            R.drawable.bg_cathedral,
            R.drawable.ic_dragon,
            "Atmosphere: Midnight Rain & Gothic Stained Glass"
        )
        else -> Triple(
            R.drawable.bg_cathedral,
            R.drawable.ic_scroll,
            "Atmosphere: Ancient Ruins"
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, GoldMuted.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. High-Resolution 2D Environmental Painting
            Image(
                painter = painterResource(id = bgDrawableRes),
                contentDescription = locationTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            )

            // 2. Cinematic Contrast Gradient Vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x66000000),
                                Color(0x880A0A0F),
                                Color(0xEE0B0C10)
                            )
                        )
                    )
            )

            // 3. Foreground Narrative UI Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = DarkSurface.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldMuted)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = sceneIcon),
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = locationTitle.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (speakerName != null) {
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SPEAKER: $speakerName",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = if (speakerName != null) "Dialogue with $speakerName" else locationTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ambientMood,
                        fontSize = 12.sp,
                        color = GoldAccent.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

