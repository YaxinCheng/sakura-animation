package com.jing.sakura.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jing.sakura.SakuraApplication
import com.jing.sakura.data.HomePageData
import com.jing.sakura.data.Resource
import com.jing.sakura.repo.AnimationSource
import com.jing.sakura.repo.MxdmSource
import com.jing.sakura.repo.WebPageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WebPageRepository
) : ViewModel() {

    private val _homePageData = MutableStateFlow<Resource<HomePageData>>(Resource.Loading)

    private val _sp = SakuraApplication.context.getSharedPreferences("source", Context.MODE_PRIVATE)

    var currentSourceId: String = _sp.getString("id", MxdmSource.SOURCE_ID)?.takeIf { id ->
        repository.animationSources.any { it.sourceId == id }
    } ?: MxdmSource.SOURCE_ID
        private set


    private val _currentSource =
        MutableStateFlow(repository.requireAnimationSource(currentSourceId))

    val currentSource: StateFlow<AnimationSource>
        get() = _currentSource

    val homePageData: StateFlow<Resource<HomePageData>>
        get() = _homePageData

    @Volatile
    var lastHomePageData: HomePageData? = null
        private set

    private var loadDataJob: Job? = null

    init {
        loadData(false)
    }

    fun changeSource(newSourceId: String) {
        if (newSourceId == currentSourceId) {
            return
        }
        val source = repository.requireAnimationSource(newSourceId)
        currentSourceId = newSourceId
        _sp.edit().putString("id", newSourceId).apply()
        viewModelScope.launch {
            _currentSource.emit(source)
        }
        loadData(false)
    }

    fun loadData(silent: Boolean = false) {
        val lastValue = _homePageData.value
        if (lastValue is Resource.Success) {
            lastHomePageData = lastValue.data
        }
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch(Dispatchers.IO) {
            _homePageData.emit(Resource.Loading(silent = silent))
            try {
                repository.fetchHomePage(currentSourceId).also {
                    _homePageData.emit(Resource.Success(it))
                }
            } catch (ex: Exception) {
                lastHomePageData = null
                Log.e("homepage", "请求数据失败", ex)

                val message = "请求数据失败:" + ex.message
                _homePageData.emit(Resource.Error(message))
            }
        }
    }

    fun getAllSources(): List<Pair<String, String>> =
        repository.animationSources.map { it.sourceId to it.name }

}