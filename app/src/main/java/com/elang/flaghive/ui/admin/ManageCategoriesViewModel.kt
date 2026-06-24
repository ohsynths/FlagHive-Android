package com.elang.flaghive.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Category
import com.elang.flaghive.data.repository.CategoryRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val showDialog: Boolean = false,
    val editingCategory: Category? = null
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageCategoriesUiState())
    val uiState: StateFlow<ManageCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = ManageCategoriesUiState(isLoading = true)
            when (val result = categoryRepository.getCategories()) {
                is Resource.Success -> {
                    _uiState.value = ManageCategoriesUiState(
                        isLoading = false,
                        categories = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageCategoriesUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true, editingCategory = null)
    }

    fun showEditDialog(category: Category) {
        _uiState.value = _uiState.value.copy(showDialog = true, editingCategory = category)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false, editingCategory = null)
    }

    fun saveCategory(name: String, description: String) {
        viewModelScope.launch {
            val editing = _uiState.value.editingCategory
            if (editing != null) {
                categoryRepository.updateCategory(
                    editing.id,
                    editing.copy(name = name, description = description)
                )
            } else {
                categoryRepository.createCategory(
                    Category(name = name, description = description)
                )
            }
            dismissDialog()
            loadCategories()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            when (categoryRepository.deleteCategory(categoryId)) {
                is Resource.Success -> loadCategories()
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Failed to delete category")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
