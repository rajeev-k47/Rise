package net.runner.show

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun rail(data: String, modifier: Modifier, updateSelected: (Int) -> Unit) {
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

            NavigationRail(modifier = modifier) {
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
                        onClick = {
                            selectedItem = index
                            updateSelected(selectedItem)
                        }
                    )
                }

            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DineElement(text: String, data: String, Daily: String,navController: NavController) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Card(
        elevation = CardDefaults.cardElevation(10.dp),

        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(  interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Color.White),
                onClick = {Unit},
                onLongClick = {
                    val encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
                    navController.navigate("assistant/$encodedData")
                }
            ),
        shape = RoundedCornerShape(16.dp)
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
            .padding(horizontal = 10.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        onClick = {Unit}


    ) {
        Text(
            "Daily : \n$Daily",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }


}

