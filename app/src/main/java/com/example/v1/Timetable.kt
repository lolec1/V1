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

data class Lesson(
    val subject: String,
    val teacher: String,
    val room: String,
    val startTime: String,
    val endTime: String,
    val color: Color,
)
/*
fun getWeekSchedule(startDate: LocalDate): Map<LocalDate, List<Lesson>> {
    return (0..6).associate { offset ->
        val date = startDate.plus(offset, DateTimeUnit.DAY)

        val lessons = when (offset) {
            0 -> listOf(
                Lesson("Physics 2", "Atamurodov Farrukh", "A607", "09:30", "11:00", Color(0xFF2196F3), "Thu"),
                Lesson("AE", "Niyazkulova Rano", "A308", "11:00", "12:30", Color(0xFF4CAF50), "Thu"),
                Lesson("Break", "", "", "12:30", "13:30", Color(0xFFE57373), "Thu"),
                Lesson("Calculus 2", "Safarov Utkir", "B202", "13:30", "15:00", Color(0xFFFFC107), "Thu")
            )
            else -> emptyList()
        }

        date to lessons
    }
}

 */
@Composable
fun TimetableScreen() {
    val startDate = LocalDate(2026, 3, 19)
    val schedule = remember { generateWeekSchedule(startDate) }

    var selectedDate by remember { mutableStateOf(startDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F9))
            .padding(top = 16.dp)
    ) {
        // 📅 Месяц и год
        Text(
            text = "${monthName(startDate.month.value)} ${startDate.year}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Дни недели
        WeekHeader(
            startDate = startDate,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(28.dp)) // сдвигаем карточки пар ниже

        val lessons = schedule[selectedDate] ?: emptyList()

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lessons) { lesson ->
                LessonCard(lesson)
            }
        }
    }
}

// Вспомогательная функция для названия месяца
fun monthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }
}

@Composable
fun WeekHeader(
    startDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = (0..6).map { startDate.plus(it, DateTimeUnit.DAY) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { date ->
                val isSelected = date == selectedDate

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDateSelected(date) }
                        .background(
                            if (isSelected) Color(0xFF1565C0)
                            else Color.Transparent
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 12.sp
                    )

                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
fun LessonCard(lesson: Lesson) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Иконка
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(lesson.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lesson.subject.take(1),
                    color = lesson.color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Текст
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (lesson.subject != "Break") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(lesson.teacher, Color(0xFF4CAF50))
                        Chip(lesson.room, Color(0xFFF44336))
                    }
                }
            }

            // Время
            Column(horizontalAlignment = Alignment.End) {
                TimeBubble(lesson.startTime)
                Spacer(modifier = Modifier.height(6.dp))
                TimeBubble(lesson.endTime)
            }
        }
    }
}
@Composable
fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}
@Composable
fun TimeBubble(time: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE3F2FD))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(time, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
fun TimetableScreenWithBack(onBack: () -> Unit) {
    val startDate = LocalDate(2026, 3, 19)

    val schedule = remember { generateWeekSchedule(startDate) }

    var selectedDate by remember { mutableStateOf(startDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F9))
    ) {

        // 🔙 Назад
        Text(
            text = "← Back",
            modifier = Modifier
                .clickable { onBack() }
                .padding(16.dp)
        )

        // 📅 Дни недели (как на скрине)
        WeekHeader(
            startDate = startDate,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val lessons = schedule[selectedDate] ?: emptyList()

        // 📚 Карточки (как раньше)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lessons) { lesson ->
                LessonCard(lesson)
            }
        }
    }
}


fun randomRoom(): String {
    val building = listOf("A", "B").random()

    val floor = if (building == "A") (1..7).random() else (1..4).random()
    val room = (1..10).random()

    return "$building$floor${room.toString().padStart(2, '0')}"
}
val subjects = listOf(
    "Calculus 2" to "Safarov Utkir",
    "Physics 2" to "Atamurodov Farrukh",
    "OOP" to "Suvanov Sharof",
    "CED" to "Abdullaev Sarvar",
    "AE" to "Niyazkulova Rano",
    "TWD" to "Saydasheva Angelina"
)
val lessonTimes = listOf(
    "08:30 - 09:50",
    "10:00 - 11:20",
    "11:30 - 12:50",
    "13:30 - 14:50",
    "15:00 - 16:20"
)
fun generateWeekSchedule(startDate: LocalDate): Map<LocalDate, List<Lesson>> {
    return (0..6).associate { offset ->
        val date = startDate.plus(offset, DateTimeUnit.DAY)

        val lessonsPerDay = (2..4).random()
        val usedTimes = lessonTimes.shuffled().take(lessonsPerDay)

        val lessons = usedTimes.map { time ->
            val (subject, teacher) = subjects.random()
            val start = time.substringBefore(" - ")
            val end = time.substringAfter(" - ")
            val color = when (subject) {
                "Calculus 2" -> Color(0xFFFFC107)
                "Physics 2" -> Color(0xFF2196F3)
                "OOP" -> Color(0xFF9C27B0)
                "CED" -> Color(0xFF4CAF50)
                "AE" -> Color(0xFFFF5722)
                "TWD" -> Color(0xFF009688)
                else -> Color.Gray
            }

            Lesson(subject, teacher, randomRoom(), start, end, color)
        }

        date to lessons
    }
}