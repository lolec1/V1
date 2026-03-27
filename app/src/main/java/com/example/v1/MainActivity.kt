package com.example.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod

@Composable
fun DeadlinesPager(deadlines: List<Deadline>) {
    val sorted = deadlines.sortedBy { it.date }.take(3)

    var currentIndex by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    },
                    onDragEnd = {
                        if (dragOffset < -100 && currentIndex < sorted.lastIndex) {
                            currentIndex++
                        } else if (dragOffset > 100 && currentIndex > 0) {
                            currentIndex--
                        }
                        dragOffset = 0f
                    }
                )
            }
    ) {
        val deadline = sorted.getOrNull(currentIndex) ?: return

        DeadlineBannerCard(deadline)

        // Индикаторы (точки)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(sorted.size) { i ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentIndex) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}
fun formatTime(hoursLeft: Int): String {
    return if (hoursLeft >= 24) {
        val d = hoursLeft / 24
        val h = hoursLeft % 24
        "${d}d ${h}h"
    } else {
        "${hoursLeft}h"
    }
}
@Composable
fun DeadlineBannerCard(deadline: Deadline) {
    val hoursLeft = calculateHoursLeft(deadline.date, deadline.time)
    val totalHours = 7 * 24f
    val progress = (1f - (hoursLeft / totalHours)).coerceIn(0f, 1f)

    val isUrgent = hoursLeft < 24

    val gradient = if (isUrgent) {
        Brush.horizontalGradient(listOf(Color(0xFFFF8A80), Color(0xFFE53935)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                // 🔥 Urgent badge
                if (isUrgent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Urgent deadline",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = deadline.type,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = deadline.subject,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            ProgressCircle(
                progress = progress,
                hoursLeft = hoursLeft.toInt(),
                color = Color.White
            )
        }
    }
}
@Composable
fun ProgressCircle(progress: Float, hoursLeft: Int, color: Color) {
    val animatedProgress by animateFloatAsState(progress, label = "")

    val days = hoursLeft / 24
    val hours = hoursLeft % 24

    val text = if (hoursLeft >= 24) {
        "${days}d ${hours}h"
    } else {
        "${hoursLeft}h"
    }

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10f

            // 👇 гарантированно квадрат
            val diameter = size.minDimension
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )

            val arcSize = Size(diameter, diameter)

            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke)
            )
        }

        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AcademicCalendarApp()
        }
    }
}
@Composable
fun TimeTableButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF4A90E2), Color(0xFF1565C0))
                )
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Time Table",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarApp() {
    var listContent by remember { mutableStateOf("deadlines") }
    var hwEnabled by remember { mutableStateOf(true) }
    var quizEnabled by remember { mutableStateOf(true) }
    var projectEnabled by remember { mutableStateOf(true) }
    var labEnabled by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("calendar") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val deadlines = listOf(
        Deadline(
            title = "Final Project",
            subject = "Software Engineering",
            date = LocalDate(2026, 4, 5),
            time = "23:00",
            type = "Project",
            color = Color(0xFF9C27B0)
        ),
        Deadline(
            title = "Homework 1",
            subject = "Calculus 2",
            date = LocalDate(2026, 3, 27),
            time = "23:00",
            type = "H/W",
            color = Color(0xFF2196F3)
        ),
        Deadline(
            title = "Homework 1",
            subject = "Physics 2",
            date = LocalDate(2026, 3, 28),
            time = "12:00",
            type = "H/W",
            color = Color(0xFF2196F3)
        ),
        Deadline(
            title = "Homework 2",
            subject = "Calculus 2",
            date = LocalDate(2026, 3, 30),
            time = "22:00",
            type = "H/W",
            color = Color(0xFF4CAF50)
        ),
        Deadline(
            title = "Quiz 2",
            subject = "AE 2",
            date = LocalDate(2026, 4, 4),
            time = "23:00",
            type = "Quiz",
            color = Color(0xFF2196F3)
        ),
        Deadline(
            title = "Quiz 3",
            subject = "Physics 2",
            date = LocalDate(2026, 3, 27),
            time = "10:00",
            type = "Quiz",
            color = Color(0xFF2196F3)
        ),
        Deadline(
            title = "Laboratory",
            subject = "OOP",
            date = LocalDate(2026, 3, 24),
            time = "23:00",
            type = "Lab",
            color = Color(0xFFFF9800)
        ),
        Deadline(
            title = "Laboratory",
            subject = "OOP",
            date = LocalDate(2026, 4, 2),
            time = "23:00",
            type = "Lab",
            color = Color(0xFFFF9800)
        ),
        Deadline(
            title = "Quiz 3",
            subject = "Physics 2",
            date = LocalDate(2026, 4, 10),
            time = "23:00",
            type = "Quiz",
            color = Color(0xFF2196F3)
        ),
        Deadline(
            title = "Lab 4",
            subject = "OOP",
            date = LocalDate(2026, 4, 18),
            time = "23:00",
            type = "Lab",
            color = Color(0xFFFF9800)
        )
    )


    val filteredDeadlines = deadlines.filter { deadline ->
        deadline.date >= todayDate && when (deadline.type) {
            "H/W" -> hwEnabled
            "Quiz" -> quizEnabled
            "Lab" -> labEnabled
            "Project" -> projectEnabled
            else -> false
        }
    }.sortedBy { it.date }

    val upcomingDeadlines = deadlines.filter { it.date >= todayDate }
        .sortedBy { it.date }.take(3)

    when (currentScreen) {

        "calendar" -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item { TopHeader() }

                item { DeadlinesPager(deadlines = upcomingDeadlines) }

                item {
                    TimeTableButton {
                        currentScreen = "timetable"
                    }
                }
                item {
                    FiltersRow(
                        hwEnabled = hwEnabled,
                        onHwToggle = { hwEnabled = !hwEnabled },
                        quizEnabled = quizEnabled,
                        onQuizToggle = { quizEnabled = !quizEnabled },
                        labEnabled = labEnabled,
                        onLabToggle = { labEnabled = !labEnabled }, projectEnabled = projectEnabled,
                        onProjectToggle = { projectEnabled = !projectEnabled },

                        // 👇 ВАЖНО
                        onTimetableClick = { currentScreen = "timetable" }
                    )
                }
                item { CalendarSection(today = todayDate, deadlines = filteredDeadlines) }

                item { SwitchButtons(selected = listContent, onSelect = { listContent = it }) }

                item {
                    when (listContent) {
                        "deadlines" -> {
                            filteredDeadlines.forEach { DeadlineCard(it) }
                        }

                        "grades" -> {
                            val sortedWeeks = gradesByWeek.keys
                                .sortedByDescending { it.removePrefix("Week ").toInt() }

                            sortedWeeks.forEach { week ->
                                val grades = gradesByWeek[week] ?: emptyList()
                                Text(week, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                grades.forEach { grade ->
                                    GradeCard(grade) {
                                        selectedSubject = grade.subject
                                        currentScreen = "subjectDetails"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        "timetable" -> {
            // 👇 ТВОЙ экран
            TimetableScreenWithBack(
                onBack = { currentScreen = "calendar" }
            )
        }

        "subjectDetails" -> {
            selectedSubject?.let { subject ->
                SubjectDetailsScreen(
                    subject = subject,
                    gradesByWeek = gradesByWeek,
                    onBack = { currentScreen = "calendar" }
                )
            }
        }
    }
}
@Composable
fun SubjectDetailsScreen(
    subject: String,
    gradesByWeek: Map<String, List<Grade>>,
    onBack: () -> Unit
) {
    val filtered = gradesByWeek.mapValues { entry ->
        entry.value.filter { it.subject == subject }
    }.filterValues { it.isNotEmpty() }

    val sortedWeeks = filtered.keys
        .sortedByDescending { it.removePrefix("Week ").toInt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 🔙 Back + Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "< Back",
                modifier = Modifier.clickable { onBack() },
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = subject,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sortedWeeks.forEach { week ->
                val grades = filtered[week] ?: emptyList()

                item {
                    Text(
                        text = week,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(grades) { grade ->
                    GradeCard(grade) // no click needed here
                }
            }
        }
    }
}
@Composable
fun SwitchButtons(selected: String, onSelect: (String) -> Unit) {
    val blue = Color(0xFF1565C0)
    val isDeadlines = selected == "deadlines"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        // Фон
        Box(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE3F2FD))
        )

        val offset by animateFloatAsState(
            targetValue = if (isDeadlines) -1f else 1f,
            label = ""
        )

        // Индикатор
        Box(
            modifier = Modifier
                .offset(x = (offset * 55).dp) // 👈 110 / 2 = 55
                .width(100.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(blue)
        )

        Row(
            modifier = Modifier.width(220.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentItem(
                text = "Deadlines",
                isSelected = isDeadlines,
                onClick = { onSelect("deadlines") },
                modifier = Modifier.width(100.dp)
            )

            SegmentItem(
                text = "Grades",
                isSelected = !isDeadlines,
                onClick = { onSelect("grades") },
                modifier = Modifier.width(100.dp)
            )
        }
    }
}
@Composable
fun SegmentItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val blue = Color(0xFF1565C0)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else blue,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blue = Color(0xFF1565C0)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) blue else Color.White
        ),
        modifier = modifier
            .height(28.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxSize(),
            color = if (isSelected) Color.White else blue,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )
    }
}
@Composable
fun ContentIndicator(isDeadlinesMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Первая точка — Дедлайны
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isDeadlinesMode) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.3f)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Вторая точка — Оценки
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (!isDeadlinesMode) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.3f)
                )
        )
    }
}
@Composable
fun ListContent(
    contentType: String,
    deadlines: List<Deadline>,
    gradesByWeek: Map<String, List<Grade>>,
    onSwipeRightToLeft: () -> Unit,
    onSwipeLeftToRight: () -> Unit
) {
    var totalDragAmount by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { _ -> totalDragAmount = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragAmount += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragAmount < -50f) onSwipeRightToLeft()  // Справа налево → Оценки
                        else if (totalDragAmount > 50f) onSwipeLeftToRight() // Слева направо → Дедлайны
                        totalDragAmount = 0f
                    },
                    onDragCancel = { totalDragAmount = 0f }
                )
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (contentType) {
            "deadlines" -> {
                // ВЕСЬ список дедлайнов (НЕ скроллится сам)
                deadlines.forEach { deadline ->
                    DeadlineCard(deadline)
                }
            }
            "grades" -> {
                // ВСЕ недели оценок (НЕ скроллится сам)
                val sortedWeeks = gradesByWeek.keys
                    .sortedByDescending { it.removePrefix("Week ").toInt() }

                sortedWeeks.forEach { week ->
                    val grades = gradesByWeek[week] ?: emptyList()
                    Text(
                        text = week,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    grades.forEach { grade ->
                        GradeCard(grade)
                    }
                }
            }
        }
    }
}
@Composable
fun DeadlinesBottomContent(deadlines: List<Deadline>, today: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Календарь ВСЕГДА виден вверху
        CalendarSection(today = today, deadlines = deadlines)

        // Список дедлайнов внизу (скроллится если много)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Занимает оставшееся место
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(deadlines) { deadline ->
                DeadlineCard(deadline)
            }
        }
    }
}

