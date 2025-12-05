# 🐛 ANALISIS BUG KRITIS - Commitech

**Tanggal:** 2025-12-05  
**Dilaporkan oleh:** User  
**Status:** ✅ FIXED

---

## 📋 DAFTAR MASALAH

### **BUG #1: Data Peserta Tidak Langsung Muncul**
**Severity:** 🔴 HIGH  
**Lokasi:** `SeleksiWawancaraScreen.kt`

### **BUG #2: Harus Login Ulang Saat Tap Notifikasi**
**Severity:** 🔴 CRITICAL  
**Lokasi:** `AuthViewModel.kt`, `MainActivity.kt`

---

## 🔍 BUG #1: Data Peserta Tidak Langsung Muncul

### **Gejala:**
- Saat membuka halaman Seleksi Wawancara, data peserta tidak muncul
- Harus tap berulang kali (back & forth) baru data muncul
- Loading indicator tidak muncul

### **Root Cause Analysis:**

#### **Problem 1: Race Condition di LaunchedEffect**

**File:** `SeleksiWawancaraScreen.kt` (Line 158-176)

```kotlin
// ❌ KODE BERMASALAH (SEBELUM FIX)
LaunchedEffect(Unit) {
    InterviewNotificationHelper.ensureChannels(context)
    
    // Load jadwal dari database jika token tersedia
    authState.token?.let { token ->
        if (viewModel.days.isEmpty()) {  // ⚠️ MASALAH: Check isEmpty SEBELUM load
            viewModel.loadJadwalWawancaraFromDatabase(token)
        }
    }
}

// Reload jadwal jika token berubah
LaunchedEffect(authState.token) {
    authState.token?.let { token ->
        if (viewModel.days.isEmpty()) {  // ⚠️ MASALAH: Check isEmpty lagi
            viewModel.loadJadwalWawancaraFromDatabase(token)
        }
    }
}
```

**Kenapa Bermasalah?**

1. **Double LaunchedEffect dengan Key Berbeda:**
   - `LaunchedEffect(Unit)` → Hanya run sekali saat pertama kali composable dibuat
   - `LaunchedEffect(authState.token)` → Run setiap kali token berubah
   - Kedua effect ini bisa **race condition** saling override

2. **Check `isEmpty()` Terlalu Dini:**
   - Saat pertama kali screen dibuka, `viewModel.days` pasti kosong
   - Tapi karena ada 2 LaunchedEffect, bisa jadi:
     - Effect pertama mulai load data
     - Effect kedua check `isEmpty()` → masih kosong
     - Effect kedua mulai load data lagi
     - **RESULT:** Double API call atau data tidak muncul karena conflict

3. **Shared ViewModel di NavGraph:**
   ```kotlin
   // NavGraph.kt line 99
   val seleksiWawancaraViewModel: SeleksiWawancaraViewModel = viewModel()
   ```
   - ViewModel di-share antar screen
   - Saat kembali ke screen ini, `days` mungkin sudah ada data
   - Tapi karena check `isEmpty()`, data tidak di-reload
   - **RESULT:** Data lama tidak di-refresh

#### **Problem 2: Tidak Ada Loading State Indicator**

**File:** `SeleksiWawancaraScreen.kt`

```kotlin
// ❌ KODE BERMASALAH
// Tidak ada UI untuk menampilkan loading state
val isLoadingJadwal by viewModel.isLoadingJadwal.collectAsState()

// Loading state tidak digunakan di UI!
```

**Kenapa Bermasalah?**

- User tidak tahu apakah data sedang di-load
- Tampilan kosong membuat user bingung
- User tap berulang kali karena tidak ada feedback

---

### **Solusi BUG #1:**

#### **Fix 1: Hapus Check `isEmpty()` yang Redundant**

