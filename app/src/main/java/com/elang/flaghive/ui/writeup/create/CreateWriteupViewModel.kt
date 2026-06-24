package com.elang.flaghive.ui.writeup.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Category
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.AuthRepository
import com.elang.flaghive.data.repository.CategoryRepository
import com.elang.flaghive.data.repository.UserRepository
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateWriteupUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isSuccess: Boolean = false,
    val createdId: String = "",
    val error: String? = null
)

@HiltViewModel
class CreateWriteupViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWriteupUiState())
    val uiState: StateFlow<CreateWriteupUiState> = _uiState.asStateFlow()

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

    fun createWriteup(
        title: String,
        content: String,
        categoryId: String,
        categoryName: String,
        eventName: String,
        challengeName: String,
        difficulty: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = authRepository.getCurrentUserId()
            val userResult = userRepository.getUserProfile(userId)
            val authorName = (userResult as? Resource.Success)?.data?.displayName ?: ""

            val writeup = Writeup(
                title = title,
                content = content,
                categoryId = categoryId,
                categoryName = categoryName,
                eventName = eventName,
                challengeName = challengeName,
                difficulty = difficulty,
                authorId = userId,
                authorName = authorName
            )

            when (val result = writeupRepository.createWriteup(writeup)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdId = result.data
                    )
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
}
