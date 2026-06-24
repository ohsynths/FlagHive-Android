package com.elang.flaghive.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.User
import com.elang.flaghive.data.repository.UserRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageUsersUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageUsersUiState())
    val uiState: StateFlow<ManageUsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = ManageUsersUiState(isLoading = true)
            when (val result = userRepository.getAllUsers()) {
                is Resource.Success -> {
                    _uiState.value = ManageUsersUiState(
                        isLoading = false,
                        users = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageUsersUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            when (userRepository.deleteUser(uid)) {
                is Resource.Success -> loadUsers()
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Failed to delete user")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
