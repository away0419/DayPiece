# DayPiece

일정 관리 앱

## 프로젝트 개요

DayPiece는 직관적이고 효율적인 일정 관리를 제공하는 애플리케이션입니다. 일간, 주간, 월간 다양한 뷰를 통해 시간을 효과적으로 관리할 수 있습니다.

## 주요 기능

<details>
<summary>다양한 시간표 뷰</summary>

- **일간표**: 원형 형태로 하루 일정을 시각화
- **주간표**: 선형 형태로 일주일 일정을 한눈에 확인
- **월간표**: 달력 형태로 한 달 일정을 전체적으로 파악
</details>
<br/>

<details>
<summary>상세한 시간 표시</summary>

- **일간표/주간표**: 분 단위까지 정확한 시간 표시로 세밀한 일정 관리 가능
- **월간표**: 해당 날짜에 존재하는 일정만 간략하게 표시
</details>
<br/>

<details>
<summary>반복 일정 관리</summary>

- 습관이나 특정 시간대를 등록하면 자동으로 반복 표시
- 규칙적인 일정을 쉽게 관리하고 추적
</details>
<br/>

<details>
<summary>위젯 지원</summary>

- 각 시간표 뷰를 위젯으로 제공하여 빠른 접근 가능
- 홈 화면에서 바로 일정 확인 및 관리
</details>
<br/>

## 기술 스택

- **언어**: Kotlin
- **플랫폼**: Android
- **최소 SDK**: 24 (Android 7.0)
- **타겟 SDK**: 34 (Android 14)
- **아키텍처**: 단일 액티비티, 커스텀 뷰 기반

## 설치 및 실행

<details>
<summary>프로젝트 설정</summary>

1. Android Studio에서 프로젝트 열기
2. Gradle 동기화 실행
3. 에뮬레이터 또는 실제 기기 연결
4. Run 버튼으로 앱 실행
</details>
<br/>

<details>
<summary>빌드 요구사항</summary>

- Android Studio Hedgehog 이상
- JDK 8 이상
- Android SDK 34
</details>
<br/>

## 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/daypiece/
│   │   ├── MainActivity.kt          # 메인 액티비티
│   │   ├── model/
│   │   │   └── ScheduleItem.kt      # 일정 데이터 모델
│   │   └── view/
│   │       └── CircularDayView.kt   # 원형 일간표 커스텀 뷰
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # 메인 레이아웃
│   │   └── values/
│   │       ├── colors.xml            # 색상 리소스
│   │       ├── strings.xml           # 문자열 리소스
│   │       └── themes.xml            # 테마 리소스
│   └── AndroidManifest.xml
└── build.gradle
```

## 개발 계획

1. ✅ 프로젝트 초기 설정
2. ✅ 일간표(원형) 구현
3. 주간표(선형) 구현
4. 월간표(달력형) 구현
5. 반복 일정 기능 구현
6. 위젯 기능 구현

## 라이선스

(추가 예정)
