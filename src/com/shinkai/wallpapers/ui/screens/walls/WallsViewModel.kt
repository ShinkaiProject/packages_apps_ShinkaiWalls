package com.shinkai.wallpapers.ui.screens.walls

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.repository.WallCategories
import com.shinkai.wallpapers.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
sealed interface WallsUiState {
  data object Loading : WallsUiState

  data class Success(val categories: List<WallCategory>) : WallsUiState

  data class Error(val message: String) : WallsUiState
}

class WallsViewModel : ViewModel() {

  private val _uiState = MutableStateFlow<WallsUiState>(WallsUiState.Loading)
  val uiState: StateFlow<WallsUiState> = _uiState.asStateFlow()

  fun loadCategories(context: Context) {
    if (_uiState.value is WallsUiState.Success) return
    viewModelScope.launch {
      try {
        val library = WallpaperRepository.load()
        val categories = WallCategories.resolve(context, library)
        _uiState.value = WallsUiState.Success(categories)
      } catch (e: Exception) {
        _uiState.value = WallsUiState.Error(e.localizedMessage ?: "Failed to load categories")
      }
    }
  }
}
