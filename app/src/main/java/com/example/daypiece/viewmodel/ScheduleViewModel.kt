package com.example.daypiece.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import com.example.daypiece.model.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * 일정 관리를 위한 ViewModel
 * 날짜별로 일정을 관리합니다
 */
class ScheduleViewModel : ViewModel() {
    // 날짜별 일정 목록 (날짜 키: yyyy-MM-dd 형식)
    private val _schedulesByDate = mutableStateMapOf<String, MutableList<ScheduleItem>>()
    val schedulesByDate: SnapshotStateMap<String, MutableList<ScheduleItem>> = _schedulesByDate
    
    // 날짜 포맷터
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Calendar를 날짜 문자열로 변환
     */
    fun calendarToDateString(calendar: Calendar): String {
        return dateFormat.format(calendar.time)
    }

    /**
     * 특정 날짜의 일정 목록 가져오기
     */
    fun getSchedulesForDate(date: String): List<ScheduleItem> {
        return _schedulesByDate[date]?.toList() ?: emptyList()
    }
    
    /**
     * Calendar 기준으로 일정 목록 가져오기
     */
    fun getSchedulesForDate(calendar: Calendar): List<ScheduleItem> {
        return getSchedulesForDate(calendarToDateString(calendar))
    }

    /**
     * 일정 추가
     */
    fun addSchedule(schedule: ScheduleItem) {
        val dateKey = schedule.date
        if (_schedulesByDate.containsKey(dateKey)) {
            _schedulesByDate[dateKey]?.add(schedule)
        } else {
            _schedulesByDate[dateKey] = mutableListOf(schedule)
        }
    }

    /**
     * 일정 수정
     */
    fun updateSchedule(schedule: ScheduleItem) {
        // 모든 날짜에서 해당 ID를 찾아서 업데이트
        _schedulesByDate.forEach { (dateKey, schedules) ->
            val index = schedules.indexOfFirst { it.id == schedule.id }
            if (index != -1) {
                // 날짜가 변경되었다면 이전 날짜에서 제거하고 새 날짜에 추가
                if (dateKey != schedule.date) {
                    schedules.removeAt(index)
                    if (schedules.isEmpty()) {
                        _schedulesByDate.remove(dateKey)
                    }
                    addSchedule(schedule)
                } else {
                    schedules[index] = schedule
                }
                return@forEach
            }
        }
    }

    /**
     * 일정 삭제
     */
    fun deleteSchedule(scheduleId: String) {
        _schedulesByDate.forEach { (dateKey, schedules) ->
            val removed = schedules.removeIf { it.id == scheduleId }
            if (removed && schedules.isEmpty()) {
                _schedulesByDate.remove(dateKey)
            }
        }
    }

    /**
     * ID로 일정 찾기
     */
    fun getScheduleById(id: String): ScheduleItem? {
        _schedulesByDate.forEach { (_, schedules) ->
            schedules.find { it.id == id }?.let { return it }
        }
        return null
    }

    /**
     * 모든 일정 개수 가져오기
     */
    fun getTotalScheduleCount(): Int {
        return _schedulesByDate.values.sumOf { it.size }
    }
    
    /**
     * 특정 날짜에 일정이 있는지 확인
     */
    fun hasSchedulesForDate(date: String): Boolean {
        return _schedulesByDate.containsKey(date) && _schedulesByDate[date]?.isNotEmpty() == true
    }
}

