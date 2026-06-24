package com.elang.flaghive.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Category
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.CategoryRepository
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val results: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allResults: List<Writeup> = emptyList()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(categories = result.data)
                }
                else -> {}
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.isNotBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                when (val result = writeupRepository.searchWriteups(query)) {
                    is Resource.Success -> {
                        allResults = result.data
                        applyCategoryFilter()
                    }
                    is Resource.Error -> {
                        _uiState.value = SearchUiState(
                            query = query,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } else {
                allResults = emptyList()
                _uiState.value = SearchUiState()
            }
        }
    }

    fun filterByCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        applyCategoryFilter()
    }

    private fun applyCategoryFilter() {
        val filtered = if (_uiState.value.selectedCategoryId == null) {
            allResults
        } else {
            allResults.filter { it.categoryId == _uiState.value.selectedCategoryId }
        }
        _uiState.value = _uiState.value.copy(
            results = filtered,
            isLoading = false
        )
    }
}
