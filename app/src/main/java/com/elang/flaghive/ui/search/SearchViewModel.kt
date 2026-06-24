package com.elang.flaghive.ui.search

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

    private var allResults: List<Writeup> = emptyList()

    init {
        loadCategories()
        loadAllWriteups()
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

    private fun loadAllWriteups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = writeupRepository.getWriteups()) {
                is Resource.Success -> {
                    allResults = result.data
                    applyFilters()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        applyFilters()
    }

    fun filterByCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.query.lowercase()
        var filtered = allResults

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.eventName.lowercase().contains(query) ||
                it.challengeName.lowercase().contains(query) ||
                it.categoryName.lowercase().contains(query)
            }
        }

        val selectedCategoryId = _uiState.value.selectedCategoryId
        if (selectedCategoryId != null) {
            filtered = filtered.filter { it.categoryId == selectedCategoryId }
        }

        _uiState.value = _uiState.value.copy(results = filtered, isLoading = false)
    }
}
