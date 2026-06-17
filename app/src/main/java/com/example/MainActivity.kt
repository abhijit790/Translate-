package com.example

import android.app.Application
import kotlinx.coroutines.delay
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.widget.Toast
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguagePack
import com.example.data.model.TranslationHistory
import com.example.data.repository.TranslationRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup App database and repository instantiation manually
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TranslationRepository(
            historyDao = database.translationHistoryDao(),
            dictionaryDao = database.dictionaryWordDao(),
            customDao = database.customTranslationDao(),
            packDao = database.languagePackDao()
        )

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TranslationViewModel(application, repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val viewModel: TranslationViewModel = ViewModelProvider(this, factory)[TranslationViewModel::class.java]
                val isSplashLoaded by viewModel.isSplashLoaded.collectAsStateWithLifecycle()

                if (!isSplashLoaded) {
                    SplashLoaderScreen()
                } else {
                    MainAppScaffold(viewModel)
                }
            }
        }
    }
}

// ==================== SPLASH SCREEN / LOADING ANIMATION ====================
@Composable
fun SplashLoaderScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Rotating globe degree
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteSpec(9000),
        label = "rotation"
    )

    // Pulse size factor
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Sequential greetings text loop index
    var greetingIndex by remember { mutableIntStateOf(0) }
    val greetings = listOf(
        "Hello" to "en",
        "নমস্কার" to "bn",
        "नमस्ते" to "hi",
        "Hola" to "es"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(750)
            greetingIndex = (greetingIndex + 1) % greetings.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulse & Rotate Centered Globe
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background radial glassmorphism circles
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )

                // Globe image overlay
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Globe Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .rotate(rotationAngle)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic looping translation greeting headings
            AnimatedContent(
                targetState = greetings[greetingIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "greeting"
            ) { currentGreeting ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentGreeting.first,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (currentGreeting.second) {
                            "bn" -> "Bengali Offline Translation"
                            "hi" -> "Hindi Offline Translation"
                            "es" -> "Spanish Offline Translation"
                            else -> "English Core Engine"
                        },
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Linear Glowing Progress Slider
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                val loadingTransition = rememberInfiniteTransition(label = "loader")
                val slideOffset by loadingTransition.animateFloat(
                    initialValue = -1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "slide"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.4f)
                        .align(Alignment.CenterStart)
                        .graphicsLayer {
                            translationX = slideOffset * 180f
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00D2FF),
                                    Color(0xFF3A7BD5)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Initializing Offline Vocabularies...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun infiniteSpec(duration: Int): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(duration, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}

// ==================== APP LAYOUT & MAIN SCREEN SCALINGS ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScaffold(viewModel: TranslationViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val inAppNotification by viewModel.inAppNotification.collectAsStateWithLifecycle()
    
    // Track standard flags, triggers
    Scaffold(
        bottomBar = {
            CustomNavigationBar(activeTab) { tabIndex ->
                viewModel.setActiveTab(tabIndex)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Branding
                TranslationAppHeader()

                // Active Workspace Page Router
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        0 -> TranslateWorkspaceTab(viewModel)
                        1 -> OfflineDictionaryTab(viewModel)
                        2 -> HistoryBookmarksTab(viewModel)
                    }
                }
            }

            // In-App HUD Toast Layer (Custom sliding heads-up message container)
            AnimatedVisibility(
                visible = inAppNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                inAppNotification?.let { msg ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Notification Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// Header layout bar
@Composable
fun TranslationAppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini Logo Sphere in Gradient container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "Globe",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "LinguaGlobe",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Safe & Private Offline Translation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF44474E)
                    )
                }
            }

            // Elegant Blue Offline Badge Banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD3E3FD))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Offline indicators",
                        tint = Color(0xFF001D35),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Offline Mode",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001D35)
                    )
                }
            }
        }
    }
}

