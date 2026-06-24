package com.elang.flaghive.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val writeupRepository: WriteupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.isNotBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                when (val result = writeupRepository.searchWriteups(query)) {
                    is Resource.Success -> {
                        _uiState.value = SearchUiState(
                            query = query,
                            results = result.data
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = SearchUiState(
                            query = query,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _uiState.value = SearchUiState()
            }
        }
    }
}
