package com.example.daypiece.model

/**
 * 일정 속성 타입
 */
enum class ScheduleAttribute {
    HABIT,      // 습관
    ALARM       // 알림
}

/**
 * 일정 아이템 데이터 모델
 * @param id 일정 고유 ID
 * @param date 일정 날짜 (yyyy-MM-dd 형식)
 * @param title 일정 제목
 * @param startHour 시작 시간 (시)
 * @param startMinute 시작 시간 (분)
 * @param endHour 종료 시간 (시)
 * @param endMinute 종료 시간 (분)
 * @param color 일정 색상
 * @param description 일정 설명 (선택사항)
 * @param attributes 일정 속성 집합 (습관, 알림 등 - 확장 가능)
 */
data class ScheduleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd 형식
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF6200EE),
    val description: String = "",
    val attributes: Set<ScheduleAttribute> = emptySet()
) {
    /**
     * 습관 속성이 있는지 확인
     */
    fun isHabit(): Boolean = attributes.contains(ScheduleAttribute.HABIT)
    
    /**
     * 알림 속성이 있는지 확인
     */
    fun hasAlarm(): Boolean = attributes.contains(ScheduleAttribute.ALARM)
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

