package com.example.daypiece.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypiece.model.ScheduleItem
import kotlinx.coroutines.launch

/**
 * 스크롤 가능한 TimePicker 컴포넌트
 * 
 * @param label 라벨 ("시작 시간" 또는 "종료 시간")
 * @param selectedHour 선택된 시간
 * @param selectedMinute 선택된 분
 * @param onTimeChange 시간 변경 콜백
 * @param existingSchedules 기존 일정 (제한용)
 * @param isStartTime 시작 시간 여부
 * @param otherTime 다른 시간 (시작 시간이면 종료 시간, 반대도 마찬가지)
 * @param modifier Modifier
 */
@Composable
fun ScrollableTimePicker(
    label: String,
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChange: (Int, Int) -> Unit,
    existingSchedules: List<ScheduleItem>,
    isStartTime: Boolean,
    otherTime: Pair<Int, Int>?, // (hour, minute)
    modifier: Modifier = Modifier
) {
    // 순환 스크롤을 위한 큰 아이템 수 (시간: 24 * 1000, 분: 12 * 1000)
    val hourItemCount = 24 * 1000
    val minuteItemCount = 12 * 1000
    
    // 중간에서 시작
    val hourInitialIndex = hourItemCount / 2 + selectedHour
    val minuteInitialIndex = minuteItemCount / 2 + (selectedMinute / 5)
    
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hourInitialIndex)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minuteInitialIndex)
    val coroutineScope = rememberCoroutineScope()
    
    // 선택 가능한 시간 범위 계산
    val availableRange = remember(existingSchedules, isStartTime, otherTime) {
        calculateAvailableTimeRange(
            existingSchedules = existingSchedules,
            isStartTime = isStartTime,
            otherTime = otherTime
        )
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 시간 선택 (순환 구조)
            Box(modifier = Modifier.width(60.dp).height(150.dp)) {
                LazyColumn(
                    state = hourListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 50.dp), // 중앙 정렬을 위한 패딩
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(hourItemCount) { index ->
                        val hour = index % 24
                        // 중앙에 있는 항목 계산 (contentPadding 50dp 고려)
                        val layoutInfo = hourListState.layoutInfo
                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
                        val centerItem = layoutInfo.visibleItemsInfo.minByOrNull {
                            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                        }
                        // 실제 시간 값으로 선택 여부 판단
                        val centerHour = if (centerItem != null) centerItem.index % 24 else selectedHour
                        val isSelected = hour == centerHour
                        
                        val isAvailable = isTimeAvailable(
                            hour = hour,
                            minute = if (isSelected) selectedMinute else 0,
                            availableRange = availableRange
                        )
                        
                        Text(
                            text = String.format("%02d", hour),
                            fontSize = if (isSelected) 28.sp else 20.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                !isAvailable -> Color.Gray.copy(alpha = 0.3f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
            }
            
            Text(
                text = ":",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // 분 선택 (5분 단위, 순환 구조)
            Box(modifier = Modifier.width(60.dp).height(150.dp)) {
                LazyColumn(
                    state = minuteListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 50.dp), // 중앙 정렬을 위한 패딩
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(minuteItemCount) { index ->
                        val minute = (index % 12) * 5
                        // 중앙에 있는 항목 계산 (contentPadding 50dp 고려)
                        val layoutInfo = minuteListState.layoutInfo
                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
                        val centerItem = layoutInfo.visibleItemsInfo.minByOrNull {
                            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                        }
                        // 실제 분 값으로 선택 여부 판단
                        val centerMinute = if (centerItem != null) (centerItem.index % 12) * 5 else selectedMinute
                        val isSelected = minute == centerMinute
                        
                        val isAvailable = isTimeAvailable(
                            hour = selectedHour,
                            minute = minute,
                            availableRange = availableRange
                        )
                        
                        Text(
                            text = String.format("%02d", minute),
                            fontSize = if (isSelected) 28.sp else 20.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                !isAvailable -> Color.Gray.copy(alpha = 0.3f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
    
    // 스크롤 위치 감지 및 시간 업데이트
    LaunchedEffect(Unit) {
        snapshotFlow { 
            hourListState.isScrollInProgress to minuteListState.isScrollInProgress
        }.collect { (hourScrolling, minuteScrolling) ->
            // 스크롤이 멈췄을 때만 처리
            if (!hourScrolling && !minuteScrolling) {
                val hourLayoutInfo = hourListState.layoutInfo
                val minuteLayoutInfo = minuteListState.layoutInfo
                
                val hourViewportCenter = hourLayoutInfo.viewportStartOffset + hourLayoutInfo.viewportSize.height / 2
                val hourCenterItem = hourLayoutInfo.visibleItemsInfo.minByOrNull {
                    kotlin.math.abs((it.offset + it.size / 2) - hourViewportCenter)
                }
                
                val minuteViewportCenter = minuteLayoutInfo.viewportStartOffset + minuteLayoutInfo.viewportSize.height / 2
                val minuteCenterItem = minuteLayoutInfo.visibleItemsInfo.minByOrNull {
                    kotlin.math.abs((it.offset + it.size / 2) - minuteViewportCenter)
                }
                
                val newHour = (hourCenterItem?.index ?: hourInitialIndex) % 24
                val newMinute = ((minuteCenterItem?.index ?: minuteInitialIndex) % 12) * 5
                
                // 선택 가능한 시간인지 확인
                if (isTimeAvailable(newHour, newMinute, availableRange)) {
                    if (newHour != selectedHour || newMinute != selectedMinute) {
                        onTimeChange(newHour, newMinute)
                    }
                } else {
                    // 선택 불가능한 시간이면 가까운 유효한 시간으로 이동
                    val validTime = findNearestValidTime(newHour, newMinute, availableRange)
                    val validHourIndex = hourItemCount / 2 + validTime.first
                    val validMinuteIndex = minuteItemCount / 2 + (validTime.second / 5)
                    hourListState.animateScrollToItem(validHourIndex)
                    minuteListState.animateScrollToItem(validMinuteIndex)
                    onTimeChange(validTime.first, validTime.second)
                }
            }
        }
    }
    
    // 선택된 시간으로 초기화 (드래그 연동)
    LaunchedEffect(selectedHour, selectedMinute) {
        if (!hourListState.isScrollInProgress && !minuteListState.isScrollInProgress) {
            val currentHourCenter = hourListState.layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - 
                    (hourListState.layoutInfo.viewportStartOffset + hourListState.layoutInfo.viewportSize.height / 2))
            }
            val currentMinuteCenter = minuteListState.layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - 
                    (minuteListState.layoutInfo.viewportStartOffset + minuteListState.layoutInfo.viewportSize.height / 2))
            }
            
            val currentHour = (currentHourCenter?.index ?: hourInitialIndex) % 24
            val currentMinute = ((currentMinuteCenter?.index ?: minuteInitialIndex) % 12) * 5
            
            if (currentHour != selectedHour) {
                val targetIndex = hourItemCount / 2 + selectedHour
                hourListState.scrollToItem(targetIndex)
            }
            if (currentMinute != selectedMinute) {
                val targetIndex = minuteItemCount / 2 + (selectedMinute / 5)
                minuteListState.scrollToItem(targetIndex)
            }
        }
    }
}

/**
 * 선택 가능한 시간 범위 계산 (24시를 넘는 경우 고려)
 */
private fun calculateAvailableTimeRange(
    existingSchedules: List<ScheduleItem>,
    isStartTime: Boolean,
    otherTime: Pair<Int, Int>?
): List<Pair<Int, Int>> {
    // 기존 일정을 분 단위로 변환
    val occupiedMinutes = mutableSetOf<Int>()
    existingSchedules.forEach { schedule ->
        val start = schedule.startHour * 60 + schedule.startMinute
        val end = schedule.endHour * 60 + schedule.endMinute
        for (minute in start until end) {
            occupiedMinutes.add(minute)
        }
    }
    
    if (isStartTime) {
        // 시작 시간: 기존 일정과 겹치지 않는 모든 시간 선택 가능
        val availableRanges = mutableListOf<Pair<Int, Int>>()
        var rangeStart = 0
        
        for (minute in 0..1440) {
            if (minute == 1440 || occupiedMinutes.contains(minute)) {
                // 최소 1분 이상의 범위만 추가
                if (minute > rangeStart) {
                    availableRanges.add(rangeStart to minute)
                }
                rangeStart = minute + 1
            }
        }
        return availableRanges
    } else {
        // 종료 시간: 시작 시간을 기준으로 다음 일정 시작 전까지
        if (otherTime == null) {
            return emptyList()
        }
        
        val startMinutes = otherTime.first * 60 + otherTime.second
        val availableRanges = mutableListOf<Pair<Int, Int>>()
        
        // 시작 시간 이후부터 다음 날 시작 시간 전까지 확인 (최대 24시간)
        var rangeStart = startMinutes
        
        for (offset in 1..1440) {
            val currentMinute = (startMinutes + offset) % 1440
            
            if (occupiedMinutes.contains(currentMinute)) {
                // 기존 일정을 만나면 그 전까지가 선택 가능 범위
                if (offset > 1) {
                    val rangeEnd = (startMinutes + offset) % 1440
                    // 자정을 넘는 경우를 처리하기 위해 분리
                    if (rangeStart < 1440 && rangeEnd < rangeStart) {
                        // 자정을 넘는 경우: 두 개의 범위로 분리
                        availableRanges.add(rangeStart to 1440)
                        // 빈 범위가 아닌 경우만 추가
                        if (rangeEnd > 0) {
                            availableRanges.add(0 to rangeEnd)
                        }
                    } else if (rangeEnd > rangeStart) {
                        // 최소 1분 이상의 범위만 추가
                        availableRanges.add(rangeStart to rangeEnd)
                    }
                }
                break
            }
            
            // 전체 24시간을 순회했으면 종료 (occupied가 없는 경우)
            if (offset == 1440) {
                val rangeEnd = (startMinutes + 1440) % 1440
                if (rangeEnd == rangeStart) {
                    // 전체 24시간이 available (한 바퀴 돌아서 시작점으로 돌아옴)
                    if (rangeStart > 0) {
                        // 자정을 넘는 경우: rangeStart ~ 1440, 0 ~ rangeStart
                        availableRanges.add(rangeStart to 1440)
                        availableRanges.add(0 to rangeStart)
                    } else {
                        // rangeStart가 0인 경우: 0 ~ 1440 전체
                        availableRanges.add(0 to 1440)
                    }
                } else if (rangeStart < 1440 && rangeEnd < rangeStart) {
                    // 자정을 넘는 경우
                    availableRanges.add(rangeStart to 1440)
                    // 빈 범위가 아닌 경우만 추가
                    if (rangeEnd > 0) {
                        availableRanges.add(0 to rangeEnd)
                    }
                } else if (rangeEnd > rangeStart) {
                    // 최소 1분 이상의 범위만 추가
                    availableRanges.add(rangeStart to rangeEnd)
                }
            }
        }
        
        return availableRanges
    }
}

/**
 * 특정 시간이 선택 가능한지 확인
 */
private fun isTimeAvailable(
    hour: Int,
    minute: Int,
    availableRange: List<Pair<Int, Int>>
): Boolean {
    val totalMinutes = hour * 60 + minute
    return availableRange.any { (start, end) ->
        totalMinutes in start until end
    }
}

/**
 * 가장 가까운 유효한 시간 찾기
 */
private fun findNearestValidTime(
    hour: Int,
    minute: Int,
    availableRange: List<Pair<Int, Int>>
): Pair<Int, Int> {
    val totalMinutes = hour * 60 + minute
    
    // 가장 가까운 범위 찾기
    val nearestRange = availableRange.minByOrNull { (start, end) ->
        when {
            totalMinutes < start -> start - totalMinutes
            totalMinutes >= end -> totalMinutes - end + 1
            else -> 0 // 범위 내에 있음
        }
    }
    
    return if (nearestRange != null) {
        // 범위가 유효한지 확인 (최소 1분 이상)
        val rangeEnd = nearestRange.second - 1
        if (rangeEnd < nearestRange.first) {
            // 빈 범위인 경우 범위의 시작 시간 반환
            (nearestRange.first / 60) to (nearestRange.first % 60)
        } else {
            val validMinute = totalMinutes.coerceIn(nearestRange.first, rangeEnd)
            (validMinute / 60) to (validMinute % 60)
        }
    } else {
        // 사용 가능한 범위가 없는 경우 현재 시간 그대로 반환
        hour to minute
    }
}

