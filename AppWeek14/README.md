# Week 14: Retrofit & REST API - Hello Network Calls

**Master HTTP networking and understand how to integrate APIs into your app**

---

## Learning Objectives

1. **Retrofit Basics**: HTTP GET/POST requests with declarative API
2. **REST API Concepts**: Endpoints, responses, JSON structure
3. **JSON Deserialization**: Automatic mapping with Gson
4. **Network Error Handling**: Try-catch, loading states, error messages
5. **API Integration**: Move from static data (Room) to dynamic APIs

---

## Project Overview

### AppWeek14a - Hello Retrofit (Two Versions)

**Version 1: Fixed Data (JSONPlaceholder)**
- Endpoint: `https://jsonplaceholder.typicode.com/users`
- Response: Always the same 10 fixed users
- Purpose: Understand Retrofit basics

**Version 2: Random Data (Random User API)**
- Endpoint: `https://randomuser.me/api/?results=10`
- Response: Different users each request
- Purpose: See API calls working in real-time

### Key Difference from Week 13
```
Week 13: Room Database (Local storage)
  ViewModel → Room → UI
  Data: Persistent, offline-first

Week 14: Retrofit API (Network)
  ViewModel → Retrofit API → UI
  Data: Live, requires internet, no storage
```

---

## Architecture Diagram

```
┌──────────────────────────────────────┐
│           UI Layer                   │
│  MainActivity (Compose)              │
│  - Button: "Fetch Users"             │
│  - Loading spinner                   │
│  - Error message display             │
│  - LazyColumn list                   │
└──────────────────────────────────────┘
              ↓ Coroutine
┌──────────────────────────────────────┐
│      ViewModel/StateFlow              │
│  - users: StateFlow<List<User>>      │
│  - isLoading: StateFlow<Boolean>     │
│  - errorMessage: StateFlow<String>   │
│  - fetchUsers() function             │
└──────────────────────────────────────┘
              ↓ Suspend
┌──────────────────────────────────────┐
│      Retrofit Client                  │
│  - Retrofit.Builder                  │
│  - GsonConverterFactory              │
│  - RandomUserApi interface           │
└──────────────────────────────────────┘
              ↓ HTTP
┌──────────────────────────────────────┐
│      Network Layer                   │
│  GET https://randomuser.me/api/...   │
└──────────────────────────────────────┘
```

---

## Files Structure

```
AppWeek14a/
├── data/
│   ├── RandomUserApi.kt          (Retrofit interface)
│   ├── RandomUserModels.kt       (Data classes for JSON)
│   └── RetrofitClient.kt         (Retrofit configuration)
├── MainActivity.kt               (Compose UI + state)
├── build.gradle.kts             (Dependencies)
└── libs.versions.toml           (Dependency versions)
```

---

## Core Concepts Explained

### 1. Retrofit Interface (API Specification)

```kotlin
interface RandomUserApi {
    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") count: Int = 5
    ): RandomUserResponse
}
```

**Breaking it down:**
- `@GET("api/")` - HTTP GET request to `/api/` endpoint
- `suspend fun` - Can be called from coroutine context
- `@Query("results")` - Query parameter (?results=5)
- `RandomUserResponse` - Auto-converted from JSON

### 2. Data Models (Matching JSON Structure)

```kotlin
data class RandomUserResponse(
    val results: List<RandomUser>
)

data class RandomUser(
    val gender: String,
    val email: String,
    val name: RandomUserName,
    val picture: RandomUserPicture
)
```

**Why this structure?**
- Gson automatically maps JSON keys to properties
- Property names must match JSON keys exactly
- Nested objects = nested data classes

### 3. Retrofit Client (Singleton Configuration)

```kotlin
object RetrofitClient {
    private const val RANDOM_USER_BASE_URL = "https://randomuser.me/"
    
    private val randomUserRetrofit = Retrofit.Builder()
        .baseUrl(RANDOM_USER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val randomUserApi = randomUserRetrofit.create(RandomUserApi::class.java)
}
```

**Key parts:**
- `baseUrl` - Server base address
- `GsonConverterFactory` - Converts JSON ↔ Kotlin objects
- `.create()` - Generates API client from interface
- `object` - Kotlin singleton (one instance globally)

### 4. Making API Calls (MainActivity)

```kotlin
Button(onClick = {
    scope.launch {
        isLoading = true
        errorMessage = ""
        try {
            val response = RetrofitClient.randomUserApi.getRandomUsers(count = 10)
            randomUsers = response.results
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
})
```

**Flow:**
1. Button click → launch coroutine
2. Set `isLoading = true` → UI shows spinner
3. Call `getRandomUsers()` → suspends until response
4. Parse JSON response → set `randomUsers`
5. Catch errors → display error message
6. Finally block → hide spinner

