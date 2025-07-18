package com.csakitheone.streetmusic.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.Firestore
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.hub.HubTabMain
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.Auth
import com.csakitheone.streetmusic.util.CustomTabsManager
import java.time.LocalDate

class HubActivity : ComponentActivity() {
    private val TAB_MAIN = "main"
    private val TAB_BROWSE = "browse"
    private val TAB_DATA = "data"
    private var selectedTab by mutableStateOf(TAB_MAIN)

    private var filterTags by mutableStateOf(listOf<String>())
    private var filterYears by mutableStateOf(listOf<Int>())
    private var filterCountries by mutableStateOf(listOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HubScreen()
        }
        enableEdgeToEdge()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth.onActivityResult(this, requestCode, data)
    }

    private fun clearFilters() {
        filterTags = listOf()
        filterYears = listOf()
        filterCountries = listOf()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
        ExperimentalMaterial3ExpressiveApi::class
    )
    @Preview
    @Composable
    fun HubScreen() {
        UtcazeneTheme(
            isTintingNavbar = false,
        ) {
            val colorScheme = MaterialTheme.colorScheme
            val scroll = rememberLazyListState()

            var isWebsitesMenuVisible by remember { mutableStateOf(false) }
            val searchFocusRequester = remember { FocusRequester() }
            var isSearchVisible by remember { mutableStateOf(false) }

            var musicians by remember { mutableStateOf(listOf<Musician>()) }
            var musiciansQuery by remember(isSearchVisible) { mutableStateOf("") }

            LaunchedEffect(Unit) {
                Firestore.Musicians.getAll {
                    musicians = it
                }
                window.navigationBarColor = colorScheme.surface.toArgb()
            }

            LaunchedEffect(selectedTab) {
                if (selectedTab != TAB_BROWSE) isSearchVisible = false
                scroll.scrollToItem(0)
            }

            LaunchedEffect(isSearchVisible) {
                if (isSearchVisible) searchFocusRequester.requestFocus()
            }

            BackHandler(
                selectedTab == TAB_BROWSE && (filterTags.isNotEmpty() || filterYears.isNotEmpty() || filterCountries.isNotEmpty())
            ) {
                clearFilters()
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                //TODO translate
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
                        shadowElevation = if (scroll.canScrollBackward || isSearchVisible) 16.dp
                        else 0.dp,
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AnimatedContent(
                            targetState = isSearchVisible,
                            label = "TopSearch"
                        ) { targetState ->
                            if (targetState) {
                                TextField(
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .focusRequester(searchFocusRequester),
                                    value = musiciansQuery,
                                    onValueChange = { musiciansQuery = it },
                                    singleLine = true,
                                    placeholder = {
                                        Text(text = stringResource(id = R.string.search_by_name))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                musiciansQuery = ""
                                                isSearchVisible = false
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                )
                            } else {
                                TopAppBar(
                                    title = { Text(text = stringResource(id = R.string.app_name)) },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                isWebsitesMenuVisible = true
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Web,
                                                contentDescription = null,
                                            )
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
                                        IconButton(
                                            onClick = {
                                                musicians = listOf()
                                                Firestore.Musicians.getAll {
                                                    musicians = it
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedTab = TAB_BROWSE
                                                isSearchVisible = true
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        AnimatedVisibility(visible = musicians.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                LoadingIndicator()
                            }
                        }
                        AnimatedVisibility(visible = musicians.isNotEmpty()) {
                            AnimatedContent(targetState = selectedTab, label = "TabChange") { tab ->
                                when (tab) {
                                    TAB_MAIN -> HubTabMain(
                                        scrollState = scroll,
                                        musicians = musicians,
                                        onBrowseRequest = { countries ->
                                            clearFilters()
                                            filterCountries = countries
                                            selectedTab = TAB_BROWSE
                                        },
                                    )

                                    TAB_BROWSE -> TabBrowse(
                                        scrollState = scroll,
                                        musicians = musicians,
                                        musiciansQuery = musiciansQuery,
                                    )

                                    TAB_DATA -> TabData(
                                        scrollState = scroll,
                                    )
                                }
                            }
                        }
                    }
                    NavigationBar(
                        tonalElevation = if (scroll.canScrollForward) 2.dp else 0.dp,
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == TAB_MAIN,
                            onClick = { selectedTab = TAB_MAIN },
                            icon = {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            },
                        )
                        NavigationBarItem(
                            selected = selectedTab == TAB_BROWSE,
                            onClick = { selectedTab = TAB_BROWSE },
                            icon = {
                                Icon(imageVector = Icons.Default.List, contentDescription = null)
                            },
                        )
                        NavigationBarItem(
                            selected = selectedTab == TAB_DATA,
                            onClick = { selectedTab = TAB_DATA },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            },
                        )
                    }
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TabBrowse(
        scrollState: LazyListState,
        musicians: List<Musician>,
        musiciansQuery: String,
    ) {
        val visibleMusicians by remember(
            musicians,
            filterTags,
            filterYears,
            filterCountries,
            musiciansQuery,
        ) {
            mutableStateOf(
                musicians
                    .asSequence()
                    .filter { filterTags.isEmpty() || it.tags?.containsAll(filterTags) == true }
                    .filter { filterYears.isEmpty() || it.years?.containsAll(filterYears) == true }
                    .filter { filterCountries.isEmpty() || filterCountries.contains(it.country) }
                    .filter {
                        musiciansQuery.length < 3 ||
                                it.name.lowercase()
                                    .contains(
                                        musiciansQuery.lowercase()
                                    )
                    }
                    .sortedBy { it.name }
                    .toList()
            )
        }

        LazyColumn(
            state = scrollState,
        ) {
            item {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.filters),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(visible = filterTags.isNotEmpty() || filterYears.isNotEmpty() || filterCountries.isNotEmpty()) {
                        Button(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = {
                                clearFilters()
                            },
                        ) {
                            Text(text = stringResource(id = R.string.clear_all_filters))
                        }
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    (2004..LocalDate.now().year)
                        .reversed()
                        .map { year ->
                            ElevatedFilterChip(
                                enabled = musicians.any { it.years?.contains(year) == true },
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
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    musicians.mapNotNull { it.country }
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
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.musicians),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                AnimatedVisibility(visible = musicians.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
            items(items = visibleMusicians, key = { it.name }) { musician ->
                MusicianCard(
                    modifier = Modifier.padding(8.dp),
                    musician = musician,
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

    @Composable
    fun TabData(
        scrollState: LazyListState,
    ) {
        LazyColumn(state = scrollState) {
            item {
                if (!Auth.isSignedInState) {
                    MenuCard(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            Auth.signInBegin(this@HubActivity)
                        },
                        imageVector = Icons.Default.Login,
                        title = stringResource(id = R.string.sign_in_with_google),
                    )
                } else {
                    MenuCard(
                        modifier = Modifier.padding(8.dp),
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
                        },
                        imageVector = Icons.Default.Dashboard,
                        title = stringResource(id = R.string.admin_dashboard),
                    )
                    MenuCard(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            Firestore.Musicians.export {
                                val clipboardManager = getSystemService(ClipboardManager::class.java)
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("Musicians", it))
                                Toast.makeText(
                                    this@HubActivity,
                                    "Copied to clipboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        imageVector = Icons.Default.ImportExport,
                        title = stringResource(id = R.string.export_database),
                    )
                    MenuCard(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            Auth.signOut()
                        },
                        imageVector = Icons.Default.Logout,
                        title = stringResource(id = R.string.sign_out),
                    )
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
            }
        }
    }
}