package com.guru.composenavgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guru.composenavgen.destination.ScreenADestination
import com.guru.composenavgen.destination.ScreenBDestination
import com.guru.composenavgen.ui.theme.ComposeNavGenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeNavGenTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = ScreenADestination.destination()
                ) {
                    composable(
                        route = ScreenADestination.destination()
                    ) {
                        ScreenA {
                            navController.navigate(
                                ScreenBDestination.route("Hello i am from A")
                            )
                        }
                    }
                    composable(
                        arguments = ScreenBDestination.types(),
                        route = ScreenBDestination.destination()
                    ) {
                        ScreenB(
                            ScreenBDestination.message(it.arguments!!)
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun ScreenA(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onClick, modifier = Modifier.align(
                Alignment.Center
            )
        ) {
            Text(text = "Go to B")
        }
    }
}

@Composable
fun ScreenB(message: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = message, modifier = Modifier.align(
                Alignment.Center
            )
        )

    }
}

