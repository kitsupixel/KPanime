package pt.kitsupixel.kpanime.ui.latest

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository

class LatestViewModel(application: Application) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    val episodes = showsRepository.latest

    init {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshLatest()
            _refreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val _refreshing = MutableLiveData<Boolean?>()
    val refreshing: LiveData<Boolean?>
        get() = _refreshing

    fun refresh() {
        _refreshing.value = true
        viewModelScope.launch {
            showsRepository.refreshLatest()
        }
        _refreshing.value = false
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LatestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LatestViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
