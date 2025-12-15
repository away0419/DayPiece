package com.example.daypiece.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.example.daypiece.model.ScheduleItem
import com.example.daypiece.utils.ColorTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 일정 관리를 위한 ViewModel
 * 날짜별로 일정을 관리하고 SharedPreferences를 통해 영속성을 제공합니다
 */
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    // 날짜별 일정 목록 (날짜 키: yyyy-MM-dd 형식)
    private val _schedulesByDate = mutableStateMapOf<String, MutableList<ScheduleItem>>()
    val schedulesByDate: SnapshotStateMap<String, MutableList<ScheduleItem>> = _schedulesByDate
    
    // 날짜 포맷터
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // SharedPreferences
    private val sharedPreferences = application.getSharedPreferences(
        "DayPieceSchedules",
        Context.MODE_PRIVATE
    )
    
    // Gson (Color 직렬화 지원)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
        .create()
    
    // SharedPreferences 키
    private companion object {
        const val KEY_SCHEDULES = "schedules_data"
    }
    
    init {
        // ViewModel 생성 시 저장된 데이터 자동 로드
        loadSchedules()
    }

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
        saveSchedules() // 자동 저장
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
                    addSchedule(schedule) // addSchedule에서 저장됨
                } else {
                    schedules[index] = schedule
                    saveSchedules() // 자동 저장
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
        saveSchedules() // 자동 저장
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
    
    /**
     * 일정 데이터를 SharedPreferences에 저장
     */
    private fun saveSchedules() {
        try {
            // Map을 JSON으로 직렬화
            val json = gson.toJson(_schedulesByDate.toMap())
            sharedPreferences.edit()
                .putString(KEY_SCHEDULES, json)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * SharedPreferences에서 일정 데이터 로드
     */
    private fun loadSchedules() {
        try {
            val json = sharedPreferences.getString(KEY_SCHEDULES, null)
            if (json != null) {
                // JSON을 Map으로 역직렬화
                val type = object : TypeToken<Map<String, List<ScheduleItem>>>() {}.type
                val loadedSchedules: Map<String, List<ScheduleItem>> = gson.fromJson(json, type)
                
                // 로드된 데이터를 _schedulesByDate에 복사
                _schedulesByDate.clear()
                loadedSchedules.forEach { (date, schedules) ->
                    _schedulesByDate[date] = schedules.toMutableList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 모든 일정 데이터 삭제 (디버그/테스트용)
     */
    fun clearAllSchedules() {
        _schedulesByDate.clear()
        sharedPreferences.edit()
            .remove(KEY_SCHEDULES)
            .apply()
    }
}

