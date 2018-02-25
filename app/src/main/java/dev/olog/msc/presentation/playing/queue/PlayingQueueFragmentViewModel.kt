package dev.olog.msc.presentation.playing.queue

import android.arch.lifecycle.ViewModel
import dev.olog.msc.R
import dev.olog.msc.domain.entity.PlayingQueueSong
import dev.olog.msc.domain.interactor.music.service.ObservePlayingQueueUseCase
import dev.olog.msc.domain.interactor.prefs.MusicPreferencesUseCase
import dev.olog.msc.presentation.model.DisplayableItem
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.asLiveData
import dev.olog.msc.utils.k.extension.mapToList
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class PlayingQueueFragmentViewModel(
        private val musicPreferencesUseCase: MusicPreferencesUseCase,
        observePlayingQueueUseCase: ObservePlayingQueueUseCase

) : ViewModel() {

    val data = Observable.merge(
            observePlayingQueueUseCase.execute().take(1),
            observePlayingQueueUseCase.execute().skip(1)
                    .debounce(1, TimeUnit.SECONDS))
            .distinctUntilChanged()
            .mapToList { it.toPlayingQueueDisplayableItem() }
            .asLiveData()

    val observeCurrentSongId  = musicPreferencesUseCase.observeLastIdInPlaylist()
            .skip(1)
            .asLiveData()

    fun getCurrentSongId(): Int = musicPreferencesUseCase.getLastIdInPlaylist()

    private fun PlayingQueueSong.toPlayingQueueDisplayableItem(): DisplayableItem {

        return DisplayableItem(
                R.layout.item_playing_queue,
                MediaId.songId(this.id),
                title,
                artist,
                image,
                true,
                isRemix,
                isExplicit,
                this.idInPlaylist.toString()
        )
    }
}