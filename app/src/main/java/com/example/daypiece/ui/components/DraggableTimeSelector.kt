package com.example.daypiece.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.daypiece.model.ScheduleItem
import kotlin.math.*

/**
 * 드래그 가능한 시간 선택 타일 테두리 컴포넌트
 * 
 * @param selectedTimeRange 선택된 시간 범위 (시작분, 종료분)
 * @param onTimeRangeChange 시간 범위 변경 콜백
 * @param existingSchedules 기존 일정 목록 (선택 불가 영역)
 * @param modifier Modifier
 */
@Composable
fun DraggableTimeSelector(
    selectedTimeRange: Pair<Int, Int>?, // (startMinutes, endMinutes)
    onTimeRangeChange: (Pair<Int, Int>?) -> Unit,
    existingSchedules: List<ScheduleItem>,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragAnchorMinute by remember { mutableStateOf<Int?>(null) } // 고정된 드래그 시작점 (앵커)
    var lastDragMinute by remember { mutableStateOf<Int?>(null) } // 이전 드래그 위치
    var accumulatedMinutes by remember { mutableStateOf(0) } // 누적 드래그 거리 (분)
    
    // 기존 일정의 시간 범위를 분 단위로 변환
    val occupiedRanges = remember(existingSchedules) {
        existingSchedules.map { schedule ->
            val start = schedule.startHour * 60 + schedule.startMinute
            val end = schedule.endHour * 60 + schedule.endMinute
            start to end
        }
    }
    
    Box(
        modifier = modifier
            .size(400.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val result = offsetToMinuteWithAngle(offset, size.width.toFloat(), size.height.toFloat())
                        val minute = result.first
                        if (minute != null && !isMinuteOccupied(minute, occupiedRanges)) {
                            isDragging = true
                            dragAnchorMinute = minute // 앵커 고정
                            lastDragMinute = minute // 이전 위치 초기화
                            accumulatedMinutes = 0 // 누적 거리 초기화
                            onTimeRangeChange(minute to minute)
                        }
                    },
                    onDrag = { change, _ ->
                        if (isDragging && dragAnchorMinute != null && lastDragMinute != null) {
                            val result = offsetToMinuteWithAngle(
                                change.position,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                            val currentMinute = result.first
                            
                            if (currentMinute != null) {
                                // 이전 위치에서 현재 위치까지의 증분 계산
                                val prevMinute = lastDragMinute!!
                                var deltaMinutes = currentMinute - prevMinute
                                
                                // 0도(12시) 경계를 넘는 경우 처리
                                if (deltaMinutes > 720) {
                                    // 예: 1430 -> 10 = 1420, 실제로는 -1420 (반시계)
                                    deltaMinutes -= 1440
                                } else if (deltaMinutes < -720) {
                                    // 예: 10 -> 1430 = -1420, 실제로는 +20 (시계)
                                    deltaMinutes += 1440
                                }
                                
                                // 누적 거리 업데이트
                                accumulatedMinutes += deltaMinutes
                                lastDragMinute = currentMinute
                                
                                // 누적 거리로 방향 판단
                                val isClockwise = accumulatedMinutes >= 0
                                
                                // 실제 종료 위치 계산 (앵커 + 누적 거리)
                                val anchor = dragAnchorMinute!!
                                var endMinute = anchor + accumulatedMinutes
                                
                                // 범위를 0-1439로 정규화
                                endMinute = ((endMinute % 1440) + 1440) % 1440
                                
                                // 시작/종료 결정
                                val (start, end) = if (isClockwise) {
                                    // 시계방향: 앵커가 시작, 계산된 위치가 종료
                                    anchor to endMinute
                                } else {
                                    // 반시계방향: 계산된 위치가 시작, 앵커가 종료
                                    endMinute to anchor
                                }
                                
                                // 기존 일정과의 겹침 확인 및 제한
                                val (validStart, validEnd) = getValidRange(
                                    start = start,
                                    end = end,
                                    anchor = anchor,
                                    isClockwise = isClockwise,
                                    occupiedRanges = occupiedRanges
                                )
                                
                                if (validEnd != validStart) {
                                    onTimeRangeChange(validStart to validEnd)
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        dragAnchorMinute = null
                        lastDragMinute = null
                        accumulatedMinutes = 0
                        // 드래그 종료 시 5분 단위로 스냅
                        selectedTimeRange?.let { (start, end) ->
                            // 시작: 내림 (0~4 → 0, 5~9 → 5)
                            val snappedStart = (start / 5) * 5
                            // 종료: 올림 (1~5 → 5, 6~10 → 10)
                            val snappedEnd = ((end + 4) / 5) * 5
                            
                            // 최소 5분 보장
                            val finalEnd = if (snappedEnd <= snappedStart) {
                                snappedStart + 5
                            } else {
                                snappedEnd
                            }.coerceAtMost(1439) // 1440분(24시)을 넘지 않도록
                            
                            onTimeRangeChange(snappedStart to finalEnd)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        dragAnchorMinute = null
                        lastDragMinute = null
                        accumulatedMinutes = 0
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val outerRadius = minOf(centerX, centerY) - 20f
            val tileWidth = 25f // 타일 두께
            val innerRadius = outerRadius - tileWidth
            
            // 288개의 5분 타일 (24시간 * 12)
            for (i in 0 until 288) {
                val startMinute = i * 5
                val endMinute = startMinute + 5
                val startAngle = (startMinute * 360f / 1440f) - 90f
                val fullSweepAngle = 5 * 360f / 1440f
                // 간격을 주기 위해 실제 타일은 약간 작게 (80%)
                val sweepAngle = fullSweepAngle * 0.8f
                val gapAngle = fullSweepAngle * 0.2f
                
                // 타일 색상 결정
                val tileColor = when {
                    // 선택된 범위 (타일 범위와 선택 범위의 겹침 체크)
                    selectedTimeRange != null && isTileInRange(
                        startMinute,
                        endMinute,
                        selectedTimeRange.first,
                        selectedTimeRange.second
                    ) -> {
                        Color(0xFF424242) // 다크 그레이
                    }
                    // 기존 일정이 있는 영역
                    isRangeOccupied(startMinute, endMinute, occupiedRanges) -> {
                        Color(0xFF616161) // 회색 (선택 불가)
                    }
                    // 선택 가능한 영역
                    else -> {
                        Color(0xFFE0E0E0) // 밝은 회색
                    }
                }
                
                // 타일 그리기 (원형 섹터, 간격 포함)
                drawArc(
                    color = tileColor,
                    startAngle = startAngle + gapAngle / 2,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = tileWidth)
                )
            }
        }
    }
}

/**
 * 화면 좌표를 시간(분)과 각도로 변환
 */
private fun offsetToMinuteWithAngle(offset: Offset, width: Float, height: Float): Pair<Int?, Float?> {
    val centerX = width / 2
    val centerY = height / 2
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    
    // 각도 계산 (0도 = 12시)
    var angle = atan2(dy, dx) * 180 / PI.toFloat()
    angle = (angle + 90f + 360f) % 360f
    
    // 각도를 분으로 변환
    val minute = (angle * 1440f / 360f).toInt()
    return minute.coerceIn(0, 1439) to angle
}

/**
 * 타일 범위가 선택 범위와 겹치는지 확인 (자정 넘는 경우 고려)
 */
private fun isTileInRange(tileStart: Int, tileEnd: Int, rangeStart: Int, rangeEnd: Int): Boolean {
    return if (rangeStart <= rangeEnd) {
        // 일반 범위: 타일과 선택 범위의 겹침 체크
        // [tileStart, tileEnd) 와 [rangeStart, rangeEnd) 겹침
        tileStart < rangeEnd && tileEnd > rangeStart
    } else {
        // 자정을 넘는 범위: [rangeStart, 1440) ∪ [0, rangeEnd)
        // 타일이 두 범위 중 하나라도 겹치면 true
        (tileStart < 1440 && tileEnd > rangeStart) || (tileStart < rangeEnd)
    }
}

/**
 * 특정 분이 기존 일정에 포함되는지 확인
 */
private fun isMinuteOccupied(minute: Int, occupiedRanges: List<Pair<Int, Int>>): Boolean {
    return occupiedRanges.any { (start, end) ->
        // 기존 일정의 끝 시간도 occupied로 간주 (겹침 방지)
        minute >= start && minute < end
    }
}

/**
 * 시간 범위가 기존 일정과 겹치는지 확인
 */
private fun isRangeOccupied(start: Int, end: Int, occupiedRanges: List<Pair<Int, Int>>): Boolean {
    return occupiedRanges.any { (occStart, occEnd) ->
        // 범위가 겹치는 경우
        (start < occEnd && end > occStart)
    }
}

/**
 * 유효한 드래그 범위 계산 (앵커 기준, 기존 일정 고려)
 */
private fun getValidRange(
    start: Int,
    end: Int,
    anchor: Int,
    isClockwise: Boolean,
    occupiedRanges: List<Pair<Int, Int>>
): Pair<Int, Int> {
    // 앵커가 기존 일정 안에 있으면 드래그 불가
    if (isMinuteOccupied(anchor, occupiedRanges)) {
        return anchor to anchor
    }
    
    // 앵커에서 현재 위치까지 순회하면서 기존 일정을 만나면 그 지점에서 멈춤
    var validStart = start
    var validEnd = end
    
    if (isClockwise) {
        // 시계방향: anchor → end 방향으로 확인
        // 자정을 넘는지 확인
        val crossesMidnight = end < anchor
        
        if (crossesMidnight) {
            // 자정을 넘는 경우: anchor ~ 1439, 0 ~ end
            var foundBlocker = false
            
            // anchor부터 자정까지 확인
            for (minute in anchor until 1440) {
                if (isMinuteOccupied(minute, occupiedRanges) && minute != anchor) {
                    // 자정 전에 기존 일정을 만남: 해당 지점이 종료
                    validEnd = minute
                    foundBlocker = true
                    break
                }
            }
            
            // 차단되지 않았으면 자정 이후도 확인
            if (!foundBlocker) {
                for (minute in 0..end) {
                    if (isMinuteOccupied(minute, occupiedRanges)) {
                        // 자정 이후에 기존 일정을 만남: 해당 지점이 종료 (end 값 유지)
                        validEnd = minute
                        foundBlocker = true
                        break
                    }
                }
            }
            
            // 기존 일정을 만나지 않았다면 end 값 그대로 유지
            // validEnd는 이미 end로 초기화되어 있음
        } else {
            // 일반적인 경우: anchor ~ end
            for (minute in anchor..end) {
                if (isMinuteOccupied(minute, occupiedRanges) && minute != anchor) {
                    validEnd = minute
                    break
                }
            }
        }
    } else {
        // 반시계방향: anchor → start 역방향으로 확인
        // 자정을 넘는지 확인
        val crossesMidnight = start > anchor
        
        if (crossesMidnight) {
            // 자정을 넘는 경우 (역방향): anchor ~ 0, 1439 ~ start
            var foundBlocker = false
            
            // anchor부터 0까지 역방향 확인
            for (minute in anchor downTo 0) {
                if (isMinuteOccupied(minute, occupiedRanges) && minute != anchor) {
                    validStart = minute + 1
                    foundBlocker = true
                    break
                }
            }
            
            // 차단되지 않았으면 자정 이전도 확인 (1439 ~ start)
            if (!foundBlocker) {
                for (minute in 1439 downTo start) {
                    if (isMinuteOccupied(minute, occupiedRanges)) {
                        validStart = minute + 1
                        if (validStart >= 1440) validStart = 0
                        break
                    }
                }
            }
        } else {
            // 일반적인 경우: anchor → start 역방향
            for (minute in anchor downTo start) {
                if (isMinuteOccupied(minute, occupiedRanges) && minute != anchor) {
                    validStart = minute + 1
                    break
                }
            }
        }
    }
    
    return validStart to validEnd
}

