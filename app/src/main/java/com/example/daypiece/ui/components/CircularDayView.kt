package com.example.daypiece.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.cos
import kotlin.math.sin
import java.util.Calendar

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
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "circular_view_animation"
    )

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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isToday) 1f else 0.5f),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${dates[index]}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isToday) 1f else 0.7f),
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
    val startAngle = timeToAngle(schedule.startHour, schedule.startMinute)
    val endAngle = timeToAngle(schedule.endHour, schedule.endMinute)
    
    val progress = (animatedProgress * 1000 - delay).coerceIn(0f, 1000f) / 1000f
    val animatedEndAngle = startAngle + (endAngle - startAngle) * progress
    
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
            sweepAngleDegrees = animatedEndAngle - startAngle,
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
    val progress = (animatedProgress * 1000 - delay).coerceIn(0f, 1000f) / 1000f
    if (progress < 0.7f) return
    
    val startAngle = timeToAngle(schedule.startHour, schedule.startMinute)
    val endAngle = timeToAngle(schedule.endHour, schedule.endMinute)
    val middleAngle = (startAngle + endAngle) / 2f
    
    // 섹터의 각도 범위 계산
    val sweepAngle = endAngle - startAngle
    val sweepAngleRad = Math.toRadians(sweepAngle.toDouble() / 2.0)
    
    // 텍스트가 섹터를 벗어나지 않도록 최대 너비 계산
    val maxTextWidth = 2 * radius * sin(sweepAngleRad).toFloat() * 0.7f
    val calculatedTextSize = (maxTextWidth / schedule.title.length).coerceIn(10f, 14f)
    
    // 텍스트 길이 제한
    val maxChars = (maxTextWidth / (calculatedTextSize * density * 0.55f)).toInt().coerceAtMost(schedule.title.length)
    val text = if (schedule.title.length > maxChars) {
        schedule.title.take(maxChars - 3) + "..."
    } else {
        schedule.title
    }
    
    // 텍스트 위치 계산 (섹터의 중간 지점, 반지름의 60% 지점)
    val textRadius = radius * 0.6f
    val angleRad = Math.toRadians(middleAngle.toDouble())
    val textX = centerX + textRadius * cos(angleRad).toFloat()
    val textY = centerY + textRadius * sin(angleRad).toFloat()
    
    // 텍스트 그리기
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            val alpha = (0.95f * 255).toInt()
            val red = (onSurfaceColor.red * 255).toInt()
            val green = (onSurfaceColor.green * 255).toInt()
            val blue = (onSurfaceColor.blue * 255).toInt()
            color = android.graphics.Color.argb(alpha, red, green, blue)
            textSize = calculatedTextSize * density
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.NORMAL
            )
            isAntiAlias = true
        }
        
        canvas.nativeCanvas.save()
        
        // 텍스트 위치로 이동
        canvas.nativeCanvas.translate(textX, textY)
        
        // 섹터의 중간 각도에 맞춰 텍스트 회전 (섹터 아크를 따라)
        canvas.nativeCanvas.rotate(middleAngle + 90f)
        
        // 텍스트 그리기 (중앙 정렬)
        canvas.nativeCanvas.drawText(text, 0f, 0f, paint)
        
        canvas.nativeCanvas.restore()
    }
}

/**
 * 시간과 분을 각도로 변환
 */
private fun timeToAngle(hour: Int, minute: Int): Float {
    val totalMinutes = hour * 60 + minute
    return (totalMinutes * 360f / 1440f) - 90f
}