@Composable
fun GradesBottomContent(gradesByWeek: Map<String, List<Grade>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(0.dp) // Убрал отступы снизу
    ) {
        gradesByWeek.forEach { (week, grades) ->
            item {
                Text(
                    week,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(grades) { grade ->
                GradeCard(grade)
            }
        }
    }
}
@Composable
fun CalendarContent(
    filteredDeadlines: List<Deadline>,
    today: LocalDate,
    hwEnabled: Boolean, onHwToggle: () -> Unit,
    quizEnabled: Boolean, onQuizToggle: () -> Unit,
    labEnabled: Boolean, onLabToggle: () -> Unit,
    projectEnabled: Boolean, onProjectToggle: () -> Unit // ✅ ДОБАВЬ
) {
    var currentScreen by remember { mutableStateOf("calendar") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { DeadlinesPager(filteredDeadlines) }
        item {
            FiltersRow(hwEnabled, onHwToggle, quizEnabled, onQuizToggle, labEnabled, onLabToggle,onTimetableClick = { currentScreen = "timetable" }, projectEnabled = projectEnabled,
                onProjectToggle = { onProjectToggle })
        }
        item { CalendarSection(today = today, deadlines = filteredDeadlines) }
        items(filteredDeadlines) { DeadlineCard(it) }
    }
}

@Composable
fun GradesContent(gradesByWeek: Map<String, List<Grade>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gradesByWeek.forEach { (week, grades) ->
            item {
                Text(week, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
            items(grades) { GradeCard(it) }
        }
    }
}
@Composable
fun CalendarScreen(
    filteredDeadlines: List<Deadline>,
    today: LocalDate,
    hwEnabled: Boolean, onHwToggle: () -> Unit,
    quizEnabled: Boolean, onQuizToggle: () -> Unit,
    labEnabled: Boolean, onLabToggle: () -> Unit,
    projectEnabled: Boolean, onProjectToggle: () -> Unit // 👈 ДОБАВЬ
) {
    var currentScreen by remember { mutableStateOf("calendar") }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { TopHeader() }
        item { DeadlinesPager(filteredDeadlines) }
        item {
            FiltersRow(
                hwEnabled, onHwToggle,
                quizEnabled, onQuizToggle,
                labEnabled, onLabToggle,
                projectEnabled = projectEnabled,
                onProjectToggle = onProjectToggle,
                onTimetableClick = { currentScreen = "timetable" }
            )
        }
        item { CalendarSection(today = today, deadlines = filteredDeadlines) }
        items(filteredDeadlines) { DeadlineCard(it) }
    }
}
@Composable
fun GradesScreen(gradesByWeek: Map<String, List<Grade>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gradesByWeek.forEach { (week, grades) ->
            item {
                Text(
                    text = week,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(grades) { grade ->
                GradeCard(grade)
            }
        }
    }
}
@Composable
fun TabIndicator(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.3f)
            )
    )
}

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
            modifier = Modifier.size(48.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Р", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Рахмонов Санжар", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Urgent deadlines", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CalendarSection(today: LocalDate, deadlines: List<Deadline>, modifier: Modifier = Modifier) {
    var selectedText by remember { mutableStateOf<String?>(null) }
    var currentMonthOffset by remember { mutableStateOf(0) }

    val baseDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonthDate = baseDate.plus(DatePeriod(months = currentMonthOffset))

    var dragOffset by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            if (dragOffset < -100 && currentMonthOffset < 2) currentMonthOffset++
                            if (dragOffset > 100 && currentMonthOffset > 0) currentMonthOffset--
                            dragOffset = 0f
                        }
                    )
                }
        ) {
            val monthNames = listOf(
                "Январь","Февраль","Март","Апрель","Май","Июнь",
                "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "<",
                    fontSize = 20.sp,
                    modifier = Modifier.clickable {
                        if (currentMonthOffset > 0) currentMonthOffset--
                    }
                )
                Text(
                    text = ">",
                    fontSize = 20.sp,
                    modifier = Modifier.clickable {
                        if (currentMonthOffset < 2) currentMonthOffset++
                    }
                )
            }
            Text(
                text = "${monthNames[currentMonthDate.monthNumber - 1]} ${currentMonthDate.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            CalendarGrid(
                today = today,
                displayedMonth = currentMonthDate,
                deadlines = deadlines,
                onDayClick = { text -> selectedText = text }
            )
            if (!selectedText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedText!!,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    today: LocalDate,
    displayedMonth: LocalDate,
    deadlines: List<Deadline>,
    onDayClick: (String) -> Unit
) {
    val daysInMonth = when (displayedMonth.monthNumber) {
        1,3,5,7,8,10,12 -> 31
        4,6,9,11 -> 30
        2 -> 28
        else -> 30
    }
    val firstDayOffset = 0 // Понедельник

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Заголовки дней недели
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }

        // 5-6 недель календаря
        repeat(6) { weekIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { dayIndex ->
                    val dayOfMonth = weekIndex * 7 + dayIndex - firstDayOffset + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dayDate = LocalDate(displayedMonth.year, displayedMonth.monthNumber, dayOfMonth)
                        val dayDeadlines = deadlines.filter { it.date == dayDate }

                        DayCellWithDeadlines(
                            day = dayOfMonth,
                            dayDate = dayDate,
                            isToday = dayDate == today,
                            deadlines = dayDeadlines,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (dayDeadlines.isNotEmpty()) {
                                        val text = dayDeadlines.joinToString { "${it.subject} ${it.title}" }
                                        onDayClick(text)
                                    } else {
                                        onDayClick("") // 👈 CLEAR TEXT
                                    }
                                }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
@Composable
fun GradeCard(grade: Grade, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круг с оценкой
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (grade.score == "?") {
                            Brush.radialGradient(listOf(Color.LightGray, Color.Gray))
                        } else {
                            Brush.radialGradient(listOf(grade.color, grade.color.copy(alpha = 0.5f)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = grade.score,
                    color = if (grade.score == "?") Color.DarkGray else Color.White,
                    fontSize = if (grade.score.length > 5) 14.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Название предмета (чуть больше)
            Text(
                text = grade.subject,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            // Тип оценки справа
            Text(
                text = grade.type,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = grade.color
            )
        }
    }
}
@Composable
fun DayCellWithDeadlines(
    day: Int,
    dayDate: LocalDate,
    isToday: Boolean,
    deadlines: List<Deadline>,
    modifier: Modifier = Modifier
) {
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isPast = dayDate < todayDate
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isToday -> Color(0xFF2196F3).copy(alpha = 0.9f)
                    isPast && deadlines.isNotEmpty() -> Color.LightGray.copy(alpha = 0.3f)
                    deadlines.isNotEmpty() -> deadlines.first().color.copy(alpha = 0.3f)
                    else -> Color.White
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = when {
                    isToday -> Color.White
                    isPast -> Color.Gray
                    deadlines.isNotEmpty() -> deadlines.first().color
                    else -> Color.Black
                },
                fontSize = 14.sp,
                fontWeight = if (isToday || deadlines.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
            )

            // Цветные точки дедлайнов (все типы)
            deadlines.take(3).forEach { deadline ->
                Box(
                    modifier = Modifier
                        .size(6.dp) // Увеличил размер точек
                        .padding(top = 1.dp)
                        .clip(CircleShape)
                        .background(deadline.color)
                )
            }
                if (dayDate.monthNumber == 4 && day == 10) {
                    Text("🔥", fontSize = 10.sp)
                }
                if (dayDate.monthNumber == 4 && day == 18) {
                    Text("📚", fontSize = 10.sp)
                }

        }
    }
}

@Composable
fun DayCell(day: Int, isToday: Boolean, hasDeadline: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isToday) Color(0xFF2196F3)
                else if (hasDeadline) Color(0xFFFFEB3B).copy(alpha = 0.3f)
                else Color.Transparent
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isToday) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isToday || hasDeadline) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FiltersRow(
    hwEnabled: Boolean, onHwToggle: () -> Unit,
    quizEnabled: Boolean, onQuizToggle: () -> Unit,
    labEnabled: Boolean, onLabToggle: () -> Unit,
    projectEnabled: Boolean, onProjectToggle: () -> Unit,
    onTimetableClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton("H/W", hwEnabled, Color(0xFF4CAF50), onHwToggle)
            FilterButton("Quiz", quizEnabled, Color(0xFF2196F3), onQuizToggle)
            FilterButton("Lab", labEnabled, Color(0xFFFF9800), onLabToggle)

            // 👇 ШИРОКАЯ кнопка
            FilterButton(
                text = "Project Work",
                isActive = projectEnabled,
                color = Color(0xFF9C27B0),
                onClick = onProjectToggle,
                modifier = Modifier.width(130.dp)
            )
        }
    }
}

