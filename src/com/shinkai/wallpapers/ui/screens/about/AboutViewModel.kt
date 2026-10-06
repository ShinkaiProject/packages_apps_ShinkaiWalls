package com.shinkai.wallpapers.ui.screens.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinkai.wallpapers.data.model.GitHubContributor
import com.shinkai.wallpapers.data.repository.ContributorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AboutUiState {
    data object Loading : AboutUiState
    data class Success(val contributors: List<GitHubContributor>) : AboutUiState
}

class AboutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AboutUiState>(AboutUiState.Loading)
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        fetchContributors()
    }

    fun fetchContributors() {
        viewModelScope.launch {
            _uiState.value = AboutUiState.Loading
            val list = ContributorRepository.getContributors()
            _uiState.value = AboutUiState.Success(list)
        }
    }
}
