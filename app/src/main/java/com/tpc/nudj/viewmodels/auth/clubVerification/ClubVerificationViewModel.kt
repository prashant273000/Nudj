package com.tpc.nudj.viewmodels.auth.clubVerification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tpc.nudj.repository.user.UserRepository
import com.tpc.nudj.ui.screen.auth.clubVerification.ClubVerificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ClubVerificationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _clubVerificationUiState = MutableStateFlow(ClubVerificationUiState())
    val clubVerificationUiState: StateFlow<ClubVerificationUiState> =
        _clubVerificationUiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun onCheckStatusClick(onApproved: () -> Unit, onMessage: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _clubVerificationUiState.update {
                it.copy(isLoading = true)
            }

            try {
                val status = userRepository.fetchClubVerificationStatus(uid)
                _clubVerificationUiState.update {
                    it.copy(
                        isLoading = false,
                        verificationStatus = status,
                        isVerified = status == "approved"
                    )
                }
                
                when (status) {
                    "approved" -> {
                        onMessage("Verification Successful!")
                        onApproved()
                    }
                    "pending" -> onMessage("Verification is still pending. Please wait.")
                    "rejected" -> onMessage("Verification rejected. Please contact support.")
                    else -> onMessage("Current status: $status")
                }
            } catch (e: Exception) {
                _clubVerificationUiState.update {
                    it.copy(isLoading = false)
                }
                onMessage("Error checking status: ${e.message}")
            }
        }
    }
}