package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.BookmarkedSong
import com.spotlyric.app.domain.repository.ManageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSongsUseCase @Inject constructor(
    private val manageRepository: ManageRepository
) {
    operator fun invoke(): Flow<List<BookmarkedSong>> {
        return manageRepository.getSongsFlow()
    }

    fun search(query: String): Flow<List<BookmarkedSong>> {
        return if (query.isBlank()) {
            manageRepository.getSongsFlow()
        } else {
            manageRepository.searchSongsFlow(query)
        }
    }
}
