package com.elang.flaghive.ui.admin

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

data class AdminWriteupUiState(
    val isLoading: Boolean = true,
    val writeups: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminWriteupViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWriteupUiState())
    val uiState: StateFlow<AdminWriteupUiState> = _uiState.asStateFlow()

    init {
        loadWriteups()
    }

    fun loadWriteups() {
        viewModelScope.launch {
            _uiState.value = AdminWriteupUiState(isLoading = true)
            when (val result = writeupRepository.getWriteups()) {
                is Resource.Success -> {
                    _uiState.value = AdminWriteupUiState(
                        isLoading = false,
                        writeups = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = AdminWriteupUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteWriteup(writeupId: String) {
        viewModelScope.launch {
            when (writeupRepository.deleteWriteup(writeupId)) {
                is Resource.Success -> loadWriteups()
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }
}
