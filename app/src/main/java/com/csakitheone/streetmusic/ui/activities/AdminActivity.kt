package com.csakitheone.streetmusic.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
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
import com.csakitheone.streetmusic.ui.components.CompactAdminMusicianCard
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
            var showOnlyPartial by remember { mutableStateOf(false) }
            val visibleMusicians by remember(musicians, musiciansQuery, showOnlyPartial) {
                mutableStateOf(
                    musicians
                        .filter { !showOnlyPartial || it.isIncomplete() }
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

            val SYNC_STATE_SYNC = "In sync with database if no one else changed anything"
            val SYNC_STATE_LOCAL_CHANGES = "Local changes made! Save or dismiss to be in sync!"
            var syncState by remember { mutableStateOf(SYNC_STATE_LOCAL_CHANGES) }
            var isMenuVisible by remember { mutableStateOf(false) }

            var selectedMusician: Musician? by remember { mutableStateOf(null) }
            var isMassAddDialogVisible by remember { mutableStateOf(false) }
            var massAddNames by remember(isMassAddDialogVisible) { mutableStateOf("") }
            var massAddYear by remember(isMassAddDialogVisible) { mutableStateOf(LocalDate.now().year) }

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
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "Write down the names of every musician you'd like to add. " +
                                        "One name per comma. Don't worry if you write someone " +
                                        "who's already in the database, you can check for " +
                                        "duplicates and merge later.",
                            )
                            TextField(
                                modifier = Modifier.padding(8.dp),
                                value = massAddNames,
                                onValueChange = { massAddNames = it },
                            )
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
                                text = "${massAddNames.split(',').size} musicians will be added.",
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
                                Firestore.Musicians.addAll(
                                    massAddNames
                                        .split(',')
                                        .map { Musician(name = it.trim(), years = listOf(massAddYear)) }
                                ) {
                                    musicians = listOf()
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
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                    )
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
                                            text = { Text(text = "Mass-add musicians") },
                                            onClick = {
                                                isMassAddDialogVisible = true
                                                isMenuVisible = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.GroupAdd,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(text = "Merge years") },
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
                                                isMenuVisible = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Merge,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                        if (syncState == SYNC_STATE_LOCAL_CHANGES) {
                                            DropdownMenuItem(
                                                text = { Text(text = "Dismiss") },
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
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
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
                            LazyColumn(
                                modifier = Modifier
                                    .padding(8.dp),
                                state = scroll,
                            ) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = "Musicians",
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
                                            label = { Text(text = "Has missing info") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (showOnlyPartial) Icons.Filled.Label
                                                    else Icons.Outlined.Label,
                                                    contentDescription = null,
                                                )
                                            }
                                        )
                                    }
                                }
                                items(items = visibleMusicians, key = { it.name }) { musician ->
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
                    TextButton(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        onClick = { onSaveRequest(null) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
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