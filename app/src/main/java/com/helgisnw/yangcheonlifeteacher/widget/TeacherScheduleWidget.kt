package com.helgisnw.yangcheonlifeteacher.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.helgisnw.yangcheonlifeteacher.R
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import com.helgisnw.yangcheonlifeteacher.data.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TeacherScheduleWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(200.dp, 110.dp), // medium
            DpSize(320.dp, 280.dp)  // large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiState = withContext(Dispatchers.Default) { buildUiState(context) }
        provideContent {
            TeacherScheduleWidgetContent(uiState)
        }
    }

    private suspend fun buildUiState(context: Context): TeacherScheduleWidgetUiState {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val teacherId = prefs.getString("selectedTeacherId", null)
        val teacherName = prefs.getString("selectedTeacherName", null)

        if (teacherId.isNullOrBlank() || teacherName.isNullOrBlank()) {
            return TeacherScheduleWidgetUiState.Message(
                title = context.getString(R.string.widget_missing_teacher_title),
                body = context.getString(R.string.widget_missing_teacher_body)
            )
        }

        val repository = ScheduleRepository()
        val scheduleData = repository.getTeacherSchedule(teacherId).getOrNull()
            ?: return TeacherScheduleWidgetUiState.Message(
                title = context.getString(R.string.widget_update_error_title),
                body = context.getString(R.string.widget_update_error_body)
            )

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        val dayIndex = dayOfWeek.value - 1 // Monday = 1

        val todaysSchedule = scheduleData.getOrNull(dayIndex)
        val periodRows = buildPeriodRows(prefs, todaysSchedule)

        val nextClass = if (!isWeekend) {
            buildNextClassInfo(prefs, scheduleData, dayIndex, today)
        } else {
            null
        }

        val headerFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val headerDate = headerFormatter.format(today)

        val infoMessage = when {
            isWeekend -> context.getString(R.string.widget_weekend_message)
            periodRows.all { it.subject.isNullOrBlank() } -> context.getString(R.string.widget_no_lessons_today)
            else -> null
        }

        val nextClassFallback = when {
            isWeekend -> context.getString(R.string.widget_weekend_message)
            nextClass == null -> context.getString(R.string.widget_all_done_message)
            else -> null
        }

        return TeacherScheduleWidgetUiState.Content(
            headerDate = headerDate,
            teacherName = context.getString(R.string.widget_teacher_suffix_format, teacherName),
            periodRows = periodRows,
            nextClass = nextClass,
            infoMessage = infoMessage,
            nextClassFallbackMessage = nextClassFallback,
            nextClassLabel = context.getString(R.string.widget_next_class_label),
            inProgressLabel = context.getString(R.string.widget_in_progress_label),
            noUpcomingLabel = context.getString(R.string.widget_no_upcoming_class),
            emptyPeriodLabel = context.getString(R.string.widget_empty_period)
        )
    }

    private fun buildPeriodRows(
        prefs: SharedPreferences,
        todaysSchedule: List<ScheduleItem>?
    ): List<WidgetPeriodRow> {
        val rows = mutableListOf<WidgetPeriodRow>()
        val now = LocalTime.now()

        for (period in 1..PERIOD_TIMES.size) {
            val item = todaysSchedule?.getOrNull(period - 1)
            val displayValues = item?.let { resolveDisplayValues(prefs, it) }
            val subject = displayValues?.first
            val destination = displayValues?.second
            val isCurrent = isCurrentPeriod(now, period)
            rows.add(
                WidgetPeriodRow(
                    period = period,
                    subject = subject,
                    destination = destination,
                    isCurrent = isCurrent
                )
            )
        }
        return rows
    }

    private fun buildNextClassInfo(
        prefs: SharedPreferences,
        scheduleData: List<List<ScheduleItem>>,
        todayIndex: Int,
        today: LocalDate
    ): WidgetNextClass? {
        val now = LocalDateTime.now()

        val todaysLessons = scheduleData.getOrNull(todayIndex).orEmpty()
        for (period in 1..PERIOD_TIMES.size) {
            val lesson = todaysLessons.getOrNull(period - 1) ?: continue
            val (subject, destination) = resolveDisplayValues(prefs, lesson)
            if (subject.isNullOrBlank()) continue

            val (start, end) = PERIOD_TIMES[period - 1]
            val startDateTime = LocalDateTime.of(today, start)
            val endDateTime = LocalDateTime.of(today, end)

            val status = when {
                now.isBefore(startDateTime) -> NextClassStatus.UPCOMING
                now.isAfter(endDateTime) -> continue
                now.isAfter(startDateTime) && now.isBefore(endDateTime) -> NextClassStatus.ONGOING
                else -> NextClassStatus.UPCOMING
            }

            val timeLabel = when (status) {
                NextClassStatus.UPCOMING -> formatTimeRemaining(now, startDateTime)
                NextClassStatus.ONGOING -> null
            }

            val durationLabel = if (status == NextClassStatus.ONGOING) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                "${formatter.format(start)} ~ ${formatter.format(end)}"
            } else null

            return WidgetNextClass(
                period = period,
                subject = subject,
                destination = destination,
                status = status,
                startTime = start,
                endTime = end,
                timeLabel = timeLabel,
                durationLabel = durationLabel
            )
        }
        return null
    }

    private fun resolveDisplayValues(
        prefs: SharedPreferences,
        item: ScheduleItem
    ): Pair<String, String?> {
        var subject = item.subject
        var location = item.teacher

        if (subject.contains("반")) {
            val key = "selected${subject}Subject"
            val selected = prefs.getString(key, null)
            if (!selected.isNullOrBlank() && selected != "선택 없음" && selected != subject) {
                val components = selected.split("/")
                if (components.size == 2) {
                    subject = components[0]
                    location = components[1]
                }
            }
        }

        val classLabel = formatClassLabel(item.grade, item.`class`)
        val destination = when {
            !classLabel.isNullOrBlank() -> classLabel
            !location.isNullOrBlank() -> location
            else -> null
        }

        return subject to destination
    }

    private fun formatClassLabel(grade: Int, classNumber: Int): String? {
        return if (grade in 1..3 && classNumber in 1..12) {
            "${grade}-${classNumber}반"
        } else {
            null
        }
    }

    private fun isCurrentPeriod(now: LocalTime, period: Int): Boolean {
        val (start, end) = PERIOD_TIMES.getOrNull(period - 1) ?: return false
        return now.isAfter(start.minusMinutes(1)) && now.isBefore(end.plusMinutes(1))
    }

    private fun formatTimeRemaining(now: LocalDateTime, startDateTime: LocalDateTime): String? {
        val duration = Duration.between(now, startDateTime)
        if (duration.isNegative) return null

        val totalMinutes = duration.toMinutes()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return if (hours > 0) {
            "${hours}시간 ${minutes}분 남음"
        } else {
            "${minutes}분 남음"
        }
    }

    companion object {
        private val PERIOD_TIMES = listOf(
            LocalTime.of(8, 20) to LocalTime.of(9, 10),
            LocalTime.of(9, 20) to LocalTime.of(10, 10),
            LocalTime.of(10, 20) to LocalTime.of(11, 10),
            LocalTime.of(11, 20) to LocalTime.of(12, 10),
            LocalTime.of(13, 10) to LocalTime.of(14, 0),
            LocalTime.of(14, 10) to LocalTime.of(15, 0),
            LocalTime.of(15, 10) to LocalTime.of(16, 0)
        )
    }
}