@Composable
fun FilterButton(text: String,
                     isActive: Boolean,
                     color: Color,
                     onClick: () -> Unit,
                     modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) color else Color.LightGray.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isActive) Color.White else Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeadlinesList(deadlines: List<Deadline>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()  // ← Замени weight(1f) на это
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(deadlines) { deadline ->
            DeadlineCard(deadline)
        }
    }
}




@Composable
fun DeadlineCard(deadline: Deadline) {
    val hoursLeft = calculateHoursLeft(deadline.date, deadline.time)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(deadline.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = deadline.subject.first().uppercase(),
                    color = deadline.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deadline.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = deadline.title,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(hoursLeft.toInt()),
                    color = if (hoursLeft < 24) Color.Red else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class Deadline(
    val title: String,
    val subject: String,
    val date: LocalDate,
    val time: String,
    val type: String,
    val color: Color
)
@Composable
fun calculateHoursLeft(deadlineDate: LocalDate, deadlineTime: String): Float {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDate = currentTime.date
    val currentHour = currentTime.hour
    val currentMinute = currentTime.minute

    // Парсим время дедлайна (23:00 → 23 часа)
    val deadlineHour = deadlineTime.substringBefore(":").toInt()

    val deadlineTotalHours = (deadlineDate.dayOfYear - currentDate.dayOfYear) * 24f +
            (deadlineHour - currentHour) +
            (60f - currentMinute) / 60f

    return deadlineTotalHours.coerceAtLeast(0f)
}

data class Grade(
    val subject: String,
    val type: String,
    val score: String,
    val color: Color
)
val gradesByWeek = mapOf(
    "Week 5" to listOf(
        Grade("Calculus 2", "H/W", "?", Color.Gray),
        Grade("Physics 2", "Quiz", "?", Color.Gray),
        Grade("OOP", "Lab", "82/100", Color(0xFFFF9800)),
        Grade("Physics 2", "H/W", "87/100", Color(0xFF4CAF50))
    ),
    "Week 4" to listOf(
        Grade("Physics 2", "H/W", "95/100", Color(0xFF4CAF50)),
        Grade("OOP", "Quiz", "38/40", Color(0xFF2196F3))
    ),
    "Week 3" to listOf(
        Grade("Physics 2", "Quiz", "32/40", Color(0xFF2196F3)),
        Grade("OOP", "Lab", "88/100", Color(0xFFFF9800))
    ),
    "Week 2" to listOf(
        Grade("Physics 2", "Lab", "92/100", Color(0xFFFF9800)),
        Grade("OOP", "H/W", "78/100", Color(0xFF4CAF50))
    ),
    "Week 1" to listOf(
        Grade("Physics 2", "H/W", "85/100", Color(0xFF4CAF50)),
        Grade("OOP", "Quiz", "36/40", Color(0xFF2196F3)),
        // 🔽 НОВЫЕ

    )
)
@Composable
fun GradesScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val sortedWeeks = gradesByWeek.keys
            .sortedByDescending { it.removePrefix("Week ").toInt() }

        sortedWeeks.forEach { week ->
            val grades = gradesByWeek[week] ?: emptyList()
            item {
                Text(
                    text = week,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(grades) { grade ->
                GradeCard(grade)
            }
        }
    }
}

