package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.Firestore
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.google.gson.reflect.TypeToken

class HubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HubScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun HubScreen() {
        UtcazeneTheme {
            var isMenuVisible by remember { mutableStateOf(false) }
            var isWebsitesMenuVisible by remember { mutableStateOf(false) }

            var musicians by remember { mutableStateOf(listOf<Musician>()) }
            var musiciansPinned by remember { mutableStateOf<List<Musician>>(listOf()) }
            var isOnlyPinned by remember { mutableStateOf(false) }
            var filterTags by remember { mutableStateOf(listOf<Int>()) }
            var filterYears by remember { mutableStateOf(listOf<Int>()) }
            val visibleMusicians by remember(musicians, musiciansPinned, isOnlyPinned, filterTags, filterYears) {
                mutableStateOf(
                    musicians
                        .filter { !isOnlyPinned || (isOnlyPinned && musiciansPinned.contains(it)) }
                        .filter { filterTags.isEmpty() || it.tags?.containsAll(filterTags) == true }
                        .filter { filterYears.isEmpty() || it.years?.containsAll(filterYears) == true }
                        .sortedBy { it.name }
                )
            }

            LaunchedEffect(Unit) {
                Firestore.getAllMusicians { 
                    musicians = it
                }
            }

            ListPreferenceHolder(
                id = "authorsPinned",
                value = musiciansPinned,
                onValueChanged = { musiciansPinned = it.toList() },
                type = object : TypeToken<Musician>() {}.type,
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        actions = {
                            IconButton(
                                onClick = { isMenuVisible = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                )
                                DropdownMenu(
                                    expanded = isMenuVisible,
                                    onDismissRequest = { isMenuVisible = false },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = "Go back to festival screen")
                                        },
                                        onClick = {
                                            startActivity(
                                                Intent(this@HubActivity, MainActivity::class.java)
                                                    .putExtra(MainActivity.EXTRA_IGNORE_EVENT_ENDED, true)
                                            )
                                            isMenuVisible = false
                                        },
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@HubActivity,
                                        ExtrasActivity::class.java
                                    )
                                )
                            },
                            imageVector = Icons.Default.VideogameAsset,
                            title = stringResource(id = R.string.extras),
                        )
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = { isWebsitesMenuVisible = true },
                            imageVector = Icons.Default.Language,
                            title = stringResource(id = R.string.websites),
                        ) {
                            DropdownMenu(
                                expanded = isWebsitesMenuVisible,
                                onDismissRequest = { isWebsitesMenuVisible = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Feed,
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_website)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@HubActivity,
                                            "https://utcazene.hu/"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_facebook),
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_facebook)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@HubActivity,
                                            "https://facebook.com/utcazene"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_instagram),
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_instagram)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@HubActivity,
                                            "https://instagram.com/utcazene"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                            }
                        }
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@HubActivity,
                                        SupportActivity::class.java
                                    )
                                )
                            },
                            imageVector = Icons.Default.Code,
                            title = stringResource(id = R.string.made_by_csaki),
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.musicians),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ElevatedFilterChip(
                                modifier = Modifier.padding(8.dp),
                                selected = isOnlyPinned,
                                onClick = { isOnlyPinned = !isOnlyPinned },
                                label = { Text(text = stringResource(id = R.string.filter_pinned)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isOnlyPinned) Icons.Default.Star
                                        else Icons.Default.StarBorder,
                                        contentDescription = null,
                                    )
                                },
                            )
                            musicians.flatMap { it.tags ?: listOf() }
                                .distinct()
                                .map { tag ->
                                    ElevatedFilterChip(
                                        modifier = Modifier.padding(8.dp),
                                        selected = filterTags.contains(tag),
                                        onClick = {
                                            filterTags =
                                                if (filterTags.contains(tag)) filterTags - tag
                                                else filterTags + tag
                                        },
                                        label = { Text(text = stringResource(id = tag)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (filterTags.contains(tag)) Icons.Filled.Label
                                                else Icons.Outlined.Label,
                                                contentDescription = null,
                                            )
                                        }
                                    )
                                }
                            musicians.flatMap { it.years ?: listOf() }
                                .distinct()
                                .map { year ->
                                    ElevatedFilterChip(
                                        modifier = Modifier.padding(8.dp),
                                        selected = filterYears.contains(year),
                                        onClick = {
                                            filterYears =
                                                if (filterYears.contains(year)) filterYears - year
                                                else filterYears + year
                                        },
                                        label = { Text(text = "$year") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (filterYears.contains(year)) Icons.Filled.Label
                                                else Icons.Outlined.Label,
                                                contentDescription = null,
                                            )
                                        }
                                    )
                                }
                        }
                        visibleMusicians.map { musician ->
                            MusicianCard(
                                modifier = Modifier.padding(8.dp),
                                musician = musician,
                                isPinned = musiciansPinned.contains(musician),
                                onPinnedChangeRequest = {
                                    musiciansPinned = if (it) musiciansPinned + musician
                                    else musiciansPinned - musician
                                },
                                showYears = true,
                            )
                        }
                        TextButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {},
                        ) {
                            Text(text = "Help me expand this list")
                        }
                    }
                }
            }
        }
    }
}