// Bottom Custom Navigation bar with active indication pill
@Composable
fun CustomNavigationBar(activeTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .drawBehind {
                drawLine(
                    color = Color(0xFFDEE2F1),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 0) Icons.Default.Refresh else Icons.Outlined.Build,
                    contentDescription = "Translate Workspace"
                )
            },
            label = { Text("Translate", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF001D35),
                selectedTextColor = Color(0xFF001D35),
                indicatorColor = Color(0xFFD3E3FD),
                unselectedIconColor = Color(0xFF44474E),
                unselectedTextColor = Color(0xFF44474E)
            )
        )
        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 1) Icons.Default.Search else Icons.Filled.List,
                    contentDescription = "Offline Explores"
                )
            },
            label = { Text("Dictionary", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF001D35),
                selectedTextColor = Color(0xFF001D35),
                indicatorColor = Color(0xFFD3E3FD),
                unselectedIconColor = Color(0xFF44474E),
                unselectedTextColor = Color(0xFF44474E)
            )
        )
        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 2) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Saved list log"
                )
            },
            label = { Text("Saved Log", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF001D35),
                selectedTextColor = Color(0xFF001D35),
                indicatorColor = Color(0xFFD3E3FD),
                unselectedIconColor = Color(0xFF44474E),
                unselectedTextColor = Color(0xFF44474E)
            )
        )
    }
}

// Flag language picker helpers
fun getLanguageNameByCode(code: String): String {
    return when (code) {
        "en" -> "English"
        "bn" -> "Bengali"
        "hi" -> "Hindi"
        "es" -> "Spanish"
        else -> "English"
    }
}

fun getLanguageIconEmoji(code: String): String {
    return when (code) {
        "en" -> "🇺🇸"
        "bn" -> "🇮🇳"
        "hi" -> "🇮🇳" // standard emoji markers
        "es" -> "🇪🇸"
        else -> "🇺🇸"
    }
}

