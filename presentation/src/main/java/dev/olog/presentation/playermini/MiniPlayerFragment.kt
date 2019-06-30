package dev.olog.presentation.playermini

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.core.math.MathUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.olog.media.*
import dev.olog.presentation.R
import dev.olog.presentation.base.BaseFragment
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.utils.expand
import dev.olog.presentation.utils.isCollapsed
import dev.olog.presentation.utils.isExpanded
import dev.olog.shared.extensions.*
import dev.olog.shared.theme.hasPlayerAppearance
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_mini_player.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MiniPlayerFragment : BaseFragment(){

    companion object {
        private const val TAG = "MiniPlayerFragment"
        private const val PROGRESS_BAR_INTERVAL = 250L
        private const val BUNDLE_IS_VISIBLE = "$TAG.BUNDLE_IS_VISIBLE"
    }

    @Inject lateinit var presenter: MiniPlayerFragmentPresenter

    private var seekBarDisposable: Disposable? = null

    private val media by lazyFast { requireActivity() as MediaProvider }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            view.toggleVisibility(it.getBoolean(BUNDLE_IS_VISIBLE), true)
        }
        val (modelTitle, modelSubtitle) = presenter.getMetadata()
        title.text = modelTitle
        artist.text = DisplayableItem.adjustArtist(modelSubtitle)

        coverWrapper.toggleVisibility(requireContext().hasPlayerAppearance().isMini(), true)
        title.isSelected = true

        media.observeMetadata()
                .subscribe(viewLifecycleOwner) {
                    title.text = it.getTitle()
                    presenter.startShowingLeftTime(it.isPodcast(), it.getDuration())
                    if (!it.isPodcast()){
                        artist.text = it.getArtist()
                    }
                    updateProgressBarMax(it.getDuration())
                    updateImage(it)
                }

        presenter.observeProgress
                .map { resources.getQuantityString(R.plurals.mini_player_time_left, it.toInt(), it) }
                .filter { text -> artist.text != text }
                .asLiveData()
                .subscribe(viewLifecycleOwner) {
                    artist.text = it
                }

        media.observePlaybackState()
                .filter { it.isPlaying()|| it.isPaused() }
                .distinctUntilChanged()
                .subscribe(viewLifecycleOwner) {
                    updateProgressBarProgress(it.position)
                    handleProgressBar(it.isPlaying(), it.playbackSpeed)
                }

        media.observePlaybackState()
                .map { it.state }
                .filter { it == PlaybackStateCompat.STATE_PLAYING ||
                        it == PlaybackStateCompat.STATE_PAUSED
                }.distinctUntilChanged()
                .subscribe(viewLifecycleOwner) { state ->

                    if (state == PlaybackStateCompat.STATE_PLAYING){
                        playAnimation(true)
                    } else {
                        pauseAnimation(true)
                    }
                }

        media.observePlaybackState()
                .map { it.state }
                .filter { state -> state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT ||
                        state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS }
                .map { state -> state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT }
                .subscribe(viewLifecycleOwner, this::animateSkipTo)

        presenter.skipToNextVisibility
                .subscribe(viewLifecycleOwner) {
                    next.updateVisibility(it)
                }

        presenter.skipToPreviousVisibility
                .subscribe(viewLifecycleOwner) {
                    previous.updateVisibility(it)
                }
    }

    override fun onResume() {
        super.onResume()
        getSlidingPanel()!!.addPanelSlideListener(slidingPanelListener)
        view?.setOnClickListener { getSlidingPanel()?.expand() }
        view?.toggleVisibility(!getSlidingPanel().isExpanded(), true)
        next.setOnClickListener { media.skipToNext() }
        playPause.setOnClickListener { media.playPause() }
        previous.setOnClickListener { media.skipToPrevious() }
    }

    override fun onPause() {
        super.onPause()
        getSlidingPanel()!!.removePanelSlideListener(slidingPanelListener)
        view?.setOnClickListener(null)
        next.setOnClickListener(null)
        playPause.setOnClickListener(null)
        previous.setOnClickListener(null)
    }

    override fun onStop() {
        super.onStop()
        seekBarDisposable.unsubscribe()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_IS_VISIBLE, getSlidingPanel().isCollapsed())
    }

    private fun playAnimation(animate: Boolean) {
        playPause.animationPlay(getSlidingPanel().isCollapsed() && animate)
    }

    private fun pauseAnimation(animate: Boolean) {
        playPause.animationPause(getSlidingPanel().isCollapsed() && animate)
    }

    private fun animateSkipTo(toNext: Boolean) {
        if (getSlidingPanel().isExpanded()) return

        if (toNext) {
            next.playAnimation()
        } else {
            previous.playAnimation()
        }
    }

    private fun updateProgressBarProgress(progress: Long) {
        progressBar.progress = progress.toInt()
    }

    private fun updateProgressBarMax(max: Long) {
        progressBar.max = max.toInt()
    }

    private fun updateImage(metadata: MediaMetadataCompat){
        if (!requireContext().hasPlayerAppearance().isMini()){
            return
        }
        bigCover.loadImage(metadata)
    }

    private fun handleProgressBar(isPlaying: Boolean, speed: Float) {
        seekBarDisposable.unsubscribe()
        if (isPlaying) {
            resumeProgressBar(speed)
        }
    }

    private fun resumeProgressBar(speed: Float) {
        seekBarDisposable = Observable
                .interval(PROGRESS_BAR_INTERVAL, TimeUnit.MILLISECONDS, Schedulers.computation())
                .subscribe({
                    progressBar.incrementProgressBy((PROGRESS_BAR_INTERVAL * speed).toInt())
                    presenter.updateProgress((progressBar.progress + (PROGRESS_BAR_INTERVAL * speed)).toLong())
                }, Throwable::printStackTrace)
    }


    private val slidingPanelListener = object : BottomSheetBehavior.BottomSheetCallback(){
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            view?.alpha = MathUtils.clamp(1 - slideOffset * 3f, 0f, 1f)
            view?.toggleVisibility(slideOffset <= .8f, true)
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            title.isSelected = newState == BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun provideLayoutId(): Int = R.layout.fragment_mini_player
}