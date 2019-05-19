package dev.olog.msc.data.repository.queries

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore.Audio.Media.*
import dev.olog.contentresolversql.querySql
import dev.olog.msc.core.MediaIdCategory
import dev.olog.msc.core.entity.data.request.Filter
import dev.olog.msc.core.entity.data.request.Request
import dev.olog.msc.core.gateway.prefs.AppPreferencesGateway
import dev.olog.msc.core.gateway.prefs.SortPreferencesGateway

class FolderQueries(
    prefsGateway: AppPreferencesGateway,
    sortGateway: SortPreferencesGateway,
    private val contentResolver: ContentResolver
) : BaseQueries(prefsGateway, sortGateway,false) {


    fun getAll(request: Request?): Cursor {
        val (filter, bindParams) = createFilter(request?.filter, overrideTitleColumn = Columns.FOLDER)

        val query = """
            SELECT distinct $folderProjection as ${Columns.FOLDER}, count(*) as ${Columns.N_SONGS}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection()} $filter
            GROUP BY ${Columns.FOLDER}
            ORDER BY ${sortOrder()}
            ${tryGetChunk(request?.page)}
        """

        return contentResolver.querySql(query, bindParams)
    }

    fun getByPath(folderPath: String): Cursor {
        val query = """
            SELECT distinct $folderProjection as ${Columns.FOLDER}, count(*) as ${Columns.N_SONGS}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${Columns.FOLDER} = ? AND ${defaultSelection()}
            GROUP BY ${Columns.FOLDER}
        """

        return contentResolver.querySql(query, arrayOf(folderPath))
    }

    fun getSongList(folderPath: String, request: Request?): Cursor {
        val (filter, bindParams) = createFilter(request?.filter)

        val query = """
            SELECT $_ID, $ARTIST_ID, $ALBUM_ID,
                $TITLE,
                $artistProjection as ${Columns.ARTIST},
                $albumProjection as ${Columns.ALBUM},
                $albumArtistProjection,
                $DURATION, $DATA, $YEAR,
                $discNumberProjection as ${Columns.N_DISC},
                $trackNumberProjection as ${Columns.N_TRACK},
                $DATE_ADDED, $IS_PODCAST,
                $folderProjection as ${Columns.FOLDER}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection()} AND ${Columns.FOLDER} = ? $filter
            ORDER BY ${songListSortOrder(MediaIdCategory.FOLDERS, DEFAULT_SORT_ORDER)}
            ${tryGetChunk(request?.page)}
        """
        return contentResolver.querySql(query, arrayOf(folderPath).plus(bindParams))
    }

    fun getSongListDuration(folderPath: String, filterRequest: Filter?): Cursor {
        val (filter, bindParams) = createFilter(filterRequest, overrideTitleColumn = folderProjection)

        val query = """
            SELECT sum($DURATION)
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection()} AND $folderProjection = ? $filter
        """
        return contentResolver.querySql(query, arrayOf(folderPath).plus(bindParams))
    }

    fun getRecentlyAddedSongs(folderPath: String, request: Request?): Cursor {

        val query = """
            SELECT $_ID, $ARTIST_ID, $ALBUM_ID,
                $TITLE,
                $artistProjection as ${Columns.ARTIST},
                $albumProjection as ${Columns.ALBUM},
                $albumArtistProjection,
                $DURATION, $DATA, $YEAR,
                $discNumberProjection as ${Columns.N_DISC},
                $trackNumberProjection as ${Columns.N_TRACK},
                $DATE_ADDED, $IS_PODCAST,
                $folderProjection as ${Columns.FOLDER}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection()} AND ${isRecentlyAdded()} AND ${Columns.FOLDER} = ?
            GROUP BY ${Columns.FOLDER}
            ORDER BY $DATE_ADDED DESC
            ${tryGetChunk(request?.page)}
        """
        return contentResolver.querySql(query, arrayOf(folderPath))
    }

    fun getSiblings(folderPath: String, request: Request?): Cursor {
        val (filter, bindParams) = createFilter(request?.filter, overrideTitleColumn = Columns.FOLDER)

        val query = """
            SELECT distinct $folderProjection as ${Columns.FOLDER}, count(*) as ${Columns.N_SONGS}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${Columns.FOLDER} <> ? AND ${defaultSelection()} $filter
            GROUP BY ${Columns.FOLDER}
            ORDER BY ${sortOrder()}
            ${tryGetChunk(request?.page)}
        """
        return contentResolver.querySql(query, arrayOf(folderPath).plus(bindParams))
    }

    fun getRelatedArtists(folderPath: String, request: Request?): Cursor {
        val (filter, bindParams) = createFilter(request?.filter)

        val query = """
            SELECT distinct $ARTIST_ID,
                $artistProjection as ${Columns.ARTIST},
                $albumArtistProjection,
                count(*) as ${Columns.N_SONGS},
                count(distinct $ALBUM_ID) as ${Columns.N_ALBUMS},
                $folderProjection as ${Columns.FOLDER}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${Columns.FOLDER} = ? AND ${defaultSelection()} AND $ARTIST <> '<unknown>'
                $filter
            GROUP BY $ARTIST_ID
            ORDER BY $ARTIST_KEY
            ${tryGetChunk(request?.page)}
        """
        return contentResolver.querySql(query, arrayOf(folderPath).plus(bindParams))
    }

    fun getExisting(folderPath: String, songIds: String): Cursor {
        val query = """
            SELECT $_ID, $ARTIST_ID, $ALBUM_ID,
                $TITLE,
                $artistProjection as ${Columns.ARTIST},
                $albumProjection as ${Columns.ALBUM},
                $albumArtistProjection,
                $DURATION, $DATA, $YEAR,
                $discNumberProjection as ${Columns.N_DISC},
                $trackNumberProjection as ${Columns.N_TRACK},
                $DATE_ADDED, $IS_PODCAST,
                $folderProjection as ${Columns.FOLDER}
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection()} AND ${Columns.FOLDER} = ? AND $_ID in ($songIds)
        """
        return contentResolver.querySql(query, arrayOf(folderPath))
    }

    private fun defaultSelection(): String{
        return "${isPodcast()} AND ${notBlacklisted()}"
    }

    private fun sortOrder(): String {
        return "lower(${Columns.FOLDER}) COLLATE UNICODE"
    }


}