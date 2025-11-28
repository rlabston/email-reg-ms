package com.android.ai.catalog.ui.home

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.ai.catalog.R
import com.android.ai.catalog.auth.AuthManager
import com.android.ai.catalog.network.ApiService
import com.android.ai.catalog.network.HomeScreenData
import com.android.ai.catalog.network.ServiceItem
import com.android.ai.catalog.network.ContactInfo
import kotlinx.coroutines.launch

// Helper function to detect emulator (same logic as ApiService)
private fun isEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    // Navigate to email management with a target mode: "menu" | "register" | "list"
    onNavigateToEmails: (String) -> Unit = {},
    onNavigateToChatbot: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    // Load dynamic service data from backend or use rich mock data
    var homeData by remember { mutableStateOf<HomeScreenData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load home screen data on composition
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                error = null
                // Try to load from backend API
                val (ok, data, err) = ApiService.homeData()
                if (ok && data != null) {
                    homeData = data
                } else {
                    // Use rich mock data based on Technet7 services
                    homeData = createTechnet7ServicesData()
                }
                fun HomeScreen(
                    modifier: Modifier = Modifier,
                    onLogout: () -> Unit = {},
                    onNavigateToEmails: (String) -> Unit = {},
                    onNavigateToChatbot: () -> Unit = {},
                    onLogin: () -> Unit = {}
                ) {
    }

    Scaffold(
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val userEmail = AuthManager.getEmail()
                                val isLoggedIn = userEmail != null
                                val isAdmin = AuthManager.getRoles().contains("ROLE_ADMIN")
                                var menuExpanded by remember { mutableStateOf(false) }
                                // Hamburger menu (top right)
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Menu",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Home") },
                                        onClick = {
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Email Registration") },
                                        onClick = {
                                            menuExpanded = false
                                            onNavigateToEmails("register")
                                        }
                                    )
                                    if (!isLoggedIn) {
                                        DropdownMenuItem(
                                            text = { Text("Login") },
                                            onClick = {
                                                menuExpanded = false
                                                onLogin()
                                            }
                                        )
                                    }
                                    if (isLoggedIn) {
                                        DropdownMenuItem(
                                            text = { Text("AI Chatbot") },
                                            onClick = {
                                                menuExpanded = false
                                                onNavigateToChatbot()
                                            }
                                        )
                                    }
                                    if (isAdmin) {
                                        DropdownMenuItem(
                                            text = { Text("View All Emails") },
                                            onClick = {
                                                menuExpanded = false
                                                onNavigateToEmails("list")
                                            }
                                        )
                                    }
                                    if (isLoggedIn) {
                                        DropdownMenuItem(
                                            text = { Text("Logout ($userEmail)") },
                                            onClick = {
                                                menuExpanded = false
                                                AuthManager.clearAuth()
                                                onLogout()
                                            }
                                        )
                                    }
                                }
                            }
                        },
                                menuExpanded = false
                                // Already on home; no-op
                            }
                        )
                        
                        // Email Registration (all users)
                        DropdownMenuItem(
                            text = { Text("Email Registration") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToEmails("register")
                            }
                        )
                        
                        // Login (guests only)
                        if (!isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("Login") },
                                onClick = {
                                    menuExpanded = false
                                    onLogin()
                                }
                            )
                        }
                        
                        // AI Chatbot (authenticated users)
                        if (isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("AI Chatbot") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToChatbot()
                                }
                            )
                        }
                        
                        // View All Emails (admin only)
                        if (isAdmin) {
                            DropdownMenuItem(
                                text = { Text("View All Emails") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToEmails("list")
                                }
                            )
                        }
                        
                        // Logout (authenticated users)
                        if (isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("Logout ($userEmail)") },
                                onClick = {
                                    menuExpanded = false
                                    AuthManager.clearAuth()
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingScreen()
                }
                homeData != null -> {
                    HomeContent(homeData = homeData!!)
                }
                else -> {
                    ErrorScreen(
                        error = error ?: "Failed to load services",
                        onRetry = {
                            scope.launch {
                                isLoading = true
                                homeData = createTechnet7ServicesData()
                                isLoading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Loading services...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error loading services",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = error,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun HomeContent(homeData: HomeScreenData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Welcome Section
        item {
            WelcomeSection(
                title = homeData.welcomeTitle,
                subtitle = homeData.welcomeSubtitle
            )
        }

        // Featured Services Section
        item {
            FeaturedServicesSection(services = homeData.featuredServices)
        }

        // Services by Category
        homeData.servicesByCategory.forEach { (category, services) ->
            item {
                ServiceCategorySection(
                    category = category,
                    services = services
                )
            }
        }

        // Technologies Section
        item {
            TechnologiesSection(technologies = homeData.supportedTechnologies)
        }

        // Contact Section
        item {
            ContactSection(contactInfo = homeData.contactInfo)
        }
    }
}

@Composable
private fun WelcomeSection(
    title: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeaturedServicesSection(services: List<ServiceItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Featured Services",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(services) { service ->
                FeaturedServiceCard(service = service)
            }
        }
    }
}

