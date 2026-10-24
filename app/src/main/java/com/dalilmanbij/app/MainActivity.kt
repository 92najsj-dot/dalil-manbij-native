package com.dalilmanbij.app

import android.os.Bundle
import android.util.Base64
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF176B50)
private val DarkGreen = Color(0xFF123D30)
private val Gold = Color(0xFFF4B45F)
private val Muted = Color(0xFF68776E)
private val Mint = Color(0xFFE8F5ED)
private val categories = listOf("مطاعم ومقاهي", "بقاليات", "ملابس", "موبايلات وإلكترونيات", "خدمات", "عقارات", "سيارات", "أثاث منزلي")

private data class DemoItem(val title: String, val category: String, val area: String, val details: String, val price: String = "", val currency: String = "ل.س", val contact: String = "")
private val demoItems = listOf(
    DemoItem("مثال: محل ألبسة في منبج", "ملابس", "منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً."),
    DemoItem("مثال: صيانة موبايلات", "موبايلات وإلكترونيات", "منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً."),
    DemoItem("مثال: أثاث منزلي للبيع", "أثاث منزلي", "ريف منبج", "محتوى توضيحي فقط، وليس إعلاناً منشوراً."),
    DemoItem("مثال: مطعم محلي", "مطاعم ومقاهي", "منبج", "محتوى توضيحي فقط، وليس بيانات محل حقيقي.")
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
    var filters by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<DemoItem?>(null) }
    var reportItem by remember { mutableStateOf<DemoItem?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var showMyItems by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DemoItem?>(null) }
    val context = LocalContext.current
    val savedPrefs = remember(context) { context.getSharedPreferences("dalil_native_preferences", Context.MODE_PRIVATE) }
    var userItems by remember { mutableStateOf(loadUserItems(savedPrefs)) }
    var saved by remember {
        mutableStateOf(savedPrefs.getStringSet("saved_titles", emptySet())?.toSet() ?: emptySet())
    }

    LaunchedEffect(userItems) { saveUserItems(savedPrefs, userItems) }

    LaunchedEffect(saved) {
        savedPrefs.edit().putStringSet("saved_titles", saved.toSet()).apply()
    }

    BackHandler(enabled = detail != null || category != null || reportItem != null || showCreate || showMyItems) {
        if (showCreate) showCreate = false else if (showMyItems) showMyItems = false else if (reportItem != null) reportItem = null else if (detail != null) detail = null else category = null
    }

    val allItems = demoItems + userItems
    val visible = allItems.filter {
        (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.area.contains(query, true)) &&
        (category == null || it.category == category) &&
        (area == "كل المناطق" || it.area == area) && (filterCurrency == "الكل" || (it.price.isNotBlank() && it.currency == filterCurrency)) && (minPrice.isBlank() || (it.price.replace(",", ".").toDoubleOrNull()?.let { price -> price >= (minPrice.toDoubleOrNull() ?: 0.0) } ?: false)) && (maxPrice.isBlank() || (it.price.replace(",", ".").toDoubleOrNull()?.let { price -> price <= (maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE) } ?: false))
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Gold, background = Color(0xFFF5F8F5))) {
        Scaffold(
            containerColor = Color(0xFFF5F8F5),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (detail != null || category != null || showCreate || showMyItems) IconButton(onClick = { if (showCreate) showCreate = false else if (showMyItems) showMyItems = false else if (detail != null) detail = null else category = null }) {
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
                    NavigationBarItem(tab == 0, { tab = 0; category = null; detail = null }, { Icon(Icons.Default.Home, "الرئيسية") }, label = { Text("الرئيسية") })
                    NavigationBarItem(tab == 1, { tab = 1; category = null; detail = null }, { Icon(Icons.Default.Store, "المحلات") }, label = { Text("المحلات") })
                    NavigationBarItem(tab == 2, { tab = 2; category = null; detail = null }, { Icon(Icons.Default.FavoriteBorder, "المحفوظات") }, label = { Text("المحفوظات") })
                    NavigationBarItem(tab == 3, { tab = 3; category = null; detail = null }, { Icon(Icons.Default.Person, "حسابي") }, label = { Text("حسابي") })
                }
            }
        ) { padding ->
            when {
                showCreate -> CreateItemPage(initialItem = editingItem, onCancel = { showCreate = false; editingItem = null }, onCreate = { item -> val oldItem = editingItem; userItems = if (oldItem == null) userItems + item else userItems.map { if (it == oldItem) item else it }; editingItem = null; tab = 0; category = null; query = ""; showCreate = false; Toast.makeText(context, if (oldItem == null) "تم حفظ الإعلان محلياً للتجربة فقط" else "تم تعديل الإعلان محلياً", Toast.LENGTH_LONG).show() }, modifier = Modifier.padding(padding))
                showMyItems -> MyItemsPage(userItems = userItems, onBack = { showMyItems = false }, onEdit = { item -> editingItem = item; showMyItems = false; showCreate = true }, onDelete = { item -> userItems = userItems.filterNot { it == item }; saved = saved - item.title; Toast.makeText(context, "حُذف الإعلان من هذا الجهاز فقط", Toast.LENGTH_SHORT).show() }, onSold = { item -> userItems = userItems.map { if (it == item) it.copy(details = if (it.details.startsWith("الحالة: تم البيع")) it.details.removePrefix("الحالة: تم البيع\n") else "الحالة: تم البيع\n" + it.details) else it }; Toast.makeText(context, "تم تحديث الحالة محلياً", Toast.LENGTH_SHORT).show() }, modifier = Modifier.padding(padding))
                reportItem != null -> ReportDialog(reportItem!!, { reportItem = null }, { reason ->
                    val prefs = context.getSharedPreferences("dalil_native_preferences", Context.MODE_PRIVATE)
                    val old = prefs.getString("local_reports", "").orEmpty()
                    prefs.edit().putString("local_reports", old + "\n" + reportItem!!.title + " — " + reason).apply()
                    Toast.makeText(context, "تم حفظ البلاغ على هذا الجهاز للتجربة فقط", Toast.LENGTH_LONG).show()
                    reportItem = null
                })
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
                tab == 0 -> HomePage(query, { query = it; category = null }, filters, { filters = !filters }, area, { area = it }, minPrice, { minPrice = it }, maxPrice, { maxPrice = it }, filterCurrency, { filterCurrency = it }, visible, saved, { category = it }, { detail = it }, { title -> saved = if (saved.contains(title)) saved - title else saved + title }, Modifier.padding(padding))
                tab == 1 -> ShopsPage({ tab = 0; category = it }, Modifier.padding(padding))
                tab == 2 -> SavedPage(allItems.filter { saved.contains(it.title) }, { detail = it }, { title -> saved = saved - title }, Modifier.padding(padding))
                else -> AccountPage(onCreate = { showCreate = true }, onMyItems = { showMyItems = true }, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomePage(
    query: String, onQuery: (String) -> Unit, filters: Boolean, onToggleFilters: () -> Unit,
    area: String, onArea: (String) -> Unit, minPrice: String, onMinPrice: (String) -> Unit, maxPrice: String, onMaxPrice: (String) -> Unit, filterCurrency: String, onFilterCurrency: (String) -> Unit, items: List<DemoItem>, saved: Set<String>,
    onCategory: (String) -> Unit, onDetail: (DemoItem) -> Unit, onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCategories = categories.filter { query.isBlank() || it.contains(query, ignoreCase = true) }
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(13.dp), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DarkGreen)) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("كل منبج بمكان واحد", color = Gold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text("أهلاً بك في دليل منبج", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(7.dp))
                    Text("اكتشف المحلات والخدمات والإعلانات المحلية", color = Color.White)
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
private fun ShopsPage(onCategory: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item { Heading("المحلات", "تصفح المحلات حسب النشاط") }
        items(categories) { name ->
            ElevatedCard(onClick = { onCategory(name) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, null, tint = Green)
                    Spacer(Modifier.width(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
        item { Text("قائمة المحلات الحقيقية ستظهر بعد ربط البيانات.", color = Muted) }
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
private fun AccountPage(onCreate: () -> Unit, onMyItems: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountCircle, null, tint = Green, modifier = Modifier.size(60.dp))
                    Text("حسابي", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                    Text("تسجيل الدخول وإدارة الإعلانات قيد التطوير.", color = Muted)
                }
            }
        }
        item { ElevatedCard(onClick = onMyItems, modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Campaign, null, tint = Green); Spacer(Modifier.width(12.dp)); Column { Text("إعلاناتي التجريبية", fontWeight = FontWeight.Bold); Text("عرض وحذف وتغيير حالة إعلانات هذا الجهاز", color = Muted, style = MaterialTheme.typography.bodySmall) } } } }
        item { ElevatedCard(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AddCircle, null, tint = Green); Spacer(Modifier.width(12.dp)); Column { Text("إضافة إعلان تجريبي", fontWeight = FontWeight.Bold); Text("حفظ محلي على هذا الجهاز فقط", color = Muted, style = MaterialTheme.typography.bodySmall) } } } }
        item { AccountRow(Icons.Default.Settings, "الإعدادات", "إعدادات الحساب قيد التطوير") }
        item { Text("لا يوجد اتصال بالخادم في هذه النسخة التجريبية.", color = Muted, style = MaterialTheme.typography.bodySmall) }
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
            Text(item.details, color = Muted, style = MaterialTheme.typography.bodyMedium)
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
                Icon(Icons.Default.Storefront, null, tint = Green, modifier = Modifier.size(52.dp))
                Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("${item.category} • ${item.area}", color = Muted)
                if (item.price.isNotBlank()) Text("السعر: ${item.price} ${item.currency}", color = Green, fontWeight = FontWeight.Bold)
                if (item.contact.isNotBlank()) Text("للتواصل: ${item.contact}", color = Muted)
                Spacer(Modifier.height(12.dp))
                Text(item.details)
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
    var details by remember(initialItem) { mutableStateOf(initialItem?.details?.removePrefix("الحالة: تم البيع\\n").orEmpty()) }
    var price by remember(initialItem) { mutableStateOf(initialItem?.price.orEmpty()) }
    var currency by remember(initialItem) { mutableStateOf(initialItem?.currency ?: "ل.س") }
    var contact by remember(initialItem) { mutableStateOf(initialItem?.contact.orEmpty()) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading(if (initialItem == null) "إضافة إعلان تجريبي" else "تعديل إعلان تجريبي", "لن يُنشر على الموقع ولن يراه مستخدمون آخرون") }
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
        item { OutlinedTextField(value = details, onValueChange = { details = it; error = null }, label = { Text("تفاصيل الإعلان") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        if (error != null) item { Text(error!!, color = Color(0xFFB3261E)) }
        item { Button(onClick = { if (title.trim().length < 3) error = "اكتب عنواناً من 3 أحرف على الأقل" else if (details.trim().length < 5) error = "أضف تفاصيل أوضح للإعلان" else onCreate(DemoItem(title.trim(), category, area, details.trim(), price.trim().replace(',', '.'), currency, contact.trim())) }, modifier = Modifier.fillMaxWidth()) { Text(if (initialItem == null) "حفظ الإعلان التجريبي" else "حفظ التعديلات") } }
        item { OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("إلغاء") } }
    }
}

private fun loadUserItems(prefs: android.content.SharedPreferences): List<DemoItem> =
    prefs.getString("user_items_v1", "").orEmpty().lineSequence().filter { it.isNotBlank() }.mapNotNull { row ->
        runCatching {
            val p = row.split(".")
            if (p.size != 4 && p.size != 7) null else {
                fun decode(index: Int) = String(Base64.decode(p[index], Base64.NO_WRAP), Charsets.UTF_8)
                DemoItem(decode(0), decode(1), decode(2), decode(3),
                    if (p.size == 7) decode(4) else "",
                    if (p.size == 7) decode(5) else "ل.س",
                    if (p.size == 7) decode(6) else "")
            }
        }.getOrNull()
    }.toList()

private fun saveUserItems(prefs: android.content.SharedPreferences, items: List<DemoItem>) {
    val encoded = items.joinToString("\n") { item ->
        listOf(item.title, item.category, item.area, item.details, item.price, item.currency, item.contact)
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