package com.csakitheone.streetmusic.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.Firestore
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.BigMusicianCard
import com.csakitheone.streetmusic.ui.components.CompactAdminMusicianCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import java.time.LocalDate

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun AdminScreen() {
        UtcazeneTheme {
            val scroll = rememberLazyListState()

            var isSelfAdmin by remember { mutableStateOf(false) }

            var musicians by remember { mutableStateOf(listOf<Musician>()) }
            var musiciansQuery by remember { mutableStateOf("") }
            var filterTags by remember { mutableStateOf(listOf<String>()) }
            var filterYears by remember { mutableStateOf(listOf<Int>()) }
            var showOnlyPartial by remember { mutableStateOf(false) }
            val visibleMusicians by remember(
                musicians,
                filterTags,
                filterYears,
                showOnlyPartial,
                musiciansQuery,
            ) {
                mutableStateOf(
                    musicians
                        .asSequence()
                        .filter { filterTags.isEmpty() || it.tags?.containsAll(filterTags) == true }
                        .filter { filterYears.isEmpty() || it.years?.containsAll(filterYears) == true }
                        .filter { !showOnlyPartial || it.isIncomplete() }
                        .filter {
                            musiciansQuery.length < 3 ||
                                    it.name.lowercase()
                                        .contains(
                                            musiciansQuery.lowercase()
                                        )
                        }
                        .sortedBy { it.name }
                )
            }

            val SYNC_STATE_SYNC = "In sync with database if no one else changed anything"
            val SYNC_STATE_LOCAL_CHANGES = "Local changes made! Save or dismiss to be in sync!"
            var syncState by remember { mutableStateOf(SYNC_STATE_LOCAL_CHANGES) }
            var isMenuVisible by remember { mutableStateOf(false) }

            var selectedMusician: Musician? by remember { mutableStateOf(null) }
            var isMassAddDialogVisible by remember { mutableStateOf(false) }
            var massAddNames by remember(isMassAddDialogVisible) { mutableStateOf("") }
            var massAddTags by remember(isMassAddDialogVisible) { mutableStateOf(listOf<String>()) }
            var massAddYear by remember(isMassAddDialogVisible) { mutableStateOf(LocalDate.now().year) }

            BackHandler(syncState != SYNC_STATE_SYNC) {
                Toast.makeText(this, "There are unsaved changes!", Toast.LENGTH_SHORT).show()
                isMenuVisible = true
            }

            LaunchedEffect(Unit) {
                Firestore.Users.isSelfAdmin {
                    if (!it) finish()
                    isSelfAdmin = it
                    Firestore.Musicians.getAll { musiciansList ->
                        musicians = musiciansList
                        syncState = SYNC_STATE_SYNC
                    }
                }
            }

            if (selectedMusician != null) {
                EditMusicianDialog(
                    musician = selectedMusician ?: Musician(),
                    onDismissRequest = { selectedMusician = null },
                    onSaveRequest = {
                        syncState = SYNC_STATE_LOCAL_CHANGES
                        if (selectedMusician != null && musicians.contains(selectedMusician)) {
                            musicians -= selectedMusician!!
                        }
                        if (it != null) musicians += it
                        selectedMusician = null
                    },
                )
            }

            if (isMassAddDialogVisible) {
                AlertDialog(
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = { Text(text = "Mass-add musicians") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "Write down the names of every musician you'd like to add. " +
                                        "You can also write countries in parentheses. " +
                                        "One name per line. Don't worry if you write someone " +
                                        "who's already in the database, you can check for " +
                                        "duplicates and merge later.",
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                value = massAddNames,
                                onValueChange = { massAddNames = it },
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            ) {
                                Musician.tagStrings.map {
                                    FilterChip(
                                        modifier = Modifier.padding(8.dp),
                                        selected = massAddTags.contains(it.key),
                                        onClick = {
                                            massAddTags =
                                                if (massAddTags.contains(it.key)) massAddTags - it.key
                                                else massAddTags + it.key
                                        },
                                        label = { Text(text = stringResource(id = it.value)) },
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            ) {
                                (2004..LocalDate.now().year).reversed().map { year ->
                                    FilterChip(
                                        modifier = Modifier.padding(8.dp),
                                        selected = massAddYear == year,
                                        onClick = { massAddYear = year },
                                        label = { Text(text = year.toString()) },
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "${massAddNames.split('\n').size} musicians will be added.",
                            )
                        }
                    },
                    onDismissRequest = { isMassAddDialogVisible = false },
                    dismissButton = {
                        TextButton(onClick = { isMassAddDialogVisible = false }) {
                            Text(text = "Dismiss")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                musicians = listOf()
                                Firestore.Musicians.addAll(
                                    massAddNames
                                        .split('\n')
                                        .map {
                                            Musician(
                                                name = it.substringBefore('(').trim(),
                                                country = if (it.contains('(')) it
                                                    .substringAfter('(')
                                                    .substringBefore(')')
                                                else null,
                                                tags = massAddTags,
                                                years = listOf(massAddYear),
                                            )
                                        }
                                ) {
                                    isMassAddDialogVisible = false
                                    Firestore.Musicians.getAll {
                                        musicians = it
                                        syncState = SYNC_STATE_SYNC
                                    }
                                }
                            },
                        ) {
                            Text(text = "Add all")
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
                            title = {
                                Column {
                                    Text(text = "Admin dashboard")
                                    Text(
                                        text = syncState,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (syncState == SYNC_STATE_SYNC) MaterialTheme.colorScheme.onBackground
                                        else MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            navigationIcon = {
                                AnimatedVisibility(visible = syncState == SYNC_STATE_SYNC) {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { isMenuVisible = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                    )
                                    DropdownMenu(
                                        expanded = isMenuVisible,
                                        onDismissRequest = { isMenuVisible = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Dismiss / Refresh") },
                                            onClick = {
                                                isMenuVisible = false
                                                musicians = listOf()
                                                Firestore.Musicians.getAll {
                                                    musicians = it
                                                    syncState = SYNC_STATE_SYNC
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                        if (syncState == SYNC_STATE_LOCAL_CHANGES) {
                                            DropdownMenuItem(
                                                text = { Text(text = "Save") },
                                                onClick = {
                                                    isMenuVisible = false
                                                    Firestore.Musicians.setAll(musicians) {
                                                        Firestore.Musicians.getAll {
                                                            musicians = it
                                                            syncState = SYNC_STATE_SYNC
                                                        }
                                                    }
                                                    musicians = listOf()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudUpload,
                                                        contentDescription = null,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                    if (!isSelfAdmin || musicians.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                        ) {
                            LazyColumn(state = scroll) {
                                item {
                                    MenuCard(
                                        modifier = Modifier.padding(8.dp),
                                        onClick = { isMassAddDialogVisible = true },
                                        imageVector = Icons.Default.GroupAdd,
                                        title = "Mass-add musicians",
                                    )
                                    MenuCard(
                                        modifier = Modifier.padding(8.dp),
                                        onClick = {
                                            syncState = SYNC_STATE_LOCAL_CHANGES
                                            try {
                                                musicians = listOf()
                                                Firestore.Musicians.mergeYears {
                                                    Firestore.Musicians.getAll {
                                                        musicians = it
                                                        syncState = SYNC_STATE_SYNC
                                                    }
                                                }
                                            } catch (ex: Exception) {
                                                Toast.makeText(
                                                    this@AdminActivity,
                                                    "${ex.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Firestore.Musicians.getAll {
                                                    musicians = it
                                                    syncState = SYNC_STATE_SYNC
                                                }
                                            }
                                        },
                                        imageVector = Icons.Default.Merge,
                                        title = "Merge",
                                    )
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = "Musicians (${musicians.size})",
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
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null
                                            )
                                        },
                                        trailingIcon = {
                                            Row {
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
                                            }
                                        },
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ElevatedFilterChip(
                                            modifier = Modifier.padding(8.dp),
                                            selected = showOnlyPartial,
                                            onClick = { showOnlyPartial = !showOnlyPartial },
                                            label = {
                                                Text(text = "Has missing info (${musicians.count { it.isIncomplete() }})")
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (showOnlyPartial) Icons.Filled.Label
                                                    else Icons.Outlined.Label,
                                                    contentDescription = null,
                                                )
                                            }
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
                                            .sortedDescending()
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
                                    }
                                    if (visibleMusicians.toList().isNotEmpty()) {
                                        MusicianCard(
                                            modifier = Modifier.padding(8.dp),
                                            musician = visibleMusicians.first(),
                                        )
                                        BigMusicianCard(
                                            modifier = Modifier.padding(8.dp),
                                            musician = visibleMusicians.first(),
                                        )
                                    }
                                }
                                items(
                                    items = visibleMusicians.toList(),
                                    key = { it.name + it.years }) { musician ->
                                    CompactAdminMusicianCard(
                                        modifier = Modifier.padding(8.dp),
                                        musician = musician,
                                        onClick = { selectedMusician = musician },
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                            FloatingActionButton(
                                modifier = Modifier.padding(16.dp),
                                onClick = {
                                    selectedMusician = Musician()
                                },
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun EditMusicianDialog(
        musician: Musician = Musician(),
        onDismissRequest: () -> Unit = {},
        onSaveRequest: (Musician?) -> Unit = {},
    ) {
        var currentMusician by remember { mutableStateOf(musician) }

        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(text = "Name") },
                    value = currentMusician.name,
                    onValueChange = {
                        currentMusician = currentMusician.copy(name = it)
                    },
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(text = "Description") },
                        value = currentMusician.description ?: "",
                        onValueChange = {
                            currentMusician = currentMusician.copy(description = it)
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(text = "Country ${currentMusician.getFlag()}") },
                        value = currentMusician.country ?: "",
                        onValueChange = {
                            currentMusician = currentMusician.copy(country = it.uppercase())
                        },
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(text = "Image URL") },
                        value = currentMusician.imageUrl ?: "",
                        onValueChange = {
                            currentMusician = currentMusician.copy(imageUrl = it)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    CustomTabsManager.open(
                                        this@AdminActivity,
                                        currentMusician.imageUrl
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                )
                            }
                        },
                        supportingText = {
                            if (currentMusician.imageUrl?.startsWith("https://utcazene.hu/") == true) {
                                Text(text = "The official website is not a sustainable source!")
                            } else if (currentMusician.imageUrl?.startsWith("https://web.archive.org") == true) {
                                Text(text = "Reliable but slow source.")
                            }
                        },
                        isError = currentMusician.imageUrl?.startsWith("https://utcazene.hu/") == true,
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(text = "YouTube URL") },
                        value = currentMusician.youtubeUrl ?: "",
                        onValueChange = {
                            currentMusician = currentMusician.copy(youtubeUrl = it)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    CustomTabsManager.open(
                                        this@AdminActivity,
                                        currentMusician.youtubeUrl
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        Musician.tagStrings.map {
                            FilterChip(
                                modifier = Modifier.padding(8.dp),
                                selected = currentMusician.tags?.contains(it.key) == true,
                                onClick = {
                                    currentMusician = currentMusician.copy(
                                        tags = if (currentMusician.tags?.contains(it.key) == true) (currentMusician.tags
                                            ?: listOf()) - it.key
                                        else (currentMusician.tags ?: listOf()) + it.key
                                    )
                                },
                                label = { Text(text = stringResource(id = it.value)) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        (2004..LocalDate.now().year).reversed().map { year ->
                            FilterChip(
                                modifier = Modifier.padding(8.dp),
                                selected = currentMusician.years?.contains(year) == true,
                                onClick = {
                                    currentMusician = currentMusician.copy(
                                        years = if (currentMusician.years?.contains(year) == true) (currentMusician.years
                                            ?: listOf()) - year
                                        else (currentMusician.years ?: listOf()) + year
                                    )
                                },
                                label = { Text(text = year.toString()) },
                            )
                        }
                    }
                    Button(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        onClick = { onSaveRequest(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    ) {
                        Text(text = "Delete")
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() },
                ) {
                    Text(text = "Dismiss")
                }
            },
            confirmButton = {
                TextButton(
                    enabled = currentMusician.name.isNotBlank(),
                    onClick = { onSaveRequest(currentMusician) },
                ) {
                    Text(text = "Save")
                }
            },
        )
    }
}