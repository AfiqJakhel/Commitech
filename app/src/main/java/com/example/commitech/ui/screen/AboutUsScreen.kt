package com.example.commitech.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.commitech.R
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

data class Developer(
    val name: String,
    val nim: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation on screen load
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val developers = listOf(
        Developer("Fadhilla Firma", "2311522031", Color(0xFF6366F1)),
        Developer("Farhan Fitrahadi", "2311522037", Color(0xFFEC4899)),
        Developer("Muhammad Afiq Jakhel", "2311523011", Color(0xFF8B5CF6)),
        Developer("Muhammad Diaz Ananda S", "2311521015", Color(0xFF10B981))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "About Us",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            AboutUsBottomBar(onHomeClick = onHomeClick)
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // App Logo with animation
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.commitechlogo),
                            contentDescription = "Commitech Logo",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }

            // App Title with animation
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = tween(600, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 100))
                ) {
                    Text(
                        text = "Commitech",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Description Card with animation
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    DescriptionCard()
                }
            }

            // Team Section Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -100 },
                        animationSpec = tween(600, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Team",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dibuat oleh:",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }
                }
            }

            // Developer Carousel
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                            scaleIn(
                                initialScale = 0.9f,
                                animationSpec = tween(600, delayMillis = 400)
                            )
                ) {
                    DeveloperCarousel(developers = developers)
                }
            }

            // Footer with animation
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(800, delayMillis = 1000)) +
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(800, delayMillis = 1000)
                            )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "University",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Universitas Andalas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "2025",
                            fontSize = 14.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DescriptionCard() {
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Deskripsi Aplikasi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Commitech adalah aplikasi yang dirancang untuk memudahkan pihak rekruter dalam mengelola seluruh proses rekrutmen kegiatan kampus secara terintegrasi. Melalui aplikasi ini, proses seperti seleksi berkas, pengumuman hasil, penjadwalan wawancara, serta pengelolaan data pendaftar dapat dilakukan secara efisien dan terorganisir dalam satu platform. Commitech hadir sebagai solusi digital untuk meningkatkan efektivitas dan transparansi proses rekrutmen di lingkungan kampus.",
                fontSize = 15.sp,
                color = colorScheme.onSurface,
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeveloperCarousel(developers: List<Developer>) {
    val colorScheme = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { developers.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val developer = developers[page]
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Scale effect based on position
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        
                        // Alpha effect
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    developer.color.copy(alpha = 0.15f),
                                    developer.color.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            developer.color,
                                            developer.color.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = developer.name.split(" ").map { it.first() }.take(2).joinToString(""),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name
                        Text(
                            text = developer.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // NIM
                        Text(
                            text = developer.nim,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Decorative line
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(developer.color)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(developers.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) colorScheme.primary
                            else colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        .animateContentSize()
                )
            }
        }
    }
}

@Composable
fun AboutUsBottomBar(onHomeClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { 
                Icon(
                    Icons.Default.Home, 
                    contentDescription = "Home",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "Home",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
                selectedTextColor = colorScheme.primary,
                unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { 
                Icon(
                    Icons.Default.Info, 
                    contentDescription = "About Us",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "About Us",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
                selectedTextColor = colorScheme.primary,
                unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { 
                Icon(
                    Icons.Default.Person, 
                    contentDescription = "Profile",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "Profile",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
                selectedTextColor = colorScheme.primary,
                unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = colorScheme.primaryContainer
            )
        )
    }
}
