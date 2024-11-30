package net.runner.show

import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.runner.show.ui.theme.ShowTheme
import org.json.JSONArray
import java.util.Calendar

val ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID

class MainActivity : ComponentActivity() {
    private val dataviewModel: DataLoaderViewModel by viewModels()
    private val dineDayModel: DineDayViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        scheduleDailyNotifications(this)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
        val packageInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        val vid = packageInfo.versionName.toString()

        setContent {

            ShowTheme {
                var Dinedata = rememberSaveable {
                    mutableStateOf("")
                }
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "Main"
                ) {
                    composable(route = "Main") {
                        navDine(dataviewModel = dataviewModel, navController, vid,dineDayModel) { fdata ->
                            Dinedata.value = fdata
                        }
                    }
                    composable(
                        route = "assistant/{data}",
                        arguments = listOf(navArgument("data") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val data = backStackEntry.arguments?.getString("data")
                        Assistant(Dinedata.value,data!!)

                    }
                    composable(
                        route = "assistant"
                    ) {
                        Assistant(Dinedata.value, "")

                    }

                }


            }
        }
    }

    override fun onResume() {
        super.onResume()
        dineDayModel.resetToToday()

    }
}

@Composable
fun navDine(
    dataviewModel: DataLoaderViewModel,
    navController: NavController,
    vid: String,
    dineDayViewModel: DineDayViewModel,
    setdata: (String) -> Unit
) {
    val context = LocalContext.current
    val dataLoaded by dataviewModel.dataLoaded.observeAsState(initial = false)
    val fetchedData by dataviewModel.fetchedData.observeAsState("")
    val updateState by dataviewModel.state.observeAsState(vid)

    if (updateState == vid) {
        OneSignal.User.removeTag("update")
    } else {
        Log.d("saregama", updateState)
        OneSignal.InAppMessages.addTriggers(mapOf("update" to "true"))
        OneSignal.User.addTag("update", "true")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center

    ) {

        if (dataLoaded) {
            if (fetchedData.isNotEmpty()) {
                dine(dineData = fetchedData, navController,dineDayViewModel)
                setdata(fetchedData)
            }
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

        }
    }
}



@Composable
fun dine(dineData: String, navController: NavController,dineDayViewModel: DineDayViewModel) {
    var DineData = rememberSaveable {mutableStateOf(dineData) }
    var DineDay by remember { mutableStateOf(0) }

    DineDay = dineDayViewModel.dineDay.observeAsState(0).value
    if (DineData.value.isEmpty())  return

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DineUi(
            DineData.value,
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth()
                .padding(top = 10.dp, start = 10.dp),
            DineDay,
            navController
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val context = LocalContext.current
            val imageLoader = remember {
                ImageLoader.Builder(context)
                    .components {
                        add(ImageDecoderDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
            }


            var randomNumber by remember { mutableStateOf((10..44).random()) }
            var webpUrl by remember { mutableStateOf("https://fonts.gstatic.com/s/e/notoemoji/latest/1f6${randomNumber}/512.webp") }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(webpUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.transp)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "Animated WebP",
                modifier = Modifier.padding(top = 45.dp).height(55.dp).width(55.dp)
                    .clickable {
                        randomNumber = (10..44).random()
                        webpUrl = "https://fonts.gstatic.com/s/e/notoemoji/latest/1f6${randomNumber}/512.webp"
                        Log.d("rnadom", randomNumber.toString())
                    }

            )

            rail(
                DineData.value,
                modifier = Modifier
                    .weight(0.15f),
                dineDayViewModel
            ) { day ->
                dineDayViewModel.updateDineDay(day)
            }


            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .padding(bottom = 30.dp), contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("assistant")
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ai),
                        "fab",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

        }


    }
}

data class Meal(val Day: String, val Lunch: String, val Dinner: String, val BreakFast: String)

@Composable
fun DineUi(data: String, modifier: Modifier, CURRENTDAY: Int,navController: NavController) {
    val datajson = JSONArray(data)
    val Dinemeal = mutableListOf<Meal>()
    var DailyLunch = ""
    var DailyDinner = ""
    var DailyBreakFast = ""
    for (i in 0 until datajson.length()) {
        val Dineobject = datajson.getJSONObject(i)
        val day = Dineobject.getString("Day")
        val Lunch = Dineobject.getString("Lunch")
        val BreakFast = Dineobject.getString("Breakfast")
        val Dinner = Dineobject.getString("Dinner")

        if (day == "Daily") {
            DailyLunch = Dineobject.getString("Lunch")
            DailyDinner = Dineobject.getString("Dinner")
            DailyBreakFast = Dineobject.getString("Breakfast")
        }

        Dinemeal.add(Meal(day, Lunch, Dinner, BreakFast))
    }

//    ScheduleTimedNotifications(Dinemeal)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(30.dp))
                DineElement(text = "BreakFast", Dinemeal[CURRENTDAY].BreakFast, DailyBreakFast,navController)
                Spacer(modifier = Modifier.height(30.dp))
                DineElement(text = "Lunch", Dinemeal[CURRENTDAY].Lunch, DailyLunch,navController)
                Spacer(modifier = Modifier.height(30.dp))
                DineElement(text = "Dinner", Dinemeal[CURRENTDAY].Dinner, DailyDinner,navController)
                Spacer(modifier = Modifier.height(30.dp))

            }
        }
    }

}









