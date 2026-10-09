package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LyricLine

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lyrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Looking up synchronized lyrics from LRCLIB...",
                color = Color(0xFFA1A1AA),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    // Determine current active lyric index
    val activeIndex = lyrics.indexOfLast { it.timestampMs <= currentPositionMs }.coerceAtLeast(0)
    val listState = rememberLazyListState()

    // Smooth auto-scroll to current lyric line
    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            val target = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 60.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("lyrics_view")
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == activeIndex
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else Color(0x66FFFFFF),
                label = "lyric_color"
            )
            val fontSize = if (isActive) 26.sp else 21.sp
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeekTo(line.timestampMs) }
                    .padding(vertical = 12.dp)
                    .testTag("lyric_line_$index")
            ) {
                Text(
                    text = line.text,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    lineHeight = if (isActive) 34.sp else 28.sp
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
