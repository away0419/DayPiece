package com.example.daypiece

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daypiece.model.ScheduleItem
import com.example.daypiece.ui.components.CircularDayView
import com.example.daypiece.ui.components.ScheduleDetailDialog
import com.example.daypiece.ui.components.ScheduleEditDialog
import com.example.daypiece.ui.theme.DayPieceTheme
import com.example.daypiece.ui.theme.ScheduleBlue
import com.example.daypiece.ui.theme.ScheduleGreen
import com.example.daypiece.ui.theme.ScheduleOrange
import com.example.daypiece.ui.theme.SchedulePurple
import com.example.daypiece.ui.theme.ScheduleRed
import com.example.daypiece.ui.theme.ScheduleTeal
import com.example.daypiece.viewmodel.ScheduleViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayPieceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DayPieceScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DayPieceScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = viewModel()
) {
    // 샘플 일정 데이터 생성 (초기 로드)
    LaunchedEffect(Unit) {
        if (viewModel.schedules.isEmpty()) {
            val sampleSchedules = listOf(
                ScheduleItem(
                    title = "잠",
                    startHour = 23,
                    startMinute = 0,
                    endHour = 5,
                    endMinute = 30,
                    color = SchedulePurple
                ),
                ScheduleItem(
                    title = "헬스",
                    startHour = 5,
                    startMinute = 30,
                    endHour = 7,
                    endMinute = 0,
                    color = ScheduleGreen
                ),
                ScheduleItem(
                    title = "아침식사 및 출근준비",
                    startHour = 7,
                    startMinute = 0,
                    endHour = 8,
                    endMinute = 0,
                    color = ScheduleOrange
                ),
                ScheduleItem(
                    title = "출근",
                    startHour = 8,
                    startMinute = 0,
                    endHour = 9,
                    endMinute = 0,
                    color = ScheduleBlue
                ),
                ScheduleItem(
                    title = "회사",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 0,
                    color = ScheduleTeal
                ),
                ScheduleItem(
                    title = "자유시간",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 23,
                    endMinute = 0,
                    color = ScheduleRed
                )
            )
            viewModel.loadSampleData(sampleSchedules)
        }
    }

    // 선택된 날짜 상태
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // 선택된 날짜의 일정만 필터링
    val filteredSchedules = remember(selectedDate, viewModel.schedules.size) {
        viewModel.schedules.filter { schedule ->
            // 현재는 날짜 필터링 없이 모든 일정 표시
            // 추후 날짜별 일정 관리 기능 추가 시 여기서 필터링
            true
        }
    }
    
    // 다이얼로그 상태
    var showDetailDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<ScheduleItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            // 일정 추가 FAB
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "일정 추가"
                )
            }
        }
    ) { paddingValues ->
        // 원형 일간표 (헤더와 표 포함)
        CircularDayView(
            schedules = filteredSchedules,
            selectedDate = selectedDate,
            onDateSelected = { newDate ->
                selectedDate = newDate
                // 날짜 변경 시 해당 날짜의 일정을 로드
                // 추후 날짜별 일정 관리 기능 추가 시 여기서 처리
            },
            onScheduleClick = { schedule ->
                selectedSchedule = schedule
                showDetailDialog = true
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

        // 일정 상세 다이얼로그
        if (showDetailDialog && selectedSchedule != null) {
            ScheduleDetailDialog(
                schedule = selectedSchedule!!,
                onDismiss = {
                    showDetailDialog = false
                    selectedSchedule = null
                },
                onEdit = {
                    showDetailDialog = false
                    showEditDialog = true
                },
                onDelete = {
                    viewModel.deleteSchedule(selectedSchedule!!.id)
                    showDetailDialog = false
                    selectedSchedule = null
                }
            )
        }

        // 일정 수정 다이얼로그
        if (showEditDialog && selectedSchedule != null) {
            ScheduleEditDialog(
                schedule = selectedSchedule,
                onDismiss = {
                    showEditDialog = false
                    selectedSchedule = null
                },
                onSave = { updatedSchedule ->
                    viewModel.updateSchedule(updatedSchedule)
                    showEditDialog = false
                    selectedSchedule = null
                }
            )
        }

        // 일정 추가 다이얼로그
        if (showAddDialog) {
            ScheduleEditDialog(
                schedule = null,
                onDismiss = { showAddDialog = false },
                onSave = { newSchedule ->
                    viewModel.addSchedule(newSchedule)
                    showAddDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayPieceScreenPreview() {
    DayPieceTheme {
        DayPieceScreen()
    }
}