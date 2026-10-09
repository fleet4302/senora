package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.ui.theme.FlacGold
import com.example.ui.theme.PeerActive
import com.example.ui.theme.PeerBusy
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraDarkSurface
import com.example.ui.theme.SonoraRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesSheet(
    track: Track?,
    currentSource: SoulseekPeerSource?,
    candidates: List<SoulseekPeerSource>,
    isResolving: Boolean,
    onSelectSource: (SoulseekPeerSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SonoraDarkSurface,
        modifier = modifier.testTag("sources_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Soulseek Sources",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = if (track != null) "${track.artist} - ${track.title}" else "Ranked stream providers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA1A1AA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFA1A1AA)
                    )
                }
            }

            if (isResolving) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = SonoraRed,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Polling Soulseek network (~6s budget)...",
                        color = SonoraRed,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Candidates List
            if (candidates.isEmpty() && !isResolving) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Soulseek sources found for this query.",
                        color = Color(0xFFA1A1AA),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(candidates) { index, candidate ->
                        val isCurrent = currentSource?.peerUsername == candidate.peerUsername &&
                                currentSource.filename == candidate.filename
                        val isAutoPick = index == 0

                        SourceCandidateCard(
                            candidate = candidate,
                            isCurrent = isCurrent,
                            isAutoPick = isAutoPick,
                            rank = index + 1,
                            onClick = { onSelectSource(candidate) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SourceCandidateCard(
    candidate: SoulseekPeerSource,
    isCurrent: Boolean,
    isAutoPick: Boolean,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCurrent) SonoraRed else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SonoraDarkCard)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("source_candidate_$rank")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Text(
                    text = "#$rank",
                    color = if (isCurrent) SonoraRed else Color(0xFF71717A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Quality Badge
                QualityBadge(quality = candidate.qualityLabel)

                if (isAutoPick) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SonoraRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AUTO-PICK",
                            color = SonoraRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = SonoraRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File & Folder Name
            Text(
                text = candidate.filename,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = candidate.folder,
                color = Color(0xFF71717A),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Peer stats: User, Speed, Queue
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Peer user
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (candidate.freeUploadSlots) PeerActive else PeerBusy)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "@${candidate.peerUsername}",
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = candidate.speedDisplay,
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Queue
                Text(
                    text = if (candidate.freeUploadSlots) "Free Slot" else "Queue #${candidate.queueLength}",
                    color = if (candidate.freeUploadSlots) PeerActive else FlacGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Size
                Text(
                    text = "%.1f MB".format(candidate.sizeBytes / (1024.0 * 1024.0)),
                    color = Color(0xFF71717A),
                    fontSize = 12.sp
                )
            }
        }
    }
}
