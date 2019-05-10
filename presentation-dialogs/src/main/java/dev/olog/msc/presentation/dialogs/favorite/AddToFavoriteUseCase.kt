package dev.olog.msc.presentation.dialogs.favorite

import dev.olog.msc.core.MediaId
import dev.olog.msc.core.entity.favorite.FavoriteType
import dev.olog.msc.core.executors.IoScheduler
import dev.olog.msc.core.gateway.FavoriteGateway
import dev.olog.msc.core.interactor.GetSongListByParamUseCase
import dev.olog.msc.core.interactor.base.CompletableUseCaseWithParam
import dev.olog.msc.shared.extensions.mapToList
import io.reactivex.Completable
import javax.inject.Inject

class AddToFavoriteUseCase @Inject constructor(
        scheduler: IoScheduler,
        private val favoriteGateway: FavoriteGateway,
        private val getSongListByParamUseCase: GetSongListByParamUseCase

) : CompletableUseCaseWithParam<AddToFavoriteUseCase.Input>(scheduler) {

    override fun buildUseCaseObservable(param: Input): Completable {
        val mediaId = param.mediaId
        val type = param.type
        if (mediaId.isLeaf) {
            val songId = mediaId.leaf!!
            return favoriteGateway.addSingle(type, songId)
        }

        return getSongListByParamUseCase.execute(mediaId)
                .firstOrError()
                .mapToList { it.id }
                .flatMapCompletable { favoriteGateway.addGroup(type, it) }
    }

    class Input(
            val mediaId: MediaId,
            val type: FavoriteType
    )

}