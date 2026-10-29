package com.dalilmanbij.app
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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

private val Green=Color(0xFF176B50)
private val DarkGreen=Color(0xFF123D30)
private val Gold=Color(0xFFF4B45F)
private val Muted=Color(0xFF68776E)
private val Page=Color(0xFFF5F8F5)
private val cats=listOf("مطاعم ومقاهي","بقاليات","ملابس","موبايلات وإلكترونيات","خدمات","عقارات","سيارات","أثاث منزلي","مهن وحرف","مواد بناء")
private val areas=listOf("كل المناطق","منبج","ريف منبج","أبو قلقل","الخفسة","مسكنة")
private enum class Screen { HOME, ADS, SHOPS, REQUESTS, SERVICES, DISCOUNTS, SAVED, ACCOUNT, ADD, MY_ADS, PROMOTE, NOTICES, ADMIN, ABOUT, PRIVACY, TERMS }
private data class Ad(val id:Int,val title:String,val cat:String,val area:String,val price:String,val currency:String,val details:String,val kind:String="إعلان",val owner:String="",val featured:Boolean=false)
private val demo=listOf(
 Ad(1,"مثال: سيارة للبيع","سيارات","منبج","8500","$","بيانات توضيحية وليست إعلاناً منشوراً.",featured=true),
 Ad(2,"مثال: شقة للإيجار","عقارات","ريف منبج","120","$","نموذج إعلان عقاري تجريبي."),
 Ad(3,"مثال: صيانة موبايلات","موبايلات وإلكترونيات","منبج","0","ل.س","خدمة توضيحية.","خدمة"),
 Ad(4,"مثال: أثاث منزلي","أثاث منزلي","منبج","500000","ل.س","نموذج بطاقة إعلان."),
 Ad(5,"مثال: خصم على الألبسة","ملابس","منبج","20","%","عرض تجريبي فقط.","خصم"),
 Ad(6,"مثال: طلب شراء مولدة","خدمات","ريف منبج","300","$","طلب تجريبي.","طلب")
)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{NativePreview()}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun NativePreview(){
 val context=LocalContext.current
 val prefs=remember(context){context.getSharedPreferences("dalil_native_preferences",Context.MODE_PRIVATE)}
 var page by remember{mutableStateOf(Screen.HOME)}
 var ads by remember{mutableStateOf(demo)}
 var saved by remember{mutableStateOf(prefs.getStringSet("saved_ids",emptySet())?.toSet()?:emptySet())}
 var chosen by remember{mutableStateOf<Ad?>(null)}
 var query by remember{mutableStateOf("")}
 var area by remember{mutableStateOf("كل المناطق")}
 var category by remember{mutableStateOf("الكل")}
 var min by remember{mutableStateOf("")};var max by remember{mutableStateOf("")}
 var currency by remember{mutableStateOf("الكل")};var filters by remember{mutableStateOf(false)}
 var title by remember{mutableStateOf("")};var details by remember{mutableStateOf("")};var price by remember{mutableStateOf("")}
 var adArea by remember{mutableStateOf("منبج")};var adCat by remember{mutableStateOf(cats.first())};var adKind by remember{mutableStateOf("إعلان")};var adCurrency by remember{mutableStateOf("$")}
 var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var report by remember{mutableStateOf(false)}
 LaunchedEffect(saved){prefs.edit().putStringSet("saved_ids",saved.toSet()).apply()}
 BackHandler(chosen!=null||page!=Screen.HOME){if(chosen!=null)chosen=null else page=Screen.HOME}
 val filtered=ads.filter{a->
  (query.isBlank()||a.title.contains(query,true)||a.cat.contains(query,true)||a.area.contains(query,true))&&
  (category=="الكل"||a.cat==category)&&(area=="كل المناطق"||a.area==area)&&(currency=="الكل"||a.currency==currency)&&
  (min.isBlank()||(a.price.toDoubleOrNull()?.let{it>=(min.toDoubleOrNull()?:0.0)}!=false))&&
  (max.isBlank()||(a.price.toDoubleOrNull()?.let{it<=(max.toDoubleOrNull()?:Double.MAX_VALUE)}!=false))
 }
 MaterialTheme(colorScheme=lightColorScheme(primary=Green,secondary=Gold,background=Page,surface=Color.White)){
  Scaffold(containerColor=Page,topBar={CenterAlignedTopAppBar(
   navigationIcon={if(page!=Screen.HOME||chosen!=null)IconButton(onClick={if(chosen!=null)chosen=null else page=Screen.HOME}){Icon(Icons.Default.ArrowBack,"رجوع")}},
   title={Column(horizontalAlignment=Alignment.CenterHorizontally){Text("دليل منبج",fontWeight=FontWeight.ExtraBold,fontSize=21.sp);Text("سوقك بجيبك",fontSize=12.sp,color=Muted)}},
   actions={IconButton(onClick={page=Screen.NOTICES}){Icon(Icons.Default.Notifications,"الإشعارات")}},
   colors=TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor=Color.White)
  )},bottomBar={NavigationBar(containerColor=Color.White){
   NavigationBarItem(page==Screen.HOME,{page=Screen.HOME;chosen=null},{Icon(Icons.Default.Home,null)},{Text("الرئيسية")})
   NavigationBarItem(page==Screen.ADS||page==Screen.SHOPS,{page=Screen.ADS;chosen=null},{Icon(Icons.Default.Search,null)},{Text("تصفح")})
   NavigationBarItem(page==Screen.ADD,{page=Screen.ADD;chosen=null},{Icon(Icons.Default.AddCircle,null)},{Text("أضف")})
   NavigationBarItem(page==Screen.SAVED,{page=Screen.SAVED;chosen=null},{Icon(Icons.Default.FavoriteBorder,null)},{Text("المحفوظات")})
   NavigationBarItem(page==Screen.ACCOUNT,{page=Screen.ACCOUNT;chosen=null},{Icon(Icons.Default.Person,null)},{Text("حسابي")})
  }}){pad->
   Column(Modifier.fillMaxSize().padding(pad)){
    if(message.isNotBlank())Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){Text(message,Modifier.weight(1f),color=Green);TextButton(onClick={message=""}){Text("إغلاق")}}
    if(chosen!=null)LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
     item{Header(chosen!!.title,chosen!!.cat+" • "+chosen!!.area)}
     item{Card{Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      Box(Modifier.fillMaxWidth().height(130.dp),contentAlignment=Alignment.Center){Icon(Icons.Default.Image,null,tint=Green,modifier=Modifier.size(50.dp))}
      Text("السعر: "+chosen!!.price+" "+chosen!!.currency,fontSize=20.sp,fontWeight=FontWeight.Bold,color=Green)
      Text(chosen!!.details);Text("المنطقة: "+chosen!!.area);Text("النوع: "+chosen!!.kind)
      Button(onClick={val id=chosen!!.id.toString();saved=if(saved.contains(id))saved-id else saved+id},modifier=Modifier.fillMaxWidth()){Text(if(saved.contains(chosen!!.id.toString()))"إزالة من المحفوظات" else "حفظ الإعلان")}
      OutlinedButton(onClick={report=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Flag,null);Text("الإبلاغ عن إعلان")}
      OutlinedButton(onClick={page=Screen.PROMOTE},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Campaign,null);Text("ترويج (معاينة)")}
      Text("الصور والتواصل الحقيقي غير مفعّلين في هذه النسخة.",color=Muted,style=MaterialTheme.typography.bodySmall)
     }}}
    }else when(page){
     Screen.HOME->LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
      item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=DarkGreen)){Column(Modifier.fillMaxWidth().padding(22.dp)){Text("سوقك بجيبك",color=Gold,fontWeight=FontWeight.Bold);Text("كل منبج بمكان واحد",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold);Text("بيع واشتري، أعلن لمحلك، واكتشف المهن والخدمات.",color=Color.White);Text("نسخة اختبار مستقلة • بيانات تجريبية",color=Color.LightGray)}}}
      item{OutlinedTextField(query,{query=it;page=Screen.ADS},Modifier.fillMaxWidth(),singleLine=true,label={Text("ابحث عن إعلان أو محل أو خدمة")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={IconButton(onClick={filters=!filters}){Icon(Icons.Default.Tune,null)}})}
      if(filters)item{Column{Text("المنطقة");Row{listOf("كل المناطق","منبج","ريف منبج").forEach{FilterChip(area==it,{area=it},{Text(it)})}}}}
      item{Header("الأقسام","اختَر القسم المناسب")}
      items(cats.chunked(2)){pair->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){pair.forEach{c->ElevatedCard(onClick={category=c;page=Screen.ADS},modifier=Modifier.weight(1f)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Category,null,tint=Green);Spacer(Modifier.width(7.dp));Text(c,style=MaterialTheme.typography.bodySmall,fontWeight=FontWeight.Bold)}}};if(pair.size==1)Spacer(Modifier.weight(1f))}}
      item{Header("كل أقسام الدليل","واجهات مستقلة للمعاينة")}
      item{Quick("المحلات والمتاجر",Icons.Default.Store){page=Screen.SHOPS};Quick("طلبات الشراء",Icons.Default.ShoppingCart){page=Screen.REQUESTS};Quick("الخدمات والمهن",Icons.Default.Build){page=Screen.SERVICES};Quick("الخصومات والعروض",Icons.Default.LocalOffer){page=Screen.DISCOUNTS};Quick("الترويج والباقات",Icons.Default.Campaign){page=Screen.PROMOTE}}
      item{Header("إعلانات مميزة","أمثلة توضيحية وليست منشورة")}
      items(filtered.filter{it.featured}.ifEmpty{filtered}.take(3)){AdCard(it,saved.contains(it.id.toString()),{chosen=it},{id->saved=if(saved.contains(id.toString()))saved-id.toString()else saved+id.toString()})}
      item{Text("لا يوجد اتصال بالموقع أو Supabase. التغييرات محلية فقط.",color=Muted,style=MaterialTheme.typography.bodySmall)}
     }
     Screen.ADS,Screen.REQUESTS,Screen.SERVICES,Screen.DISCOUNTS,Screen.SAVED,Screen.MY_ADS->{
      val list=when(page){Screen.REQUESTS->filtered.filter{it.kind=="طلب"};Screen.SERVICES->filtered.filter{it.kind=="خدمة"||it.cat=="خدمات"};Screen.DISCOUNTS->filtered.filter{it.kind=="خصم"};Screen.SAVED->ads.filter{saved.contains(it.id.toString())};Screen.MY_ADS->ads.filter{it.owner=="mine"};else->filtered}
      LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
       item{Header(when(page){Screen.REQUESTS->"طلبات الشراء";Screen.SERVICES->"الخدمات والمهن";Screen.DISCOUNTS->"الخصومات والعروض";Screen.SAVED->"المحفوظات";Screen.MY_ADS->"إعلاناتي";else->"الإعلانات"},"وضع اختبار محلي")}
       if(page==Screen.ADS)item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("بحث")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={IconButton(onClick={filters=!filters}){Icon(Icons.Default.Tune,null)}})}
       if(page==Screen.ADS&&filters)item{Column{Text("المنطقة");Row{listOf("كل المناطق","منبج","ريف منبج").forEach{FilterChip(area==it,{area=it},{Text(it)})}};Text("القسم");Row{(listOf("الكل")+cats.take(3)).forEach{FilterChip(category==it,{category=it},{Text(it)})}};Row{OutlinedTextField(min,{min=it},Modifier.weight(1f),label={Text("السعر من")});Spacer(Modifier.width(6.dp));OutlinedTextField(max,{max=it},Modifier.weight(1f),label={Text("السعر إلى")})};Row{listOf("الكل","$","ل.س").forEach{FilterChip(currency==it,{currency=it},{Text(it)})}}}}
       if(list.isEmpty())item{Text("لا توجد عناصر ضمن البيانات التجريبية.",color=Muted)}
       items(list){AdCard(it,saved.contains(it.id.toString()),{chosen=it},{id->saved=if(saved.contains(id.toString()))saved-id.toString()else saved+id.toString()})}
       item{Text("هذه بيانات محلية فقط وليست بيانات الموقع.",color=Muted,style=MaterialTheme.typography.bodySmall)}
      }
     }
     Screen.SHOPS->SimpleList("المحلات والمتاجر","تصنيفات الدليل؛ لا توجد قائمة محلات حقيقية بعد.",cats,{category=it;page=Screen.ADS})
     Screen.ADD->LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      item{Header("إضافة إعلان أو طلب","حفظ محلي فقط؛ لا يوجد نشر")}
      item{OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("العنوان *")})}
      item{OutlinedTextField(details,{details=it},Modifier.fillMaxWidth(),label={Text("الوصف *")},minLines=3)}
      item{OutlinedTextField(price,{price=it},Modifier.fillMaxWidth(),label={Text("السعر")})}
      item{Text("العملة")};item{Row{listOf("$","ل.س").forEach{FilterChip(adCurrency==it,{adCurrency=it},{Text(it)})}}}
      item{Text("نوع المحتوى")};item{Row{listOf("إعلان","طلب","خدمة","خصم").forEach{FilterChip(adKind==it,{adKind=it},{Text(it)})}}}
      item{Text("القسم")};item{Column{cats.forEach{FilterChip(adCat==it,{adCat=it},{Text(it)})}}}
      item{Text("المنطقة")};item{Column{regions.forEach{FilterChip(adArea==it,{adArea=it},{Text(it)})}}}
      item{OutlinedButton(onClick={},enabled=false,modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.AddPhotoAlternate,null);Text("إضافة صور - لاحقاً")}}
      item{Button(onClick={if(title.isBlank()||details.isBlank())message="اكتب العنوان والوصف أولاً." else{ads=listOf(Ad((ads.maxOfOrNull{it.id}?:0)+1,title,adCat,adArea,price.ifBlank{"0"},adCurrency,details,adKind,"mine"))+ads;title="";details="";price="";message="حُفظ محلياً فقط ولم يُنشر على الموقع.";page=Screen.MY_ADS}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Save,null);Text("حفظ للتجربة")}}
     }
     Screen.ACCOUNT->LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      item{Header("حسابي","معاينة فقط؛ لا يتم إرسال بيانات الدخول")}
      item{OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("البريد أو اسم المستخدم")})}
      item{OutlinedTextField(password,{password=it},Modifier.fillMaxWidth(),label={Text("كلمة المرور")})}
      item{Button(onClick={message="تسجيل الدخول غير مفعّل ولن تُرسل بياناتك لأي جهة."},modifier=Modifier.fillMaxWidth()){Text("تسجيل الدخول (معاينة)")}}
      item{Quick("إعلاناتي",Icons.Default.Campaign){page=Screen.MY_ADS};Quick("إدارة المحل",Icons.Default.Store){message="إدارة المحل تحتاج خادماً آمناً."};Quick("طلب توثيق محل",Icons.Default.Verified){message="التوثيق غير متصل بالخادم."};Quick("الإشعارات",Icons.Default.Notifications){page=Screen.NOTICES};Quick("الإدارة (معاينة)",Icons.Default.Settings){page=Screen.ADMIN};Quick("عن دليل منبج",Icons.Default.Info){page=Screen.ABOUT};Quick("الخصوصية",Icons.Default.Description){page=Screen.PRIVACY};Quick("الشروط والأحكام",Icons.Default.Description){page=Screen.TERMS}}
     }
     Screen.PROMOTE->SimpleList("الترويج والباقات","معاينة فقط؛ لا يوجد دفع أو تفعيل.",listOf("تمييز الإعلان 3 أيام","تمييز الإعلان 7 أيام","تمييز الإعلان 30 يوماً","ترويج محل أو خصم"),{message="اخترت "+it+"؛ لم يتم دفع أو تفعيل شيء."})
     Screen.NOTICES->SimpleList("الإشعارات","لن تصل إشعارات فعلية قبل ربط خدمة آمنة.",listOf("تحديثات إعلاناتك","حالة البلاغات","انتهاء الترويج"),{message="هذه إشعارات تجريبية فقط."})
     Screen.ADMIN->SimpleList("الإدارة والمراجعة","معاينة فقط؛ لا توجد صلاحيات إدارية أو عمليات خادمية.",listOf("مراجعة الإعلانات","البلاغات","المستخدمون والحظر","الإحصاءات","سجل الإجراءات"),{message="الإدارة غير مفعّلة."})
     Screen.ABOUT->SimpleList("عن دليل منبج","كل منبج وريفها بين إيديك.",listOf("سوقك بجيبك","بيع • شراء • أعلن • اكتشف"),{message="نسخة Native مستقلة."})
     Screen.PRIVACY->SimpleList("الخصوصية","معاينة أولية وليست نصاً قانونياً نهائياً.",listOf("البيانات","الأذونات","التواصل والدعم"),{})
     Screen.TERMS->SimpleList("الشروط والأحكام","معاينة أولية تحتاج مراجعة قبل الإصدار.",listOf("مسؤولية المستخدم","المحتوى والإعلانات","الإبلاغ عن المخالفات"),{})
    }
   }
  }
 }
 if(report)AlertDialog(onDismissRequest={report=false},title={Text("الإبلاغ عن إعلان")},text={Text("واجهة بلاغ تجريبية فقط. لن يُرسل البلاغ إلى الموقع.")},confirmButton={TextButton(onClick={report=false;message="البلاغ غير متصل بالخادم."}){Text("حسناً")}})
}
@Composable private fun SimpleList(title:String,subtitle:String,items:List<String>,onClick:(String)->Unit){LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header(title,subtitle)};items(items){Quick(it,Icons.Default.ChevronRight){onClick(it)}}}}
@Composable private fun AdCard(ad:Ad,saved:Boolean,onOpen:()->Unit,onSave:(Int)->Unit){ElevatedCard(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(17.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Category,null,tint=Green,modifier=Modifier.size(32.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(ad.title,fontWeight=FontWeight.Bold);Text(ad.cat+" • "+ad.area,color=Muted,style=MaterialTheme.typography.bodySmall)};IconButton(onClick={onSave(ad.id)}){Icon(if(saved)Icons.Default.Favorite else Icons.Default.FavoriteBorder,null,tint=if(saved)Color(0xFFB3261E)else Muted)}};Text(ad.price+" "+ad.currency,color=Green,fontWeight=FontWeight.ExtraBold);Text(ad.details,color=Muted,style=MaterialTheme.typography.bodySmall,maxLines=2);Row(verticalAlignment=Alignment.CenterVertically){Text("بيانات تجريبية",Modifier.weight(1f),color=Color(0xFF9B5A00),style=MaterialTheme.typography.labelSmall);TextButton(onClick=onOpen){Text("التفاصيل")}}}}}
@Composable private fun Quick(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Green);Spacer(Modifier.width(10.dp));Text(title,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Icon(Icons.Default.ChevronRight,null,tint=Muted)}}}
@Composable private fun Header(title:String,subtitle:String){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text(title,fontSize=21.sp,fontWeight=FontWeight.ExtraBold,color=DarkGreen);Text(subtitle,color=Muted,style=MaterialTheme.typography.bodySmall)}}
