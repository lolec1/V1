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
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@Composable
fun NearestDeadlineCard(deadlines: List<Deadline>) {
    if (deadlines.isEmpty()) return

    val nearest = deadlines.minByOrNull { it.date }!!
    val hoursLeft = calculateHoursLeft(nearest.date, nearest.time)
    val progress = when {
        hoursLeft <= 72 -> 0.7f + (0.3f * (1f - hoursLeft / 72f)) // 3 дня = 70%
        else -> (1f - (hoursLeft / 168f).coerceIn(0f, 1f))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hoursLeft < 24) Color(0xFFF44336) else Color(0xFF2196F3)
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp).height(72.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(nearest.type, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Text(nearest.subject, fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.width(16.dp))
            ProgressCircle(progress = progress, hoursLeft = hoursLeft.toInt(), color = nearest.color)
        }
    }
}

@Composable
fun ProgressCircle(progress: Float, hoursLeft: Int, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    Box(
        modifier = Modifier
            .size(48.dp)
            .drawBehind {
                val strokeWidth = 6f
                val radius = (size.minDimension - strokeWidth) / 2
                val center = center

                // Фон круга
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(strokeWidth)
                )

                // Прогресс-дуга
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(radius - strokeWidth/2, radius - strokeWidth/2),
                    size = Size(strokeWidth * 2, strokeWidth * 2),
                    style = Stroke(strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${hoursLeft / 24}d ${hoursLeft % 24}h",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarApp() {
    // ← ВСЕ ПЕРЕМЕННЫЕ СНАЧАЛА
    var listContent by remember { mutableStateOf("deadlines") }
    var hwEnabled by remember { mutableStateOf(true) }
    var quizEnabled by remember { mutableStateOf(true) }
    var labEnabled by remember { mutableStateOf(true) }
    var examsEnabled by remember { mutableStateOf(true) }

    val deadlines = listOf(
        Deadline("Homework 2", "Physics 2", LocalDate(2026, 3, 18), "23:00", "H/W", Color(0xFF4CAF50)),
        Deadline("Quiz 2", "Physics 2", LocalDate(2026, 3, 20), "23:00", "Quiz", Color(0xFF2196F3)),
        Deadline("Quiz 2", "Physics 2", LocalDate(2026, 3, 22), "23:00", "Quiz", Color(0xFF2196F3)),
        Deadline("Laboratory", "OOP", LocalDate(2026, 3, 24), "23:00", "Lab", Color(0xFFFF9800)),
        Deadline("Laboratory", "OOP", LocalDate(2026, 3, 26), "23:00", "Lab", Color(0xFFFF9800)),
        Deadline("Final Exam", "Physics 2", LocalDate(2026, 3, 28), "23:00", "Exams", Color(0xFFF44336))
    )

    val gradesByWeek = mapOf(
        "Week 1" to listOf(Grade("Physics 2", "H/W", "A", Color(0xFF4CAF50)), Grade("OOP", "Quiz", "B+", Color(0xFF2196F3))),
        "Week 2" to listOf(Grade("Physics 2", "Lab", "A-", Color(0xFFFF9800)), Grade("OOP", "H/W", "A", Color(0xFF4CAF50))),
        "Week 3" to listOf(Grade("Physics 2", "Quiz", "B", Color(0xFF2196F3)), Grade("OOP", "Lab", "A", Color(0xFFFF9800))),
        "Week 4" to listOf(Grade("Physics 2", "H/W", "A+", Color(0xFF4CAF50)), Grade("OOP", "Quiz", "A-", Color(0xFF2196F3))),
        "Week 5" to listOf(Grade("Physics 2", "Exam", "A", Color(0xFFF44336)), Grade("OOP", "Lab", "B+", Color(0xFFFF9800)), Grade("Physics 2", "H/W", "A-", Color(0xFF4CAF50)))
    )

    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        val filteredDeadlines = deadlines.filter { deadline ->
        when (deadline.type) {
            "H/W" -> hwEnabled
            "Quiz" -> quizEnabled
            "Lab" -> labEnabled
            "Exams" -> examsEnabled
            else -> false
        }
    }.sortedBy { it.date.daysUntil(today) }

    // ← LazyColumn ПОСЛЕ всех переменных
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { TopHeader() }
        item { NearestDeadlineCard(filteredDeadlines) }
        item {
            FiltersRow(
                hwEnabled = hwEnabled,
                onHwToggle = { hwEnabled = !hwEnabled },
                quizEnabled = quizEnabled,
                onQuizToggle = { quizEnabled = !quizEnabled },
                labEnabled = labEnabled,
                onLabToggle = { labEnabled = !labEnabled },
                examsEnabled = examsEnabled,
                onExamsToggle = { examsEnabled = !examsEnabled }
            )
        }
        item { CalendarSection(today = today, deadlines = filteredDeadlines) }
        item {
            ContentIndicator(isDeadlinesMode = listContent == "deadlines")
        }
        item {
            ListContent(
                contentType = listContent,
                deadlines = filteredDeadlines,
                gradesByWeek = gradesByWeek,
                onSwipeRightToLeft = { listContent = "grades" },
                onSwipeLeftToRight = { listContent = "deadlines" }
            )
        }
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
                gradesByWeek.forEach { (week, grades) ->
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
    examsEnabled: Boolean, onExamsToggle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { NearestDeadlineCard(filteredDeadlines) }
        item {
            FiltersRow(hwEnabled, onHwToggle, quizEnabled, onQuizToggle, labEnabled, onLabToggle, examsEnabled, onExamsToggle)
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
    examsEnabled: Boolean, onExamsToggle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { TopHeader() }
        item { NearestDeadlineCard(filteredDeadlines) }
        item {
            FiltersRow(
                hwEnabled, onHwToggle,
                quizEnabled, onQuizToggle,
                labEnabled, onLabToggle,
                examsEnabled, onExamsToggle
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
            Text("Рахмон Санҷар", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Urgent deadlines", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CalendarSection(today: LocalDate, deadlines: List<Deadline>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Март 2026", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            CalendarGrid(today = today, deadlines = deadlines)
        }
    }
}

@Composable
fun CalendarGrid(today: LocalDate, deadlines: List<Deadline>) {
    val monthStart = LocalDate(2026, 3, 1) // 1 марта 2026 - понедельник
    val daysInMonth = 31
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
                        val dayDate = LocalDate(2026, 3, dayOfMonth)
                        val dayDeadlines = deadlines.filter { it.date == dayDate }

                        DayCellWithDeadlines(
                            day = dayOfMonth,
                            dayDate = dayDate,
                            isToday = dayDate == today,
                            deadlines = dayDeadlines,
                            modifier = Modifier.weight(1f)
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
fun DayCellWithDeadlines(
    day: Int,
    dayDate: LocalDate,
    isToday: Boolean,
    deadlines: List<Deadline>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isToday -> Color(0xFF2196F3).copy(alpha = 0.9f)
                    deadlines.isNotEmpty() -> {
                        // Заливка цветом первого дедлайна (или градиент если несколько)
                        val primaryColor = deadlines.first().color.copy(alpha = 0.3f)
                        primaryColor
                    }
                    else -> Color.White
                }
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = if (isToday) Color.White
                else if (deadlines.isNotEmpty()) deadlines.first().color
                else Color.Black,
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
    examsEnabled: Boolean, onExamsToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton("H/W", hwEnabled, Color(0xFF4CAF50), onHwToggle)
        FilterButton("Quiz", quizEnabled, Color(0xFF2196F3), onQuizToggle)
        FilterButton("Lab", labEnabled, Color(0xFFFF9800), onLabToggle)
        FilterButton("Exams", examsEnabled, Color(0xFFF44336), onExamsToggle)
    }
}

@Composable
fun FilterButton(text: String, isActive: Boolean, color: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) color.copy(alpha = 0.1f) else Color.White),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isActive) color else Color.Gray,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
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
    val daysLeft = (hoursLeft / 24f).toInt()
    val remainingHours = hoursLeft % 24f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(
                    brush = Brush.radialGradient(listOf(deadline.color, deadline.color.copy(alpha = 0.5f)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(deadline.type.take(1), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(deadline.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(deadline.subject, fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "${deadline.date.dayOfMonth.toString().padStart(2, '0')}.${deadline.date.monthNumber.toString().padStart(2, '0')}.${deadline.date.year} ${deadline.time}",
                    fontSize = 14.sp,
                    color = deadline.color,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$daysLeft d ${remainingHours.toInt()} h left",
                    fontSize = 12.sp,
                    color = if (hoursLeft < 24) Color(0xFFF44336) else deadline.color,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(deadline.date.dayOfMonth.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = deadline.color)
        }
    }
}
fun calculateHoursLeft(today: LocalDate, deadlineDate: LocalDate): Float {
    return (deadlineDate.dayOfYear - today.dayOfYear) * 24f +
            ((deadlineDate.dayOfMonth - today.dayOfMonth) * 24f).coerceAtLeast(0f)
}

data class Deadline(
    val title: String,
    val subject: String,
    val date: LocalDate,
    val time: String, // ← НОВОЕ ПОЛЕ
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
    "Week 1" to listOf(
        Grade("Physics 2", "H/W", "85/100", Color(0xFF4CAF50)),
        Grade("OOP", "Quiz", "36/40", Color(0xFF2196F3))
    ),
    "Week 2" to listOf(
        Grade("Physics 2", "Lab", "92/100", Color(0xFFFF9800)),
        Grade("OOP", "H/W", "78/100", Color(0xFF4CAF50))
    ),
    "Week 3" to listOf(
        Grade("Physics 2", "Quiz", "32/40", Color(0xFF2196F3)),
        Grade("OOP", "Lab", "88/100", Color(0xFFFF9800))
    ),
    "Week 4" to listOf(
        Grade("Physics 2", "H/W", "95/100", Color(0xFF4CAF50)),
        Grade("OOP", "Quiz", "38/40", Color(0xFF2196F3))
    ),
    "Week 5" to listOf(
        Grade("Physics 2", "Exam", "91/100", Color(0xFFF44336)),
        Grade("OOP", "Lab", "82/100", Color(0xFFFF9800)),
        Grade("Physics 2", "H/W", "87/100", Color(0xFF4CAF50))
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
fun GradeCard(grade: Grade) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
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
                        brush = Brush.radialGradient(
                            listOf(grade.color, grade.color.copy(alpha = 0.5f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = grade.score,
                    color = Color.White,
                    fontSize = 20.sp,
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