package dev.olog.msc.presentation.recently.added.di

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dev.olog.msc.core.MediaId
import dev.olog.msc.core.dagger.qualifier.FragmentLifecycle
import dev.olog.msc.presentation.base.ViewModelKey
import dev.olog.msc.presentation.recently.added.RecentlyAddedFragment
import dev.olog.msc.presentation.recently.added.RecentlyAddedFragmentViewModel

@Module
class RecentlyAddedFragmentModule(
    private val fragment: RecentlyAddedFragment
) {

    @Provides
    @FragmentLifecycle
    internal fun lifecycle(): Lifecycle = fragment.lifecycle

    @Provides
    internal fun provideMediaId(): MediaId {
        val mediaId = fragment.arguments!!.getString(RecentlyAddedFragment.ARGUMENTS_MEDIA_ID)
        return MediaId.fromString(mediaId)
    }

    @Module
    companion object {

        @Provides
        @JvmStatic
        @IntoMap
        @ViewModelKey(RecentlyAddedFragmentViewModel::class)
        internal fun provideViewModel(viewModel: RecentlyAddedFragmentViewModel): ViewModel {
            return viewModel
        }

    }

}