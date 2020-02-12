package pt.kitsupixel.kpanime.ui.current

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

class CurrentViewModel(application: Application) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    val shows = showsRepository.current

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val _refreshing = MutableLiveData<Boolean>(false)
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshShows()
            _refreshing.value = false
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CurrentViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
