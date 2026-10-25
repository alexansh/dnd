package com.grimoire.dnd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.data.CampaignCatalog
import com.grimoire.dnd.data.CampaignInfo
import com.grimoire.dnd.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureSelectScreen(
    onSelectAdventure: (CampaignInfo) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CHOOSE ADVENTURE",
                        color = GoldAccent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SELECT YOUR CAMPAIGN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            items(CampaignCatalog.campaigns) { campaign ->
                Card(
                    onClick = { onSelectAdventure(campaign) },
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, if (campaign.id == "campaign_1") GoldAccent else DarkBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = campaign.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextParchment,
                                fontFamily = FontFamily.Serif
                            )
                            Surface(
                                color = GoldAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = campaign.recommendedLevels,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = campaign.subtitle,
                            fontSize = 13.sp,
                            color = GoldMuted,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = campaign.description,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⏱️ ${campaign.estimatedLength}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Button(
                                onClick = { onSelectAdventure(campaign) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("EMBARK", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