```kotlin
// ✅ KODE SETELAH FIX
LaunchedEffect(authState.token) {
    // CRITICAL FIX #1: Hapus check isEmpty()
    // Alasan: Kita SELALU ingin reload data saat screen dibuka atau token berubah
    // Ini memastikan data selalu fresh dan tidak ada race condition
    authState.token?.let { token ->
        viewModel.loadJadwalWawancaraFromDatabase(token)
    }
}

// CRITICAL FIX #2: Hapus LaunchedEffect(Unit) yang redundant
// Alasan: Cukup satu LaunchedEffect yang trigger by token
// Ini menghindari double API call dan race condition
```

**Penjelasan Kritis:**

1. **Kenapa Hapus `isEmpty()` Check?**
   - Check `isEmpty()` membuat data tidak di-reload saat kembali ke screen
   - Jika ada perubahan data di backend, user tidak akan lihat update
   - Lebih baik reload setiap kali untuk data yang selalu fresh

2. **Kenapa Pakai `authState.token` sebagai Key?**
   - Token berubah saat login/logout
   - Saat token berubah, kita HARUS reload data
   - Ini memastikan data selalu sync dengan auth state

3. **Kenapa Hapus `LaunchedEffect(Unit)`?**
   - Redundant dengan `LaunchedEffect(authState.token)`
   - Bisa menyebabkan race condition
   - Satu effect sudah cukup

#### **Fix 2: Tambah Loading Indicator di UI**

```kotlin
// ✅ KODE SETELAH FIX
if (isLoadingJadwal) {
    // CRITICAL FIX #3: Tampilkan loading indicator
    // Alasan: User harus tahu bahwa data sedang di-load
    // Ini mencegah user tap berulang kali karena bingung
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Memuat jadwal wawancara...")
        }
    }
}
```

---

## 🔍 BUG #2: Harus Login Ulang Saat Tap Notifikasi

### **Gejala:**
- User sudah login
- Tap notifikasi warning (5 menit sebelum selesai)
- App terbuka tapi user harus login ulang
- Auth state hilang

### **Root Cause Analysis:**

#### **Problem 1: Auth State Tidak Di-Persist**

**File:** `AuthViewModel.kt` (Line 33-36)

```kotlin
// ❌ KODE BERMASALAH
init {
    // Tidak auto-login, user harus login manual setiap kali
    _authState.value = AuthState(isLoading = false)
}
```

**Kenapa Bermasalah?**

1. **Auth State Hanya di Memory:**
   - `_authState` adalah `MutableStateFlow` yang hanya ada di RAM
   - Saat app di-kill atau di-recreate, state hilang
   - User harus login ulang setiap kali

2. **Notifikasi Trigger App Restart:**
   - Saat user tap notifikasi, Android bisa:
     - Restart app dari cold start (jika app sudah di-kill)
     - Recreate MainActivity (jika app di background)
   - Dalam kedua kasus, ViewModel di-recreate
   - Auth state kembali ke default (tidak authenticated)

3. **Tidak Ada Persistence Layer:**
   - Token tidak disimpan di SharedPreferences
   - User data tidak disimpan di local storage
   - Setiap restart = fresh state

#### **Problem 2: MainActivity Tidak Handle Notification Intent**

**File:** `MainActivity.kt`

```kotlin
// ❌ KODE BERMASALAH
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Tidak ada handling untuk notification intent!
    // Intent dari notifikasi diabaikan
    
    setContent {
        AppNavGraph(settingsViewModel = settingsViewModel)
    }
}
```

**Kenapa Bermasalah?**

- Saat user tap notifikasi, Intent berisi data peserta & jadwal
- Tapi MainActivity tidak extract data ini
- NavGraph tidak tahu harus navigate ke mana
- User stuck di landing/login screen

---

### **Solusi BUG #2:**

#### **Fix 1: Persist Auth State dengan SharedPreferences**

**File:** `AuthViewModel.kt`

