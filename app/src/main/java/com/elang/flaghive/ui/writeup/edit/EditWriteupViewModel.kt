package com.elang.flaghive.ui.writeup.edit

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

data class EditWriteupUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val writeup: Writeup? = null,
    val categories: List<Category> = emptyList(),
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditWriteupViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditWriteupUiState())
    val uiState: StateFlow<EditWriteupUiState> = _uiState.asStateFlow()

    fun loadWriteup(writeupId: String) {
        viewModelScope.launch {
            _uiState.value = EditWriteupUiState(isLoading = true)

            val writeupResult = writeupRepository.getWriteupById(writeupId)
            val categoriesResult = categoryRepository.getCategories()

            val writeup = (writeupResult as? Resource.Success)?.data
            val categories = (categoriesResult as? Resource.Success)?.data ?: emptyList()

            if (writeup != null) {
                _uiState.value = EditWriteupUiState(
                    isLoading = false,
                    writeup = writeup,
                    categories = categories
                )
            } else {
                _uiState.value = EditWriteupUiState(
                    isLoading = false,
                    error = (writeupResult as? Resource.Error)?.message ?: "Failed to load writeup"
                )
            }
        }
    }

    fun updateWriteup(writeupId: String, writeup: Writeup) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            when (writeupRepository.updateWriteup(writeupId, writeup)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Failed to update writeup"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}
