package com.example.assign5_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.assign5_2.ui.theme.Assign5_2Theme


data class CheckableItem(var label: String, var isChecked: Boolean = false)


class MainActivity : ComponentActivity() {
    private val viewModel: MyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assign5_2Theme {
                MainScreen(viewModel)
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

class MyViewModel : ViewModel() {
    var notes by mutableStateOf(listOf<String>())
    var tasks by mutableStateOf(listOf<CheckableItem>())

    fun addNote(note: String) {
        notes = notes + note
    }
    fun addTask(task: CheckableItem) {
        tasks = tasks + task
    }
    fun toggleTaskChecked(index: Int, isChecked: Boolean) {
        val updatedTasks = tasks.toMutableList()
        val updatedItem = updatedTasks[index].copy(isChecked = isChecked)
        updatedTasks[index] = updatedItem
        tasks = updatedTasks
    }
}

@Composable
fun MainScreen(viewModel: MyViewModel){
    // 1. Create a NavController. `rememberNavController()` creates and remembers it
    // across recompositions. This is the heart of our navigation system.
    val navController = rememberNavController()

    // 2. Use Scaffold, a layout that provides slots for top bars, bottom bars,
    // floating action buttons, and the main content.
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route?.substringBefore('?') // normalize
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
            composable(Screen.Notes.route, enterTransition ={ slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right) }, exitTransition = { fadeOut()}) { NotesPage(viewModel)}
            composable(Screen.Tasks.route, enterTransition ={ fadeIn() }, exitTransition = { fadeOut()}) { TaskPage(viewModel) }
            composable(Screen.Calendar.route, enterTransition ={ fadeIn() }, exitTransition = { fadeOut()}) { Text("Calendar") }
        }
    }
}

@Composable
fun NotesPage(viewModel: MyViewModel){
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxHeight()){
        Text("Notes")
        LazyColumn {
            items(viewModel.notes.size) { index ->
                Text(viewModel.notes[index])
            }
        }
        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Add a note")}
        )
        Button(onClick = {
            viewModel.addNote(note)
            note = ""
        }) {
            Text("Add Note")
        }
    }
}

@Composable
fun TaskPage(viewModel: MyViewModel){
    var taskLabel by remember { mutableStateOf("") }
    Column(Modifier.fillMaxHeight()){
        Text("Tasks")
        LazyColumn {
            items(viewModel.tasks.size) { index ->
                Row() {
                    Checkbox(
                        checked = viewModel.tasks[index].isChecked,
                        onCheckedChange = {
                            viewModel.toggleTaskChecked(index, it)
                        }
                    )
                    Text(viewModel.tasks[index].label)
                }
            }
        }
        TextField(
            value = taskLabel,
            onValueChange = { taskLabel = it },
            label = { Text("Add a task")}
        )
        Button(onClick = {
            viewModel.addTask(task = CheckableItem(taskLabel))
            taskLabel = ""
        }) {
            Text("Add Task")
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