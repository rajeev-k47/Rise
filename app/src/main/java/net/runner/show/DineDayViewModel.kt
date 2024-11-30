package net.runner.show

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar


class DineDayViewModel : ViewModel() {
    private val _dineDay = MutableLiveData(0)
    val dineDay: LiveData<Int> = _dineDay

    fun updateDineDay(day: Int){
        Log.d("based","$day")
        _dineDay.value = day
    }
    fun resetToToday(){
        val todayDay = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2).let{ if (it==-1)6 else it }
        _dineDay.value = todayDay
    }

}