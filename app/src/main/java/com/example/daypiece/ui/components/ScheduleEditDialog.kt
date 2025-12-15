package com.example.daypiece.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.daypiece.model.ScheduleAttribute
import com.example.daypiece.model.ScheduleItem
import com.example.daypiece.ui.theme.*

/**
 * 일정 추가/수정 다이얼로그
 * @param schedule 수정할 일정 (null이면 새로 추가)
 * @param onDismiss 다이얼로그 닫기
 * @param onSave 저장 버튼 클릭
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditDialog(
    schedule: ScheduleItem? = null,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit
) {
    // 입력 상태
    var title by remember { mutableStateOf(schedule?.title ?: "") }
    var startHour by remember { mutableStateOf(schedule?.startHour ?: 9) }
    var startMinute by remember { mutableStateOf(schedule?.startMinute ?: 0) }
    var endHour by remember { mutableStateOf(schedule?.endHour ?: 18) }
    var endMinute by remember { mutableStateOf(schedule?.endMinute ?: 0) }
    var description by remember { mutableStateOf(schedule?.description ?: "") }
    var selectedColor by remember { mutableStateOf(schedule?.color ?: ScheduleBlue) }
    var isHabit by remember { mutableStateOf(schedule?.isHabit() ?: false) }
    var hasAlarm by remember { mutableStateOf(schedule?.hasAlarm() ?: false) }
    
    // 색상 팔레트
    val colorPalette = listOf(
        SchedulePurple,
        ScheduleGreen,
        ScheduleOrange,
        ScheduleBlue,
        ScheduleTeal,
        ScheduleRed
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 상단: 제목과 닫기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (schedule == null) "일정 추가" else "일정 수정",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                    // 시작 시
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
                    
                    // 시작 분
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
                    // 종료 시
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
                    
                    // 종료 분
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
                                            Color.White.copy(alpha = 0.3f),
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
                
                // 습관 체크박스
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
                
                // 알림 체크박스
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
                        if (title.isNotBlank()) {
                            // 선택된 속성들을 Set으로 변환
                            val attributes = mutableSetOf<ScheduleAttribute>()
                            if (isHabit) attributes.add(ScheduleAttribute.HABIT)
                            if (hasAlarm) attributes.add(ScheduleAttribute.ALARM)
                            
                            val newSchedule = ScheduleItem(
                                id = schedule?.id ?: java.util.UUID.randomUUID().toString(),
                                date = schedule?.date ?: "", // 기존 날짜 유지
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
                    enabled = title.isNotBlank()
                ) {
                    Text(
                        text = if (schedule == null) "추가" else "저장",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

