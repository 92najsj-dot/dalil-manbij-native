package com.dalilmanbij.app

import android.os.Bundle
import android.util.Base64
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF176B50)
private val DarkGreen = Color(0xFF123D30)
private val Gold = Color(0xFFF4B45F)
private val Muted = Color(0xFF68776E)
private val Mint = Color(0xFFE8F5ED)
private val categories = listOf("مطاعم ومقاهي", "بقاليات", "ملابس", "موبايلات وإلكترونيات", "خدمات", "طلبات", "عقارات", "سيارات", "أثاث منزلي")

private data class DemoItem(val title: String, val category: String, val area: String, val details: String, val price: String = "", val currency: String = "ل.س", val contact: String = "", val imageUri: String = "")
private data class DemoShop(val name: String, val category: String, val area: String, val description: String, val phone: String = "", val hours: String = "تُحدّد ساعات العمل من صاحب المحل", val verified: Boolean = false)
private val demoShops = listOf(
    DemoShop("مثال: متجر ألبسة", "ملابس", "منبج", "صفحة محل توضيحية. هذه البيانات ليست لمتجر حقيقي."),
    DemoShop("مثال: صيانة أجهزة", "موبايلات وإلكترونيات", "منبج", "خدمات صيانة كمثال توضيحي فقط."),
    DemoShop("مثال: مطعم محلي", "مطاعم ومقاهي", "ريف منبج", "صفحة مطعم توضيحية وليست قائمة منشآت حقيقية.")
)
private val demoItems = listOf(
    DemoItem("مثال: محل ألبسة في منبج", "ملابس", "منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً.", "150000", "ل.س"),
    DemoItem("مثال: صيانة موبايلات", "موبايلات وإلكترونيات", "منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً.", "حسب العطل", "ل.س"),
    DemoItem("مثال: أثاث منزلي للبيع", "أثاث منزلي", "ريف منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً.", "75", "$"),
    DemoItem("مثال: مطعم محلي", "مطاعم ومقاهي", "منبج", "محتوى توضيحي فقط، وليس بيانات محل حقيقي."),
    DemoItem("مثال: طلب عامل بناء", "طلبات", "ريف منبج", "طلب توضيحي: مطلوب عامل بناء للعمل اليومي. ليس طلباً حقيقياً."),
    DemoItem("مثال: طلب شراء غسالة", "طلبات", "منبج", "طلب توضيحي لشراء غسالة مستعملة. ليس طلباً حقيقياً."),
    DemoItem("مثال: خدمة كهربائي منازل", "خدمات", "منبج", "خدمة توضيحية فقط. لا يوجد مزود خدمة حقيقي مرتبط بهذا الإعلان."),
    DemoItem("مثال: خدمة نقل أثاث", "خدمات", "ريف منبج", "خدمة توضيحية فقط. تفاصيل التواصل غير متاحة."),
    DemoItem("مثال: شقة للإيجار", "عقارات", "منبج", "محتوى توضيحي فقط، وليس عرضاً حقيقياً.", "100", "$"),
    DemoItem("مثال: سيارة للبيع", "سيارات", "ريف منبج", "محتوى توضيحي فقط، وليس عرضاً حقيقياً.", "2500", "$"),
    DemoItem("مثال: بقالية محلية", "بقاليات", "منبج", "محتوى توضيحي فقط، وليس بيانات متجر حقيقي.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DalilApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DalilApp() {
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var area by remember { mutableStateOf("كل المناطق") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var filterCurrency by remember { mutableStateOf("الكل") }
    var sortOrder by remember { mutableStateOf("الأحدث أولاً") }
    var statusFilter by remember { mutableStateOf("الكل") }
    var filters by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<DemoItem?>(null) }
    var selectedShop by remember { mutableStateOf<DemoShop?>(null) }
    var reportItem by remember { mutableStateOf<DemoItem?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var showMyItems by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DemoItem?>(null) }
    var infoPage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val savedPrefs = remember(context) { context.getSharedPreferences("dalil_native_preferences", Context.MODE_PRIVATE) }
    var userItems by remember { mutableStateOf(loadUserItems(savedPrefs)) }
    var saved by remember {
        mutableStateOf(savedPrefs.getStringSet("saved_titles", emptySet())?.toSet() ?: emptySet())
    }
    var displayName by remember { mutableStateOf(savedPrefs.getString("local_display_name", "").orEmpty()) }
    var authEmail by remember { mutableStateOf(savedPrefs.getString("test_auth_email", "").orEmpty()) }
    var authToken by remember { mutableStateOf(savedPrefs.getString("test_auth_token", "").orEmpty()) }
    var remoteItems by remember { mutableStateOf<List<DemoItem>>(emptyList()) }
    var backendStatus by remember { mutableStateOf("جارٍ تحميل إعلانات بيئة الاختبار…") }

    LaunchedEffect(userItems) { saveUserItems(savedPrefs, userItems) }

    LaunchedEffect(authToken) {
        Thread {
            val result = runCatching { fetchPublishedTestListings(authToken.ifBlank { null }) }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                result.onSuccess { loaded ->
                    remoteItems = loaded
                    backendStatus = "اتصال قاعدة الاختبار ناجح • " + loaded.size + " إعلان منشور"
                }.onFailure { error ->
                    backendStatus = "تعذر تحميل الإعلانات التجريبية: " + (error.message ?: "خطأ اتصال")
                }
            }
        }.start()
    }

    LaunchedEffect(saved) {
        savedPrefs.edit().putStringSet("saved_titles", saved.toSet()).apply()
    }

    BackHandler(enabled = detail != null || selectedShop != null || category != null || reportItem != null || showCreate || showMyItems || infoPage != null) {
        if (showCreate) showCreate = false else if (showMyItems) showMyItems = false else if (reportItem != null) reportItem = null else if (detail != null) detail = null else if (selectedShop != null) selectedShop = null else if (infoPage != null) infoPage = null else category = null
    }

    val allItems = demoItems + userItems + remoteItems
    val visible = allItems.filter {
        (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.area.contains(query, true) || it.details.contains(query, true)) &&
        (category == null || it.category == category) &&
        (area == "كل المناطق" || it.area == area) &&
        (statusFilter == "الكل" || (statusFilter == "متاح" && !it.details.startsWith("الحالة: تم البيع")) || (statusFilter == "تم البيع" && it.details.startsWith("الحالة: تم البيع"))) &&
        (filterCurrency == "الكل" || (it.price.isNotBlank() && it.currency == filterCurrency)) &&
        (minPrice.isBlank() || (it.price.replace(",", ".").toDoubleOrNull()?.let { price -> price >= (minPrice.toDoubleOrNull() ?: 0.0) } ?: false)) &&
        (maxPrice.isBlank() || (it.price.replace(",", ".").toDoubleOrNull()?.let { price -> price <= (maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE) } ?: false))
    }.let { result ->
        when (sortOrder) {
            "السعر الأقل" -> result.sortedBy { it.price.replace(",", ".").toDoubleOrNull() ?: Double.MAX_VALUE }
            "السعر الأعلى" -> result.sortedByDescending { it.price.replace(",", ".").toDoubleOrNull() ?: -1.0 }
            "الاسم أ-ي" -> result.sortedBy { it.title.lowercase() }
            else -> result.reversed()
        }
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Gold, background = Color(0xFFF5F8F5))) {
        Scaffold(
            containerColor = Color(0xFFF5F8F5),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (detail != null || selectedShop != null || category != null || showCreate || showMyItems || infoPage != null) IconButton(onClick = { if (showCreate) showCreate = false else if (showMyItems) showMyItems = false else if (detail != null) detail = null else if (selectedShop != null) selectedShop = null else if (infoPage != null) infoPage = null else category = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    title = { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("دليل منبج", fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                        Text("سوقك بجيبك", fontSize = 12.sp, color = Muted)
                    } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFFF3EDF7)) {
                    NavigationBarItem(tab == 0, { tab = 0; category = null; detail = null; selectedShop = null; infoPage = null }, { Icon(Icons.Default.Home, "الرئيسية") }, label = { Text("الرئيسية") })
                    NavigationBarItem(tab == 1, { tab = 1; category = null; detail = null; selectedShop = null }, { Icon(Icons.Default.Store, "المحلات") }, label = { Text("المحلات") })
                    NavigationBarItem(tab == 2, { tab = 2; category = null; detail = null; selectedShop = null }, { Icon(Icons.Default.FavoriteBorder, "المحفوظات") }, label = { Text("المحفوظات") })
                    NavigationBarItem(tab == 3, { tab = 3; category = null; detail = null; selectedShop = null }, { Icon(Icons.Default.Person, "حسابي") }, label = { Text("حسابي") })
                }
            }
        ) { padding ->
            when {
                showCreate -> CreateItemPage(initialItem = editingItem, onCancel = { showCreate = false; editingItem = null }, onCreate = { item ->
                    val oldItem = editingItem
                    if (authToken.isNotBlank() && oldItem == null) {
                        Thread {
                            val result = runCatching { createTestListing(item, authToken) }
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                result.onSuccess {
                                    Toast.makeText(context, "تم إرسال الإعلان لقاعدة الاختبار، وهو بانتظار المراجعة", Toast.LENGTH_LONG).show()
                                    Thread {
                                        val refresh = runCatching { fetchPublishedTestListings(authToken.ifBlank { null }) }
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            refresh.onSuccess { loaded -> remoteItems = loaded }
                                                .onFailure { backendStatus = "تم إرسال الإعلان، لكن تعذّر تحديث القائمة تلقائياً" }
                                        }
                                    }.start()
                                }.onFailure { error ->
                                    Toast.makeText(context, "تعذر إرسال الإعلان: " + (error.message ?: "خطأ"), Toast.LENGTH_LONG).show()
                                }
                            }
                        }.start()
                    } else {
                        userItems = if (oldItem == null) userItems + item else userItems.map { if (it == oldItem) item else it }
                        Toast.makeText(context, if (oldItem == null) "تم حفظ الإعلان محلياً للتجربة فقط؛ سجّل الدخول لإرساله" else "تم تعديل الإعلان محلياً", Toast.LENGTH_LONG).show()
                    }
                    editingItem = null; tab = 0; category = null; query = ""; showCreate = false
                }, modifier = Modifier.padding(padding))
                showMyItems -> MyItemsPage(userItems = userItems, onBack = { showMyItems = false }, onEdit = { item -> editingItem = item; showMyItems = false; showCreate = true }, onDelete = { item -> userItems = userItems.filterNot { it == item }; saved = saved - item.title; Toast.makeText(context, "حُذف الإعلان من هذا الجهاز فقط", Toast.LENGTH_SHORT).show() }, onSold = { item -> userItems = userItems.map { if (it == item) it.copy(details = if (it.details.startsWith("الحالة: تم البيع")) it.details.removePrefix("الحالة: تم البيع\n") else "الحالة: تم البيع\n" + it.details) else it }; Toast.makeText(context, "تم تحديث الحالة محلياً", Toast.LENGTH_SHORT).show() }, modifier = Modifier.padding(padding))
                reportItem != null -> ReportDialog(reportItem!!, { reportItem = null }, { reason ->
                    val prefs = context.getSharedPreferences("dalil_native_preferences", Context.MODE_PRIVATE)
                    val old = prefs.getString("local_reports", "").orEmpty()
                    prefs.edit().putString("local_reports", old + "\n" + reportItem!!.title + " — " + reason).apply()
                    Toast.makeText(context, "تم حفظ البلاغ على هذا الجهاز للتجربة فقط", Toast.LENGTH_LONG).show()
                    reportItem = null
                })
                selectedShop != null -> ShopProfilePage(selectedShop!!, onBack = { selectedShop = null }, onContact = { shop -> openShopWhatsApp(context, shop) }, modifier = Modifier.padding(padding))
                detail != null -> DetailPage(
                    detail!!,
                    saved.contains(detail!!.title),
                    { saved = if (saved.contains(detail!!.title)) saved - detail!!.title else saved + detail!!.title },
                    { shareItem(context, detail!!) },
                    { openWhatsApp(context, detail!!) },
                    { reportItem = detail },
                    Modifier.padding(padding)
                )
                tab == 0 && category != null -> ResultsPage(category!!, visible, saved, { detail = it }, { title -> saved = if (saved.contains(title)) saved - title else saved + title }, Modifier.padding(padding))
                tab == 0 -> HomePage(query, { query = it; category = null }, filters, { filters = !filters }, area, { area = it }, minPrice, { minPrice = it }, maxPrice, { maxPrice = it }, filterCurrency, { filterCurrency = it }, sortOrder, { sortOrder = it }, statusFilter, { statusFilter = it }, visible, saved, { category = it }, { detail = it }, { title -> saved = if (saved.contains(title)) saved - title else saved + title }, backendStatus, Modifier.padding(padding))
                tab == 1 -> ShopsPage(demoShops, { selectedShop = it }, Modifier.padding(padding))
                tab == 2 -> SavedPage(allItems.filter { saved.contains(it.title) }, { detail = it }, { title -> saved = saved - title }, Modifier.padding(padding))
                infoPage == "تسجيل الدخول" -> AuthPage(
                    currentEmail = authEmail,
                    onBack = { infoPage = null },
                    onAuthSuccess = { email, token ->
                        authEmail = email
                        authToken = token
                        savedPrefs.edit().putString("test_auth_email", email).putString("test_auth_token", token).apply()
                        infoPage = null
                        Toast.makeText(context, "تم تسجيل الدخول إلى بيئة الاختبار", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.padding(padding)
                )
                infoPage != null -> InfoPage(infoPage!!, onBack = { infoPage = null }, modifier = Modifier.padding(padding))
                else -> AccountPage(
                    displayName = displayName,
                    authEmail = authEmail,
                    onLogout = { authEmail = ""; authToken = ""; savedPrefs.edit().remove("test_auth_email").remove("test_auth_token").apply(); Toast.makeText(context, "تم تسجيل الخروج من بيئة الاختبار", Toast.LENGTH_SHORT).show() },
                    onDisplayNameChange = { name ->
                        displayName = name.trim()
                        savedPrefs.edit().putString("local_display_name", displayName).apply()
                        Toast.makeText(context, "تم حفظ الاسم على هذا الجهاز فقط", Toast.LENGTH_SHORT).show()
                    },
                    onCreate = { showCreate = true },
                    onMyItems = { showMyItems = true },
                    onInfo = { infoPage = it },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun HomePage(
    query: String, onQuery: (String) -> Unit, filters: Boolean, onToggleFilters: () -> Unit,
    area: String, onArea: (String) -> Unit, minPrice: String, onMinPrice: (String) -> Unit, maxPrice: String, onMaxPrice: (String) -> Unit, filterCurrency: String, onFilterCurrency: (String) -> Unit, sortOrder: String, onSortOrder: (String) -> Unit, statusFilter: String, onStatusFilter: (String) -> Unit, items: List<DemoItem>, saved: Set<String>,
    onCategory: (String) -> Unit, onDetail: (DemoItem) -> Unit, onSave: (String) -> Unit, backendStatus: String,
    modifier: Modifier = Modifier
) {
    val visibleCategories = categories.filter { query.isBlank() || it.contains(query, ignoreCase = true) }
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(13.dp), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DarkGreen)) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("كل منبج بمكان واحد", color = Gold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text("كل اللي بدك ياه، بتلاقيه قريب منك.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(7.dp))
                    Text("دليل المحلات والخدمات، وسوق محلي للبيع والشراء، وطلباتك اليومية من مكان واحد.", color = Color.White)
                    Spacer(Modifier.height(5.dp))
                    Text(backendStatus, color = Color(0xFFD8F1E3), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D9))) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF9B6500), modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("العروض والإعلانات المميزة", fontWeight = FontWeight.ExtraBold, color = DarkGreen)
                        Text("معاينة محلية فقط — لا توجد حملة أو دفعة حقيقية", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                        Text("تجريبي", color = Color(0xFF9B6500), modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(15.dp), placeholder = { Text("ابحث عن محل أو إعلان...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { Row {
                    IconButton(onClick = onToggleFilters) { Icon(Icons.Default.Tune, "الفلاتر") }
                    if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Default.Close, "مسح البحث") }
                } }
            )
        }
        if (filters) item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("تصفية حسب المنطقة", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        FilterChip(area == "كل المناطق", { onArea("كل المناطق") }, { Text("كل المناطق") })
                        FilterChip(area == "منبج", { onArea("منبج") }, { Text("منبج") })
                        FilterChip(area == "ريف منبج", { onArea("ريف منبج") }, { Text("ريف منبج") })
                    }
                    Text("تصفية تجريبية على بيانات نموذجية فقط.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    Text("السعر والعملة", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = minPrice, onValueChange = { value -> onMinPrice(value.filter { ch -> ch.isDigit() || ch == '.' }) }, label = { Text("من") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = maxPrice, onValueChange = { value -> onMaxPrice(value.filter { ch -> ch.isDigit() || ch == '.' }) }, label = { Text("إلى") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(filterCurrency == "الكل", { onFilterCurrency("الكل") }, { Text("كل العملات") })
                        FilterChip(filterCurrency == "ل.س", { onFilterCurrency("ل.س") }, { Text("ليرة سورية") })
                        FilterChip(filterCurrency == "$", { onFilterCurrency("$") }, { Text("دولار") })
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("حالة الإعلان", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(statusFilter == "الكل", { onStatusFilter("الكل") }, { Text("الكل") })
                        FilterChip(statusFilter == "متاح", { onStatusFilter("متاح") }, { Text("متاح") })
                        FilterChip(statusFilter == "تم البيع", { onStatusFilter("تم البيع") }, { Text("تم البيع") })
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("ترتيب النتائج", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        FilterChip(sortOrder == "الأحدث أولاً", { onSortOrder("الأحدث أولاً") }, { Text("الأحدث") })
                        FilterChip(sortOrder == "السعر الأقل", { onSortOrder("السعر الأقل") }, { Text("الأرخص") })
                        FilterChip(sortOrder == "السعر الأعلى", { onSortOrder("السعر الأعلى") }, { Text("الأغلى") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        FilterChip(sortOrder == "الاسم أ-ي", { onSortOrder("الاسم أ-ي") }, { Text("الاسم أ-ي") })
                    }
                }
            }
        }
        item { Heading("الأقسام", "اختَر القسم المناسب") }
        if (visibleCategories.isEmpty()) item { EmptyBox("ما لقينا قسم مطابق", "جرّب كلمة بحث مختلفة.") }
        items(visibleCategories) { name ->
            ElevatedCard(onClick = { onCategory(name) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF7F1F9))) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, null, tint = Green)
                    Spacer(Modifier.width(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, "فتح القسم")
                }
            }
        }
        item { Heading("نماذج إعلانات", "بيانات تجريبية وليست منشورة") }
        if (items.isEmpty()) item { EmptyBox("ما لقينا نتائج مطابقة", "غيّر كلمة البحث أو المنطقة.") }
        items(items) { item -> ItemCard(item, saved.contains(item.title), { onDetail(item) }, { onSave(item.title) }) }
        item { Text("نسخة تطوير مستقلة؛ لا تتصل حالياً بالموقع أو قاعدة البيانات.", color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun ResultsPage(category: String, items: List<DemoItem>, saved: Set<String>, onDetail: (DemoItem) -> Unit, onSave: (String) -> Unit, modifier: Modifier = Modifier) {
    val results = items.filter { it.category == category }
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item { Heading(category, "قسم مستقل") }
        if (results.isEmpty()) item { EmptyBox("لا توجد عناصر في هذا القسم", "ستظهر البيانات الحقيقية بعد الربط لاحقاً.") }
        items(results) { item -> ItemCard(item, saved.contains(item.title), { onDetail(item) }, { onSave(item.title) }) }
    }
}

@Composable
private fun ShopsPage(shops: List<DemoShop>, onOpen: (DemoShop) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item { Heading("المحلات", "صفحات محلات تجريبية — ليست بيانات حقيقية") }
        items(shops) { shop ->
            ElevatedCard(onClick = { onOpen(shop) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Mint, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Storefront, null, tint = Green, modifier = Modifier.padding(12.dp).size(28.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(shop.name, fontWeight = FontWeight.Bold)
                        Text("${shop.category} • ${shop.area}", color = Muted, style = MaterialTheme.typography.bodySmall)
                        if (shop.verified) Text("محل موثّق", color = Green, style = MaterialTheme.typography.labelSmall)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
        item { EmptyBox("بيانات توضيحية فقط", "ستظهر المحلات الفعلية عند ربط خلفية آمنة بالتطبيق.") }
    }
}

@Composable
private fun ShopProfilePage(shop: DemoShop, onBack: () -> Unit, onContact: (DemoShop) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = DarkGreen), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Storefront, null, tint = Gold, modifier = Modifier.size(58.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(shop.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("${shop.category} • ${shop.area}", color = Color.White)
                    if (shop.verified) Text("✓ محل موثّق", color = Gold, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Heading("نبذة عن المحل", "معلومات الصفحة") }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(shop.description)
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, tint = Green); Spacer(Modifier.width(8.dp)); Text(shop.hours) }
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Green); Spacer(Modifier.width(8.dp)); Text("المنطقة: ${shop.area}") }
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Category, null, tint = Green); Spacer(Modifier.width(8.dp)); Text("النشاط: ${shop.category}") }
        } } }
        item { Button(onClick = { onContact(shop) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text("التواصل عبر واتساب") } }
        item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة إلى المحلات") } }
        item { Text("هذه صفحة توضيحية محلية. لا يوجد اتصال بصاحب محل حقيقي أو قاعدة بيانات.", color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun SavedPage(items: List<DemoItem>, onDetail: (DemoItem) -> Unit, onRemove: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item { Heading("المحفوظات", "العناصر التي حفظتها خلال الاستخدام") }
        if (items.isEmpty()) item { EmptyBox("ما عندك عناصر محفوظة", "اضغط القلب على بطاقة لحفظها.") }
        items(items) { item -> ItemCard(item, true, { onDetail(item) }, { onRemove(item.title) }) }
    }
}


@Composable
private fun AuthPage(
    currentEmail: String,
    onBack: () -> Unit,
    onAuthSuccess: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    var createAccount by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("هذه بيئة اختبار منفصلة عن الموقع الأساسي.") }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading("حساب الاختبار", "تسجيل دخول حقيقي إلى قاعدة الاختبار") }
        item { OutlinedTextField(value = email, onValueChange = { email = it.trim(); message = "" }, label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !createAccount, onClick = { createAccount = false }); Text("تسجيل الدخول")
            Spacer(Modifier.width(12.dp))
            RadioButton(selected = createAccount, onClick = { createAccount = true }); Text("إنشاء حساب")
        } }
        item { Text(message, color = Muted, style = MaterialTheme.typography.bodySmall) }
        item {
            Button(enabled = !busy && email.contains("@") && password.length >= 6, modifier = Modifier.fillMaxWidth(), onClick = {
                busy = true
                message = "جارٍ الاتصال…"
                Thread {
                    val result = runCatching {
                        val endpoint = if (createAccount) "/auth/v1/signup" else "/auth/v1/token?grant_type=password"
                        val conn = URL(BuildConfig.SUPABASE_URL + endpoint).openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"; conn.connectTimeout = 15000; conn.readTimeout = 15000; conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        val body = JSONObject().put("email", email.trim()).put("password", password)
                        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
                        val code = conn.responseCode
                        val response = (if (code in 200..299) conn.inputStream else conn.errorStream).bufferedReader(Charsets.UTF_8).use { it.readText() }
                        conn.disconnect()
                        val json = JSONObject(response.ifBlank { "{}" })
                        if (code !in 200..299) throw IllegalStateException(json.optString("msg", json.optString("message", json.optString("error_description", "خطأ من الخادم (" + code + ")"))))
                        val token = json.optString("access_token", "")
                        if (token.isBlank()) "تم إنشاء الحساب. إن طُلب تأكيد البريد، أكّده ثم سجّل الدخول."
                        else "AUTH_OK|" + email.trim() + "|" + token
                    }.getOrElse { "تعذّر الاتصال: " + (it.message ?: "خطأ غير معروف") }
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy = false
                        if (result.startsWith("AUTH_OK|")) {
                            val parts = result.split("|", limit = 3)
                            if (parts.size == 3) onAuthSuccess(parts[1], parts[2])
                            message = "نجح تسجيل الدخول."
                        } else message = result
                    }
                }.start()
            }) { Text(if (busy) "يرجى الانتظار…" else if (createAccount) "إنشاء حساب اختبار" else "تسجيل الدخول") }
        }
        item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") } }
        item { Text("استخدم كلمة مرور جديدة مخصصة للاختبار، ولا تستخدم كلمة مرورك الشخصية.", color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun AccountPage(
    displayName: String,
    authEmail: String,
    onLogout: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onMyItems: () -> Unit,
    onInfo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember(displayName) { mutableStateOf(displayName) }
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("الاسم الظاهر") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { if (it.length <= 40) nameDraft = it },
                    label = { Text("اسم اختياري") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDisplayNameChange(nameDraft)
                    showNameDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("إلغاء") }
            }
        )
    }
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountCircle, null, tint = Green, modifier = Modifier.size(60.dp))
                    Text("حسابي", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                    Text(displayName.ifBlank { "أضف اسماً ظاهراً (اختياري)" }, color = Muted)
                    Text(if (authEmail.isBlank()) "الحسابات التجريبية عبر بيئة الاختبار" else "حساب الاختبار: $authEmail", color = Muted)
                    if (authEmail.isNotBlank()) { TextButton(onClick = onLogout) { Text("تسجيل الخروج من حساب الاختبار") } }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { nameDraft = displayName; showNameDialog = true }) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (displayName.isBlank()) "إضافة اسم ظاهر" else "تعديل الاسم")
                    }
                }
            }
        }
        item { ElevatedCard(onClick = onMyItems, modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Campaign, null, tint = Green); Spacer(Modifier.width(12.dp)); Column { Text("إعلاناتي التجريبية", fontWeight = FontWeight.Bold); Text("عرض وحذف وتغيير حالة إعلانات هذا الجهاز", color = Muted, style = MaterialTheme.typography.bodySmall) } } } }
        item { ElevatedCard(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AddCircle, null, tint = Green); Spacer(Modifier.width(12.dp)); Column { Text("إضافة إعلان تجريبي", fontWeight = FontWeight.Bold); Text("حفظ محلي على هذا الجهاز فقط", color = Muted, style = MaterialTheme.typography.bodySmall) } } } }
        item { ElevatedCard(onClick = { onInfo("تسجيل الدخول") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Login, if (authEmail.isBlank()) "تسجيل الدخول / إنشاء حساب" else "الحساب متصل: $authEmail", "حسابات بيئة الاختبار المنفصلة") } }
        item { ElevatedCard(onClick = { onInfo("الملف الشخصي") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Person, "الملف الشخصي والاسم الظاهر", "معاينة إعدادات الملف الشخصي والصفحة العامة") } }
        item { ElevatedCard(onClick = { onInfo("الطلبات والخدمات") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Handyman, "الطلبات والخدمات", "تصفحها من الأقسام الرئيسية؛ البيانات محلية تجريبية") } }
        item { ElevatedCard(onClick = { onInfo("التحقق من المحل") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Verified, "طلب توثيق محل", "معاينة متطلبات التوثيق دون إرسال طلب حقيقي") } }
        item { ElevatedCard(onClick = { onInfo("الإشعارات") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Notifications, "الإشعارات", "سجل توضيحي؛ لا توجد إشعارات خادمية حقيقية") } }
        item { ElevatedCard(onClick = { onInfo("الترويج") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Star, "ترويج الإعلانات", "معاينة الباقات والمدة والأولوية دون دفع") } }
        item { ElevatedCard(onClick = { onInfo("العروض والبانرات") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Campaign, "العروض والبانرات", "معاينة واجهة الحملات الإعلانية؛ الإدارة غير متصلة") } }
        item { ElevatedCard(onClick = { onInfo("لوحة الإدارة") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.AdminPanelSettings, "لوحة الإدارة (معاينة)", "أدوات مراجعة وحظر وإحصاءات تجريبية فقط") } }
        item { ElevatedCard(onClick = { onInfo("عن التطبيق") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Info, "عن دليل منبج", "فكرة التطبيق ومعلومات النسخة") } }
        item { ElevatedCard(onClick = { onInfo("الخصوصية") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Security, "سياسة الخصوصية", "كيف تتعامل النسخة التجريبية مع البيانات") } }
        item { ElevatedCard(onClick = { onInfo("الشروط") }, modifier = Modifier.fillMaxWidth()) { AccountRow(Icons.Default.Description, "الشروط والأحكام", "حدود استخدام النسخة التجريبية") } }
        item { Text("لا يوجد اتصال بالخادم في هذه النسخة التجريبية.", color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun InfoPage(page: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val body = when (page) {
        "عن التطبيق" -> listOf(
            "دليل منبج — سوقك بجيبك",
            "نسخة أندرويد أصلية قيد التطوير لتجربة تصفح المحلات والإعلانات والخدمات المحلية.",
            "العناصر والمحلات الظاهرة حالياً أمثلة توضيحية وليست بيانات تجارية حقيقية.",
            "هذه النسخة لا تتصل بموقع دليل منبج الإنتاجي أو بقاعدة بياناته."
        )
        "الخصوصية" -> listOf(
            "خصوصية النسخة التجريبية",
            "الإعلانات التي تنشئها في هذه النسخة تُحفظ محلياً على جهازك باستخدام التخزين الداخلي للتطبيق.",
            "المحفوظات والبلاغات التجريبية محلية ولا تُرسل إلى الإدارة أو إلى خادم.",
            "لا تُدخل كلمات مرور أو معلومات مالية أو بيانات شخصية حساسة في هذه النسخة.",
            "حذف التطبيق أو بياناته من إعدادات Android قد يؤدي إلى حذف البيانات المحلية."
        )
        "تسجيل الدخول" -> listOf(
            "تسجيل الدخول وإنشاء حساب — معاينة",
            "الموقع الأصلي يدعم حسابات المستخدمين؛ هذه النسخة لا تتصل بمزود المصادقة ولا تنشئ حسابات حقيقية.",
            "تسجيل الدخول بالبريد وكلمة المرور، استعادة كلمة المرور، وتسجيل الدخول الاجتماعي غير مفعّلة.",
            "لا تدخل كلمة مرورك الحقيقية هنا. يلزم إعداد خلفية اختبار ومصادقة آمنة قبل تفعيلها."
        )
        "الملف الشخصي" -> listOf(
            "الملف الشخصي والصفحة العامة — معاينة",
            "يمكن حفظ الاسم الظاهر محلياً من شاشة حسابي.",
            "الملفات العامة، صورة الحساب، وتعديل الملف المتزامن بين الأجهزة غير متاحة حالياً لأنها تحتاج حساباً وخادماً."
        )
        "الطلبات والخدمات" -> listOf(
            "الطلبات والخدمات — تجربة محلية",
            "يمكن تجربة فئات الطلبات والخدمات عبر البحث والفلاتر وإضافة إعلان تجريبي.",
            "الطلب في هذه النسخة يستخدم نموذج إعلان محلي؛ لا يوجد مسار مستقل لإغلاق الطلب أو إدارة الردود.",
            "كل البيانات أمثلة أو بيانات محلية، ولا تُعرض على الموقع الأصلي."
        )
        "التحقق من المحل" -> listOf(
            "طلب توثيق محل — معاينة فقط",
            "في الموقع الأصلي يرسل طلب التوثيق إلى الإدارة للمراجعة.",
            "هذه النسخة لا ترسل الطلب ولا تمنح شارة موثّق فعلية. لا توجد مراجعة إدارية متصلة.",
            "يتطلب التوثيق الفعلي حفظ الطلب على الخادم والتحقق من هوية مالك المحل وصلاحيات الإدارة."
        )
        "لوحة الإدارة" -> listOf(
            "لوحة الإدارة — معاينة آمنة",
            "واجهة الموقع المرجعية تتضمن مراجعة الإعلانات، إدارة المستخدمين والحظر، إحصاءات الزوار، سجل الإجراءات وإدارة الحملات.",
            "هذه العناصر غير متصلة وغير متاحة كإجراءات إدارية في APK التجريبي؛ لن نضع صلاحيات إدارة حقيقية داخل تطبيق عام.",
            "التنفيذ الحقيقي يتطلب خادماً يفرض الصلاحيات، وسجلاً تدقيقياً، وحسابات إدارة مخوّلة. لا يمكن الوثوق بفحص صلاحية داخل التطبيق وحده."
        )
        "العروض والبانرات" -> listOf(
            "العروض والبانرات — معاينة",
            "الموقع يدعم إبراز الإعلانات والعروض والبانرات.",
            "لا توجد حالياً إدارة حملات فعلية أو رفع بانرات أو مزامنة عروض في التطبيق التجريبي.",
            "ستظهر هذه الوظائف فعلياً بعد وجود خلفية اختبار آمنة وآلية مراجعة ونشر."
        )
        "الإشعارات" -> listOf(
            "الإشعارات التجريبية",
            "لا توجد إشعارات حقيقية لأن التطبيق غير متصل بالخادم.",
            "عند إنشاء إعلان أو الإبلاغ عن عنصر في هذه النسخة، لا تُرسل إشعارات إلى الإدارة أو إلى مستخدمين آخرين.",
            "سيتم ربط إشعارات صاحب الإعلان وقراءة الإشعارات عند تنفيذ نظام الحساب والخادم الآمن."
        )
        "الترويج" -> listOf(
            "ترويج الإعلانات — معاينة فقط",
            "فكرة الترويج هي إبراز الإعلان في قسم مميز لفترة محددة، مع باقات ومدد وأولوية ظهور.",
            "لا توجد حالياً باقات مدفوعة أو دفع إلكتروني أو نشر مميز حقيقي داخل التطبيق.",
            "لا تدفع أي مبلغ من خلال هذه النسخة. سيتم تنفيذ الترويج الفعلي لاحقاً عبر خادم آمن."
        )
        else -> listOf(
            "الشروط والأحكام التجريبية",
            "التطبيق نسخة اختبار وليست خدمة نشر إعلانات فعلية.",
            "المحتوى التجريبي لا يمثل عروضاً أو أسعاراً أو ساعات عمل حقيقية.",
            "لا تُجرَ عمليات دفع أو تحقق محلات أو مراجعة إدارية حقيقية من داخل هذه النسخة.",
            "استخدام النسخة حالياً مخصص لتجربة الواجهة والوظائف المحلية."
        )
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(page, "معلومات مهمة عن النسخة الحالية") }
        items(body) { paragraph ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Text(paragraph, modifier = Modifier.fillMaxWidth().padding(16.dp), fontWeight = if (paragraph == body.first()) FontWeight.Bold else FontWeight.Normal)
            }
        }
        item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") } }
    }
}