// ==================== WORKSPACE 1: ACTIVE TRANSLATE PANEL ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslateWorkspaceTab(viewModel: TranslationViewModel) {
    val sourceText by viewModel.sourceText.collectAsStateWithLifecycle()
    val sourceLangCode by viewModel.sourceLangCode.collectAsStateWithLifecycle()
    val targetLangCode by viewModel.targetLangCode.collectAsStateWithLifecycle()
    val translationResult by viewModel.translationResult.collectAsStateWithLifecycle()
    val languagePacks by viewModel.languagePacksList.collectAsStateWithLifecycle()
    
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    var expandSourceSelector by remember { mutableStateOf(false) }
    var expandTargetSelector by remember { mutableStateOf(false) }
    
    val supportLanguages = listOf("en", "bn", "hi", "es")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Pack Quick Download Manager (Indicates statuses)
        item {
            LanguagePackManagerBanner(languagePacks) { packCode ->
                // User triggered download or removal
                val clickedPack = languagePacks.firstOrNull { it.languageCode == packCode }
                clickedPack?.let { p ->
                    if (p.isDownloaded) {
                        viewModel.removeLanguagePack(p)
                    } else if (!p.isDownloading) {
                        viewModel.downloadLanguagePack(p)
                    }
                }
            }
        }

        // B. Language Selecting Headers (Dropdowns + elegant swap button rotating on click)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Source Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { expandSourceSelector = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SOURCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getLanguageIconEmoji(sourceLangCode), fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = getLanguageNameByCode(sourceLangCode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1F)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Source",
                                    tint = Color(0xFF44474E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandSourceSelector,
                            onDismissRequest = { expandSourceSelector = false }
                        ) {
                            supportLanguages.forEach { code ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(getLanguageIconEmoji(code))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(getLanguageNameByCode(code))
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSourceLanguage(code)
                                        expandSourceSelector = false
                                    }
                                )
                            }
                        }
                    }

                    // Swap Action Button (with arrow / dynamic sync_alt-like display representation)
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.swapLanguages()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("swap_languages_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Swap Languages Icon",
                            tint = Color(0xFF44474E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Right Target Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { expandTargetSelector = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TARGET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getLanguageIconEmoji(targetLangCode), fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = getLanguageNameByCode(targetLangCode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1F)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Target",
                                    tint = Color(0xFF44474E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandTargetSelector,
                            onDismissRequest = { expandTargetSelector = false }
                        ) {
                            supportLanguages.forEach { code ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(getLanguageIconEmoji(code))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(getLanguageNameByCode(code))
                                        }
                                    },
                                    onClick = {
                                        viewModel.setTargetLanguage(code)
                                        expandTargetSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // C. Original Source Text Input Field Card
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSLATE FROM ${getLanguageNameByCode(sourceLangCode).uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )

                        if (sourceText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSourceTextChanged("") },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F9))
                                    .testTag("clear_input_button")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Input", tint = Color(0xFF44474E), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = sourceText,
                        onValueChange = { if (it.length <= 500) viewModel.onSourceTextChanged(it) },
                        placeholder = { Text("Tap to type text for offline Translation...", fontSize = 16.sp, color = Color(0xFF939094)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("translation_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 26.sp, color = Color(0xFF1B1B1F))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sourceText.length}/500",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF44474E).copy(alpha = 0.7f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Speaker / Hear TTS - styled as circular button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F9))
                                    .clickable(enabled = sourceText.isNotBlank()) {
                                        viewModel.speak(sourceText, sourceLangCode)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Speak Original text",
                                    tint = if (sourceText.isNotBlank()) Color(0xFF44474E) else Color(0xFF44474E).copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Interactive mockup "Translate" button
                            Button(
                                onClick = {
                                    // Provide feedback / click play
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (sourceText.isBlank()) {
                                        Toast.makeText(context, "Please enter some text first!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Translation synced offline!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF005CBB),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Translate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // D. Translated Result Card
        item {
            AnimatedVisibility(
                visible = translationResult != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                translationResult?.let { res ->
                    val isMissingPack = res.translatedText.startsWith("[Offline Pack Missing]")

                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMissingPack) {
                                Color(0xFFFADBD8).copy(alpha = 0.6f)
                            } else {
                                Color(0xFFE1E2EC)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isMissingPack) Color(0xFFEC7063).copy(alpha = 0.6f) else Color(0xFFDEE2F1),
                                shape = RoundedCornerShape(32.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TRANSLATION (${getLanguageNameByCode(targetLangCode).uppercase()})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF44474E),
                                        letterSpacing = 1.sp
                                    )
                                    if (res.usedCustomTerm) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Custom Definition", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = Color.White.copy(alpha = 0.6f),
                                                labelColor = Color(0xFF6750A4)
                                            ),
                                            border = AssistChipDefaults.assistChipBorder(borderColor = Color(0xFFDEE2F1), enabled = true)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isMissingPack) {
                                        // TTS Speaker playback Button - Styled as circular button
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.5f))
                                                .clickable { viewModel.speak(res.translatedText, targetLangCode) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Read translation text",
                                                tint = Color(0xFF1B1B1F),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Copy to clipboard Button - Styled circular
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.5f))
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(res.translatedText))
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.onCustomSourceWordChanged("") // trigger updates
                                                    viewModel.speak("Copied!", "en")
                                                    Toast.makeText(context, "Translation copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                }
                                                .testTag("copy_translation_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Copy Translation clipboard",
                                                tint = Color(0xFF44474E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = res.translatedText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMissingPack) Color(0xFFBA1A1A) else Color(0xFF1B1B1F),
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 28.sp
                            )

                            // Show Word-by-Word analyzer list if available!
                            if (res.wordBreakdown.isNotEmpty() && !isMissingPack) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color(0xFF1B1B1F).copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Offline Word-By-Word Breakdown",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    res.wordBreakdown.forEach { detail ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFDEE2F1))
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                                Text(
                                                    text = detail.originalWord,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6750A4),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = detail.translatedWord,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1B1B1F),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Show offline sub phrase recommendations if matches!
                            if (res.phraseSuggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color(0xFF1B1B1F).copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Matching Offline Phrases Search",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF005CBB),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    res.phraseSuggestions.forEach { ph ->
                                        // Pick source matching phrase text
                                        val textToShow = when (sourceLangCode) {
                                            "en" -> ph.englishText
                                            "bn" -> ph.bengaliText
                                            "hi" -> ph.hindiText
                                            "es" -> ph.spanishText
                                            else -> ph.englishText
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.onSourceTextChanged(textToShow) }
                                                .background(Color.White)
                                                .border(BorderStroke(1.dp, Color(0xFFDEE2F1)), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search mapping Icon",
                                                tint = Color(0xFF005CBB),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = textToShow,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B1B1F),
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simulated Downloader panel
@Composable
fun LanguagePackManagerBanner(languagePacks: List<LanguagePack>, onPackClicked: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Download center Icon",
                        tint = Color(0xFF005CBB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Language Models Packs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F)
                    )
                }

                Text(
                    text = "🛜 Offline Mode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                languagePacks.forEach { pack ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getLanguageIconEmoji(pack.languageCode), fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pack.languageName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1F)
                                )
                            }
                            Text(
                                text = if (pack.languageCode == "en") "Built-in Core Engine" else "File size: ${pack.sizeMB} MB",
                                fontSize = 11.sp,
                                color = Color(0xFF44474E)
                            )
                        }

                        // Downloading states controls
                        Box(contentAlignment = Alignment.Center) {
                            if (pack.languageCode == "en") {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Core", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Check, "core", tint = Color(0xFF001D35), modifier = Modifier.size(12.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFFD3E3FD),
                                        labelColor = Color(0xFF001D35)
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(borderColor = Color(0xFFDEE2F1), enabled = true)
                                )
                            } else if (pack.isDownloading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        progress = pack.progress,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF005CBB)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${(pack.progress * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005CBB)
                                    )
                                }
                            } else if (pack.isDownloaded) {
                                OutlinedButton(
                                    onClick = { onPackClicked(pack.languageCode) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                                    shape = RoundedCornerShape(100.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Uninstall", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onPackClicked(pack.languageCode) },
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF005CBB),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, "download", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper wrapper flow class
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}


// ==================== WORKSPACE 2: OFFLINE DICTIONARY EXPLORER ====================
@Composable
fun OfflineDictionaryTab(viewModel: TranslationViewModel) {
    val dictSearchQuery by viewModel.dictSearchQuery.collectAsStateWithLifecycle()
    val dictSelectedCategory by viewModel.dictSelectedCategory.collectAsStateWithLifecycle()
    val dictSearchResults by viewModel.dictSearchResults.collectAsStateWithLifecycle()
    val customTranslations by viewModel.customTranslationsList.collectAsStateWithLifecycle()
    
    val customSourceWord by viewModel.customSourceWord.collectAsStateWithLifecycle()
    val customTargetWord by viewModel.customTargetWord.collectAsStateWithLifecycle()

    val sourceLangCode by viewModel.sourceLangCode.collectAsStateWithLifecycle()
    val targetLangCode by viewModel.targetLangCode.collectAsStateWithLifecycle()

    val categories = listOf("✨ Greetings", "🚇 Travel", "🍳 Food & Dining", "🚨 Emergency", "📘 Vocabulary: Nouns", "🎨 Vocabulary: Adjectives")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Add Custom Phrase Accordion Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                var expandCustomCreator by remember { mutableStateOf(false) }
                
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandCustomCreator = !expandCustomCreator },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add custom icon",
                                tint = Color(0xFF005CBB)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Custom Offline Translation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1F)
                            )
                        }

                        Icon(
                            imageVector = if (expandCustomCreator) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle accordion expand",
                            tint = Color(0xFF6750A4)
                        )
                    }

                    if (expandCustomCreator) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "Add custom words/phrases to translate offline instantly between ${getLanguageNameByCode(sourceLangCode)} and ${getLanguageNameByCode(targetLangCode)}.",
                            fontSize = 11.sp,
                            color = Color(0xFF44474E),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customSourceWord,
                            onValueChange = { viewModel.onCustomSourceWordChanged(it) },
                            label = { Text("Original text (${getLanguageNameByCode(sourceLangCode)})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customTargetWord,
                            onValueChange = { viewModel.onCustomTargetWordChanged(it) },
                            label = { Text("Offline Translated text (${getLanguageNameByCode(targetLangCode)})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.addNewCustomTranslation() },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF005CBB),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("add_custom_translation_button")
                        ) {
                            Text("Save Translation locally", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // List custom additions
                        if (customTranslations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text("My Custom Local Terms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                customTranslations.filter {
                                    it.sourceLanguageCode == sourceLangCode && it.targetLanguageCode == targetLangCode
                                }.forEach { custom ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(custom.sourceText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(custom.translatedText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteCustomTranslation(custom.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete custom text",
                                                tint = Color(0xFFC62828),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // B. Offline search bar
        item {
            OutlinedTextField(
                value = dictSearchQuery,
                onValueChange = { viewModel.onDictSearchQueryChanged(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFF005CBB)) },
                placeholder = { Text("Search offline dictionary definitions...", fontSize = 14.sp, color = Color(0xFF939094)) },
                shape = RoundedCornerShape(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF005CBB),
                    unfocusedIndicatorColor = Color(0xFFDEE2F1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictionary_search_input")
            )
        }

        // C. Category Buttons Scrollbar Row
        item {
            ScrollableCategoryChips(categories, dictSelectedCategory) { cat ->
                viewModel.setDictCategory(cat)
            }
        }

        // D. Dict List results
        if (dictSearchQuery.isNotBlank()) {
            if (dictSearchResults.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, "No results", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching terms found offline", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(dictSearchResults) { word ->
                    DictionaryCardItem(word, sourceLangCode, targetLangCode) { selectedText ->
                        viewModel.onSourceTextChanged(selectedText)
                        viewModel.setActiveTab(0) // jump to translation workspace
                    }
                }
            }
        } else {
            // Preloaded lists for Category
            // Since on-database populate query is flow-based, we hardcode display map of dictionary categories
            // to render beautifully and fast.
            val filteredMockDict = getLocalPrefetchedCategoryCatalog(dictSelectedCategory)
            
            items(filteredMockDict) { word ->
                DictionaryCardItem(word, sourceLangCode, targetLangCode) { selectedText ->
                    viewModel.onSourceTextChanged(selectedText)
                    viewModel.setActiveTab(0) // jump to translation workspace
                }
            }
        }
    }
}

@Composable
fun ScrollableCategoryChips(categories: List<String>, selected: String, onCategorySelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = selected == cat
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
fun DictionaryCardItem(word: DictionaryWord, srcCode: String, tgtCode: String, onTextSelected: (String) -> Unit) {
    // Determine texts based on src/tgt codes
    val primaryText = when (srcCode) {
        "en" -> word.englishText
        "bn" -> word.bengaliText
        "hi" -> word.hindiText
        "es" -> word.spanishText
        else -> word.englishText
    }

    val translatedText = when (tgtCode) {
        "en" -> word.englishText
        "bn" -> word.bengaliText
        "hi" -> word.hindiText
        "es" -> word.spanishText
        else -> word.englishText
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTextSelected(primaryText) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(getLanguageIconEmoji(srcCode), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(getLanguageIconEmoji(tgtCode), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = translatedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF005CBB)
                    )
                }
            }

            IconButton(
                onClick = { onTextSelected(primaryText) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F9))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send phrase to input",
                    tint = Color(0xFF005CBB),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Prefetched mappings used to render Categories in Offline Dictionary fully fast without queries blockers or delays!
fun getLocalPrefetchedCategoryCatalog(category: String): List<DictionaryWord> {
    val result = mutableListOf<DictionaryWord>()
    when (category) {
        "✨ Greetings" -> {
            result.add(DictionaryWord(1, "✨ Greetings", "hello", "নমস্কার", "नमस्ते", "hola"))
            result.add(DictionaryWord(2, "✨ Greetings", "how are you", "আপনি কেমন আছেন", "आप कैसे हैं", "¿cómo estás"))
            result.add(DictionaryWord(3, "✨ Greetings", "thank you", "ধন্যবাদ", "धन्यवाद", "gracias"))
            result.add(DictionaryWord(4, "✨ Greetings", "good morning", "শুভ সকাল", "शुभ प्रभात", "buenos días"))
            result.add(DictionaryWord(5, "✨ Greetings", "good night", "শুভ রাত্রি", "शुभ रात्रि", "buenas noches"))
            result.add(DictionaryWord(6, "✨ Greetings", "goodby", "বিদায়", "अलविदा", "adiós"))
            result.add(DictionaryWord(7, "✨ Greetings", "please", "দয়া করে", "कृपया", "por favor"))
            result.add(DictionaryWord(8, "✨ Greetings", "welcome", "স্বাগতম", "स्वागत", "bienvenido"))
        }
        "🚇 Travel" -> {
            result.add(DictionaryWord(9, "🚇 Travel", "where is the hotel", "হোটেলটি কোথায়", "होटल कहाँ है", "¿dónde está el hotel"))
            result.add(DictionaryWord(10, "🚇 Travel", "where is the bathroom", "বাথরুম কোথায়", "शौचालय कहाँ है", "¿dónde está el baño"))
            result.add(DictionaryWord(11, "🚇 Travel", "where is the airport", "বিমানবন্দর কোথায়", "हवाई अड्डा कहाँ है", "¿dónde está el aeropuerto"))
            result.add(DictionaryWord(12, "🚇 Travel", "where is the train station", "রেল স্টেশন কোথায়", "रेलवे स्टेशन कहाँ है", "¿dónde está la estación de tren"))
            result.add(DictionaryWord(13, "🚇 Travel", "how much is this ticket", "এই টিকিটের দাম কত", "इस टिकट की कीमत कितनी है", "¿cuánto cuesta este boleto"))
            result.add(DictionaryWord(14, "🚇 Travel", "stop here", "এখানে থামুন", "यहाँ रुकिए", "deténgase aquí"))
        }
        "🍳 Food & Dining" -> {
            result.add(DictionaryWord(15, "🍳 Food & Dining", "i am hungry", "আমি ক্ষুধার্ত", "मुझे भूख लगी है", "tengo hambre"))
            result.add(DictionaryWord(16, "🍳 Food & Dining", "i want water", "আমি জল চাই", "मुझे पानी चाहिए", "quiero agua"))
            result.add(DictionaryWord(17, "🍳 Food & Dining", "the food is delicious", "খাবারটি খুব সুস্বাদু", "खाना बहुत स्वादिष्ट है", "la comida está deliciosa"))
            result.add(DictionaryWord(18, "🍳 Food & Dining", "the bill please", "বিলটি দয়া করে দিন", "कृप्या बिल लाइए", "la cuenta por favor"))
            result.add(DictionaryWord(19, "🍳 Food & Dining", "where is a restaurant", "রেস্তোরাঁ কোথায়", "एक रेस्तरां कहाँ है", "¿dónde hay un restaurante"))
        }
        "🚨 Emergency" -> {
            result.add(DictionaryWord(20, "🚨 Emergency", "help me", "আমাকে সাহায্য করুন", "मेरी मदद करो", "ayúdame"))
            result.add(DictionaryWord(21, "🚨 Emergency", "call a doctor", "ডাক্তার ডাকুন", "डॉक्टर को बुलाओ", "llame a un médico"))
            result.add(DictionaryWord(22, "🚨 Emergency", "where is the hospital", "হাসপাতাল কোথায়", "अस्पताल कहाँ है", "¿dónde está el hospital"))
            result.add(DictionaryWord(23, "🚨 Emergency", "i am lost", "আমি হারিয়ে গেছি", "मैं खो गया हूँ", "estoy perdido"))
            result.add(DictionaryWord(24, "🚨 Emergency", "call the police", "পুলিশ ডাকুন", "पुलिस को बुलाओ", "llame a la policía"))
        }
        "📘 Vocabulary: Nouns" -> {
            result.add(DictionaryWord(25, "📘 Vocabulary: Nouns", "water", "জল", "पानी", "agua"))
            result.add(DictionaryWord(26, "📘 Vocabulary: Nouns", "friend", "বন্ধু", "दोस्त", "amigo"))
            result.add(DictionaryWord(27, "📘 Vocabulary: Nouns", "house", "বাড়ি", "घर", "casa"))
            result.add(DictionaryWord(28, "📘 Vocabulary: Nouns", "book", "বই", "किताब", "libro"))
            result.add(DictionaryWord(29, "📘 Vocabulary: Nouns", "phone", "ফোন", "फ़ोन", "teléfono"))
            result.add(DictionaryWord(30, "📘 Vocabulary: Nouns", "car", "গাড়ি", "गाड़ी", "coche"))
        }
        "🎨 Vocabulary: Adjectives" -> {
            result.add(DictionaryWord(31, "🎨 Vocabulary: Adjectives", "good", "ভালো", "अच्छा", "bueno"))
            result.add(DictionaryWord(32, "🎨 Vocabulary: Adjectives", "bad", "খারাপ", "बुरा", "malo"))
            result.add(DictionaryWord(33, "🎨 Vocabulary: Adjectives", "happy", "খুশি", "खुश", "feliz"))
            result.add(DictionaryWord(34, "🎨 Vocabulary: Adjectives", "sad", "দুঃখিত", "उदास", "triste"))
        }
    }
    return result
}


// ==================== WORKSPACE 3: HISTORY & SAVED LOGS ====================
@Composable
fun HistoryBookmarksTab(viewModel: TranslationViewModel) {
    val history by viewModel.historyList.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarksList.collectAsStateWithLifecycle()
    
    var filterByBookmarksOnly by remember { mutableStateOf(false) }

    val activeDisplayList = if (filterByBookmarksOnly) bookmarks else history

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Toggle logs header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = !filterByBookmarksOnly,
                    onClick = { filterByBookmarksOnly = false },
                    label = { Text("Recent History") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterByBookmarksOnly,
                    onClick = { filterByBookmarksOnly = true },
                    label = { Text("Starred (Bookmarks)") }
                )
            }

            if (history.isNotEmpty() && !filterByBookmarksOnly) {
                IconButton(onClick = { viewModel.clearAllHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History Logs",
                        tint = Color(0xFFC62828)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeDisplayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (filterByBookmarksOnly) Icons.Default.Star else Icons.Default.Search,
                        contentDescription = "Empty state Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filterByBookmarksOnly) "No starred translations yet" else "Your offline translation log is empty",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (filterByBookmarksOnly) "Tap the star on translations to save them" else "Translated words will automatically appear here offline",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeDisplayList) { item ->
                    HistoryCardItem(item, viewModel)
                }
            }
        }
    }
}

@Composable
fun HistoryCardItem(item: TranslationHistory, viewModel: TranslationViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFDEE2F1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Language Mapping Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(getLanguageIconEmoji(item.sourceLanguageCode))
                    Text(
                        text = " ${item.sourceLanguageCode.uppercase()} ➜ ${item.targetLanguageCode.uppercase()} ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                    Text(getLanguageIconEmoji(item.targetLanguageCode))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Star bookmark Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F9))
                            .clickable { viewModel.toggleBookmark(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star Bookmark Icon",
                            tint = if (item.isBookmarked) Color(0xFFFFB300) else Color(0xFF44474E),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Sound Speaker playback Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F9))
                            .clickable { viewModel.speak(item.translatedText, item.targetLanguageCode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Read out loud text",
                            tint = Color(0xFF1B1B1F),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete single history log click
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F9))
                            .clickable { viewModel.deleteHistoryItem(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item log",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Original Source Text
            Text(
                text = item.sourceText,
                fontSize = 15.sp,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Translated Text Click to Copy
            Text(
                text = item.translatedText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF005CBB),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(item.translatedText))
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Translation copied!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
