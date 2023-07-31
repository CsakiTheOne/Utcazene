package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.Firestore
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.Auth
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.google.gson.reflect.TypeToken

class HubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HubScreen()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth.onActivityResult(this, requestCode, data)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun HubScreen() {
        UtcazeneTheme {
            val scroll = rememberLazyListState()

            var is2023ended by remember { mutableStateOf(false) }

            var isMenuVisible by remember { mutableStateOf(false) }
            var isWebsitesMenuVisible by remember { mutableStateOf(false) }

            var musicians by remember { mutableStateOf(listOf<Musician>()) }
            var musiciansPinned by remember { mutableStateOf<List<Musician>>(listOf()) }
            var musiciansQuery by remember { mutableStateOf("") }
            var filterTags by remember { mutableStateOf(listOf<String>()) }
            var filterYears by remember { mutableStateOf(listOf<Int>()) }
            var filterCountries by remember { mutableStateOf(listOf<String>()) }
            val visibleMusicians by remember(musicians, filterTags, filterYears, filterCountries, musiciansQuery) {
                mutableStateOf(
                    musicians
                        .filter { filterTags.isEmpty() || it.tags?.containsAll(filterTags) == true }
                        .filter { filterYears.isEmpty() || it.years?.containsAll(filterYears) == true }
                        .filter { filterCountries.isEmpty() || filterCountries.contains(it.country) }
                        .filter {
                            musiciansQuery.length < 3 ||
                                    it.name.toLowerCase()
                                        .contains(
                                            musiciansQuery.toLowerCase()
                                        )
                        }
                        .sortedBy { it.name }
                )
            }

            LaunchedEffect(Unit) {
                Firestore.Musicians.getAll {
                    musicians = it
                }
                is2023ended = PreferenceManager.getDefaultSharedPreferences(this@HubActivity)
                    .getBoolean("is2023ended", false)
            }

            ListPreferenceHolder(
                id = "authorsPinned",
                value = musiciansPinned,
                onValueChanged = { musiciansPinned = it.toList().sortedBy { m -> m.name } },
                type = object : TypeToken<Musician>() {}.type,
            )

            if (!is2023ended) {
                AlertDialog(
                    title = { Text(text = "Thank you for joining Utcazene 2023") },
                    text = {
                        Text(
                            text = "The party ended, but the fun is not over! Here you will be " +
                                    "able to check out all musicians who ever performed at " +
                                    "Utcazene. But first we have to clean up some things. I " +
                                    "plan to rewrite this app so we never have to delete your " +
                                    "favorite musicians or any other user data, but now let's " +
                                    "start with a clean slate. You may need to restart the app " +
                                    "to apply all changes."
                        )
                    },
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                PreferenceManager.getDefaultSharedPreferences(this@HubActivity).edit {
                                    clear()
                                    putBoolean("is2023ended", true)
                                    commit()
                                    finishAffinity()
                                }
                            },
                        ) {
                            Text(text = "Clean up and continue")
                        }
                    },
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Surface(
                        modifier = Modifier.zIndex(2f),
                        shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
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
                                        if (Auth.isSignedInState) {
                                            DropdownMenuItem(
                                                text = { Text(text = "Admin dashboard") },
                                                onClick = {
                                                    Firestore.Users.isSelfAdmin {
                                                        if (!it) {
                                                            Toast.makeText(
                                                                this@HubActivity,
                                                                "Access denied",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            return@isSelfAdmin
                                                        }
                                                        startActivity(
                                                            Intent(
                                                                this@HubActivity,
                                                                AdminActivity::class.java
                                                            )
                                                        )
                                                    }
                                                    isMenuVisible = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(text = "Sign out") },
                                                onClick = {
                                                    Auth.signOut()
                                                    isMenuVisible = false
                                                }
                                            )
                                        }
                                        else {
                                            DropdownMenuItem(
                                                text = { Text(text = "Sign in with Google") },
                                                onClick = {
                                                    Auth.signInBegin(this@HubActivity)
                                                    isMenuVisible = false
                                                }
                                            )
                                        }
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
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(8.dp),
                        state = scroll,
                    ) {
                        item {
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
                            AnimatedVisibility(visible = musiciansPinned.isNotEmpty()) {
                                Column {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = stringResource(id = R.string.filter_pinned),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    ) {
                                        musiciansPinned.map { musician ->
                                            MusicianCard(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .widthIn(max = 300.dp),
                                                musician = musician,
                                                isPinned = musiciansPinned.contains(musician),
                                                onPinnedChangeRequest = {
                                                    musiciansPinned = if (it) musiciansPinned + musician
                                                    else musiciansPinned - musician
                                                },
                                                showYears = true,
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = stringResource(id = R.string.musicians),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                value = musiciansQuery,
                                onValueChange = { musiciansQuery = it },
                                placeholder = {
                                    Text(text = stringResource(id = R.string.search_by_name))
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    AnimatedVisibility(visible = musiciansQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { musiciansQuery = "" },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
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
                                            label = { Text(text = stringResource(id = Musician.tagStrings[tag]!!)) },
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
                                                    imageVector = if (filterYears.contains(year)) Icons.Filled.DateRange
                                                    else Icons.Outlined.DateRange,
                                                    contentDescription = null,
                                                )
                                            }
                                        )
                                    }
                                musicians.map { it.country ?: "" }
                                    .distinct()
                                    .map { country ->
                                        ElevatedFilterChip(
                                            modifier = Modifier.padding(8.dp),
                                            selected = filterCountries.contains(country),
                                            onClick = {
                                                filterCountries =
                                                    if (filterCountries.contains(country)) filterCountries - country
                                                    else filterCountries + country
                                            },
                                            label = { Text(text = "${Musician.countryFlags[country] ?: ""} $country") },
                                        )
                                    }
                            }
                            AnimatedVisibility(visible = musicians.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        items(items = visibleMusicians, key = { it.name }) { musician ->
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
                        item {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.hub_list_expand_info),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}