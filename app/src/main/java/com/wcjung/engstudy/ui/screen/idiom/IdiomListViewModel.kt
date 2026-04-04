package com.wcjung.engstudy.ui.screen.idiom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.data.local.dao.KnownItemDao
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.domain.model.Idiom
import com.wcjung.engstudy.domain.repository.IdiomRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IdiomListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val idiomRepository: IdiomRepository,
    private val knownItemDao: KnownItemDao
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.IdiomList>()
    val type: String? = route.type

    private val _allIdioms = MutableStateFlow<List<Idiom>>(emptyList())

    private val _hideKnown = MutableStateFlow(false)
    val hideKnown: StateFlow<Boolean> = _hideKnown.asStateFlow()

    val knownIds: StateFlow<Set<Int>> = knownItemDao.getKnownIds(ITEM_TYPE)
        .combine(MutableStateFlow(Unit)) { ids, _ -> ids.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val idioms: StateFlow<List<Idiom>> = combine(
        _allIdioms, knownIds, _hideKnown
    ) { all, known, hide ->
        if (hide) all.filter { it.id !in known } else all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                _allIdioms.value = list
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
                    _allIdioms.value = if (type != null) {
                        list.filter { it.type.key == type }
                    } else {
                        list
                    }
                }
            }
        }
    }

    fun markAsKnown(idiomId: Int) {
        viewModelScope.launch {
            knownItemDao.markAsKnown(KnownItemEntity(itemId = idiomId, itemType = ITEM_TYPE))
        }
    }

    fun unmarkKnown(idiomId: Int) {
        viewModelScope.launch {
            knownItemDao.unmarkKnown(idiomId, ITEM_TYPE)
        }
    }

    fun toggleHideKnown() {
        _hideKnown.value = !_hideKnown.value
    }

    companion object {
        const val ITEM_TYPE = "idiom"
    }
}
