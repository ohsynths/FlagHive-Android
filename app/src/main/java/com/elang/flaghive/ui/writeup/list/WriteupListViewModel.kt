package com.elang.flaghive.ui.writeup.list

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

data class WriteupListUiState(
    val isLoading: Boolean = true,
    val writeups: List<Writeup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val error: String? = null
)

@HiltViewModel
class WriteupListViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteupListUiState())
    val uiState: StateFlow<WriteupListUiState> = _uiState.asStateFlow()

    private var allWriteups: List<Writeup> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = WriteupListUiState(isLoading = true)

            val writeupsResult = writeupRepository.getWriteups()
            val categoriesResult = categoryRepository.getCategories()

            val categories = (categoriesResult as? Resource.Success)?.data ?: emptyList()

            when (writeupsResult) {
                is Resource.Success -> {
                    allWriteups = writeupsResult.data
                    _uiState.value = WriteupListUiState(
                        isLoading = false,
                        writeups = allWriteups,
                        categories = categories
                    )
                }
                is Resource.Error -> {
                    _uiState.value = WriteupListUiState(
                        isLoading = false,
                        categories = categories,
                        error = writeupsResult.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun filterByCategory(categoryId: String?) {
        val filtered = if (categoryId == null) {
            allWriteups
        } else {
            allWriteups.filter { it.categoryId == categoryId }
        }
        _uiState.value = _uiState.value.copy(
            writeups = filtered,
            selectedCategoryId = categoryId
        )
    }
}
