# 🧹 CARA MENGHAPUS FILE SHAREDPREFERENCES LAMA

## ⚠️ PROBLEM

File `commitech_auth.xml` masih ada dari implementasi lama (sebelum hybrid session).
Ini harus dihapus untuk memastikan hybrid session bekerja dengan benar.

---

## ✅ SOLUSI 1: Uninstall & Reinstall App (RECOMMENDED)

### **Step 1: Uninstall via Android Studio**
```
1. Buka Android Studio
2. Klik menu "Run" → "Stop 'app'" (jika app sedang running)
3. Buka Terminal di Android Studio (Alt + F12)
4. Jalankan command:
   adb uninstall com.example.commitech
5. Output expected:
   Success
```

### **Step 2: Clean Project**
```
1. Klik menu "Build" → "Clean Project"
2. Tunggu sampai selesai
3. Klik menu "Build" → "Rebuild Project"
4. Tunggu sampai selesai
```

### **Step 3: Run App**
```
1. Klik tombol Run (▶️) atau Shift + F10
2. App akan di-install ulang
3. Semua data lama (termasuk SharedPreferences) akan terhapus
```

### **Step 4: Verify**
```
1. Login dengan credentials:
   Email: admin@gmail.com
   Password: [your password]

2. Check Device File Explorer:
   Path: /data/data/com.example.commitech/shared_prefs/
   
   Expected: ❌ Folder kosong atau file commitech_auth.xml tidak ada
   
   Jika file ada, isi harus:
   <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
   <map />
```

---

## ✅ SOLUSI 2: Delete via ADB (ALTERNATIVE)

Jika tidak mau uninstall, bisa delete file manual:

### **Step 1: Delete File**
```bash
# Via ADB command
adb shell "run-as com.example.commitech rm shared_prefs/commitech_auth.xml"

# Expected output:
# (no output = success)
```

### **Step 2: Verify**
```bash
# Check if file deleted
adb shell "run-as com.example.commitech ls shared_prefs/"

# Expected output:
# (empty) atau error "No such file or directory"
```

### **Step 3: Force Stop App**
```bash
# Force stop app untuk clear memory
adb shell am force-stop com.example.commitech
```

### **Step 4: Run App Again**
```
1. Klik Run di Android Studio
2. Login ulang
3. Check Device File Explorer lagi
```

---

## ✅ SOLUSI 3: Clear App Data via Device Settings

### **Step 1: Open Settings**
```
1. Di emulator/device, buka "Settings"
2. Pilih "Apps" atau "Applications"
3. Cari "Commitech"
4. Tap pada app
```

### **Step 2: Clear Data**
```
1. Tap "Storage"
2. Tap "Clear Data" atau "Clear Storage"
3. Confirm
```

### **Step 3: Run App**
```
1. Buka app dari launcher
2. Login ulang
3. Verify SharedPreferences kosong
```

---

## 🔍 VERIFICATION CHECKLIST

Setelah clean, verify bahwa hybrid session bekerja:

### **✅ Test 1: SharedPreferences Kosong**
```
1. Login
2. Check Device File Explorer:
   /data/data/com.example.commitech/shared_prefs/
   
Expected: ❌ File commitech_auth.xml TIDAK ADA atau KOSONG
```

### **✅ Test 2: Token di Memory**
```
1. Login
2. Check Logcat:
   adb logcat | grep "AuthViewModel"
   
Expected: ✅ "Token: 41|3aQ9Lq..." (di memory)
Expected: ❌ TIDAK ada "saveAuthState" atau "loadAuthState"
```

### **✅ Test 3: App Killed = Login Ulang**
```
1. Login
2. Kill app dari recent apps
3. Buka app lagi

Expected: ✅ Harus login ulang
Expected: ❌ TIDAK auto-login
```

### **✅ Test 4: Session di Database**
```
1. Login
2. Check database (phpMyAdmin):
   SELECT * FROM sessions WHERE user_id = 2;
   
Expected: ✅ Ada 1 row dengan token yang sama seperti di Logcat
```

---

## 🎯 EXPECTED BEHAVIOR AFTER CLEAN

| Action | Before Clean | After Clean |
|--------|--------------|-------------|
| Login | ✅ Success | ✅ Success |
| SharedPreferences | ❌ Ada data | ✅ Kosong |
| Token location | ❌ Storage | ✅ Memory only |
| App killed | ❌ Auto-login | ✅ Harus login ulang |
| Session in DB | ✅ Yes | ✅ Yes |

---

## 📝 NOTES

### **Kenapa File Lama Masih Ada?**
- File SharedPreferences dibuat oleh kode lama (sebelum hybrid session)
- Uninstall app tidak otomatis terjadi saat update code
- File persist sampai app di-uninstall atau data di-clear

### **Apakah Harus Uninstall Setiap Kali?**
- ❌ TIDAK! Hanya sekali ini saja
- Setelah clean, file tidak akan dibuat lagi
- Hybrid session tidak pakai SharedPreferences

### **Bagaimana dengan User Production?**
- Jika deploy ke production, user yang sudah install akan auto-logout sekali
- Setelah itu, hybrid session akan bekerja normal
- Ini expected behavior untuk migration

---

## 🚀 QUICK COMMAND

Copy-paste command ini di Terminal Android Studio:

```bash
# All-in-one: Uninstall, Clean, Rebuild, Install
adb uninstall com.example.commitech && ./gradlew clean && ./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk

# Atau lebih simple (via Android Studio):
# 1. Run → Stop 'app'
# 2. Build → Clean Project
# 3. Build → Rebuild Project
# 4. Run → Run 'app'
```

---

**Setelah clean, test lagi dan pastikan file SharedPreferences tidak ada! 🎯**
