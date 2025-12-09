package com.example.daypiece.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypiece.model.ScheduleItem
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.toArgb


/**
 * 이미지와 동일한 디자인의 원형 일간표 뷰
 */
@Composable
fun CircularDayView(
    schedules: List<ScheduleItem>,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "circular_view_animation"
    )
    
    // 현재 시간을 추적하여 분침을 움직이게 함
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000) // 1초마다 업데이트
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp)
    ) {
        // 상단 헤더: "2024년 5월"과 "주간" 드롭다운
        MonthHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        // 요일/날짜 행
        WeekHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 원형 시간표
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(340.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val outerRadius = minOf(centerX, centerY) - 50f
                // 흰색 테두리 크기(2.5dp)만큼만 간격 두기
                val borderWidth = 2.5.dp.toPx()
                val innerRadius = outerRadius - borderWidth // 시간 텍스트 전까지

                // 외곽 원형 배경 (연한 회색)
                drawCircle(
                    color = Color(0xFFE5E7EB),
                    radius = outerRadius,
                    center = Offset(centerX, centerY)
                )

                // 내부 배경 (약간 더 진한 회색)
                drawCircle(
                    color = Color(0xFFF3F4F6),
                    radius = innerRadius,
                    center = Offset(centerX, centerY)
                )

                // 일정 섹터 그리기
                schedules.forEachIndexed { index, schedule ->
                    drawScheduleSector(
                        centerX = centerX,
                        centerY = centerY,
                        radius = innerRadius,
                        schedule = schedule,
                        animatedProgress = animatedProgress,
                        delay = index * 100
                    )
                }

                // 시간 표시선 그리기
                for (hour in 0..23) {
                    drawHourMarker(
                        centerX = centerX,
                        centerY = centerY,
                        radius = innerRadius,
                        hour = hour,
                        animatedProgress = animatedProgress,
                        onSurfaceColor = onSurfaceColor
                    )
                }

                // 분 표시선 그리기
                for (hour in 0..23) {
                    for (minute in listOf(15, 30, 45)) {
                        drawMinuteMarker(
                            centerX = centerX,
                            centerY = centerY,
                            radius = innerRadius,
                            hour = hour,
                            minute = minute,
                            animatedProgress = animatedProgress,
                            onSurfaceColor = onSurfaceColor
                        )
                    }
                }
                
                // 현재 시간 분침 그리기
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = currentTimeMillis
                }
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)
                
                drawCurrentTimeHand(
                    centerX = centerX,
                    centerY = centerY,
                    radius = innerRadius,
                    hour = currentHour,
                    minute = currentMinute,
                    onSurfaceColor = onSurfaceColor
                )
            }

            // 시간 텍스트 표시 (원형 가장자리 가까이, 진하게)
            // 흰색 테두리 크기(2.5dp)만큼만 간격 두기
            val borderWidthPx = 2.5.dp.value
            for (hour in 0..23) {
                val angle = Math.toRadians((hour * 15.0) - 90.0)
                val radius = 170.dp.value * animatedProgress
                val textX = (radius - borderWidthPx) * cos(angle).toFloat() // 흰색 테두리 크기만큼만
                val textY = (radius - borderWidthPx) * sin(angle).toFloat()

                Text(
                    text = "${hour}",
                    color = onSurfaceColor.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = textX.dp, y = textY.dp)
                )
            }

            // 일정 텍스트를 섹터 안에 중앙에 배치 (섹터 아크를 따라 회전)
            Canvas(
                modifier = Modifier
                    .size(340.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val outerRadius = minOf(centerX, centerY) - 50f
                // 흰색 테두리 크기(2.5dp)만큼만 간격 두기
                val borderWidth = 2.5.dp.toPx()
                val innerRadius = outerRadius - borderWidth // 시간 텍스트 전까지

                schedules.forEachIndexed { index, schedule ->
                    drawScheduleText(
                        centerX = centerX,
                        centerY = centerY,
                        radius = innerRadius,
                        schedule = schedule,
                        animatedProgress = animatedProgress,
                        delay = index * 100,
                        onSurfaceColor = onSurfaceColor
                    )
                }
            }
        }

        // 하단 일정 목록
        ScheduleList(
            schedules = schedules,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 상단 헤더: "2024년 5월"과 "주간" 드롭다운
 */
@Composable
fun MonthHeader(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${year}년 ${month}월",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box {
            Text(
                text = "주간",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(8.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("주간") },
                    onClick = { expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("일간") },
                    onClick = { expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("월간") },
                    onClick = { expanded = false }
                )
            }
        }
    }
}

