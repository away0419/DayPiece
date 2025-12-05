package com.example.daypiece.model

/**
 * 일정 아이템 데이터 모델
 * @param title 일정 제목
 * @param startHour 시작 시간 (시)
 * @param startMinute 시작 시간 (분)
 * @param endHour 종료 시간 (시)
 * @param endMinute 종료 시간 (분)
 * @param color 일정 색상
 */
data class ScheduleItem(
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF6200EE)
) {
    /**
     * 시작 시간을 분 단위로 변환 (0시 0분 = 0, 23시 59분 = 1439)
     */
    fun getStartTimeInMinutes(): Int {
        return startHour * 60 + startMinute
    }

    /**
     * 종료 시간을 분 단위로 변환
     */
    fun getEndTimeInMinutes(): Int {
        return endHour * 60 + endMinute
    }

    /**
     * 일정의 지속 시간을 분 단위로 반환
     */
    fun getDurationInMinutes(): Int {
        return getEndTimeInMinutes() - getStartTimeInMinutes()
    }
}

