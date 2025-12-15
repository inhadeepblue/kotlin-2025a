# Week 13: MVVM Architecture - XML vs Jetpack Compose

**Study the same MVVM + Room backend with two different UI frameworks**

---

## Learning Objectives

1. **MVVM Pattern**: Separate concerns into UI, business logic, and data layers
2. **Repository Pattern**: Abstract data sources for flexibility and testability
3. **StateFlow**: Reactive state management with Kotlin Coroutines
4. **XML vs Compose**: Same backend, different UI technologies - understand both!
5. **Why XML still matters**: Maintain legacy apps, hybrid migration projects

---

## Project Overview

### AppWeek13a - XML + RecyclerView
- **Activity**: `AppCompatActivity` with ViewBinding
- **List**: `RecyclerView` + `ListAdapter` + `DiffUtil`
- **State**: `repeatOnLifecycle()` + `Flow.collect()`
- **UI Update**: Manual via adapter `submitList()`
- **File count**: ~10 files (2 XML layouts, 1 Adapter)

### AppWeek13b - Jetpack Compose
- **Activity**: `ComponentActivity` with `setContent {}`
- **List**: `LazyColumn` + `items()`
- **State**: `collectAsState()` (one-liner!)
- **UI Update**: Automatic via Compose recomposition
- **File count**: ~6 files (no XML, no Adapter)

### Key Insight
**Data layer identical** between both versions:
- `Student.kt` (Entity)
- `StudentDao.kt` (Room DAO)
- `StudentDatabase.kt` (Room Database)
- `StudentRepository.kt` (Data abstraction)
- `StudentViewModel.kt` (Business logic + StateFlow)

---

## MVVM Architecture Diagram

```
┌─────────────────────────────────────────┐
│         UI Layer                         │
│  ┌──────────────┐  ┌──────────────────┐ │
│  │ MainActivity │  │ StudentListScreen│ │
│  │ (AppCompat)  │  │ (Compose)        │ │
│  └──────┬───────┘  └────────┬─────────┘ │
│         │ observe            │ collectAsState
└─────────┼────────────────────┼──────────┘
          │                    │
          ↓ (identical for both)
┌─────────────────────────────────────────┐
│    ViewModel Layer                       │
│  StudentViewModel                        │
│  - StateFlow<List<Student>>              │
│  - addStudent(name)                      │
│  - deleteStudent(student)                │
└──────────────┬────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    Repository Layer                      │
│  StudentRepository                       │
│  - getAllStudents(): Flow<List>          │
│  - addStudent(...)                       │
│  - deleteStudent(...)                    │
└──────────────┬────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    Data Layer                            │
│  Room Database                           │
│  - StudentDatabase                       │
│  - StudentDao                            │
└─────────────────────────────────────────┘
```

---

## Core Differences

| Aspect | AppWeek13a (XML) | AppWeek13b (Compose) |
|--------|------------------|---------------------|
| **Base Activity** | `AppCompatActivity` | `ComponentActivity` |
| **Layout System** | XML files + ViewBinding | Composable functions |
| **List Widget** | `RecyclerView` | `LazyColumn` |
| **List Adapter** | `ListAdapter` + `ViewHolder` + `DiffCallback` | `items { }` block in LazyColumn |
| **Input Widget** | `EditText.text.toString()` | `TextField` with state |
| **State Collection** | `repeatOnLifecycle { collect { } }` | `collectAsState()` |
| **Button Click** | `setOnClickListener { }` | `onClick { }` in Button |
| **Main Lines of Code** | ~400 LOC | ~250 LOC |
| **Recomposition** | Manual `adapter.submitList()` | Automatic on state change |

---

## AppWeek13a: XML Implementation Details

### Activity Flow
```kotlin
MainActivity (AppCompatActivity)
  ↓ onCreate()
  ├─ setupAdapter() → StudentAdapter (RecyclerView + ViewHolder + DiffCallback)
  ├─ setupObservers() → Flow.collect() in repeatOnLifecycle
  └─ setupListeners() → Button.setOnClickListener { ViewModel }
```

### State Observation Pattern
```kotlin
private fun setupObservers() {
    lifecycleScope.launch {
        // Only observe when activity is STARTED
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.students.collect { students ->
                adapter.submitList(students)  // Manual update
                binding.textViewCount.text = "Total: ${students.size}"
            }
        }
    }
}
```

