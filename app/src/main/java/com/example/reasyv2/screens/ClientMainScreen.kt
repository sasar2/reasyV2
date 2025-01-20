package com.example.reasyv2.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.repository.BusinessRepository
import com.example.reasy.viewmodel.BusinessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMainScreen(
    onSeeAllClick: (String, List<BusinessEntity>) -> Unit,
    onBusinessClick: (BusinessEntity) -> Unit,
    onViewReservationsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Initialize ViewModel
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    val businessRepository = BusinessRepository(database.businessDao())
    val businessViewModel: BusinessViewModel = viewModel(
        factory = BusinessViewModel.BusinessViewModelFactory(businessRepository)
    )

    // Collect businesses from database
    val businesses by businessViewModel.getAllBusinesses().observeAsState(initial = emptyList())

    // Group businesses by category
    val businessesByCategory = businesses.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add Reservations Button
                        IconButton(
                            onClick = onViewReservationsClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "View Reservations"
                            )
                        }
                        
                        // Existing Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search businesses...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.Logout, "Logout")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            businessesByCategory.forEach { (category, categoryBusinesses) ->
                CategorySection(
                    categoryName = category,
                    businesses = categoryBusinesses,
                    onSeeAllClick = onSeeAllClick,
                    onBusinessClick = onBusinessClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search businesses...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
    )
}

@Composable
fun CategorySection(
    categoryName: String,
    businesses: List<BusinessEntity>,
    onSeeAllClick: (String, List<BusinessEntity>) -> Unit,
    onBusinessClick: (BusinessEntity) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { onSeeAllClick(categoryName, businesses) }) {
                Text("See All →")
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(businesses) { business ->
                BusinessCard(
                    business = business,
                    onClick = { onBusinessClick(business) }
                )
            }
        }
    }
}

@Composable
fun BusinessCard(
    business: BusinessEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(170.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Business Name
            Text(
                text = business.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = business.description?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = business.rating,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

