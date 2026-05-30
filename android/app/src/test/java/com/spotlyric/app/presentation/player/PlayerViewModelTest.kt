package com.spotlyric.app.presentation.player

import app.cash.turbine.test
import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.model.Song
import com.spotlyric.app.domain.repository.PlayerRepository
import com.spotlyric.app.domain.usecase.ExtractAndTranslateUseCase
import com.spotlyric.app.domain.usecase.GetCurrentSongUseCase
import com.spotlyric.app.domain.usecase.GetLyricsUseCase
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase
import com.spotlyric.app.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getCurrentSongUseCase: GetCurrentSongUseCase = mockk()
    private val searchLyricsSourcesUseCase: SearchLyricsSourcesUseCase = mockk()
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase = mockk()
    private val extractAndTranslateUseCase: ExtractAndTranslateUseCase = mockk()
    private val getLyricsUseCase: GetLyricsUseCase = mockk()
    private val playerRepository: PlayerRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - when nothing playing, state is NothingPlaying`() = runTest {
        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.NothingPlaying

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        assertEquals(PlayerUiState.NothingPlaying, viewModel.state.value)
    }

    @Test
    fun `init - when unauthenticated, state is Unauthenticated`() = runTest {
        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Error("Not authenticated")

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        assertEquals(PlayerUiState.Unauthenticated, viewModel.state.value)
    }

    @Test
    fun `init - when playing and has lyrics, state is HasLyrics`() = runTest {
        val song = Song("Song Title", "Artist Name", "albumArt")
        val lyrics = Lyrics(1L, 1L, "Original Lyrics", "", null, null)

        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Playing(song)
        coEvery { getLyricsUseCase(song.songName, song.artistName) } returns lyrics

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        assertEquals(PlayerUiState.HasLyrics(song), viewModel.state.value)
    }

    @Test
    fun `init - when playing and no lyrics, loads sources and state is ShowingSources`() = runTest {
        val song = Song("Song Title", "Artist Name", "albumArt")
        val sources = listOf(LyricsSource("Source Title", "http://lyrics.com", null, false))
        val bookmarks = listOf("http://lyrics.com")

        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Playing(song)
        coEvery { getLyricsUseCase(song.songName, song.artistName) } returns null
        coEvery { searchLyricsSourcesUseCase(song.songName, song.artistName) } returns sources
        coEvery { playerRepository.getBookmarkedUrls(song.songName, song.artistName) } returns bookmarks

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        val expectedState = PlayerUiState.ShowingSources(
            song = song,
            sources = sources,
            bookmarkedUrls = bookmarks,
            bookmarkingUrl = null,
            extractingUrl = null
        )
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `toggleBookmark - success updates bookmarks`() = runTest {
        val song = Song("Song Title", "Artist Name", "albumArt")
        val source = LyricsSource("Source Title", "http://lyrics.com", null, false)
        val sources = listOf(source)
        val bookmarks = emptyList<String>()

        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Playing(song)
        coEvery { getLyricsUseCase(song.songName, song.artistName) } returns null
        coEvery { searchLyricsSourcesUseCase(song.songName, song.artistName) } returns sources
        coEvery { playerRepository.getBookmarkedUrls(song.songName, song.artistName) } returns bookmarks
        coEvery { toggleBookmarkUseCase(song.songName, song.artistName, source.url, source.title) } returns true

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        viewModel.toggleBookmark(source)

        val expectedState = PlayerUiState.ShowingSources(
            song = song,
            sources = sources,
            bookmarkedUrls = listOf(source.url),
            bookmarkingUrl = null,
            extractingUrl = null
        )
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `extractAndTranslate - success transitions to HasLyrics`() = runTest {
        val song = Song("Song Title", "Artist Name", "albumArt")
        val source = LyricsSource("Source Title", "http://lyrics.com", null, false)
        val sources = listOf(source)
        val bookmarks = emptyList<String>()

        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Playing(song)
        coEvery { getLyricsUseCase(song.songName, song.artistName) } returns null
        coEvery { searchLyricsSourcesUseCase(song.songName, song.artistName) } returns sources
        coEvery { playerRepository.getBookmarkedUrls(song.songName, song.artistName) } returns bookmarks
        coEvery { extractAndTranslateUseCase(source.url, song.songName, song.artistName) } returns mockk()

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        viewModel.extractAndTranslate(source)

        assertEquals(PlayerUiState.HasLyrics(song), viewModel.state.value)
    }

    @Test
    fun `extractAndTranslate - failure emits ShowToast event`() = runTest {
        val song = Song("Song Title", "Artist Name", "albumArt")
        val source = LyricsSource("Source Title", "http://lyrics.com", null, false)
        val sources = listOf(source)
        val bookmarks = emptyList<String>()
        val exceptionMessage = "Network timed out"

        coEvery { getCurrentSongUseCase() } returns CurrentSongResult.Playing(song)
        coEvery { getLyricsUseCase(song.songName, song.artistName) } returns null
        coEvery { searchLyricsSourcesUseCase(song.songName, song.artistName) } returns sources
        coEvery { playerRepository.getBookmarkedUrls(song.songName, song.artistName) } returns bookmarks
        coEvery { extractAndTranslateUseCase(source.url, song.songName, song.artistName) } throws Exception(exceptionMessage)

        val viewModel = PlayerViewModel(
            getCurrentSongUseCase,
            searchLyricsSourcesUseCase,
            toggleBookmarkUseCase,
            extractAndTranslateUseCase,
            getLyricsUseCase,
            playerRepository
        )

        viewModel.uiEvent.test {
            viewModel.extractAndTranslate(source)
            val event = awaitItem()
            assertTrue(event is PlayerUiEvent.ShowToast)
            assertEquals(exceptionMessage, (event as PlayerUiEvent.ShowToast).message)
            cancelAndConsumeRemainingEvents()
        }
    }
}