/**
 * 요일/날짜 헤더
 */
@Composable
fun WeekHeader(
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_WEEK)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        val dates = (0..6).map { offset ->
            val dayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, offset - (today - Calendar.MONDAY))
            }
            dayCalendar.get(Calendar.DAY_OF_MONTH)
        }

        days.forEachIndexed { index, day ->
            val isToday = index == (today - Calendar.MONDAY)
            // 토요일(index=5)은 파란색, 일요일(index=6)은 빨간색
            val dayColor = when (index) {
                5 -> Color(0xFF2196F3) // 토요일 - 파란색
                6 -> Color(0xFFE53935) // 일요일 - 빨간색
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = if (isToday) 1f else 0.5f)
            }
            
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = dayColor,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${dates[index]}",
                    fontSize = 16.sp,
                    color = dayColor,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * 하단 일정 목록
 */
@Composable
fun ScheduleList(
    schedules: List<ScheduleItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // 헤더: "일정 6"
        Text(
            text = "일정 ${schedules.size}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 일정 목록
        schedules.forEach { schedule ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 시간
                Text(
                    text = String.format(
                        "%02d:%02d",
                        schedule.startHour,
                        schedule.startMinute
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(50.dp)
                )

                // 색상 인디케이터 (세로선)
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(
                            schedule.color,
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 제목과 설명
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = schedule.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // 설명은 ScheduleItem에 없으므로 생략
                }

                // 검색 아이콘
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 시간 표시선 그리기
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHourMarker(
    centerX: Float,
    centerY: Float,
    radius: Float,
    hour: Int,
    animatedProgress: Float,
    onSurfaceColor: Color
) {
    val angle = Math.toRadians((hour * 15.0) - 90.0)
    val animatedRadius = radius * animatedProgress

    val startX = centerX + (animatedRadius - 8) * cos(angle).toFloat()
    val startY = centerY + (animatedRadius - 8) * sin(angle).toFloat()
    val endX = centerX + animatedRadius * cos(angle).toFloat()
    val endY = centerY + animatedRadius * sin(angle).toFloat()

    drawLine(
        color = onSurfaceColor.copy(alpha = 0.3f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/**
 * 분 표시선 그리기
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMinuteMarker(
    centerX: Float,
    centerY: Float,
    radius: Float,
    hour: Int,
    minute: Int,
    animatedProgress: Float,
    onSurfaceColor: Color
) {
    val totalMinutes = hour * 60 + minute
    val angle = Math.toRadians((totalMinutes * 360.0 / 1440.0) - 90.0)
    val animatedRadius = radius * animatedProgress

    val startX = centerX + (animatedRadius - 4) * cos(angle).toFloat()
    val startY = centerY + (animatedRadius - 4) * sin(angle).toFloat()
    val endX = centerX + (animatedRadius - 1) * cos(angle).toFloat()
    val endY = centerY + (animatedRadius - 1) * sin(angle).toFloat()

    drawLine(
        color = onSurfaceColor.copy(alpha = 0.15f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 0.8.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/**
 * 현재 시간을 나타내는 화살표 인디케이터 그리기
 * 원형 테두리와 시간 텍스트 사이의 빈 공간에 화살표만 표시
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurrentTimeHand(
    centerX: Float,
    centerY: Float,
    radius: Float,
    hour: Int,
    minute: Int,
    onSurfaceColor: Color
) {
    // 현재 시간을 분 단위로 계산
    val totalMinutes = hour * 60 + minute
    // 각도 계산 (0분 = -90도(12시), 360분 = 0도(3시), 시계방향)
    val angle = Math.toRadians((totalMinutes * 360.0 / 1440.0) - 90.0)
    
    // 화살표 위치: 원형 테두리(radius)와 시간 텍스트(약 170dp) 사이의 빈 공간
    // 화살표 끝점 (중심쪽을 가리킴)
    val arrowTipRadius = radius + 8f // 원형 테두리 바로 안쪽
    val arrowTipX = centerX + arrowTipRadius * cos(angle).toFloat()
    val arrowTipY = centerY + arrowTipRadius * sin(angle).toFloat()
    
    // 화살표 크기
    val arrowSize = 18.dp.toPx() // 화살표 높이
    val arrowWidth = 16.dp.toPx() // 화살표 밑변 너비
    
    // 화살표 방향 (중심을 향하도록)
    val arrowAngle = angle + Math.PI // 180도 회전 (안쪽을 가리키도록)
    
    // 화살표 밑변의 왼쪽 점
    val leftAngle = arrowAngle + Math.toRadians(90.0)
    val arrowLeft1X = arrowTipX + arrowSize * cos(arrowAngle).toFloat() + (arrowWidth / 2) * cos(leftAngle).toFloat()
    val arrowLeft1Y = arrowTipY + arrowSize * sin(arrowAngle).toFloat() + (arrowWidth / 2) * sin(leftAngle).toFloat()
    
    // 화살표 밑변의 오른쪽 점
    val rightAngle = arrowAngle - Math.toRadians(90.0)
    val arrowRight1X = arrowTipX + arrowSize * cos(arrowAngle).toFloat() + (arrowWidth / 2) * cos(rightAngle).toFloat()
    val arrowRight1Y = arrowTipY + arrowSize * sin(arrowAngle).toFloat() + (arrowWidth / 2) * sin(rightAngle).toFloat()
    
    // 화살표 Path 생성 (삼각형)
    val arrowPath = Path().apply {
        moveTo(arrowTipX, arrowTipY) // 화살표 끝점
        lineTo(arrowLeft1X, arrowLeft1Y) // 왼쪽 점
        lineTo(arrowRight1X, arrowRight1Y) // 오른쪽 점
        close()
    }
    
    // 화살표 그림자 효과 (약간 큰 검은색 화살표)
    drawPath(
        path = arrowPath,
        color = Color.Black.copy(alpha = 0.2f)
    )
    
    // 화살표 그리기 (밝은 빨간색)
    drawPath(
        path = arrowPath,
        color = Color(0xFFFF5252)
    )
    
    // 화살표 테두리 (흰색으로 강조)
    drawPath(
        path = arrowPath,
        color = Color.White,
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * 일정을 섹터(채워진 영역) 형태로 그리기 - 단색, 흰색 테두리
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScheduleSector(
    centerX: Float,
    centerY: Float,
    radius: Float,
    schedule: ScheduleItem,
    animatedProgress: Float,
    delay: Int = 0
) {
    // 시작 시간과 종료 시간을 분 단위로 계산
    val startMinutes = schedule.startHour * 60 + schedule.startMinute
    val endMinutes = schedule.endHour * 60 + schedule.endMinute
    
    // 각도 계산: 0분 = -90도(12시), 360분 = 0도(3시), 720분 = 90도(6시), 1080분 = 180도(9시)
    var startAngle = (startMinutes * 360f / 1440f) - 90f
    var endAngle = (endMinutes * 360f / 1440f) - 90f
    
    // 종료 시간이 시작 시간보다 작거나 같으면 자정을 넘어간 경우이므로 360도를 더함
    // 분 단위로 비교하는 것이 더 정확함
    if (endMinutes <= startMinutes) {
        endAngle += 360f
    }
    
    // 각도를 0-360도 범위로 정규화 (arcTo가 음수 각도를 제대로 처리하지 못할 수 있음)
    startAngle = ((startAngle % 360f) + 360f) % 360f
    endAngle = ((endAngle % 360f) + 360f) % 360f
    
    // 스윕 각도 계산 (항상 양수, 0-360도 범위 내에서)
    val sweepAngle = if (endAngle >= startAngle) {
        endAngle - startAngle
    } else {
        endAngle + 360f - startAngle
    }.coerceAtLeast(0.1f) // 최소 0.1도 보장

    // 애니메이션 진행도 계산 (delay를 고려하되, 최종적으로는 1.0이 되도록)
    val totalDuration = 1000f
    val delayedStart = delay.toFloat()
    val animationDuration = totalDuration - delayedStart
    
    val progress = if (animatedProgress * totalDuration < delayedStart) {
        0f // 아직 시작 안 함
    } else {
        ((animatedProgress * totalDuration - delayedStart) / animationDuration).coerceIn(0f, 1f)
    }
    
    val animatedSweepAngle = sweepAngle * progress

    val path = Path().apply {
        moveTo(centerX, centerY)
        lineTo(
            centerX + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat(),
            centerY + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
        )
        arcTo(
            rect = Rect(
                center = Offset(centerX, centerY),
                radius = radius
            ),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = animatedSweepAngle,
            forceMoveTo = false
        )
        lineTo(centerX, centerY)
        close()
    }

    // 섹터 그리기 (단색)
    drawPath(
        path = path,
        color = schedule.color.copy(alpha = 0.4f) // 단색, 반투명
    )

    // 흰색 테두리 그리기
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * 일정 텍스트를 섹터 안에 중앙에 배치 (섹터 아크를 따라 회전)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScheduleText(
    centerX: Float,
    centerY: Float,
    radius: Float,
    schedule: ScheduleItem,
    animatedProgress: Float,
    delay: Int = 0,
    onSurfaceColor: Color
) {
    // 애니메이션 진행도 계산 (delay를 고려하되, 최종적으로는 1.0이 되도록)
    val totalDuration = 1000f
    val delayedStart = delay.toFloat()
    val animationDuration = totalDuration - delayedStart
    
    val progress = if (animatedProgress * totalDuration < delayedStart) {
        0f // 아직 시작 안 함
    } else {
        ((animatedProgress * totalDuration - delayedStart) / animationDuration).coerceIn(0f, 1f)
    }
    
    if (progress < 0.7f) return

    // 시작 시간과 종료 시간을 분 단위로 계산
    val startMinutes = schedule.startHour * 60 + schedule.startMinute
    val endMinutes = schedule.endHour * 60 + schedule.endMinute
    
    // 각도 계산
    val startAngle = (startMinutes * 360f / 1440f) - 90f
    var endAngle = (endMinutes * 360f / 1440f) - 90f
    
    // 종료 시간이 시작 시간보다 작거나 같으면 자정을 넘어간 경우이므로 360도를 더함
    // 분 단위로 비교하는 것이 더 정확함
    if (endMinutes <= startMinutes) {
        endAngle += 360f
    }

    // 섹터 중간 각도 계산 (0-360도 범위로 정규화)
    val middleAngle = ((startAngle + endAngle) / 2f).let { angle ->
        if (angle < 0) angle + 360f else if (angle >= 360f) angle - 360f else angle
    }
    val middleRad = Math.toRadians(middleAngle.toDouble())

    // 중심에서 밖으로 향하는 단위 벡터
    val dirX = cos(middleRad).toFloat()
    val dirY = sin(middleRad).toFloat()

    // 텍스트를 시작할 반지름 (중앙에서 얼마나 떨어져서 첫 줄을 그릴지)
    val startDistance = radius * 0.5f

    // 섹터 각도로부터 한 줄에 들어갈 최대 너비 산출
    // 섹터의 sweep 절반을 사용해 대략적인 내부 폭 계산
    // endAngle은 이미 자정을 넘어가는 경우 360도가 더해져 있으므로 단순히 차이만 계산
    val sweepAngle = endAngle - startAngle
    val sweepHalfRad = Math.toRadians((sweepAngle / 2f).toDouble())
    val approxMaxWidth = (2 * radius * sin(sweepHalfRad)).toFloat() * 0.7f
    val maxTextWidth = approxMaxWidth.coerceAtLeast(30f) // 최소 보장

    // 기본 텍스트 크기(디바이스 밀도 보정)
    val baseTextSizePx = (14f * density).coerceIn(10f * density, 18f * density)

    // native Paint
    val paint = android.graphics.Paint().apply {
        color = onSurfaceColor.toArgb()
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = baseTextSizePx
    }

    // 텍스트를 한 줄로만 표시 (줄바꿈 없음)
    fun measureTextWidth(s: String): Float = paint.measureText(s)
    
    // 텍스트가 너무 길면 자르기
    var text = schedule.title
    if (measureTextWidth(text) > maxTextWidth) {
        var truncated = text
        while (measureTextWidth("$truncated...") > maxTextWidth && truncated.isNotEmpty()) {
            truncated = truncated.dropLast(1)
        }
        text = if (truncated.isEmpty()) "..." else "$truncated..."
    }

    // 텍스트 위치 계산 (섹터의 중간 지점)
    val px = centerX + dirX * startDistance
    val py = centerY + dirY * startDistance

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        native.save()
        
        // 텍스트 위치로 이동
        native.translate(px, py)
        
        // 섹터의 중간 각도 방향으로 텍스트 회전
        // timeToAngle: -90도 = 12시, 0도 = 3시, 90도 = 6시, 180도 = 9시
        // Android Canvas rotate: 0도 = 오른쪽(3시), 90도 = 아래쪽(6시), -90도 = 위쪽(12시)
        // middleAngle을 그대로 사용하면 섹터와 동일한 각도로 회전됨
        native.rotate(middleAngle)

        // 세로 중심 보정: paint.descent()를 이용
        val textOffsetY = (paint.descent() + paint.ascent()) / 2f * -1f

        // drawText는 baseline 기준이므로 작은 조정 필요
        native.drawText(text, 0f, textOffsetY, paint)

        native.restore()
    }
}


/**
 * 시간과 분을 각도로 변환
 */
private fun timeToAngle(hour: Int, minute: Int): Float {
    val totalMinutes = hour * 60 + minute
    return (totalMinutes * 360f / 1440f) - 90f
}