private sealed interface TeacherScheduleWidgetUiState {
    data class Content(
        val headerDate: String,
        val teacherName: String,
        val periodRows: List<WidgetPeriodRow>,
        val nextClass: WidgetNextClass?,
        val infoMessage: String?,
        val nextClassFallbackMessage: String?,
        val nextClassLabel: String,
        val inProgressLabel: String,
        val noUpcomingLabel: String,
        val emptyPeriodLabel: String
    ) : TeacherScheduleWidgetUiState

    data class Message(
        val title: String,
        val body: String
    ) : TeacherScheduleWidgetUiState
}

private data class WidgetPeriodRow(
    val period: Int,
    val subject: String?,
    val destination: String?,
    val isCurrent: Boolean
)

private data class WidgetNextClass(
    val period: Int,
    val subject: String,
    val destination: String?,
    val status: NextClassStatus,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val timeLabel: String?,
    val durationLabel: String?
)

enum class NextClassStatus {
    UPCOMING,
    ONGOING
}

@Composable
@OptIn(ExperimentalGlanceApi::class)
private fun TeacherScheduleWidgetContent(state: TeacherScheduleWidgetUiState) {
    val size = LocalSize.current
    val isLarge = size.height >= 220.dp

    when (state) {
        is TeacherScheduleWidgetUiState.Content -> {
            if (isLarge) {
                LargeWidgetContent(state)
            } else {
                MediumWidgetContent(state)
            }
        }

        is TeacherScheduleWidgetUiState.Message -> {
            CenteredMessage(state.title, state.body)
        }
    }
}