```kotlin
// ✅ KODE SETELAH FIX

// CRITICAL FIX #4: Tambah SharedPreferences untuk persist auth state
private val sharedPreferences = application.getSharedPreferences(
    "commitech_auth",  // Nama file SharedPreferences
    Context.MODE_PRIVATE  // Private mode (hanya app ini yang bisa akses)
)

init {
    // CRITICAL FIX #5: Load auth state dari SharedPreferences saat init
    // Alasan: Restore session user yang sudah login sebelumnya
    // Ini mencegah user harus login ulang setiap kali app restart
    loadAuthState()
}

/**
 * CRITICAL FUNCTION: Load auth state dari SharedPreferences
 * 
 * Fungsi ini dipanggil saat ViewModel pertama kali dibuat (init block).
 * Tujuan: Restore session user yang sudah login sebelumnya.
 * 
 * Flow:
 * 1. Ambil token dari SharedPreferences
 * 2. Jika token ada, ambil juga user data (id, name, email)
 * 3. Set auth state ke authenticated
 * 4. Jika token tidak ada, user harus login
 * 
 * PENTING: Ini membuat user tetap login meskipun app di-restart!
 * NOTE: Model User hanya memiliki 3 field: id, name, email (tidak ada 'role')
 */
private fun loadAuthState() {
    val token = sharedPreferences.getString("auth_token", null)
    
    if (token != null) {
        // Token ada, restore user session
        val userName = sharedPreferences.getString("user_name", null)
        val userEmail = sharedPreferences.getString("user_email", null)
        
        if (userName != null && userEmail != null) {
            // Restore full auth state
            _authState.value = AuthState(
                isLoading = false,
                isAuthenticated = true,
                user = User(
                    id = sharedPreferences.getInt("user_id", 0),
                    name = userName,
                    email = userEmail
                ),
                token = token,
                error = null
            )
        }
    }
}

/**
 * CRITICAL FUNCTION: Save auth state ke SharedPreferences
 * 
 * NOTE: Model User hanya memiliki 3 field: id, name, email (tidak ada 'role')
 */
private fun saveAuthState(token: String, user: User) {
    sharedPreferences.edit().apply {
        putString("auth_token", token)
        putInt("user_id", user.id)
        putString("user_name", user.name)
        putString("user_email", user.email)
        apply()  // Async save (tidak block UI thread)
    }
}

/**
 * CRITICAL FUNCTION: Clear auth state dari SharedPreferences
 * 
 * Fungsi ini dipanggil saat user logout.
 * Tujuan: Hapus semua data session user.
 * 
 * PENTING: Setelah clear, user harus login ulang!
 */
private fun clearAuthState() {
    sharedPreferences.edit().clear().apply()
}
```

**Penjelasan Kritis:**

1. **Kenapa Pakai SharedPreferences?**
   - Data persist meskipun app di-kill
   - Secure (MODE_PRIVATE = hanya app ini yang bisa akses)
   - Fast (data di-cache di memory)
   - Android best practice untuk session management

2. **Kenapa Save Token & User Data?**
   - Token: Untuk API authentication
   - User data: Untuk tampilkan info user di UI tanpa API call
   - Lengkap: Semua data yang dibutuhkan untuk restore session

3. **Kenapa Pakai `apply()` bukan `commit()`?**
   - `apply()`: Async, tidak block UI thread
   - `commit()`: Sync, block UI thread sampai selesai
   - `apply()` lebih cepat dan recommended

#### **Fix 2: Handle Notification Intent di MainActivity**

**File:** `MainActivity.kt`

```kotlin
// ✅ KODE SETELAH FIX

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    maybeRequestNotificationPermission()
    InterviewNotificationHelper.ensureChannels(this)
    
    enableEdgeToEdge()
    setContent {
        val settingsViewModel: SettingsViewModel = viewModel()
        val settingsState by settingsViewModel.settingsState.collectAsState()
        
        CommitechTheme(darkTheme = settingsState.isDarkTheme) {
            // CRITICAL FIX #6: Pass notification intent ke NavGraph
            // Alasan: NavGraph perlu tahu jika app dibuka dari notifikasi
            // Agar bisa navigate ke screen yang tepat
            AppNavGraph(
                settingsViewModel = settingsViewModel,
                notificationIntent = intent  // Pass intent dari notifikasi
            )
        }
    }
}

// CRITICAL FIX #7: Handle onNewIntent untuk app yang sudah running
// Alasan: Saat app sudah running di background, tap notifikasi trigger onNewIntent
// Kita perlu update intent agar NavGraph bisa handle navigation
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)  // Update current intent
    
    // CRITICAL: Recreate content agar NavGraph bisa handle intent baru
    // Tanpa ini, navigation tidak akan trigger
    recreate()
}
```

