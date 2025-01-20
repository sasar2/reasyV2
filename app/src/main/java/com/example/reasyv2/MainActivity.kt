package com.example.reasyv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.repository.BusinessRepository
import com.example.reasy.navigation.ReasyNavGraph
import com.example.reasy.viewmodel.BusinessViewModel
import com.example.reasyv2.ui.theme.ReasyV2Theme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReasyV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    ReasyNavGraph(navController = navController)
                    //BusinessDetailsScreen()


                }
            }
        }
    }
}


fun generateTimeSlots(workingHours: String): List<String> {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val (start, end) = workingHours.split("-").map { LocalTime.parse(it, formatter) }
    val slots = mutableListOf<String>()

    var current = start
    while (current.plusMinutes(30).isBefore(end) || current.plusMinutes(30) == end) {
        val next = current.plusMinutes(30)
        slots.add("${current.format(formatter)} - ${next.format(formatter)}")
        current = next
    }
    return slots
}


@Composable
fun BusinessDetailsScreen() {
    // Fetch a single business by ID

    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    val businessRepository = BusinessRepository(database.businessDao())
    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModel.BusinessViewModelFactory(businessRepository)
    )
    val business by businessViewModel.getBusinessById(1).observeAsState()

    business?.let { selectedBusiness ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = selectedBusiness.name,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Working Hours: ${selectedBusiness.workingHours}",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Generate time slots
            val timeSlots = remember(selectedBusiness.workingHours) {
                generateTimeSlots(selectedBusiness.workingHours)
            }

            // ✅ Wrap LazyVerticalGrid in a Box with fixed height to avoid scroll conflicts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Adjust height as needed
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // 2-column grid
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeSlots.size) { index ->
                        TimeSlotItem(time = timeSlots[index])
                    }
                }
            }
        }
    } ?: run {
        Text("Loading business details...", modifier = Modifier.padding(16.dp))
    }
}


@Composable
fun BusinessList() {

    Text("Hello World")

    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    val businessRepository = BusinessRepository(database.businessDao())
    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModel.BusinessViewModelFactory(businessRepository)
    )
    val businesses by businessViewModel.getAllBusinesses().observeAsState(initial = emptyList())

    // Collect businesses from database

    LazyColumn {
        items(businesses) { business ->
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(text = business.name, fontSize = 24.sp, modifier = Modifier.padding(16.dp))
                val timeSlots = remember(business.workingHours) { generateTimeSlots(business.workingHours) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),

                    ) {
                        items(timeSlots.size) { index ->
                            TimeSlotItem(time = timeSlots[index])
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotItem(time: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            fontSize = 18.sp,
            color = Color.White
        )
    }

}
