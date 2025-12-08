package com.example.daypiece

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypiece.model.ScheduleItem
import com.example.daypiece.ui.components.CircularDayView
import com.example.daypiece.ui.theme.DayPieceTheme
import com.example.daypiece.ui.theme.ScheduleBlue
import com.example.daypiece.ui.theme.ScheduleGreen
import com.example.daypiece.ui.theme.ScheduleOrange
import com.example.daypiece.ui.theme.SchedulePurple
import com.example.daypiece.ui.theme.ScheduleRed
import com.example.daypiece.ui.theme.ScheduleTeal

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
    modifier: Modifier = Modifier
) {
    // 샘플 일정 데이터 생성
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

    // 원형 일간표 (헤더와 표 포함)
    CircularDayView(
        schedules = sampleSchedules,
        modifier = modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun DayPieceScreenPreview() {
    DayPieceTheme {
        DayPieceScreen()
    }
}