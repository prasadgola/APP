package com.example.basavaprasad.screens.readtext

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import com.example.basavaprasad.R

@Composable
fun ReadTextScreen(modifier: Modifier = Modifier) {
    val pageCount = 2
    val pagerState = rememberPagerState(pageCount = { Int.MAX_VALUE })

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        // ===== Image Slider =====
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val actualPage = page % pageCount
                when (actualPage) {
                    0 -> PostSlide1()
                    1 -> PostSlide2()
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pageCount) { index ->
                    val isSelected = (pagerState.currentPage % pageCount) == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFFFC107)
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        // ===== About Me Section =====
        AboutMeSection()

        // ===== Education Section =====
        EducationSection()

        // ===== Projects Section =====
        ProjectsSection()

        // ===== Connect Me Section =====
        ConnectMeSection()

        // ===== Contact Form Section =====
        ContactFormSection()
    }
}

// ==================== About Me ====================

@Composable
private fun AboutMeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "About",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.05f),
            lineHeight = 60.sp
        )
        Text(
            text = "About Me",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AboutInfoRow(label = "Name", value = "Basavaprasad Mallikarjun Gola")
        AboutInfoRow(label = "Address", value = "Plano, Texas, United States of America")
        AboutInfoRow(label = "Zip code", value = "75074")
        AboutInfoRow(label = "Email", value = "basavaprasadgolacs@gmail.com")
        AboutInfoRow(label = "Phone", value = "+1 (682) 266 - 3588")
        AboutInfoRow(label = "Date of birth", value = "July - 14th - 1998")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: handle CV download */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107)
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Download CV",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "$label :",
            color = Color(0xFFFFC107),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            modifier = Modifier.weight(2f)
        )
    }
}

// ==================== Education ====================

@Composable
private fun EducationSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Education",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.05f),
            lineHeight = 60.sp
        )
        Text(
            text = "Education",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        EducationCard(
            year = "2021 - 2023",
            degree = "Master Degree of Computer Science and Engineering",
            institution = "University of Texas at Arlington",
            subjects = "Database Systems, Machine Learning, Software Engineering"
        )

        EducationCard(
            year = "2016 - 2020",
            degree = "Bachelor's Degree of Electronics and Communication",
            institution = "Dayananda Sagar College of Engineering",
            subjects = "Logic Design, Embedded System Design, Advanced Digital Switching, Wireless and Mobile Communications, Cryptography and Network Security"
        )

        EducationCard(
            year = "2014 - 2016",
            degree = "Pre University College of Karnataka Board",
            institution = "Sharanabasaveshwar Residential PU College",
            subjects = "Physics, Chemistry, Electronics"
        )

        EducationCard(
            year = "2004 - 2014",
            degree = "Secondary School Leaving Certificate",
            institution = "Sharanabasaveshwar Residential Public School",
            subjects = "Science (Physics, Chemistry, Biology), Social-Science (History, Civics, Geography), Mathematics, English, Kannada, Hindi"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Download CV button centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { /* TODO: handle CV download */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Download CV",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EducationCard(
    year: String,
    degree: String,
    institution: String,
    subjects: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = year,
            color = Color(0xFFFFC107),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Degree
        Text(
            text = degree,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Institution
        Text(
            text = institution,
            color = Color(0xFFFFC107),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subjects
        Text(
            text = subjects,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}

// ==================== Projects ====================

@Composable
private fun ProjectsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Projects",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.05f),
            lineHeight = 60.sp
        )
        Text(
            text = "Our Projects",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProjectCard(
            imageRes = R.drawable.post2,
            title = "Branding & Illustration Design",
            category = "Web Design",
            height = 200
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProjectCard(
            imageRes = R.drawable.post4,
            title = "Branding & Illustration Design",
            category = "Web Design",
            height = 250
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProjectCard(
            imageRes = R.drawable.post5,
            title = "Branding & Illustration Design",
            category = "Web Design",
            height = 200
        )
    }
}

@Composable
private fun ProjectCard(
    imageRes: Int,
    title: String,
    category: String,
    height: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Text centered on image
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category,
                color = Color(0xFFFFC107),
                fontSize = 14.sp
            )
        }
    }
}

// ==================== Connect Me ====================

@Composable
private fun ConnectMeSection() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Connect",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.05f),
            lineHeight = 60.sp
        )
        Text(
            text = "Connect Me",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConnectIcon(
                icon = Icons.Default.Person,
                label = "LinkedIn",
                onClick = { uriHandler.openUri("https://www.linkedin.com/in/basavaprasad-gola/") }
            )
            ConnectIcon(
                icon = Icons.Default.Star,
                label = "Github",
                onClick = { uriHandler.openUri("https://github.com/prasadgola") }
            )
            ConnectIcon(
                icon = Icons.Default.Share,
                label = "Twitter",
                onClick = { uriHandler.openUri("https://twitter.com/gola_basava") }
            )
            ConnectIcon(
                icon = Icons.Default.Email,
                label = "Instagram",
                onClick = { uriHandler.openUri("https://www.instagram.com/prasad_gola/") }
            )
        }
    }
}

@Composable
private fun ConnectIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

// ==================== Contact Form ====================

@Composable
private fun ContactFormSection() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Black.copy(alpha = 0.2f),
        focusedBorderColor = Color(0xFFFFC107),
        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
        focusedLabelColor = Color(0xFFFFC107),
        cursorColor = Color(0xFFFFC107),
        unfocusedTextColor = Color.Black,
        focusedTextColor = Color.Black
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(20.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Your Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = textFieldColors,
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { /* TODO: handle send message */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107)
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "Send Message",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// ==================== Post Slides ====================

@Composable
private fun PostSlide1() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.post1),
            contentDescription = "Post 1",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(15.dp)
        ) {
            Text(
                text = "Hello!",
                color = Color(0xFFFFC107),
                fontSize = 20.sp,
                lineHeight = 86.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("I'm \n")
                    withStyle(style = SpanStyle(color = Color(0xFFFFC107))) {
                        append("Basavaprasad\nGola")
                    }
                },
                color = Color.White,
                fontSize = 56.sp,
                lineHeight = 62.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Developer",
                color = Color.White,
                fontSize = 36.sp,
                lineHeight = 86.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PostSlide2() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.post2),
            contentDescription = "Post 2",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(15.dp)
        ) {
            Text(
                text = "Hello!",
                color = Color(0xFFFFC107),
                fontSize = 20.sp,
                lineHeight = 86.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("Graduated from \n")
                    withStyle(style = SpanStyle(color = Color(0xFFFFC107))) {
                        append("University of Texas\nat Arlington")
                    }
                },
                color = Color.White,
                fontSize = 46.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Texas, United States",
                color = Color.White,
                fontSize = 36.sp,
                lineHeight = 86.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}