package com.business.visiting.card.creator.editor.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.business.visiting.card.creator.editor.ui.savedwork.SavedCardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch


/**
 * @author [Rafaqat Ali](https://github.com/rafaqat480)
 * @date 06-Dec-2023
 */

class SavedWorkViewModel(private val savedRepository: SavedWorkRepos) : ViewModel(){



    fun printMessage(){
        Log.d("test_data","in view ")

    }

    var imgsData: MutableStateFlow<MutableList<SavedCardModel>> = MutableStateFlow(mutableListOf())

    init {
        viewModelScope.launch {
            // Trigger the flow and consume its elements using collect
            savedRepository.latestImages.collect { savedImagesList ->
                // Update View with the latest favorite news
                imgsData.emit(savedImagesList)
                Log.d("test_data","in view model: $savedImagesList")
            }
        }
    }

    // Define ViewModel factory in a companion object
    class Factory(private val savedRepository: SavedWorkRepos) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavedWorkViewModel(savedRepository) as T
        }
    }
}