**Penjelasan Kritis:**

1. **Kenapa Perlu `onNewIntent()`?**
   - Saat app sudah running, `onCreate()` tidak dipanggil lagi
   - Android call `onNewIntent()` dengan intent baru
   - Kita harus handle ini untuk navigation yang benar

2. **Kenapa Pakai `recreate()`?**
   - Agar Compose recompose dengan intent baru
   - NavGraph bisa detect intent dan navigate
   - Alternative: Pakai event bus atau navigation state

3. **Kenapa Pass Intent ke NavGraph?**
   - NavGraph yang handle routing
   - NavGraph bisa extract data dari intent
   - Navigate ke screen yang tepat berdasarkan notification type

---

## 📊 SUMMARY FIXES

### **BUG #1: Data Tidak Muncul**

| Fix | Lokasi | Impact |
|-----|--------|--------|
| Hapus `isEmpty()` check | SeleksiWawancaraScreen.kt | Data selalu fresh |
| Hapus redundant LaunchedEffect | SeleksiWawancaraScreen.kt | No race condition |
| Tambah loading indicator | SeleksiWawancaraScreen.kt | Better UX |

### **BUG #2: Login Ulang**

| Fix | Lokasi | Impact |
|-----|--------|--------|
| Persist auth dengan SharedPreferences | AuthViewModel.kt | Session tetap ada |
| Load auth state di init | AuthViewModel.kt | Auto-login |
| Handle notification intent | MainActivity.kt | Proper navigation |
| Handle onNewIntent | MainActivity.kt | Work saat app running |

---

## ✅ TESTING CHECKLIST

### **Test BUG #1:**
```
☐ Buka Seleksi Wawancara pertama kali → Data langsung muncul
☐ Loading indicator muncul saat load data
☐ Back ke home, buka lagi → Data tetap muncul
☐ Logout, login, buka lagi → Data fresh dari server
☐ Tidak perlu tap berulang kali
```

### **Test BUG #2:**
```
☐ Login → Close app → Buka app → Masih login
☐ Login → Tap notifikasi → App terbuka, masih login
☐ Login → Kill app → Tap notifikasi → App terbuka, masih login
☐ Logout → Token & user data terhapus
☐ Notifikasi navigate ke screen yang benar
```

---

## 🎯 LESSONS LEARNED

### **1. LaunchedEffect Best Practices:**
- ❌ Jangan pakai multiple LaunchedEffect dengan logic yang sama
- ❌ Jangan check `isEmpty()` untuk data yang harus selalu fresh
- ✅ Pakai key yang tepat (token, userId, dll)
- ✅ Satu effect untuk satu purpose

### **2. State Management:**
- ❌ Jangan simpan state penting hanya di memory
- ❌ Jangan asumsikan ViewModel selalu hidup
- ✅ Persist auth state dengan SharedPreferences
- ✅ Load state saat init ViewModel

### **3. Notification Handling:**
- ❌ Jangan abaikan notification intent
- ❌ Jangan lupa handle onNewIntent
- ✅ Pass intent ke NavGraph untuk routing
- ✅ Handle both onCreate dan onNewIntent

### **4. User Experience:**
- ❌ Jangan biarkan user bingung tanpa feedback
- ❌ Jangan buat user tap berulang kali
- ✅ Tampilkan loading indicator
- ✅ Berikan feedback yang jelas

---

**Fixed by:** Cascade AI  
**Date:** 2025-12-05  
**Status:** ✅ READY FOR TESTING
