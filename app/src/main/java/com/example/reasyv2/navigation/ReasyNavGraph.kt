package com.example.reasy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.screens.Login.LoginScreen
import com.example.reasy.screens.Login.SignUpScreen
import com.example.reasyv2.screens.BusinessListScreen
import com.example.reasyv2.screens.ClientMainScreen
import com.example.reasyv2.screens.ReservationScreen
import com.example.reasyv2.screens.BusinessMainScreen
import com.example.reasyv2.screens.ClientReservationsScreen

@Composable
fun ReasyNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onClientLoginSuccess = { 
                    navController.navigate("client_main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBusinessLoginSuccess = {
                    navController.navigate("business_main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot_password")
                },
                onSignUpClick = {
                    navController.navigate("sign_up")
                }
            )
        }

        composable("sign_up") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("login") {
                        popUpTo("sign_up") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("forgot_password") {
            // TODO: Implement ForgotPasswordScreen
        }

        composable("client_main") {
            ClientMainScreen(
                onSeeAllClick = { category, businesses ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("businesses", businesses)
                    navController.navigate("business_list/$category")
                },
                onBusinessClick = { business ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_business", business)
                    navController.navigate("reservation")
                },
                onViewReservationsClick = {
                    navController.navigate("client_reservations")
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("client_main") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "business_list/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val businesses = navController.previousBackStackEntry?.savedStateHandle?.get<List<BusinessEntity>>("businesses") ?: emptyList()
            BusinessListScreen(
                categoryName = category,
                businesses = businesses,
                onBackClick = { navController.popBackStack() },
                onBusinessClick = { business ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_business", business)
                    navController.navigate("reservation")
                }
            )
        }

        composable("reservation") {
            val business = navController.previousBackStackEntry?.savedStateHandle?.get<BusinessEntity>("selected_business")
            if (business != null) {
                ReservationScreen(
                    business = business,
                    onBackClick = { navController.popBackStack() },
                    onReservationComplete = {
                        navController.navigate("client_main") {
                            popUpTo("client_main") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("business_main") {
            BusinessMainScreen(
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("business_main") { inclusive = true }
                    }
                },
                onEditBusinessClick = { business ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("business", business)
                    navController.navigate("edit_business")
                },
                onRefresh = {
                    navController.navigate("business_main") {
                        popUpTo("business_main") { inclusive = true }
                    }
                }
            )
        }

        composable("edit_business") {
            // TODO
        }

        composable("client_reservations") {
            ClientReservationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 