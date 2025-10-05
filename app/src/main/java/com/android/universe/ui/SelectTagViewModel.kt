package com.android.universe.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow

class SelectTagViewModel(): ViewModel(){
    private val selectedTags = MutableStateFlow<List<String>>(emptyList())
    val uiStateTags = selectedTags.asStateFlow()

    fun addTag(name: String){
        if (selectedTags.value.contains(name)){
            Log.e("SelectTagViewModel", "Cannot add tag '$name' because it was already in the list")
            throw IllegalArgumentException("Tag '$name' not found in selectedTags")
        }else{
            selectedTags.value = selectedTags.value + name
        }
    }

    fun deleteTag(name: String){
        if (!selectedTags.value.contains(name)) {
            Log.e("SelectTagViewModel", "Cannot delete tag '$name' because it is not in the list")
            throw IllegalArgumentException("Tag '$name' not found in selectedTags")
        }else{
            selectedTags.value = selectedTags.value - name
        }
    }

    fun saveTags(){

    }

}