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
private val examples = listOf("مثال: محل ألبسة في منبج", "مثال: صيانة موبايلات", "مثال: أثاث منزلي للبيع")

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
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Gold, background = Color(0xFFF7F8F5))) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("دليل منبج", fontWeight = FontWeight.Bold)
                        Text("سوقك بجيبك", fontSize = 12.sp)
                    }
                })
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("الرئيسية") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Store, contentDescription = null) }, label = { Text("المحلات") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) }, label = { Text("المحفوظات") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.Person, contentDescription = null) }, label = { Text("حسابي") })
                }
            }
        ) { padding ->
            if (tab == 0) {
                LazyColumn(
                    Modifier.padding(padding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Green)) {
                            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                                Text("أهلاً بك في دليل منبج", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text("اكتشف المحلات والخدمات والإعلانات المحلية", color = Color.White)
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("ابحث عن محل أو إعلان...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                    }
                    item { Text("الأقسام", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    items(categories.filter { query.isBlank() || it.contains(query, ignoreCase = true) }) { category ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = Green)
                                Spacer(Modifier.width(12.dp))
                                Text(category, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                    item { Text("نماذج توضيحية فقط", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    items(examples) { example ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(example, fontWeight = FontWeight.Bold)
                                Text("بطاقة تجريبية — ليست إعلاناً منشوراً", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item { Text("نسخة تأسيسية: غير متصلة بالموقع أو قاعدة البيانات الحالية.", style = MaterialTheme.typography.bodySmall) }
                }
            } else {
                Column(
                    Modifier.padding(padding).fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(listOf("", "المحلات", "المحفوظات", "حسابي")[tab], fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(when (tab) {
                        1 -> "سيتم بناء قائمة المحلات وربطها لاحقاً."
                        2 -> "سيتم بناء ميزة حفظ الإعلانات لاحقاً."
                        else -> "سيتم إضافة تسجيل الدخول وإدارة الإعلانات لاحقاً."
                    })
                }
            }
        }
    }
}
