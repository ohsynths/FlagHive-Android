package com.elang.flaghive.ui.writeup.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Writeup
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
    val error: String? = null
)

@HiltViewModel
class WriteupListViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteupListUiState())
    val uiState: StateFlow<WriteupListUiState> = _uiState.asStateFlow()

    init {
        loadWriteups()
    }

    fun loadWriteups() {
        viewModelScope.launch {
            _uiState.value = WriteupListUiState(isLoading = true)
            when (val result = writeupRepository.getWriteups()) {
                is Resource.Success -> {
                    _uiState.value = WriteupListUiState(
                        isLoading = false,
                        writeups = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = WriteupListUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}