@Composable
private fun AccountRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Green)
            Spacer(Modifier.width(12.dp))
            Column { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ItemCard(item: DemoItem, isSaved: Boolean, onOpen: () -> Unit, onSave: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            if (item.imageUri.isNotBlank()) {
                AsyncImage(model = item.imageUri, contentDescription = item.title, modifier = Modifier.fillMaxWidth().height(190.dp))
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Mint, shape = RoundedCornerShape(10.dp)) { Icon(Icons.Default.Storefront, null, tint = Green, modifier = Modifier.padding(12.dp).size(27.dp)) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text("${item.category} • ${item.area}", color = Muted, style = MaterialTheme.typography.bodySmall)
                    if (item.price.isNotBlank()) Text("${item.price} ${item.currency}", color = Green, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onSave) { Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "حفظ", tint = if (isSaved) Color(0xFFB3261E) else Muted) }
            }
            Spacer(Modifier.height(8.dp))
            val sold = item.details.startsWith("الحالة: تم البيع")
            if (sold) {
                Surface(color = Color(0xFFFFE9E6), shape = RoundedCornerShape(8.dp)) {
                    Text("تم البيع", color = Color(0xFF9B2C20), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                Spacer(Modifier.height(6.dp))
            }
            Text(item.details.removePrefix("الحالة: تم البيع\n"), color = Muted, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("بيانات تجريبية", color = Color(0xFF9B5A00), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpen) { Text("التفاصيل") }
            }
        }
    }
}

