package net.runner.show

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate


val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GENERATIVE_API_KEY
)

suspend fun response(query:String,data:String): String {
    val inputContent = content {
        text("DineData => $data")
        text("Query => $query")
        text("Date => ${LocalDate.now()}")
        text("Imagine you are a diet Assistant with the data provided answer the query")
    }

    val response = generativeModel.generateContent(inputContent)
    return response.text?.trimIndent() ?: "No response text"
}
fun parseBoldText(input: String): AnnotatedString {
    val builder = AnnotatedString.Builder()

    var bold = false
    input.split("**").forEachIndexed { index, part ->
        if (index % 2 == 0) {
            builder.append(part)
        } else {
            builder.withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                builder.append(part)
            }
        }
    }

    return builder.toAnnotatedString()
}
@Parcelize
data class listMessages(val type:String,val message:String):Parcelable

@Composable
fun Assistant(data:String){

    val coroutineScope = rememberCoroutineScope()


    var listMessages by rememberSaveable {
        mutableStateOf(
            mutableListOf(
                listMessages("0", "Well! How can I help you Today with your Meal ?")
            )
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .padding(top = 30.dp)){
            var message by rememberSaveable { mutableStateOf("") }
            val scrollState = rememberLazyListState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.ime)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = scrollState
                ) {
                    items(listMessages.size) { index ->
                        val currentMessage = listMessages[index]
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = if (currentMessage.type == "0") {
                                Arrangement.Start
                            } else {
                                Arrangement.End
                            }
                        ){
                            if (currentMessage.type == "0") {
                                Icon(
                                    painter = painterResource(id = R.drawable.ai),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 5.dp , top = 5.dp).size(30.dp)
                                )
                            }
                            Text(
                                parseBoldText(currentMessage.message),
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(
                                        if (currentMessage.type == "0")
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .padding(12.dp)
                                    .widthIn(min = 50.dp, max = 250.dp),
                                color = if (currentMessage.type == "0")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (currentMessage.type == "1") {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(start = 5.dp,top=5.dp).size(30.dp)
                                )
                            }

                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .navigationBarsPadding()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp).copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                        decorationBox = { innerTextField ->
                            if (message.isEmpty()) {
                                Text(text ="Type a message...")
                            }
                            innerTextField()
                        }
                    )
                        val keyboardController = LocalSoftwareKeyboardController.current
                        IconButton(onClick = {

                            if (message.isNotBlank()) {
                                keyboardController?.hide()
                                listMessages = listMessages.toMutableList().apply {
                                    add(listMessages("1", message))
                                    add(listMessages("0", "Thinking..."))
                                }
                                val indexOfThinkingMessage = listMessages.size - 1
                                message = ""
                                coroutineScope.launch {
                                    val responseMessage = response(listMessages[listMessages.size-2].message,data)
                                    listMessages = listMessages.toMutableList().apply {
                                        set(indexOfThinkingMessage, listMessages("0", responseMessage))
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                }
            }


    }
}