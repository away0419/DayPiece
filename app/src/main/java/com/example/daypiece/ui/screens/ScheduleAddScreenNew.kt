package com.example.daypiece.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypiece.model.ScheduleAttribute
import com.example.daypiece.model.ScheduleItem
import com.example.daypiece.ui.components.CircularDayViewReadOnly
import com.example.daypiece.ui.components.DraggableTimeSelector
import com.example.daypiece.ui.components.ScrollableTimePicker
import com.example.daypiece.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 새로운 일정 추가 화면
 * - 상단: 원형 시간표 + 드래그 가능한 타일 테두리
 * - 하단: TimePicker 스타일 시간 선택 + 일정 정보 입력
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAddScreenNew(
    selectedDate: Calendar,
    existingSchedules: List<ScheduleItem>,
    onSave: (ScheduleItem) -> Unit,
    onCancel: () -> Unit
) {
    // 입력 상태
    var title by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(0) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(0) }
    var endMinute by remember { mutableStateOf(10) }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(ScheduleBlue) }
    var isHabit by remember { mutableStateOf(false) }
    var hasAlarm by remember { mutableStateOf(false) }
    
    // 드래그로 선택된 시간 범위 (분 단위)
    var selectedTimeRange by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isUpdatingFromDrag by remember { mutableStateOf(false) }
    var isUpdatingFromPicker by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // 기존 일정을 분 단위로 변환
    val occupiedRanges = remember(existingSchedules) {
        existingSchedules.map { schedule ->
            val start = schedule.startHour * 60 + schedule.startMinute
            val end = schedule.endHour * 60 + schedule.endMinute
            start to end
        }
    }
    
    // 특정 시간이 기존 일정과 겹치는지 확인
    fun isTimeOccupied(minute: Int): Boolean {
        return occupiedRanges.any { (start, end) ->
            minute in start until end
        }
    }
    
    // 다음 일정 시작 시간 찾기 (자정 넘는 경우 고려)
    fun findNextScheduleStart(fromMinute: Int): Int? {
        // 같은 날 내에서 fromMinute 이후의 일정 찾기
        val sameDay = occupiedRanges
            .filter { (start, _) -> start > fromMinute }
            .minOfOrNull { (start, _) -> start }
        
        if (sameDay != null) {
            return sameDay
        }
        
        // 같은 날에 없으면 다음 날 (0시 이후) 일정 찾기
        return occupiedRanges
            .filter { (start, _) -> start < fromMinute }
            .minOfOrNull { (start, _) -> start }
    }
    
    // 드래그 범위가 변경되면 시작/종료 시간 업데이트
    LaunchedEffect(selectedTimeRange) {
        selectedTimeRange?.let { (start, end) ->
            if (!isUpdatingFromPicker) {
                isUpdatingFromDrag = true
                startHour = start / 60
                startMinute = start % 60
                endHour = end / 60
                endMinute = end % 60
                isUpdatingFromDrag = false
            }
        }
    }
    
    // 시작 시간이 변경되면 종료 시간 자동 조정 및 드래그 범위 업데이트
    LaunchedEffect(startHour, startMinute) {
        if (!isUpdatingFromDrag && isInitialized) {
            isUpdatingFromPicker = true
            
            val startMinutes = startHour * 60 + startMinute
            val endMinutes = endHour * 60 + endMinute
            
            // 시작 시간이 기존 일정과 겹치면 이전으로 되돌림
            if (isTimeOccupied(startMinutes)) {
                // 이전 값으로 복원하거나 가능한 시간으로 조정
                // 여기서는 단순히 무시
            } else {
                // 자정을 넘는지 확인
                val crossesMidnight = endMinutes < startMinutes
                
                // 종료 시간이 기존 일정을 넘어가는지 확인
                if (!crossesMidnight) {
                    // 같은 날 내에서 확인
                    val nextSchedule = findNextScheduleStart(startMinutes)
                    if (nextSchedule != null && endMinutes >= nextSchedule) {
                        // 다음 일정 전으로 종료 시간 조정
                        endHour = nextSchedule / 60
                        endMinute = nextSchedule % 60
                    }
                }
                
                selectedTimeRange = startMinutes to (endHour * 60 + endMinute)
            }
            
            isUpdatingFromPicker = false
        }
    }
    
    // 종료 시간이 변경되면 자동 조정 및 드래그 범위 업데이트
    LaunchedEffect(endHour, endMinute) {
        if (!isUpdatingFromDrag && isInitialized) {
            isUpdatingFromPicker = true
            
            val startMinutes = startHour * 60 + startMinute
            val endMinutes = endHour * 60 + endMinute
            
            // 종료 시간이 시작 시간보다 작으면 자정을 넘는 것으로 간주
            // 단, 드래그가 아닌 직접 입력일 때만 최소 10분 보장
            val crossesMidnight = endMinutes < startMinutes
            
            if (!crossesMidnight && endMinutes <= startMinutes) {
                // 같은 날 + 종료가 시작보다 작거나 같으면 최소 10분 보장
                endHour = startHour
                endMinute = startMinute + 10
                if (endMinute >= 60) {
                    endHour = (endHour + 1) % 24
                    endMinute = endMinute % 60
                }
            }
            
            // 종료 시간이 기존 일정과 겹치는지 확인
            val nextSchedule = findNextScheduleStart(startMinutes)
            if (nextSchedule != null && !crossesMidnight) {
                // 자정을 넘지 않는 경우에만 다음 일정 체크
                val proposedEndMinutes = endHour * 60 + endMinute
                if (proposedEndMinutes > nextSchedule) {
                    // 다음 일정을 넘어가면 다음 일정 시작 시간으로 조정
                    endHour = nextSchedule / 60
                    endMinute = nextSchedule % 60
                }
            }
            
            val adjustedEndMinutes = endHour * 60 + endMinute
            // 자정을 넘거나 종료가 시작보다 크면 유효한 범위
            if (adjustedEndMinutes > startMinutes || (adjustedEndMinutes < startMinutes && crossesMidnight)) {
                selectedTimeRange = startMinutes to adjustedEndMinutes
            }
            
            isUpdatingFromPicker = false
        }
        
        // 첫 로드 후 초기화 플래그 설정
        if (!isInitialized) {
            isInitialized = true
        }
    }
    
    // 색상 팔레트
    val colorPalette = listOf(
        SchedulePurple,
        ScheduleGreen,
        ScheduleOrange,
        ScheduleBlue,
        ScheduleTeal,
        ScheduleRed
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "일정 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 상단: 원형 시간표 + 드래그 가능한 타일 테두리
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 뒤: 원형 시간표 (기존 일정 표시)
                CircularDayViewReadOnly(
                    schedules = existingSchedules,
                    selectedTimeRange = selectedTimeRange,
                    selectedColor = selectedColor,
                    modifier = Modifier.size(340.dp)
                )
                
                // 앞: 드래그 가능한 타일 테두리
                DraggableTimeSelector(
                    selectedTimeRange = selectedTimeRange,
                    onTimeRangeChange = { range ->
                        selectedTimeRange = range
                    },
                    existingSchedules = existingSchedules,
                    modifier = Modifier.size(380.dp)  // 440dp에서 380dp로 축소
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // 하단: 일정 정보 입력 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // 제목 입력
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("일정 제목") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // TimePicker 스타일 시간 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 시작 시간
                    ScrollableTimePicker(
                        label = "시작 시간",
                        selectedHour = startHour,
                        selectedMinute = startMinute,
                        onTimeChange = { hour, minute ->
                            startHour = hour
                            startMinute = minute
                        },
                        existingSchedules = existingSchedules,
                        isStartTime = true,
                        otherTime = endHour to endMinute,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 종료 시간
                    ScrollableTimePicker(
                        label = "종료 시간",
                        selectedHour = endHour,
                        selectedMinute = endMinute,
                        onTimeChange = { hour, minute ->
                            endHour = hour
                            endMinute = minute
                        },
                        existingSchedules = existingSchedules,
                        isStartTime = false,
                        otherTime = startHour to startMinute,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 색상 선택
                Text(
                    text = "색상",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = color,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.background(
                                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 설명 입력
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명 (선택사항)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 속성 선택
                Text(
                    text = "속성",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isHabit = !isHabit }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isHabit,
                        onCheckedChange = { isHabit = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "습관",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasAlarm = !hasAlarm }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAlarm,
                        onCheckedChange = { hasAlarm = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "알림",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 저장 버튼
                Button(
                    onClick = {
                        if (title.isNotBlank() && endHour * 60 + endMinute > startHour * 60 + startMinute) {
                            val attributes = mutableSetOf<ScheduleAttribute>()
                            if (isHabit) attributes.add(ScheduleAttribute.HABIT)
                            if (hasAlarm) attributes.add(ScheduleAttribute.ALARM)
                            
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateString = dateFormat.format(selectedDate.time)
                            
                            val newSchedule = ScheduleItem(
                                date = dateString,
                                title = title,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                color = selectedColor,
                                description = description,
                                attributes = attributes
                            )
                            onSave(newSchedule)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = title.isNotBlank() && endHour * 60 + endMinute > startHour * 60 + startMinute,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "저장",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

