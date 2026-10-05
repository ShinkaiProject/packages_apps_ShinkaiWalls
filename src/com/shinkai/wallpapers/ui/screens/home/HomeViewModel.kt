package com.shinkai.wallpapers.ui.screens.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.data.repository.WallCategories
import com.shinkai.wallpapers.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
sealed interface HomeUiState {
  data object Loading : HomeUiState

  data class Success(val wallpapers: List<Wallpaper>, val isRefreshing: Boolean = false) :
      HomeUiState

  data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    loadWallpapers(forceRefresh = false)
  }

  fun refresh() {
    val current = _uiState.value
    if (current is HomeUiState.Success) {
      _uiState.value = current.copy(isRefreshing = true)
    }
    loadWallpapers(forceRefresh = true)
  }

  private fun loadWallpapers(forceRefresh: Boolean) {
    viewModelScope.launch {
      try {
        val library = WallpaperRepository.load(forceRefresh = forceRefresh)
        val homeWalls =
            WallCategories.wallpapersIn(library, WallCategories.DEFAULT_HOME_CATEGORY_ID)
        _uiState.value = HomeUiState.Success(homeWalls, isRefreshing = false)
      } catch (e: Exception) {
        _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load wallpapers")
      }
    }
  }
}
