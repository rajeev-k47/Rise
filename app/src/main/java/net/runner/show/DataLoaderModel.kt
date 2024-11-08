package net.runner.show

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DataLoaderViewModel : ViewModel() {
    private val _dataLoaded = MutableLiveData(false)

    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _fetchedData = MutableLiveData<String>()
    val fetchedData: LiveData<String> get() = _fetchedData

    private val updatestate = MutableLiveData<String>()
    val state: LiveData<String> get() = updatestate


    init {
        viewModelScope.launch {
            loadDataFromDatabase()
            updateState()
        }
    }
    private fun updateState() {
        val db = FirebaseFirestore.getInstance()
        db.collection("dine").document("update")
            .get()
            .addOnSuccessListener { result ->
                updatestate.postValue(result.data?.get("update").toString())
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
                updatestate.postValue("")
            }
    }

    private suspend fun loadDataFromDatabase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("dine").document("dine")
            .get()
            .addOnSuccessListener { result ->
                _fetchedData.postValue(result.data?.get("value").toString())
                _dataLoaded.postValue(true)
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
                _fetchedData.postValue("")
            }
    }
}