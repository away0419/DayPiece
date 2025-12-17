package com.example.daypiece.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypiece.model.ScheduleItem
import kotlin.math.cos
import kotlin.math.sin

/**
 * 읽기 전용 원형 시간표 (일정 추가 화면용)
 */
@Composable
fun CircularDayViewReadOnly(
    schedules: List<ScheduleItem>,
    selectedTimeRange: Pair<Int, Int>?,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val outerRadius = minOf(centerX, centerY) - 10f
            val borderWidth = 2.5.dp.toPx()
            val innerRadius = outerRadius - borderWidth

            // 외곽 원형 배경 (밝은 회색)
            drawCircle(
                color = Color.LightGray,
                radius = outerRadius,
                center = Offset(centerX, centerY)
            )

            // 내부 배경 (약간 더 진한 회색)
            drawCircle(
                color = Color(0xFFF3F4F6),
                radius = innerRadius,
                center = Offset(centerX, centerY)
            )

            // 기존 일정 섹터 그리기
            schedules.forEach { schedule ->
                drawScheduleSector(
                    centerX = centerX,
                    centerY = centerY,
                    radius = innerRadius,
                    schedule = schedule
                )
            }
            
            // 선택 중인 시간 범위 표시 (반투명)
            selectedTimeRange?.let { (start, end) ->
                drawSelectedTimeRange(
                    centerX = centerX,
                    centerY = centerY,
                    radius = innerRadius,
                    startMinute = start,
                    endMinute = end,
                    color = selectedColor
                )
            }

            // 항상 보이는 외곽 흰색 테두리
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 시간 표시선 그리기 (24시간)
            for (hour in 0..23) {
                val angle = Math.toRadians((hour * 15.0) - 90.0)
                val startX = centerX + (innerRadius - 8) * cos(angle).toFloat()
                val startY = centerY + (innerRadius - 8) * sin(angle).toFloat()
                val endX = centerX + innerRadius * cos(angle).toFloat()
                val endY = centerY + innerRadius * sin(angle).toFloat()

                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * 일정 섹터 그리기
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScheduleSector(
    centerX: Float,
    centerY: Float,
    radius: Float,
    schedule: ScheduleItem
) {
    val startMinutes = schedule.startHour * 60 + schedule.startMinute
    val endMinutes = schedule.endHour * 60 + schedule.endMinute

    var startAngle = (startMinutes * 360f / 1440f) - 90f
    var endAngle = (endMinutes * 360f / 1440f) - 90f

    if (endMinutes <= startMinutes) {
        endAngle += 360f
    }

    startAngle = ((startAngle % 360f) + 360f) % 360f
    endAngle = ((endAngle % 360f) + 360f) % 360f

    val sweepAngle = if (endAngle >= startAngle) {
        endAngle - startAngle
    } else {
        endAngle + 360f - startAngle
    }.coerceAtLeast(0.1f)

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
            sweepAngleDegrees = sweepAngle,
            forceMoveTo = false
        )
        lineTo(centerX, centerY)
        close()
    }

    // 섹터 그리기
    drawPath(
        path = path,
        color = schedule.color.copy(alpha = 0.4f)
    )

    // 흰색 테두리
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * 선택 중인 시간 범위 표시
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSelectedTimeRange(
    centerX: Float,
    centerY: Float,
    radius: Float,
    startMinute: Int,
    endMinute: Int,
    color: Color
) {
    // 각도 계산: 0분 = -90도(12시)
    var startAngle = (startMinute * 360f / 1440f) - 90f
    var endAngle = (endMinute * 360f / 1440f) - 90f
    
    // 종료 시간이 시작 시간보다 작거나 같으면 자정을 넘어간 경우
    if (endMinute <= startMinute) {
        endAngle += 360f
    }
    
    // 각도를 0-360도 범위로 정규화 (중요!)
    startAngle = ((startAngle % 360f) + 360f) % 360f
    endAngle = ((endAngle % 360f) + 360f) % 360f
    
    // 스윕 각도 계산
    val sweepAngle = if (endAngle >= startAngle) {
        endAngle - startAngle
    } else {
        endAngle + 360f - startAngle
    }.coerceAtLeast(0.1f)

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
            sweepAngleDegrees = sweepAngle,
            forceMoveTo = false
        )
        lineTo(centerX, centerY)
        close()
    }

    // 선택 영역 표시 (선택한 색상)
    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f)
    )
    
    // 흰색 테두리
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 2.5.dp.toPx())
    )
}

