package com.appweek14

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appweek14.data.RandomUser
import com.appweek14.data.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloRetrofitRandomUserScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelloRetrofitRandomUserScreen() {
    val scope = rememberCoroutineScope()

    var randomUsers by remember { mutableStateOf<List<RandomUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hello Retrofit - Random User") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 랜덤 사용자 가져오기 버튼
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            val response = RetrofitClient.randomUserApi.getRandomUsers(count = 10)
                            randomUsers = response.results
                        } catch (e: Exception) {
                            errorMessage = "에러: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch 10 Random Users")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                randomUsers.isEmpty() -> {
                    Text("Click button to fetch random users")
                }
                else -> {
                    LazyColumn {
                        items(randomUsers) { user ->
                            RandomUserItem(user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RandomUserItem(user: RandomUser) {
    // 간단히 이름 + 이메일만 표시
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val fullName = "${user.name.title} ${user.name.first} ${user.name.last}"
        Text(text = fullName, style = MaterialTheme.typography.titleMedium)
        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
    }
}