@Composable
private fun DetailPage(
    item: DemoItem,
    isSaved: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Heading("تفاصيل العنصر", "معاينة تجريبية")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                if (item.imageUri.isNotBlank()) {
                    AsyncImage(model = item.imageUri, contentDescription = item.title, modifier = Modifier.fillMaxWidth().height(240.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Icon(Icons.Default.Storefront, null, tint = Green, modifier = Modifier.size(52.dp))
                Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("${item.category} • ${item.area}", color = Muted)
                if (item.price.isNotBlank()) Text("السعر: ${item.price} ${item.currency}", color = Green, fontWeight = FontWeight.Bold)
                if (item.contact.isNotBlank()) Text("للتواصل: ${item.contact}", color = Muted)
                Spacer(Modifier.height(12.dp))
                if (item.details.startsWith("الحالة: تم البيع")) {
                    Surface(color = Color(0xFFFFE9E6), shape = RoundedCornerShape(8.dp)) {
                        Text("تم البيع", color = Color(0xFF9B2C20), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(item.details.removePrefix("الحالة: تم البيع\n"))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSaved) "إزالة من المحفوظات" else "حفظ في المحفوظات")
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(8.dp))
                    Text("مشاركة العنصر")
                }
                OutlinedButton(onClick = onWhatsApp, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text("مشاركة عبر واتساب")
                }
                TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Flag, null, tint = Color(0xFFB3261E))
                    Spacer(Modifier.width(8.dp))
                    Text("الإبلاغ عن العنصر", color = Color(0xFFB3261E))
                }
                Text("هذه نسخة تجريبية: البلاغ محلي ولا يصل إلى الإدارة.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Heading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyBox(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, tint = Muted, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}



private fun createTestListing(item: DemoItem, accessToken: String) {
    val payload = accessToken.split(".").getOrNull(1) ?: throw IllegalStateException("جلسة الدخول غير صالحة")
    val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
    val ownerId = JSONObject(decoded).optString("sub", "")
    if (ownerId.isBlank()) throw IllegalStateException("تعذر تحديد حساب المستخدم")
    val parts = item.area.split("•").map { it.trim() }.filter { it.isNotBlank() }
    val price = item.price.replace(",", ".").toDoubleOrNull()
    val currency = if (item.currency == "$") "USD" else "SYP"
    val body = JSONObject()
        .put("owner_id", ownerId)
        .put("title", item.title)
        .put("category", item.category)
        .put("area", parts.firstOrNull() ?: "منبج")
        .put("village", parts.drop(1).joinToString(" • "))
        .put("details", item.details)
        .put("price", price ?: JSONObject.NULL)
        .put("currency", currency)
        .put("contact", item.contact)
        .put("image_urls", org.json.JSONArray())
        .put("status", "pending")
        .put("moderation_note", "")
    val connection = (URL(BuildConfig.SUPABASE_URL + "/rest/v1/listings").openConnection() as HttpURLConnection)
    connection.requestMethod = "POST"
    connection.connectTimeout = 15000
    connection.readTimeout = 15000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Prefer", "return=minimal")
    connection.setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
    connection.setRequestProperty("Authorization", "Bearer " + accessToken)
    OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
    val code = connection.responseCode
    val errorBody = if (code !in 200..299) (connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "") else ""
    connection.disconnect()
    if (code !in 200..299) throw IllegalStateException("رفضت قاعدة الاختبار الطلب ($code): " + errorBody.take(180))
}

private fun fetchPublishedTestListings(accessToken: String? = null): List<DemoItem> {
    val connection = (URL(BuildConfig.SUPABASE_URL + "/rest/v1/listings?select=title,category,area,village,details,price,currency,contact,image_urls,status&order=created_at.desc&limit=100").openConnection() as HttpURLConnection)
    connection.requestMethod = "GET"
    connection.connectTimeout = 15000
    connection.readTimeout = 15000
    connection.setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
    connection.setRequestProperty("Authorization", "Bearer " + (accessToken?.takeIf { it.isNotBlank() } ?: BuildConfig.SUPABASE_PUBLISHABLE_KEY))
    connection.setRequestProperty("Accept", "application/json")
    val code = connection.responseCode
    val body = (if (code in 200..299) connection.inputStream else connection.errorStream).bufferedReader(Charsets.UTF_8).use { it.readText() }
    connection.disconnect()
    if (code !in 200..299) throw IllegalStateException("قاعدة البيانات ردّت بالرمز " + code)
    val array = org.json.JSONArray(body)
    return (0 until array.length()).mapNotNull { index ->
        val row = array.optJSONObject(index) ?: return@mapNotNull null
        val title = row.optString("title", "")
        if (title.isBlank()) return@mapNotNull null
        val currency = if (row.optString("currency", "SYP") == "USD") "$" else "ل.س"
        val imageArray = row.optJSONArray("image_urls")
        val image = if (imageArray != null && imageArray.length() > 0) imageArray.optString(0, "") else ""
        DemoItem(
            title = title,
            category = row.optString("category", ""),
            area = listOf(row.optString("area", ""), row.optString("village", "")).filter { it.isNotBlank() }.distinct().joinToString(" • "),
            details = (if (row.optString("status", "published") != "published") "الحالة: بانتظار المراجعة\\n" else "") + row.optString("details", ""),
            price = if (row.isNull("price")) "" else row.optString("price", ""),
            currency = currency,
            contact = row.optString("contact", ""),
            imageUri = image
        )
    }
}

private fun shareItem(context: Context, item: DemoItem) {
    val text = "${item.title}\nالقسم: ${item.category}\nالمنطقة: ${item.area}\n${item.details}\n— دليل منبج (نسخة تجريبية)"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة عبر"))
}

private fun openWhatsApp(context: Context, item: DemoItem) {
    val text = Uri.encode("${item.title} — ${item.category} — ${item.area}\n${item.details}\nمن دليل منبج")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$text"))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "تعذر فتح واتساب أو المتصفح", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun ReportDialog(item: DemoItem, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember(item.title) { mutableStateOf("معلومات غير صحيحة") }
    val reasons = listOf("معلومات غير صحيحة", "إعلان مكرر", "محتوى مخالف", "اشتباه احتيال", "سبب آخر")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("الإبلاغ عن العنصر") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.title, fontWeight = FontWeight.Bold)
                reasons.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = reason == option, onClick = { reason = option })
                        Text(option)
                    }
                }
                Text("سيُحفظ البلاغ محلياً على الجهاز للتجربة فقط، ولن يصل للإدارة.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(reason) }) { Text("إرسال البلاغ التجريبي") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateItemPage(initialItem: DemoItem? = null, onCancel: () -> Unit, onCreate: (DemoItem) -> Unit, modifier: Modifier = Modifier) {
    var title by remember(initialItem) { mutableStateOf(initialItem?.title.orEmpty()) }
    var category by remember(initialItem) { mutableStateOf(initialItem?.category ?: categories.first()) }
    var area by remember(initialItem) { mutableStateOf(initialItem?.area ?: "منبج") }
    var details by remember(initialItem) { mutableStateOf(initialItem?.details?.removePrefix("الحالة: تم البيع\n").orEmpty()) }
    var price by remember(initialItem) { mutableStateOf(initialItem?.price.orEmpty()) }
    var currency by remember(initialItem) { mutableStateOf(initialItem?.currency ?: "ل.س") }
    var contact by remember(initialItem) { mutableStateOf(initialItem?.contact.orEmpty()) }
    var imageUri by remember(initialItem) { mutableStateOf(initialItem?.imageUri.orEmpty()) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = uri.toString()
        }
    }
    var currencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading(if (initialItem == null) { if (category == "طلبات") "إضافة طلب تجريبي" else "إضافة إعلان تجريبي" } else { if (category == "طلبات") "تعديل طلب تجريبي" else "تعديل إعلان تجريبي" }, "لن يُنشر على الموقع ولن يراه مستخدمون آخرون") }
        item { OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text("عنوان الإعلان") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item {
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("القسم") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) { categories.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false }) } }
            }
        }
        item {
            ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = !areaExpanded }) {
                OutlinedTextField(value = area, onValueChange = {}, readOnly = true, label = { Text("المنطقة") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(areaExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) { listOf("منبج", "ريف منبج").forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { area = option; areaExpanded = false }) } }
            }
        }
        item { OutlinedTextField(value = price, onValueChange = { value -> price = value.filter { it.isDigit() || it == '.' || it == ',' }; error = null }, label = { Text("السعر (اختياري)") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = !currencyExpanded }) { OutlinedTextField(value = currency, onValueChange = {}, readOnly = true, label = { Text("العملة") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth()); ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) { listOf("ل.س", "$").forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { currency = option; currencyExpanded = false }) } } } }
        item { OutlinedTextField(value = contact, onValueChange = { contact = it; error = null }, label = { Text("رقم التواصل (اختياري)") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item {
            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Image, null)
                Spacer(Modifier.width(8.dp))
                Text(if (imageUri.isBlank()) "اختيار صورة للإعلان" else "تغيير صورة الإعلان")
            }
            if (imageUri.isNotBlank()) {
                AsyncImage(model = imageUri, contentDescription = "صورة الإعلان المختارة", modifier = Modifier.fillMaxWidth().height(190.dp))
                TextButton(onClick = { imageUri = "" }) { Text("إزالة الصورة") }
            }
        }
        item { OutlinedTextField(value = details, onValueChange = { details = it; error = null }, label = { Text("تفاصيل الإعلان") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        if (error != null) item { Text(error!!, color = Color(0xFFB3261E)) }
        item { Button(onClick = { if (title.trim().length < 3) error = "اكتب عنواناً من 3 أحرف على الأقل" else if (details.trim().length < 5) error = "أضف تفاصيل أوضح للإعلان" else onCreate(DemoItem(title.trim(), category, area, details.trim(), price.trim().replace(',', '.'), currency, contact.trim(), imageUri)) }, modifier = Modifier.fillMaxWidth()) { Text(if (initialItem == null) { if (category == "طلبات") "حفظ الطلب التجريبي" else "حفظ الإعلان التجريبي" } else "حفظ التعديلات") } }
        item { OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("إلغاء") } }
    }
}

private fun loadUserItems(prefs: android.content.SharedPreferences): List<DemoItem> =
    prefs.getString("user_items_v1", "").orEmpty().lineSequence().filter { it.isNotBlank() }.mapNotNull { row ->
        runCatching {
            val p = row.split(".")
            if (p.size != 4 && p.size != 7 && p.size != 8) null else {
                fun decode(index: Int) = String(Base64.decode(p[index], Base64.NO_WRAP), Charsets.UTF_8)
                DemoItem(decode(0), decode(1), decode(2), decode(3),
                    if (p.size >= 7) decode(4) else "",
                    if (p.size >= 7) decode(5) else "ل.س",
                    if (p.size >= 7) decode(6) else "",
                    if (p.size == 8) decode(7) else "")
            }
        }.getOrNull()
    }.toList()

private fun saveUserItems(prefs: android.content.SharedPreferences, items: List<DemoItem>) {
    val encoded = items.joinToString("\n") { item ->
        listOf(item.title, item.category, item.area, item.details, item.price, item.currency, item.contact, item.imageUri)
            .joinToString(".") { Base64.encodeToString(it.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }
    }
    prefs.edit().putString("user_items_v1", encoded).apply()
}
@Composable
private fun MyItemsPage(
    userItems: List<DemoItem>,
    onBack: () -> Unit,
    onEdit: (DemoItem) -> Unit,
    onDelete: (DemoItem) -> Unit,
    onSold: (DemoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading("إعلاناتي التجريبية", "هذه الإعلانات محفوظة على جهازك فقط ولا تظهر للناس") }
        if (userItems.isEmpty()) {
            item { EmptyBox("لا توجد إعلانات أضفتها", "ارجع إلى حسابي واضغط «إضافة إعلان تجريبي».") }
        } else {
            items(userItems) { item ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("${item.category} • ${item.area}", color = Muted, style = MaterialTheme.typography.bodySmall)
                        if (item.details.startsWith("الحالة: تم البيع")) {
                            Text("تم البيع (محلي)", color = Color(0xFF176B50), fontWeight = FontWeight.Bold)
                        }
                        Text(item.details.removePrefix("الحالة: تم البيع\n"))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(item) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(4.dp)); Text("تعديل") }
                            OutlinedButton(onClick = { onSold(item) }, modifier = Modifier.weight(1f)) {
                                Text(if (item.details.startsWith("الحالة: تم البيع")) "إلغاء حالة البيع" else "تم البيع")
                            }
                            OutlinedButton(onClick = { onDelete(item) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Delete, null)
                                Spacer(Modifier.width(4.dp))
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }
        item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") } }
    }
}
private fun openShopWhatsApp(context: Context, shop: DemoShop) {
    if (shop.phone.isBlank()) {
        Toast.makeText(context, "لا يوجد رقم تواصل لهذا المحل التجريبي", Toast.LENGTH_LONG).show()
        return
    }
    val digits = shop.phone.filter { it.isDigit() }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, "تعذر فتح واتساب أو المتصفح", Toast.LENGTH_LONG).show()
    }
}
