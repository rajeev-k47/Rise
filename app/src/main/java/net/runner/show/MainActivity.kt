package net.runner.show

import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import net.runner.show.ui.theme.ShowTheme
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID
class MainActivity : ComponentActivity() {
    private val dataviewModel: DataLoaderViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
        val packageInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        val vid = packageInfo.versionName.toString()

        setContent {

            ShowTheme {
                var data = rememberSaveable {
                    mutableStateOf("")
                }
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "Main"
                ) {
                    composable(route = "Main") {
                        navDine(dataviewModel = dataviewModel,navController,vid){fdata->
                            data.value=fdata
                        }
                    }
                    composable(route = "assistant") {
                        Assistant(data.value)
                    }
                }


            }
        }
    }
}

@Composable
fun navDine(dataviewModel:DataLoaderViewModel,navController: NavController,vid:String,setdata:(String)->Unit){
    val context = LocalContext.current
    val dataLoaded by dataviewModel.dataLoaded.observeAsState(initial = false)
    val fetchedData by dataviewModel.fetchedData.observeAsState("")
    val updateState by dataviewModel.state.observeAsState(vid)

    if(updateState==vid){
        OneSignal.User.removeTag("update")
    }else{
        Log.d("saregama",updateState)
        OneSignal.InAppMessages.addTriggers(mapOf("update" to "true"))
        OneSignal.User.addTag("update","true")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
        ,
        contentAlignment = Alignment.Center

    ) {

        if (dataLoaded) {
            if (fetchedData.isNotEmpty()) {
                dine(dineData = fetchedData,navController)
                setdata(fetchedData)
            }
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

        }
    }
}


@Composable
fun dine(dineData:String,navController: NavController) {
    var DineData = rememberSaveable {
        mutableStateOf(dineData)
    }
    var DineDay = rememberSaveable {
        mutableStateOf(0)
    }
    if(DineData.value.isEmpty()){
        return
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            rail(
                DineData.value,
                modifier = Modifier
                    .weight(0.15f)
            ){day->
                DineDay.value=day
            }
            Box(modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 30.dp), contentAlignment = Alignment.BottomCenter){
                FloatingActionButton(
                    onClick = {
                        navController.navigate("assistant")
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(painter = painterResource(id = R.drawable.ai),
                        "fab",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

        }

        DineUi(
            DineData.value,
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth()
                .padding(top = 10.dp)
            ,
            DineDay.value
        )
    }
}

data class Meal(val Day:String,val Lunch: String, val Dinner: String, val BreakFast: String)
@Composable
fun DineUi(data:String,modifier: Modifier,CURRENTDAY:Int){
    val datajson = JSONArray(data)
    val Dinemeal = mutableListOf<Meal>()
    var DailyLunch =""
    var DailyDinner =""
    var DailyBreakFast =""
    for(i in 0 until datajson.length()){
        val Dineobject =  datajson.getJSONObject(i)
        val day = Dineobject.getString("Day")
        val Lunch = Dineobject.getString("Lunch")
        val BreakFast = Dineobject.getString("Breakfast")
        val Dinner= Dineobject.getString("Dinner")

        if(day=="Daily"){
            DailyLunch=Dineobject.getString("Lunch")
            DailyDinner=Dineobject.getString("Dinner")
            DailyBreakFast=Dineobject.getString("Breakfast")
        }

        Dinemeal.add(Meal(day,Lunch,Dinner,BreakFast))
    }

    ScheduleTimedNotifications(Dinemeal)

    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        LazyColumn {
            item{
                Spacer(modifier = Modifier.height(30.dp))
                DineElement( text = "BreakFast", Dinemeal[CURRENTDAY].BreakFast,DailyBreakFast)
                Spacer(modifier = Modifier.height(30.dp))
                DineElement( text = "Lunch",Dinemeal[CURRENTDAY].Lunch,DailyLunch)
                Spacer(modifier = Modifier.height(30.dp))
                DineElement( text = "Dinner",Dinemeal[CURRENTDAY].Dinner,DailyDinner)
                Spacer(modifier = Modifier.height(30.dp))

            }
        }
    }

}

@Composable
fun DineElement(text:String,data: String,Daily:String){
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp) ,
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Text(
            data,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp) ,
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Text(
            "Daily : \n$Daily",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun rail(data: String,modifier: Modifier,updateSelected:(Int)->Unit){
    var selectedItem by rememberSaveable { mutableIntStateOf(-1) }
    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val datajson = JSONArray(data)
    val items = mutableListOf<Pair<String, String>>()
    for (i in 0 until datajson.length()) {
        val item = datajson.getJSONObject(i)

        val dateStr = item.optString("Date", "")
        if (dateStr.isNotEmpty()) {
            try {
                val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateStr)
                val itemDate = SimpleDateFormat("dd", Locale.getDefault()).format(date).toInt()
                val itemDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

                items.add(Pair(itemDate.toString(), itemDay))

                if (selectedItem == -1 && itemDate == todayDay) {
                    selectedItem = i
                    updateSelected(selectedItem)
                }
            } catch (e: ParseException) {
                println("Failed to parse date: ${e.message}")
            }
        }
    }
    LazyColumn {
        item {

            NavigationRail(modifier=modifier) {
                items.forEachIndexed { index, (date, day) ->
                    NavigationRailItem(
                        icon = {
                            Text(
                                text = date,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index
                            updateSelected(selectedItem)
                        }
                    )
                }

            }
        }
    }



}







