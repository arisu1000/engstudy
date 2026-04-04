package com.wcjung.engstudy.ui.screen.idiom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.Idiom
import com.wcjung.engstudy.domain.repository.IdiomRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IdiomListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val idiomRepository: IdiomRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.IdiomList>()
    val type: String? = route.type

    private val _idioms = MutableStateFlow<List<Idiom>>(emptyList())
    val idioms: StateFlow<List<Idiom>> = _idioms.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadIdioms()
    }

    private fun loadIdioms() {
        viewModelScope.launch {
            val flow = if (type != null) {
                idiomRepository.getByType(type)
            } else {
                idiomRepository.getAllIdioms()
            }
            flow.collect { list ->
                _idioms.value = list
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadIdioms()
            } else {
                idiomRepository.searchIdioms(query).collect { list ->
                    // 타입 필터가 있으면 검색 결과도 필터링
                    _idioms.value = if (type != null) {
                        list.filter { it.type.key == type }
                    } else {
                        list
                    }
                }
            }
        }
    }
}