### Student Input
```kotlin
private fun setupListeners() {
    binding.buttonAdd.setOnClickListener {
        val name = binding.editTextName.text.toString()  // Read text
        viewModel.addStudent(name)
        binding.editTextName.text.clear()  // Clear manually
    }
}
```

---

## AppWeek13b: Compose Implementation Details

### Activity Flow
```kotlin
MainActivity (ComponentActivity)
  ↓ onCreate()
  └─ setContent { ... }
       ├─ val students by viewModel.students.collectAsState()
       ├─ StudentListScreen(students) [Composable]
       │   ├─ StudentItem() × N [Recompose on change]
       │   └─ TextField + Button [State handled locally]
```

### State Collection (One-liner!)
```kotlin
setContent {
    val students by viewModel.students.collectAsState()
    StudentListScreen(
        students = students,
        onAddClick = { name -> viewModel.addStudent(name) },
        onDeleteClick = { student -> viewModel.deleteStudent(student) }
    )
}
```

### Student Input & List
```kotlin
@Composable
fun StudentListScreen(students: List<Student>, ...) {
    var studentName by remember { mutableStateOf("") }
    
    TextField(
        value = studentName,
        onValueChange = { studentName = it }  // Auto update
    )
    
    Button(onClick = { 
        onAddClick(studentName)
        studentName = ""  // State change triggers auto UI update
    }) { Text("Add") }
    
    LazyColumn {
        items(students) { student ->
            StudentItem(student) { onDeleteClick(student) }
        }
    }
}
```

---

## Key Learning Points

### 1. MVVM Benefits
- **Testability**: ViewModel logic independent of Activity/Composable
- **Separation of Concerns**: UI doesn't know database details
- **Reusability**: Same ViewModel in XML and Compose!

### 2. Repository Pattern Value
```
Without Repository:
  ViewModel → Room.DAO → Hard to test, hard to add API later

With Repository:
  ViewModel → Repository → Room.DAO (or Retrofit API later)
  Repository is the "switch" - easy to test and extend!
```

### 3. LazyColumn vs RecyclerView
```
RecyclerView (XML):
  - More boilerplate (Adapter, ViewHolder, DiffCallback)
  - More control
  - Used in legacy apps

LazyColumn (Compose):
  - Less boilerplate (just "items { }")
  - Automatic recomposition
  - Modern Android standard
```

### 4. StateFlow: The Reactive Bridge
```
Without StateFlow:
  Manual: adapter.submitList() after every change

With StateFlow:
  Reactive: emit() new value → UI auto-updates
  Compose: collectAsState() → recompose on change
  XML: collect() in repeatOnLifecycle → submitList()
```

---

## Migration Path (Why Learn Both?)

### Scenario 1: New Project
→ **Use AppWeek13b (Compose)** - faster, cleaner, future-proof

### Scenario 2: Maintain Existing App
→ **Use AppWeek13a (XML)** - understand the codebase, fix bugs

### Scenario 3: Gradual Migration
→ **Start with AppWeek13a**, then migrate screen-by-screen to Compose
- ViewModel/Repository unchanged!
- Only UI layer changes

### Scenario 4: Hybrid App
→ **Mix both**: Some screens in XML, some in Compose
- Retrofit integration will work identically in both!

---

## Checklist for This Week

- Understand MVVM: separate UI, logic, data
- Understand Repository: data abstraction layer
- Build AppWeek13a: run, add/delete students
- Build AppWeek13b: same features in Compose
- Compare: notice identical ViewModel!
- Modify both: add a new feature to ViewModel, see both UIs update

---

## Files to Study

### AppWeek13a (XML)
1. `StudentViewModel.kt` - Business logic center
2. `StudentRepository.kt` - Data abstraction
3. `MainActivity.kt` - repeatOnLifecycle + collect pattern
4. `StudentAdapter.kt` - RecyclerView pattern
5. `activity_main.xml` - UI layout

### AppWeek13b (Compose)
1. `StudentViewModel.kt` - **Identical to XML version!**
2. `StudentRepository.kt` - **Identical to XML version!**
3. `MainActivity.kt` - setContent + collectAsState pattern
4. `StudentListScreen.kt` - Composable functions instead of XML

## Next Steps (Week 14)

1. Add Retrofit to StudentRepository
2. Fetch random students from RandomUser API
3. Sync local Room DB with remote API
4. Same ViewModel works for both XML and Compose!
5. Understand why Repository pattern matters
