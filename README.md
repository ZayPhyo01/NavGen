# NavGen

A sample compose navigation destination code generation with kotlin symbols processing.




## Usage/Examples

There are two screens , ScreenA (start destination) and ScreenB

```kotlin
@Destination("screen_a")
class ScreenA {
}
```

```kotlin
@Destination("screen_b")
data class ScreenB(
    val message : String
)
```

ScreenA want to pass message string to ScreenB.

## Generated Code

For ScreenA

```kotlin
public object ScreenADestination {
  public fun destination(): String = "screen_a"

  public fun route(): String = "screen_a"
}
```


For ScreenB

```kotlin
public object ScreenBDestination {
  public val MESSAGE: String = "message"

  public fun destination(): String = "screen_b?message={message}"

  public fun route(message: String): String = "screen_b?message=${message}"

  public fun message(bundle: Bundle): String = bundle.getString("message")!!

  public fun types() = listOf(androidx.navigation.navArgument("message"){
  type = androidx.navigation.NavType.StringType
  nullable = false
  })
}

```

### Example Usage

```kotlin

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
```





## Limination

Currently not available default parameters and only available primitive types but not a Double , because of NavType.DoubleType not support by compose navigation.
