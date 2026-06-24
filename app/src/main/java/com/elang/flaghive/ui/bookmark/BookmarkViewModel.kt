package com.elang.flaghive.ui.bookmark

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

data class BookmarkUiState(
    val isLoading: Boolean = true,
    val writeups: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val writeupRepository: WriteupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState(isLoading = true)
            val userId = authRepository.getCurrentUserId()

            when (val bookmarkResult = bookmarkRepository.getBookmarks(userId)) {
                is Resource.Success -> {
                    val writeups = bookmarkResult.data.mapNotNull { bookmark ->
                        val result = writeupRepository.getWriteupById(bookmark.writeupId)
                        (result as? Resource.Success)?.data
                    }
                    _uiState.value = BookmarkUiState(
                        isLoading = false,
                        writeups = writeups
                    )
                }
                is Resource.Error -> {
                    _uiState.value = BookmarkUiState(
                        isLoading = false,
                        writeups = emptyList(),
                        error = bookmarkResult.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}
