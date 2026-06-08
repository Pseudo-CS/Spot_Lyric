package com.spotlyric.app.presentation.manage

import app.cash.turbine.test
import com.spotlyric.app.domain.model.BookmarkedSong
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.repository.ManageRepository
import com.spotlyric.app.domain.usecase.DeleteSongUseCase
import com.spotlyric.app.domain.usecase.GetSongsUseCase
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getSongsUseCase: GetSongsUseCase = mockk()
    private val deleteSongUseCase: DeleteSongUseCase = mockk()
    private val searchLyricsSourcesUseCase: SearchLyricsSourcesUseCase = mockk()
    private val manageRepository: ManageRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - observes songs from getSongsUseCase`() = runTest {
        val songs = listOf(
            BookmarkedSong(1L, "Song A", "Artist A", "http://a.com", "Title A", true)
        )
        coEvery { getSongsUseCase.search("") } returns flowOf(songs)

        val viewModel = ManageViewModel(
            getSongsUseCase,
            deleteSongUseCase,
            searchLyricsSourcesUseCase,
            manageRepository
        )

        assertEquals(songs, viewModel.state.value.songs)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `search - updates search query and gets new songs`() = runTest {
        val query = "Taylor"
        val initialSongs = emptyList<BookmarkedSong>()
        val filteredSongs = listOf(
            BookmarkedSong(2L, "Lover", "Taylor Swift", "http://swift.com", "Lover", true)
        )

        coEvery { getSongsUseCase.search("") } returns flowOf(initialSongs)
        coEvery { getSongsUseCase.search(query) } returns flowOf(filteredSongs)

        val viewModel = ManageViewModel(
            getSongsUseCase,
            deleteSongUseCase,
            searchLyricsSourcesUseCase,
            manageRepository
        )

        viewModel.search(query)

        assertEquals(query, viewModel.state.value.searchQuery)
        assertEquals(filteredSongs, viewModel.state.value.songs)
    }

    @Test
    fun `deleteSong - calls deleteSongUseCase`() = runTest {
        val songId = 42L
        coEvery { getSongsUseCase.search("") } returns flowOf(emptyList())
        coEvery { deleteSongUseCase(songId) } returns Unit

        val viewModel = ManageViewModel(
            getSongsUseCase,
            deleteSongUseCase,
            searchLyricsSourcesUseCase,
            manageRepository
        )

        viewModel.deleteSong(songId)

        coVerify { deleteSongUseCase(songId) }
        assertNull(viewModel.state.value.deletingId)
    }

    @Test
    fun `startAiTranslate - success calls generateAiTranslation`() = runTest {
        val song = BookmarkedSong(1L, "Song A", "Artist A", "http://a.com", "Title A", true)

        coEvery { getSongsUseCase.search("") } returns flowOf(emptyList())
        coEvery { manageRepository.generateAiTranslation(1L, "http://a.com") } returns true

        val viewModel = ManageViewModel(
            getSongsUseCase,
            deleteSongUseCase,
            searchLyricsSourcesUseCase,
            manageRepository
        )

        viewModel.startAiTranslate(song)

        coVerify { manageRepository.generateAiTranslation(1L, "http://a.com") }
        assertNull(viewModel.state.value.aiTranslatingId)
    }

    @Test
    fun `startAiTranslate - failure emits ShowToast event`() = runTest {
        val song = BookmarkedSong(1L, "Song A", "Artist A", "http://a.com", "Title A", true)
        val exceptionMessage = "API Limit Exceeded"

        coEvery { getSongsUseCase.search("") } returns flowOf(emptyList())
        coEvery { manageRepository.generateAiTranslation(1L, "http://a.com") } throws Exception(exceptionMessage)

        val viewModel = ManageViewModel(
            getSongsUseCase,
            deleteSongUseCase,
            searchLyricsSourcesUseCase,
            manageRepository
        )

        viewModel.uiEvent.test {
            viewModel.startAiTranslate(song)
            val event = awaitItem()
            assertTrue(event is ManageUiEvent.ShowToast)
            assertEquals(exceptionMessage, (event as ManageUiEvent.ShowToast).message)
            cancelAndConsumeRemainingEvents()
        }
        assertNull(viewModel.state.value.aiTranslatingId)
    }
}
