package com.elang.flaghive.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Category
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.CategoryRepository
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val recentWriteups: List<Writeup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val writeupCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val writeupsResult = writeupRepository.getWriteups()
            val categoriesResult = categoryRepository.getCategories()

            val writeups = when (writeupsResult) {
                is Resource.Success -> writeupsResult.data
                else -> {
                    _uiState.value = _uiState.value.copy(error = writeupsResult.message)
                    emptyList()
                }
            }

            val categories = when (categoriesResult) {
                is Resource.Success -> categoriesResult.data
                else -> emptyList()
            }

            _uiState.value = DashboardUiState(
                isLoading = false,
                recentWriteups = writeups.take(5),
                categories = categories,
                writeupCount = writeups.size
            )
        }
    }
}