---

## State Management Pattern

```kotlin
var randomUsers by remember { mutableStateOf<List<RandomUser>>(emptyList()) }
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf("") }
```

**State usage:**
- `randomUsers` - Holds API response data
- `isLoading` - Controls loading spinner visibility
- `errorMessage` - Shows network/parsing errors

**Update flow:**
```
User clicks button
    ↓
isLoading = true (UI shows spinner)
    ↓
Network request (API call)
    ↓
Response received
    ↓
randomUsers = data (UI updates LazyColumn)
    ↓
isLoading = false (UI hides spinner)
```

---

## API Endpoints Explained

### Fixed Data (JSONPlaceholder)
```
URL: https://jsonplaceholder.typicode.com/users
Method: GET
Response: 
[
  {
    "id": 1,
    "name": "Leanne Graham",
    "email": "Bret@april.biz",
    "phone": "1-770-736-8031"
  },
  ... 9 more users ...
]
```

### Random Data (Random User API)
```
URL: https://randomuser.me/api/?results=10
Method: GET
Response:
{
  "results": [
    {
      "gender": "female",
      "email": "jane@example.com",
      "name": {
        "title": "Ms",
        "first": "Jane",
        "last": "Doe"
      },
      "picture": {
        "large": "https://...",
        "medium": "https://...",
        "thumbnail": "https://..."
      }
    },
    ... 9 more users ...
  ]
}
```

---

## Dependencies (Updated)

**Added to `libs.versions.toml`:**
```toml
[versions]
retrofit = "2.11.0"
kotlinx-coroutines = "1.8.1"

[libraries]
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinx-coroutines" }
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
```

**Added to `build.gradle.kts`:**
```kotlin
implementation(libs.kotlinx.coroutines.android)
implementation(libs.retrofit.core)
implementation(libs.retrofit.converter.gson)
```

---

## Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `java.lang.SecurityException: Permission denied` | Missing INTERNET permission | Add `<uses-permission>` in AndroidManifest.xml |
| `Property 'randomUserApi' is never used` | IDE warning (not error) | Ignore - API gets called when button is clicked |
| `Exception: ... No address associated with hostname` | No internet connection | Use emulator with internet or physical device |
| `JsonSyntaxException` | JSON structure doesn't match data class | Check JSON response matches property names |
| `NullPointerException in bind()` | Missing null check | Use `?:` Elvis operator or optional properties |

---

## Comparison: Week 13 vs Week 14

| Aspect | Week 13 (Room) | Week 14 (Retrofit) |
|--------|---------------|--------------------|
| **Data Source** | Local database | Remote API |
| **Persistence** | Saved on device | Not saved (fetch each time) |
| **Internet Required** | No | Yes |
| **ViewModel** | Same pattern | Same pattern! |
| **State Management** | StateFlow (identical) | StateFlow (identical) |
| **Error Handling** | Database exceptions | Network exceptions |
| **Speed** | Fast (local) | Depends on network |
| **Use Case** | Offline-first, caching | Real-time, live data |

---

## Next Steps (Winter Semester)

### Option A: Combine Room + Retrofit
```
API Call → Cache in Room → Show from Room
Sync Local ↔ Remote automatically
```

### Option B: Add More Endpoints
```
GET /api/ → Random users
POST /api/ → Submit new user
PUT /api/{id} → Update user
DELETE /api/{id} → Delete user
```

### Option C: Add Authentication
```
API Key / OAuth token
Request headers with auth
Handle 401 Unauthorized responses
```

---

## Implementation Checklist

- Add Retrofit dependencies to `build.gradle.kts`
- Add INTERNET permission to `AndroidManifest.xml`
- Create `RandomUserModels.kt` data classes
- Create `RandomUserApi.kt` Retrofit interface
- Create `RetrofitClient.kt` singleton
- Build and run AppWeek14a
- Click "Fetch Random Users" button
- Verify loading spinner shows
- Verify users list appears
- Try with no internet (see error handling)

---

## Key Takeaways

1. **Retrofit handles HTTP**: You define interface, Retrofit does the rest
2. **Gson auto-deserializes JSON**: No manual parsing needed
3. **Suspend functions**: Perfect for async network calls
4. **State management**: Same pattern as Room (easy to migrate!)
5. **Error handling matters**: Network is unpredictable
6. **API design**: Study response structure before writing code

## Final Exam Infomation

### Class A
```
Date & Time : Dec. 19th 2025 (Friday) / 12:00~12:50
```
### Class B
```
Date & Time : Dec. 19th 2025 (Friday) / 15:00~15:50
```

