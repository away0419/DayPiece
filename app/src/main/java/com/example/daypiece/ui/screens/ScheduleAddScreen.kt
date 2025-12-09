package com.example.daypiece.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.daypiece.ui.theme.*

/**
 * 일정 추가 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAddScreen(
    onSave: (ScheduleItem) -> Unit,
    onCancel: () -> Unit
) {
    // 입력 상태
    var title by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(18) }
    var endMinute by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(ScheduleBlue) }
    var isHabit by remember { mutableStateOf(false) }
    var hasAlarm by remember { mutableStateOf(false) }
    
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 제목 입력
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("일정 제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 시작 시간
            Text(
                text = "시작 시간",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startHour.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { hour ->
                            if (hour in 0..23) startHour = hour
                        }
                    },
                    label = { Text("시") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = startMinute.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { minute ->
                            if (minute in 0..59) startMinute = minute
                        }
                    },
                    label = { Text("분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 종료 시간
            Text(
                text = "종료 시간",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = endHour.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { hour ->
                            if (hour in 0..23) endHour = hour
                        }
                    },
                    label = { Text("시") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = endMinute.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { minute ->
                            if (minute in 0..59) endMinute = minute
                        }
                    },
                    label = { Text("분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 저장 버튼
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val attributes = mutableSetOf<ScheduleAttribute>()
                        if (isHabit) attributes.add(ScheduleAttribute.HABIT)
                        if (hasAlarm) attributes.add(ScheduleAttribute.ALARM)
                        
                        val newSchedule = ScheduleItem(
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
                enabled = title.isNotBlank(),
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

