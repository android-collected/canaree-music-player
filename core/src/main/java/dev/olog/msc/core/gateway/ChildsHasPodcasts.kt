package dev.olog.msc.core.gateway

import dev.olog.msc.core.entity.podcast.Podcast
import io.reactivex.Observable

interface ChildsHasPodcasts<in Param> {

    fun observePodcastListByParam(param: Param): Observable<List<Podcast>>

}