@Composable
private fun FeaturedServiceCard(service: ServiceItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        modifier = Modifier
            .width(280.dp)
            .height(220.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = service.icon,
                    fontSize = 28.sp
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Featured",
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = service.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = service.description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = service.ctaText,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ServiceCategorySection(
    category: String,
    services: List<ServiceItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = category,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(services) { service ->
                ServiceCard(service = service)
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .width(240.dp)
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = service.icon,
                fontSize = 24.sp
            )
            
            Text(
                text = service.title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = service.description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = service.ctaText,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TechnologiesSection(technologies: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Technologies We Use",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(technologies) { tech ->
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = tech,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSection(contactInfo: ContactInfo) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Get In Touch",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Email: ${contactInfo.email}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Phone: ${contactInfo.phone}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Location: ${contactInfo.address}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Rich mock data based on Technet7 services at https://technet7.com/index.php/services/
private fun createTechnet7ServicesData(): HomeScreenData {
    val featuredServices = listOf(
        ServiceItem(
            title = "AI & Machine Learning Solutions",
            description = "Custom AI models, chatbots with natural language processing, and intelligent automation systems designed to transform your business processes.",
            icon = "🤖",
            category = "AI/ML",
            features = listOf("Custom AI Model Development", "ChatGPT Integration", "Natural Language Processing"),
            ctaText = "Explore AI Solutions",
            ctaUrl = "/services/ai-ml",
            isHighlighted = true
        ),
        ServiceItem(
            title = "Cloud Infrastructure & DevOps",
            description = "Scalable cloud architecture, CI/CD pipelines, and modern deployment strategies for enterprise applications on AWS, Azure, and GCP.",
            icon = "☁️",
            category = "Cloud",
            features = listOf("AWS/Azure/GCP Setup", "Kubernetes Orchestration", "CI/CD Pipeline Design"),
            ctaText = "View Cloud Services",
            ctaUrl = "/services/cloud",
            isHighlighted = true
        ),
        ServiceItem(
            title = "Mobile & Web Development",
            description = "Full-stack development with modern frameworks, responsive design, and cross-platform mobile solutions for Android and iOS.",
            icon = "📱",
            category = "Development",
            features = listOf("React/Angular Applications", "Android/iOS Development", "Progressive Web Apps"),
            ctaText = "Start Your Project",
            ctaUrl = "/services/development",
            isHighlighted = true
        )
    )

    val servicesByCategory = mapOf(
        "AI & Data Science" to listOf(
            ServiceItem("Machine Learning Models", "Custom ML models for classification, regression, clustering, and recommendation systems tailored to your business needs.", "🧠", "AI/ML", emptyList(), "Learn More", "/ai/ml-models", false),
            ServiceItem("Natural Language Processing", "Text analysis, sentiment analysis, language translation, and conversational AI solutions for enhanced customer interaction.", "💬", "AI/ML", emptyList(), "Explore NLP", "/ai/nlp", false),
            ServiceItem("Data Modeling & Analytics", "Advanced data modeling, predictive analytics, and business intelligence solutions to unlock insights from your data.", "📊", "Data", emptyList(), "View Analytics", "/ai/analytics", false)
        ),
        "Cloud & Infrastructure" to listOf(
            ServiceItem("AWS Cloud Solutions", "Complete AWS infrastructure setup, migration, and optimization for scalable, cost-effective cloud applications.", "🔶", "Cloud", emptyList(), "AWS Services", "/cloud/aws", false),
            ServiceItem("Azure Solutions", "Microsoft Azure cloud services including Azure Functions, App Services, and enterprise-grade cloud infrastructure.", "🔷", "Cloud", emptyList(), "Azure Services", "/cloud/azure", false),
            ServiceItem("Kubernetes Orchestration", "Container orchestration, microservices architecture, and automated scaling solutions for modern applications.", "⚙️", "Cloud", emptyList(), "K8s Solutions", "/cloud/kubernetes", false)
        ),
        "Web & Mobile Development" to listOf(
            ServiceItem("Web Application Development", "Modern responsive web applications using React, Angular, Vue.js, and Spring Boot for robust enterprise solutions.", "🌐", "Development", emptyList(), "Web Apps", "/dev/web", false),
            ServiceItem("Android Development", "Native Android applications with Kotlin and Jetpack Compose for exceptional mobile user experiences.", "📲", "Development", emptyList(), "Android Apps", "/dev/android", false),
            ServiceItem("iOS Development", "Native iOS applications with Swift and SwiftUI for seamless Apple ecosystem integration.", "🍎", "Development", emptyList(), "iOS Apps", "/dev/ios", false)
        )
    )

    val technologies = listOf(
        "Java", "Spring Boot", "Python", "TensorFlow", "React", "Angular", "Node.js",
        "Kotlin", "Swift", "Flutter", "AWS", "Azure", "Docker", "Kubernetes", 
        "PostgreSQL", "MongoDB", "Redis", "Elasticsearch"
    )

    val contactInfo = ContactInfo(
        email = "info@technet7.com",
        phone = "+1 (555) 123-4567",
        website = "https://technet7.com",
        address = "San Francisco, CA",
        linkedIn = "https://linkedin.com/company/technet7",
        github = "https://github.com/technet7"
    )

    return HomeScreenData(
        welcomeTitle = "Welcome to Technet7 AI Services",
        welcomeSubtitle = "Innovative Technology Solutions for Modern Businesses",
        heroImageUrl = "https://technet7.com/images/hero-background.jpg",
        featuredServices = featuredServices,
        servicesByCategory = servicesByCategory,
        supportedTechnologies = technologies,
        contactInfo = contactInfo,
        lastUpdated = System.currentTimeMillis()
    )
}
