package com.elang.flaghive.ui.category

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

data class CategoryListUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val category: Category? = null,
    val writeups: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val writeupRepository: WriteupRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(CategoryListUiState())
    val listState: StateFlow<CategoryListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(CategoryDetailUiState())
    val detailState: StateFlow<CategoryDetailUiState> = _detailState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _listState.value = CategoryListUiState(isLoading = true)
            when (val result = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    _listState.value = CategoryListUiState(
                        isLoading = false,
                        categories = result.data
                    )
                }
                is Resource.Error -> {
                    _listState.value = CategoryListUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadCategoryDetail(categoryId: String) {
        viewModelScope.launch {
            _detailState.value = CategoryDetailUiState(isLoading = true)

            val categoryResult = categoryRepository.getCategoryById(categoryId)
            val writeupsResult = writeupRepository.getWriteupsByCategory(categoryId)

            val category = (categoryResult as? Resource.Success)?.data
            val writeups = (writeupsResult as? Resource.Success)?.data ?: emptyList()

            if (category != null) {
                _detailState.value = CategoryDetailUiState(
                    isLoading = false,
                    category = category,
                    writeups = writeups
                )
            } else {
                _detailState.value = CategoryDetailUiState(
                    isLoading = false,
                    error = (categoryResult as? Resource.Error)?.message
                )
            }
        }
    }
}
