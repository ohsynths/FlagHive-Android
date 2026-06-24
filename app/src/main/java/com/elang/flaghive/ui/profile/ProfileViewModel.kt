package com.elang.flaghive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elang.flaghive.data.model.User
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.data.repository.AuthRepository
import com.elang.flaghive.data.repository.UserRepository
import com.elang.flaghive.data.repository.WriteupRepository
import com.elang.flaghive.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val myWriteups: List<Writeup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val writeupRepository: WriteupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            val userId = authRepository.getCurrentUserId()

            val userResult = userRepository.getUserProfile(userId)
            val writeupsResult = writeupRepository.getWriteupsByAuthor(userId)

            val user = (userResult as? Resource.Success)?.data
            val writeups = (writeupsResult as? Resource.Success)?.data ?: emptyList()

            if (user != null) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = user,
                    myWriteups = writeups
                )
            } else {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    error = (userResult as? Resource.Error)?.message
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
