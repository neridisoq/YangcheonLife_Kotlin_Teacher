package com.helgisnw.yangcheonlifeteacher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SubjectSelectionScreen(mainNavController: NavHostController) {
    // 화면 상태
    var currentScreen by remember { mutableStateOf("GroupSelection") }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    val defaultGrade = prefs.getInt("defaultGrade", 2)

    // 선택 정보 저장
    var selectedGroupType by remember { mutableStateOf("") }
    var selectedClassName by remember { mutableStateOf("") }

    // 항상 메인 네비게이션으로 뒤로가기
    BackHandler {
        mainNavController.popBackStack()
    }

    when (currentScreen) {
        "GroupSelection" -> {
            GroupSelectionContent(
                grade = defaultGrade,
                onGroupSelected = { group ->
                    selectedGroupType = group
                    currentScreen = "ClassSelection"
                },
                onBackClick = {
                    mainNavController.popBackStack()
                }
            )
        }
        "ClassSelection" -> {
            ClassSelectionContent(
                grade = defaultGrade,
                groupType = selectedGroupType,
                onClassSelected = { className ->
                    selectedClassName = className
                    currentScreen = "SubjectSelection"
                },
                onBackClick = {
                    currentScreen = "GroupSelection"
                }
            )
        }
        "SubjectSelection" -> {
            SubjectSelectionContent(
                grade = defaultGrade,
                className = selectedClassName,
                onComplete = {
                    mainNavController.popBackStack()
                },
                onBackClick = {
                    currentScreen = "ClassSelection"
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionContent(
    grade: Int,
    onGroupSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 그룹 유형 선택 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onGroupSelected("h반") }
            ) {
                ListItem(
                    headlineContent = { Text("h반") },
                    trailingContent = {
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onGroupSelected("t반") }
            ) {
                ListItem(
                    headlineContent = { Text("t반") },
                    trailingContent = {
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onGroupSelected("나머지반") }
            ) {
                ListItem(
                    headlineContent = { Text("나머지반") },
                    trailingContent = {
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionContent(
    grade: Int,
    groupType: String,
    onClassSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val classMappings = getClassList(grade, groupType)

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(classMappings) { className ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onClassSelected(className) }
                ) {
                    ListItem(
                        headlineContent = { Text(className) },
                        trailingContent = {
                            Icon(Icons.Filled.ArrowForward, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelectionContent(
    grade: Int,
    className: String,
    onComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }

    val savedSubject = prefs.getString("selected${className}Subject", null)
    var selectedSubject by remember { mutableStateOf(savedSubject ?: "선택 없음") }

    val subjects = getSubjectsForClass(grade, className)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 과목 선택 목록
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedSubject = subject
                                // 선택한 과목 저장
                                prefs.edit()
                                    .putString("selected${className}Subject", subject)
                                    .apply()
                            }
                    ) {
                        ListItem(
                            headlineContent = { Text(subject) },
                            trailingContent = {
                                if (subject == selectedSubject) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "선택됨",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 현재 선택된 과목 표시
            if (selectedSubject != "선택 없음") {
                val components = selectedSubject.split("/")
                if (components.size == 2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "선택된 과목 정보",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text("과목명: ${components[0]}")
                            Text("교실: ${components[1]}")
                        }
                    }
                }
            }

            // 완료 버튼
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("완료")
            }
        }
    }
}

// 학년과 그룹 유형에 따른 반 목록 가져오기
private fun getClassList(grade: Int, groupType: String): List<String> {
    return when {
        grade == 2 -> {
            when (groupType) {
                "h반" -> listOf("A 반", "B 반", "C 반", "D 반")
                "t반" -> listOf("E 반", "F 반", "G 반", "H 반")
                "나머지반" -> listOf("I 반", "J 반", "K 반", "L 반", "M 반", "N 반")
                else -> emptyList()
            }
        }
        grade == 3 -> {
            when (groupType) {
                "h반" -> listOf("A 반", "B 반", "C 반", "G 반", "H 반", "I 반")
                "t반" -> listOf("D 반", "E 반", "F 반", "J 반", "K 반", "L 반", "M 반")
                "나머지반" -> listOf("N 반", "O 반")
                else -> emptyList()
            }
        }
        else -> emptyList()
    }
}

// 각 반별 과목 목록 가져오기
private fun getSubjectsForClass(grade: Int, className: String): List<String> {
    val defaultList = listOf("선택 없음")
    return when {
        grade == 2 -> {
            when (className) {
                "A 반" -> defaultList + listOf("물리I/202", "생명I/204", "여지/201", "지구I/205", "화학I/203")
                "B 반" -> defaultList + listOf("물리I/202", "생명I/204", "여지/201", "지구I/205", "화학I/203")
                "C 반" -> defaultList + listOf("물리I/202", "여지/201", "지구I/205", "화학I/203")
                "D 반" -> defaultList + listOf("물리I/202", "생명I/204", "여지/201", "지구I/205", "화학I/210")
                "E 반" -> defaultList + listOf("물리I/209", "생활과학/211", "여지/다목적실A", "윤사/208", "정법/207", "지구I/210", "한지/206")
                "F 반" -> defaultList + listOf("물리I/209", "생활과학/211", "여지/다목적실A", "윤사/208", "정법/207", "지구I/210", "한지/206")
                "G 반" -> defaultList + listOf("경제/209", "생명I/211", "생활과학/다목적실A", "세계사/206", "윤사/208", "정법/207", "화학I/210")
                "H 반" -> defaultList + listOf("경제/209", "생명I/211", "생활과학/207", "세계사/206", "여지/다목적실A", "윤사/208")
                "I 반" -> defaultList + listOf("기하/207", "심화국어/206")
                "J 반" -> defaultList + listOf("기하/209", "심화국어/208", "영어문화/다목적실B")
                "K 반" -> defaultList + listOf("기하/211", "심화국어/홈베이스B", "영어문화/210")
                "L 반" -> defaultList + listOf("일본어I/206", "중국어I/207")
                "M 반" -> defaultList + listOf("일본어I/208", "중국어I/209")
                "N 반" -> defaultList + listOf("일본어I/210", "중국어I/다목적실B")
                else -> defaultList
            }
        }
        grade == 3 -> {
            when (className) {
                "A 반" -> defaultList + listOf("과학사/꿈담카페B", "물리/302", "사문/304", "사문탐/303", "생명/305", "세지/306", "지구/301")
                "B 반" -> defaultList + listOf("과학사/꿈담카페B", "물리/302", "사문/305", "사문탐/306", "생윤/301", "지구/303", "화학/304")
                "C 반" -> defaultList + listOf("물리/302", "사문/305", "사문탐/303", "생명/306", "생윤/301", "화학/304")
                "D 반" -> defaultList + listOf("과학사/홈베이스B", "사문탐/309", "생명/310", "생윤/308", "세지/307", "지구/311")
                "E 반" -> defaultList + listOf("과학사/홈베이스B", "물리/310", "사문/308", "사문탐/309", "생윤/307", "지구/311")
                "F 반" -> defaultList + listOf("동아사/홈베이스B", "물리/309", "사문/308", "생명/310", "지구/311", "화학/307")
                "G 반" -> defaultList + listOf("고전읽기/301", "미적분/302", "수과탐/303", "영독작/305", "AI수학/304", "진로영어/306")
                "H 반" -> defaultList + listOf("고전읽기/301", "미적분/302", "수과탐/303", "영독작/305", "영어회화/304", "진로영어H1/306", "진로영어H2/꿈담카페B")
                "I 반" -> defaultList + listOf("경제수학/301", "미적분I1/302", "미적분I2/304", "수과탐/303", "영독작/305", "AI수학/꿈담카페B", "진로영어/306")
                "J 반" -> defaultList + listOf("미적분/310", "수과탐/309", "영독작/307", "영어회화/308", "진로영어/311")
                "K 반" -> defaultList + listOf("경제수학/308", "미적분/309", "영독작/310", "AI수학/307", "진로영어/311")
                "L 반" -> defaultList + listOf("고전읽기/311", "미적분/310", "수과탐/309", "영독작/308", "AI수학/홈베이스B", "확통/307")
                "M 반" -> defaultList + listOf("고전읽기/308", "미적분/309", "수과탐/310", "AI수학/홈베이스B", "진로영어/311", "확통/307")
                "N 반" -> defaultList + listOf("언매/307", "화작N1/308", "화작N2/홈베이스B")
                "O 반" -> defaultList + listOf("언매/311", "화작/310")
                else -> defaultList
            }
        }
        else -> defaultList
    }
}