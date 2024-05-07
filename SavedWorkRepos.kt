package com.business.visiting.card.creator.editor.ui.viewmodels

import com.business.visiting.card.creator.editor.ui.savedwork.SavedCardModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * @author [Rafaqat Ali](https://github.com/rafaqat480)
 * @date 06-Dec-2023
 */

class SavedWorkRepos(private val imagesApi: ImagesApi) {
    val latestImages: Flow<MutableList<SavedCardModel>> = flow {
        val savedImages=imagesApi.fetchLatestImages()
        emit(savedImages)
//        while(true) {
//            val savedImages = imagesApi.fetchLatestImages()
//            emit(savedImages) // Emits the result of the request to the flow
//            delay(500) // Suspends the coroutine for some time
//        }
    }
}
// Interface that provides a way to make network requests with suspend functions
interface ImagesApi {
    suspend fun fetchLatestImages(): MutableList<SavedCardModel>
}