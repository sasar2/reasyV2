package com.example.reasyv2.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.dao.TimeSlotDao
import com.example.reasy.data.dao.UserDao
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.entity.ReservationEntity
import com.example.reasy.data.entity.TimeSlotEntity
import com.example.reasy.data.repository.ReservationRepository
import com.example.reasy.data.repository.TimeSlotRepository
import com.example.reasy.viewmodel.ReservationViewModel
import com.example.reasy.viewmodel.TimeSlotViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    business: BusinessEntity,
    onBackClick: () -> Unit,
    onReservationComplete: () -> Unit
) {
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    
    // Initialize repositories and view models
    val timeSlotRepository = TimeSlotRepository(database.timeSlotDao())
    val reservationRepository = ReservationRepository(database.reservationDao())
    
    val timeSlotViewModel = TimeSlotViewModel(timeSlotRepository)
    val reservationViewModel = ReservationViewModel(reservationRepository)
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTimeSlot by remember { mutableStateOf<TimeSlotEntity?>(null) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeSlots by timeSlotViewModel.getTimeSlotsByBusinessAndDate(
        business.busId,
        selectedDate.format(dateFormatter)
    ).observeAsState(initial = emptyList())

    // Add this to track visible dates
    val visibleDates = remember(selectedDate) {
        (-3..3).map { offset ->
            selectedDate.plusDays(offset.toLong())
        }.filter { date -> 
            date >= LocalDate.now() 
        }
    }

    // Function moved inside to access business and selectedDate
    fun generateTimeSlots(workingHours: String, reservationTime: Int): List<TimeSlotEntity> {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val (start, end) = workingHours.split("-").map { LocalTime.parse(it.trim(), formatter) }
        val slots = mutableListOf<TimeSlotEntity>()
        
        var current = start
        while (current.plusMinutes(reservationTime.toLong()) <= end) {
            val endTime = current.plusMinutes(reservationTime.toLong())
            slots.add(
                TimeSlotEntity(
                    busId = business.busId,
                    date = selectedDate.format(dateFormatter),
                    startTime = current.format(formatter),
                    endTime = endTime.format(formatter),
                    status = "available"
                )
            )
            current = endTime
        }
        return slots

    }
    val timeSlotDao = database.timeSlotDao()
    val reservationDao = database.reservationDao()
    val coroutineScope = rememberCoroutineScope()

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "reasy_preferences", Context.MODE_PRIVATE
    )
    val loggedInUserId = sharedPreferences.getInt("user_id", -1)

    // Get all reservations for this business's time slots
    val businessReservations by reservationViewModel.getReservationsByBusiness(business.busId)
        .observeAsState(initial = emptyList())

    // Function to check if a time slot is available
    fun isTimeSlotAvailable(timeSlot: TimeSlotEntity): Boolean {
        val reservation = businessReservations.find { it.tmsId == timeSlot.tmsId }
        return when {
            reservation == null -> true // No reservation exists
            reservation.status == "declined" -> true // Reservation was declined
            else -> false // Slot is reserved and not declined
        }
    }

    LaunchedEffect(selectedDate) {
        coroutineScope.launch {
            val generatedSlots = generateTimeSlots(business.workingHours, business.reservationTime)

            generatedSlots.forEach { slot ->
                val exists = timeSlotViewModel.isTimeSlotExists(
                    slot.busId, slot.date, slot.startTime, slot.endTime
                )
                
                if (!exists) {
                    timeSlotDao.insertTimeSlot(slot)
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(business.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Details Section
            item {
                BusinessDetailsSection(business)
            }

            // Date Selection Section
            item {
                DateSelectionSection(
                    dates = visibleDates,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            // Available Time Slots Section
            item {
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Replace items() with a single item containing LazyVerticalGrid
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp) // Adjust height as needed
                ) {
                    items(timeSlots) { timeSlot ->
                        TimeSlotCard(
                            timeSlot = timeSlot,
                            isSelected = timeSlot == selectedTimeSlot,
                            businessReservations = businessReservations,
                            onClick = { selectedTimeSlot = timeSlot }
                        )
                    }
                }
            }

            // Reserve Button
            item {

                Button(
                    onClick = {
                        selectedTimeSlot?.let { timeSlot ->
                            if (isTimeSlotAvailable(timeSlot)) {
                                coroutineScope.launch {
                                    // Update time slot status to reserved
                                    val updatedTimeSlot = timeSlot.copy(status = "reserved")
                                    timeSlotViewModel.updateSlotStatus(updatedTimeSlot, "reserved")

                                    // Create reservation
                                    val reservation = ReservationEntity(
                                        cliId = if (loggedInUserId != -1) loggedInUserId else 1,
                                        busId = business.busId,
                                        tmsId = timeSlot.tmsId,
                                        status = "pending",
                                        createdAt = LocalDate.now().toString()
                                    )
                                    reservationDao.insertReservation(reservation)
                                    onReservationComplete()
                                }
                                Toast.makeText(
                                    context,
                                    "Reservation successful! Reserved at ${timeSlot.date} on ${timeSlot.startTime}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "This time slot is no longer available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    enabled = selectedTimeSlot != null && selectedTimeSlot?.let { isTimeSlotAvailable(it) } == true
                ) {
                    Text("Reserve")
                }
            }
        }
    }
}

@Composable
private fun BusinessDetailsSection(business: BusinessEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = business.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = business.address)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = business.phone)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Working Hours",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = business.workingHours)
            }
        }
    }
}

@Composable
private fun DateSelectionSection(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Column {
        Text(
            text = "Select Date",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dates) { date ->
                DateCard(
                    date = date,
                    isSelected = date == selectedDate,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfWeek.toString().take(3),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TimeSlotCard(
    timeSlot: TimeSlotEntity,
    isSelected: Boolean,
    businessReservations: List<ReservationEntity>,
    onClick: () -> Unit
) {
    val reservation = businessReservations.find { it.tmsId == timeSlot.tmsId }
    val isAvailable = when {
        reservation == null -> true
        reservation.status == "declined" -> true
        else -> false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isAvailable -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        enabled = isAvailable,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${timeSlot.startTime} - ${timeSlot.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            if (!isAvailable) {
                Text(
                    text = when(reservation?.status) {
                        "pending" -> "Pending"
                        "accepted" -> "Reserved"
                        else -> "Unavailable"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 