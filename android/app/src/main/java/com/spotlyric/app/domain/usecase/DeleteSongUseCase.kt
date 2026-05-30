package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.repository.ManageRepository
import javax.inject.Inject

class DeleteSongUseCase @Inject constructor(
    private val manageRepository: ManageRepository
) {
    suspend operator fun invoke(id: Long) {
        manageRepository.deleteSong(id)
    }
}
