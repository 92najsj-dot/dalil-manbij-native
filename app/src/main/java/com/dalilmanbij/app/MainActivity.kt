package com.dalilmanbij.app

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF176B50)
private val DarkGreen = Color(0xFF123D30)
private val Gold = Color(0xFFF4B45F)
private val Muted = Color(0xFF68776E)
private val Mint = Color(0xFFE8F5ED)
private val categories = listOf("مطاعم ومقاهي", "بقاليات", "ملابس", "موبايلات وإلكترونيات", "خدمات", "عقارات", "سيارات", "أثاث منزلي")

private data class DemoItem(val title: String, val category: String, val area: String, val details: String)
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
    var filters by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<DemoItem?>(null) }
    var saved by remember { mutableStateOf(setOf<String>()) }

    val visible = demoItems.filter {
        (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.area.contains(query, true)) &&
        (category == null || it.category == category) &&
        (area == "كل المناطق" || it.area == area)
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Gold, background = Color(0xFFF5F8F5))) {
        Scaffold(
            containerColor = Color(0xFFF5F8F5),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (detail != null || category != null) IconButton(onClick = { if (detail != null) detail = null else category = null }) {
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
                detail != null -> DetailPage(detail!!, saved.contains(detail!!.title), { saved = if (saved.contains(detail!!.title)) saved - detail!!.title else saved + detail!!.title }, Modifier.padding(padding))
                tab == 0 && category != null -> ResultsPage(category!!, visible, saved, { detail = it }, { title -> saved = if (saved.contains(title)) saved - title else saved + title }, Modifier.padding(padding))
                tab == 0 -> HomePage(query, { query = it; category = null }, filters, { filters = !filters }, area, { area = it }, visible, saved, { category = it }, { detail = it }, { title -> saved = if (saved.contains(title)) saved - title else saved + title }, Modifier.padding(padding))
                tab == 1 -> ShopsPage({ tab = 0; category = it }, Modifier.padding(padding))
                tab == 2 -> SavedPage(demoItems.filter { saved.contains(it.title) }, { detail = it }, { title -> saved = saved - title }, Modifier.padding(padding))
                else -> AccountPage(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomePage(
    query: String, onQuery: (String) -> Unit, filters: Boolean, onToggleFilters: () -> Unit,
    area: String, onArea: (String) -> Unit, items: List<DemoItem>, saved: Set<String>,
    onCategory: (String) -> Unit, onDetail: (DemoItem) -> Unit, onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                }
            }
        }
        item { Heading("الأقسام", "اختَر القسم المناسب") }
        items(categories) { name ->
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
private fun AccountPage(modifier: Modifier = Modifier) {
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
        item { AccountRow(Icons.Default.Campaign, "إعلاناتي", "ستُفعّل بعد بناء تسجيل الدخول") }
        item { AccountRow(Icons.Default.AddCircle, "إضافة إعلان أو طلب", "سيتم تنفيذ النموذج وربطه لاحقاً") }
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
private fun DetailPage(item: DemoItem, isSaved: Boolean, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Heading("تفاصيل العنصر", "معاينة تجريبية")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Icon(Icons.Default.Storefront, null, tint = Green, modifier = Modifier.size(52.dp))
                Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("${item.category} • ${item.area}", color = Muted)
                Spacer(Modifier.height(12.dp))
                Text(item.details)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSaved) "إزالة من المحفوظات" else "حفظ في المحفوظات")
                }
                Text("لا توجد بيانات حقيقية متصلة في هذه النسخة.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
