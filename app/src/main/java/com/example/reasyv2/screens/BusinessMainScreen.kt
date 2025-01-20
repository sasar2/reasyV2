package com.example.reasyv2.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.entity.ReservationEntity
import com.example.reasy.data.repository.BusinessRepository
import com.example.reasy.data.repository.ReservationRepository
import com.example.reasy.data.repository.TimeSlotRepository
import com.example.reasy.viewmodel.BusinessViewModel
import com.example.reasy.viewmodel.ReservationViewModel
import com.example.reasy.viewmodel.TimeSlotViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessMainScreen(
    onLogoutClick: () -> Unit,
    onEditBusinessClick: (BusinessEntity) -> Unit,
    onRefresh: () -> Unit
) {
    // Initialize ViewModels
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)

    val userDao = database.userDao()

    val businessRepository = BusinessRepository(database.businessDao())
    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModel.BusinessViewModelFactory(businessRepository)
    )
    
    val reservationRepository = ReservationRepository(database.reservationDao())
    val reservationViewModel = ReservationViewModel(reservationRepository)

    val timeSlotRepository = TimeSlotRepository(database.timeSlotDao())
    val timeSlotViewModel = TimeSlotViewModel(timeSlotRepository)

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "reasy_preferences", Context.MODE_PRIVATE
    )
    val loggedInUserId = sharedPreferences.getInt("user_id", -1)

    val users = remember { mutableStateMapOf<Int, String>() }
    val timeSlots = remember { mutableStateMapOf<Int, String>() }

    // Collect business data
    val businesses by businessViewModel.getBusinessesByUserId(loggedInUserId).observeAsState(initial = emptyList())
    val selectedBusiness = remember { mutableStateOf<BusinessEntity?>(null) }
    LaunchedEffect(businesses) {
        if (selectedBusiness.value == null && businesses.isNotEmpty()) {
            selectedBusiness.value = businesses.firstOrNull()
        }
    }
    Log.d("BusinessMainScreen", "Businesses: $businesses")
    Log.d("BusinessMainScreen", "Selected Business: ${selectedBusiness.value}")

    // Collect reservations for the selected business
    val reservations by selectedBusiness.value?.let { business ->
        reservationViewModel.getReservationsByBusiness(business.busId)
    }?.observeAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    Log.d("BusinessMainScreen", "Reservations: $reservations")

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(reservations) {
        reservations.forEach { reservation ->
            if (!users.containsKey(reservation.cliId)) {
                coroutineScope.launch {
                    val user = userDao.getUserById(reservation.cliId)
                    user?.let { users[it.usrId] = it.username }
                }
            }
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
    val tabs = listOf("Pending", "Accepted", "Past")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Business Dashboard") },
                    actions = {
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.Logout, "Logout")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Info Card
            selectedBusiness.value?.let { business ->
                item {
                    BusinessInfoCard(
                        business = business,
                        onEditClick = { onEditBusinessClick(business) }
                    )
                }
            }

            // Today's Stats Card (show only in Pending tab)
            if (selectedTab == 0) {
                item {
                    TodayStatsCard(reservations)
                }
            }

            // Filter and group reservations by date
            val today = LocalDate.now()
            val filteredReservations = reservations.filter { reservation ->
                val reservationDate = LocalDate.parse(reservation.createdAt)
                when (selectedTab) {
                    0 -> reservation.status == "pending" && reservationDate >= today
                    1 -> reservation.status == "accepted" && reservationDate >= today
                    else -> reservationDate < today // Past reservations
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

                items(
                    items = filteredReservations[date] ?: emptyList(),
                    key = { it.resId }
                ) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        username = users[reservation.cliId],
                        timeSlotInfo = timeSlots[reservation.tmsId],
                        onAccept = {
                            coroutineScope.launch {
                                reservationViewModel.updateReservation(
                                    reservation.copy(status = "accepted")
                                )
                                onRefresh()
                            }
                        },
                        onDecline = {
                            coroutineScope.launch {
                                reservationViewModel.updateReservation(
                                    reservation.copy(status = "declined")
                                )
                                onRefresh()
                            }
                        },
                        showActions = selectedTab == 0 // Only show actions in Pending tab
                    )
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
                                0 -> "No pending reservations"
                                1 -> "No accepted reservations"
                                else -> "No past reservations"
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

@Composable
private fun BusinessInfoCard(
    business: BusinessEntity,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = business.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit Business")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = business.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Working Hours",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = business.workingHours)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = business.rating)
            }
        }
    }
}

@Composable
private fun TodayStatsCard(reservations: List<ReservationEntity>) {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    
    // Use remember to make stats reactive
    val stats by remember(reservations) {
        mutableStateOf(
            mapOf(
                "accepted" to reservations.count { it.createdAt == today && it.status == "accepted" },
                "pending" to reservations.count { it.createdAt == today && it.status == "pending" },
                "declined" to reservations.count { it.createdAt == today && it.status == "declined" }
            )
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Today's Statistics",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Accepted",
                    value = stats["accepted"].toString()
                )
                StatItem(
                    icon = Icons.Default.Pending,
                    label = "Pending",
                    value = stats["pending"].toString()
                )
                StatItem(
                    icon = Icons.Default.Cancel,
                    label = "Declined",
                    value = stats["declined"].toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReservationCard(
    reservation: ReservationEntity,
    username: String?,
    timeSlotInfo: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    showActions: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reservation #${reservation.resId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Client: ${username ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    timeSlotInfo?.let {
                        Text(
                            text = "Time: $it",
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
                
                if (showActions) {
                    Row {
                        IconButton(onClick = onAccept) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Accept",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDecline) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Decline",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Add this helper function to format dates
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