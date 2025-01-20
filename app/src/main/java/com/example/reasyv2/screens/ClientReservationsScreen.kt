package com.example.reasyv2.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.repository.ReservationRepository
import com.example.reasy.viewmodel.ReservationViewModel
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.repository.BusinessRepository
import com.example.reasy.viewmodel.BusinessViewModel
import com.example.reasy.data.repository.TimeSlotRepository
import com.example.reasy.viewmodel.TimeSlotViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientReservationsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    
    val businessRepository = BusinessRepository(database.businessDao())
    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModel.BusinessViewModelFactory(businessRepository)
    )
    
    val reservationRepository = ReservationRepository(database.reservationDao())
    val reservationViewModel = ReservationViewModel(reservationRepository)

    val loggedInUserId = context.getSharedPreferences(
        "reasy_preferences", Context.MODE_PRIVATE
    ).getInt("user_id", -1)

    val reservations by reservationViewModel.getReservationsByClient(loggedInUserId)
        .observeAsState(initial = emptyList())

    val businesses by businessViewModel.getAllBusinesses().observeAsState(initial = emptyList())
    val businessMap = businesses.associateBy { it.busId }

    val timeSlotRepository = TimeSlotRepository(database.timeSlotDao())
    val timeSlotViewModel = TimeSlotViewModel(timeSlotRepository)

    val timeSlots = remember { mutableStateMapOf<Int, String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(reservations) {
        reservations.forEach { reservation ->
            if (!timeSlots.containsKey(reservation.tmsId)) {
                coroutineScope.launch {
                    val timeSlot = timeSlotRepository.getTimeSlotById(reservation.tmsId)
                    timeSlot?.let { 
                        timeSlots[it.tmsId] = "${it.date} ${it.startTime}-${it.endTime}"
                    }
                }
            }
        }
    }

    // Add tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "Declined")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Reservations") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
                // Add TabRow
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filter and group reservations by date
            val today = LocalDate.now()
            val filteredReservations = reservations.filter { reservation ->
                val reservationDate = LocalDate.parse(reservation.createdAt)
                when (selectedTab) {
                    0 -> (reservation.status == "pending" || reservation.status == "accepted") && 
                         reservationDate >= today
                    1 -> reservationDate < today
                    else -> reservation.status == "declined"
                }
            }.groupBy { it.createdAt }

            // Sort dates in reverse chronological order
            val sortedDates = filteredReservations.keys.sortedDescending()

            // Show reservations grouped by date
            sortedDates.forEach { date ->
                item {
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(filteredReservations[date] ?: emptyList()) { reservation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            businessMap[reservation.busId]?.let { business ->
                                Text(
                                    text = business.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            
                            Text(
                                text = "Reservation #${reservation.resId}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            timeSlots[reservation.tmsId]?.let { timeSlotInfo ->
                                Text(
                                    text = "Time: $timeSlotInfo",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Text(
                                text = "Created: ${reservation.createdAt}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Status: ${reservation.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when(reservation.status) {
                                    "accepted" -> MaterialTheme.colorScheme.primary
                                    "pending" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }

            // Show message if no reservations
            if (filteredReservations.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "No upcoming reservations"
                                1 -> "No past reservations"
                                else -> "No declined reservations"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Add this helper function to format dates (same as in BusinessMainScreen)
private fun formatDate(dateString: String): String {
    val date = LocalDate.parse(dateString)
    val today = LocalDate.now()
    
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        date == today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }
} 