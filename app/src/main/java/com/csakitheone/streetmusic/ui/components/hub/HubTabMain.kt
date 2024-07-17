package com.csakitheone.streetmusic.ui.components.hub

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.activities.ExtrasActivity
import com.csakitheone.streetmusic.ui.components.BigMusicianCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.util.CustomTabsManager
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HubTabMain(
    scrollState: LazyListState,
    musicians: List<Musician>,
    onBrowseRequest: (countries: List<String>) -> Unit,
) {
    val context = LocalContext.current

    val musiciansOfDay = remember { musicians.shuffled(Random(LocalDate.now().dayOfYear)).take(3) }

    LazyColumn(
        state = scrollState,
    ) {
        item {
            MenuCard(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = {
                    val randomVideo = musicians
                        .filter { !it.youtubeUrl.isNullOrBlank() }
                        .random()
                        .youtubeUrl
                    CustomTabsManager.open(context, randomVideo)
                },
                painter = painterResource(id = R.drawable.ic_youtube),
                title = stringResource(id = R.string.watch_something),
            )
            MenuCard(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(
                            context,
                            ExtrasActivity::class.java
                        )
                    )
                },
                imageVector = Icons.Default.VideogameAsset,
                title = stringResource(id = R.string.extras),
            )
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = R.string.random_musicians_of_day),
            )
            HorizontalPager(
                state = rememberPagerState(pageCount = { musiciansOfDay.size })
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    BigMusicianCard(
                        modifier = Modifier.padding(8.dp),
                        musician = musiciansOfDay[it],
                    )
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(musiciansOfDay.size) { dotIndex ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .width(if (dotIndex == it) 6.dp else 4.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onBackground)
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.returning_musicians),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        items(
            items = musicians
                .sortedBy { it.name }
                .sortedByDescending { it.years?.size }
                .filter { (it.years?.size ?: 0) > 2 },
            key = { it.name },
        ) { musician ->
            MusicianCard(
                modifier = Modifier.padding(8.dp),
                musician = musician,
                showYears = true,
            )
        }
        item {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.top_countries),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        items(
            items = musicians
                .groupBy { it.country }
                .filter { !it.key.isNullOrBlank() && it.value.size > 4 }
                .toList()
                .sortedByDescending { it.second.size },
            key = { it.first!! },
        ) { countryGroup ->
            UzCard(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    onBrowseRequest(listOf(countryGroup.first!!))
                },
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "${countryGroup.second.first().getFlag()} ${countryGroup.first}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = countryGroup.second.size.toString(),
                    )
                }
            }
        }
    }
}