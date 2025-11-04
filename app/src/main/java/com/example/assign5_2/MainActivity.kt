package com.example.assign5_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.assign5_2.ui.theme.Assign5_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assign5_2Theme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Notes : Screen("notes", "Notes", Icons.Default.Menu)
    data object Tasks : Screen("tasks", "Tasks", Icons.Default.Check)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
}

// A list of all our screens to easily iterate over for the navigation bar.
val screens = listOf(
    Screen.Notes,
    Screen.Tasks,
    Screen.Calendar
)

@Composable
fun MainScreen(){
    // 1. Create a NavController. `rememberNavController()` creates and remembers it
    // across recompositions. This is the heart of our navigation system.
    val navController = rememberNavController()

    // 2. Use Scaffold, a layout that provides slots for top bars, bottom bars,
    // floating action buttons, and the main content.
    Scaffold(
        bottomBar = {
            // Our custom bottom navigation bar.
            NavigationBar {
                // 3. Get the current back stack entry. This tells us which screen is currently displayed.
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 4. Iterate over our list of screens to create a navigation item for each one.
                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) }, // The text label for the item.
                        icon = { Icon(screen.icon, contentDescription = screen.title) }, // The icon for the item.

                        // 5. Determine if this item is currently selected.
                        // We check if the current route is part of the destination's hierarchy.
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,

                        // 6. Define the click action for the item.
                        onClick = {
                            // This is the core navigation logic.
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // Save the state of the screen you're leaving.
                                }
                                // Avoid multiple copies of the same destination when re-selecting the same item.
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item.
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 7. Define the NavHost, which is the container for our screen content.
        // The content of the NavHost changes based on the current route.
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route, // The first screen to show.
            modifier = Modifier.padding(innerPadding) // Apply padding from the Scaffold.
        ) {
            // Define a composable for each screen in our navigation graph.
            composable(Screen.Notes.route) { Text("Notes") }
            composable(Screen.Tasks.route) { Text("Tasks") }
            composable(Screen.Calendar.route) { Text("Calendar") }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Assign5_2Theme {
        Greeting("Android")
    }
}