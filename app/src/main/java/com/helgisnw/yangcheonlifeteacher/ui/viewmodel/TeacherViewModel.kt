package com.helgisnw.yangcheonlifeteacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helgisnw.yangcheonlifeteacher.data.model.Teacher
import com.helgisnw.yangcheonlifeteacher.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherViewModel : ViewModel() {
    private val repository = TeacherRepository()

    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getTeachers()
                .onSuccess { teacherList ->
                    _teachers.value = teacherList
                }
                .onFailure { throwable ->
                    _error.value = throwable.message ?: "교사 목록을 불러오는데 실패했습니다."
                }
            
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}