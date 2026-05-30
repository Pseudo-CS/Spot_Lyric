package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.repository.AuthRepository
import com.spotlyric.app.domain.repository.PlayerRepository
import javax.inject.Inject

class GetCurrentSongUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(): CurrentSongResult {
        val token = authRepository.getToken()
            ?: return CurrentSongResult.Error("Not authenticated")
        return playerRepository.getCurrentSong(token)
    }
}
