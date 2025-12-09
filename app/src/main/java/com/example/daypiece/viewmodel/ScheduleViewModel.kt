package com.example.daypiece.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.daypiece.model.ScheduleItem

/**
 * 일정 관리를 위한 ViewModel
 */
class ScheduleViewModel : ViewModel() {
    // 일정 목록 (변경 가능한 상태)
    private val _schedules = mutableStateListOf<ScheduleItem>()
    val schedules: SnapshotStateList<ScheduleItem> = _schedules

    /**
     * 일정 추가
     */
    fun addSchedule(schedule: ScheduleItem) {
        _schedules.add(schedule)
    }

    /**
     * 일정 수정
     */
    fun updateSchedule(schedule: ScheduleItem) {
        val index = _schedules.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            _schedules[index] = schedule
        }
    }

    /**
     * 일정 삭제
     */
    fun deleteSchedule(scheduleId: String) {
        _schedules.removeIf { it.id == scheduleId }
    }

    /**
     * ID로 일정 찾기
     */
    fun getScheduleById(id: String): ScheduleItem? {
        return _schedules.find { it.id == id }
    }

    /**
     * 초기 샘플 데이터 로드
     */
    fun loadSampleData(sampleSchedules: List<ScheduleItem>) {
        _schedules.clear()
        _schedules.addAll(sampleSchedules)
    }
}

