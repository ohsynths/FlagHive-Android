package com.elang.flaghive.ui.writeup.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.AuthRepository
import com.elang.flaghive.data.repository.BookmarkRepository
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteupDetailUiState(
    val isLoading: Boolean = true,
    val writeup: Writeup? = null,
    val isBookmarked: Boolean = false,
    val isAuthor: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WriteupDetailViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteupDetailUiState())
    val uiState: StateFlow<WriteupDetailUiState> = _uiState.asStateFlow()

    private var writeupId: String = ""

    fun loadWriteup(writeupId: String) {
        this.writeupId = writeupId
        viewModelScope.launch {
            _uiState.value = WriteupDetailUiState(isLoading = true)

            when (val result = writeupRepository.getWriteupById(writeupId)) {
                is Resource.Success -> {
                    val writeup = result.data
                    val currentUserId = authRepository.getCurrentUserId()

                    val bookmarkResult = bookmarkRepository.isBookmarked(currentUserId, writeupId)
                    val isBookmarked = bookmarkResult is Resource.Success && bookmarkResult.data

                    _uiState.value = WriteupDetailUiState(
                        isLoading = false,
                        writeup = writeup,
                        isBookmarked = isBookmarked,
                        isAuthor = writeup.authorId == currentUserId,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = WriteupDetailUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val current = _uiState.value

            if (current.isBookmarked) {
                val bookmarkIdResult = bookmarkRepository.getBookmarkId(userId, writeupId)
                if (bookmarkIdResult is Resource.Success) {
                    bookmarkRepository.removeBookmark(bookmarkIdResult.data)
                    _uiState.value = current.copy(isBookmarked = false)
                }
            } else {
                bookmarkRepository.addBookmark(userId, writeupId)
                _uiState.value = current.copy(isBookmarked = true)
            }
        }
    }

    fun deleteWriteup(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (writeupRepository.deleteWriteup(writeupId)) {
                is Resource.Success -> onDeleted()
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete writeup"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}
