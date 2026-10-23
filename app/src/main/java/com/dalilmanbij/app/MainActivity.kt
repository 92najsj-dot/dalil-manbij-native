package com.dalilmanbij.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private val Green = Color(0xFF124734)
private val Gold = Color(0xFFD7B45A)
private val categories = listOf("مطاعم ومقاهي", "بقاليات", "ملابس", "موبايلات وإلكترونيات", "خدمات", "عقارات")
private val examples = listOf(
    "مثال: محل ألبسة في منبج" to "الأسعار والتفاصيل ستظهر عند ربط قاعدة البيانات",
    "مثال: صيانة موبايلات" to "نموذج تجريبي فقط",
    "مثال: أثاث منزلي للبيع" to "نموذج تجريبي فقط"
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
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Gold, background = Color(0xFFF7F8F5))) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (selectedCategory != null) {
                            IconButton(onClick = { selectedCategory = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                            }
                        }
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("دليل منبج", fontWeight = FontWeight.Bold)
                            Text("سوقك بجيبك", fontSize = 12.sp)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0; selectedCategory = null }, icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("الرئيسية") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1; selectedCategory = null }, icon = { Icon(Icons.Default.Store, contentDescription = null) }, label = { Text("المحلات") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2; selectedCategory = null }, icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) }, label = { Text("المحفوظات") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3; selectedCategory = null }, icon = { Icon(Icons.Default.Person, contentDescription = null) }, label = { Text("حسابي") })
                }
            }
        ) { padding ->
            when {
                tab == 0 && selectedCategory != null -> CategoryScreen(
                    category = selectedCategory!!,
                    modifier = Modifier.padding(padding)
                )
                tab == 0 -> HomeScreen(
                    query = query,
                    onQueryChange = { query = it },
                    onCategoryClick = { selectedCategory = it },
                    modifier = Modifier.padding(padding)
                )
                else -> OtherTabScreen(tab = tab, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCategories = categories.filter { query.isBlank() || it.contains(query, ignoreCase = true) }
    val visibleExamples = examples.filter { query.isBlank() || it.first.contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Green)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("أهلاً بك في دليل منبج", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("اكتشف المحلات والخدمات والإعلانات المحلية", color = Color.White)
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("ابحث عن محل أو إعلان...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                    }
                }
            )
        }
        item { Text("الأقسام", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        if (visibleCategories.isEmpty()) {
            item { Text("ما لقينا قسم مطابق لبحثك.") }
        }
        items(visibleCategories) { category ->
            ElevatedCard(
                onClick = { onCategoryClick(category) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = Green)
                    Spacer(Modifier.width(12.dp))
                    Text(category, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "فتح القسم")
                }
            }
        }
        item { Text("نماذج توضيحية فقط", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(visibleExamples) { example ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(example.first, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(example.second, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    AssistChip(onClick = {}, label = { Text("بطاقة تجريبية") })
                }
            }
        }
        item {
            Text(
                "هذه نسخة أولية غير متصلة بالموقع أو قاعدة البيانات. المحتوى المعروض تجريبي.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CategoryScreen(category: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Storefront, contentDescription = null, tint = Green, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(category, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("فتح القسم يعمل الآن. ستظهر المحلات والإعلانات الحقيقية بعد ربط قاعدة البيانات في مرحلة لاحقة.")
        Spacer(Modifier.height(16.dp))
        Text("لا توجد بيانات منشورة في هذه النسخة التجريبية.", color = Color.Gray)
    }
}

@Composable
private fun OtherTabScreen(tab: Int, modifier: Modifier = Modifier) {
    val title = listOf("", "المحلات", "المحفوظات", "حسابي")[tab]
    val message = when (tab) {
        1 -> "قسم المحلات يعمل، وسيُعرض المحتوى بعد إضافة ربط البيانات."
        2 -> "المحفوظات واجهتها جاهزة مبدئياً؛ حفظ الإعلانات يحتاج تنفيذاً لاحقاً."
        else -> "صفحة الحساب جاهزة مبدئياً؛ تسجيل الدخول وإدارة الإعلانات قيد التطوير."
    }
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            when (tab) {
                1 -> Icons.Default.Store
                2 -> Icons.Default.FavoriteBorder
                else -> Icons.Default.Person
            },
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(message)
    }
}