@Composable
private fun LargeWidgetContent(state: TeacherScheduleWidgetUiState.Content) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(16.dp),
    ) {
        Text(
            text = state.headerDate,
            style = TextStyle(
                fontWeight = FontWeight.Medium, 
                fontSize = 16.sp,
                color = ColorProvider(R.color.widget_text_primary)
            )
        )
        Text(
            text = state.teacherName,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                textAlign = TextAlign.End,
                color = ColorProvider(R.color.widget_text_primary)
            )
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            state.periodRows.forEach { row ->
                Text(
                    text = row.period.toString(),
                    modifier = GlanceModifier.padding(horizontal = 4.dp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = if (row.isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (row.isCurrent) ColorProvider(R.color.md_theme_light_primary) else ColorProvider(R.color.widget_text_secondary),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (!state.infoMessage.isNullOrBlank() && state.periodRows.all { it.subject.isNullOrBlank() }) {
            CenteredMessage(title = state.infoMessage, body = "")
        } else {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                state.periodRows.forEach { row ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "${row.period}교시",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold, 
                                fontSize = 12.sp,
                                color = ColorProvider(R.color.widget_text_primary)
                            ),
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            Text(
                                text = row.subject ?: state.emptyPeriodLabel,
                                style = TextStyle(
                                    fontSize = 14.sp, 
                                    fontWeight = if (row.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = ColorProvider(R.color.widget_text_primary)
                                ),
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                            row.destination?.let {
                                Text(
                                    text = it,
                                    style = TextStyle(fontSize = 12.sp, color = ColorProvider(R.color.widget_text_secondary), textAlign = TextAlign.End),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediumWidgetContent(state: TeacherScheduleWidgetUiState.Content) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(16.dp),
    ) {
        Text(
            text = state.teacherName,
            style = TextStyle(
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium, 
                color = ColorProvider(R.color.widget_text_primary)
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = state.nextClassLabel,
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(R.color.widget_text_secondary))
        )
        Spacer(modifier = GlanceModifier.height(6.dp))

        val nextClass = state.nextClass
        if (nextClass != null) {
            Text(
                text = nextClass.subject,
                style = TextStyle(
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(R.color.widget_text_primary)
                )
            )
            nextClass.destination?.let {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = it,
                    style = TextStyle(fontSize = 12.sp, color = ColorProvider(R.color.widget_text_secondary))
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "${nextClass.period}교시",
                style = TextStyle(
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_text_primary)
                )
            )
            if (nextClass.status == NextClassStatus.ONGOING) {
                Text(
                    text = nextClass.durationLabel ?: state.inProgressLabel,
                    style = TextStyle(fontSize = 12.sp, color = ColorProvider(R.color.widget_text_secondary))
                )
            } else {
                Text(
                    text = nextClass.timeLabel ?: "",
                    style = TextStyle(
                        fontSize = 12.sp, 
                        color = ColorProvider(R.color.widget_text_primary)
                    )
                )
            }
        } else {
            Text(
                text = state.nextClassFallbackMessage ?: state.noUpcomingLabel,
                style = TextStyle(
                    fontSize = 14.sp, 
                    fontStyle = FontStyle.Italic,
                    color = ColorProvider(R.color.widget_text_secondary)
                )
            )
        }
    }
}

@Composable
private fun CenteredMessage(title: String, body: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium, 
                textAlign = TextAlign.Center,
                color = ColorProvider(R.color.widget_text_primary)
            )
        )
        if (body.isNotBlank()) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = body,
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(R.color.widget_text_secondary), textAlign = TextAlign.Center)
            )
        }
    }
}
