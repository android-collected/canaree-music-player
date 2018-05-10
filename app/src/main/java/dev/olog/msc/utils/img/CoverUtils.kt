package dev.olog.msc.utils.img

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import dev.olog.msc.R
import dev.olog.msc.presentation.theme.AppTheme
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.MediaIdCategory
import dev.olog.msc.utils.k.extension.tint

object CoverUtils {

    private val COLORS = listOf (
            intArrayOf(0xff_00_c9_ff.toInt(), 0xff_92_fe_9d.toInt()),
            intArrayOf(0xff_f5_4e_a2.toInt(), 0xff_ff_76_76.toInt()),
            intArrayOf(0xff_17_ea_d9.toInt(), 0xff_92_fe_9d.toInt()),
            intArrayOf(0xff_7b_43_97.toInt(), 0xff_dc_24_30.toInt()),
            intArrayOf(0xff_1c_d8_d2.toInt(), 0xff_93_ed_c7.toInt()),
            intArrayOf(0xff_1f_86_ef.toInt(), 0xff_56_41_db.toInt()),
            intArrayOf(0xff_f0_2f_c2.toInt(), 0xff_60_94_ea.toInt()),
            intArrayOf(0xff_00_d2_ff.toInt(), 0xff_3a_7b_d5.toInt()),
            intArrayOf(0xff_f8_57_a6.toInt(), 0xff_ff_58_58.toInt()),
            intArrayOf(0xff_aa_ff_a9.toInt(), 0xff_11_ff_bd.toInt()),
            intArrayOf(0xff_00_c6_ff.toInt(), 0xff_00_72_ff.toInt()),
            intArrayOf(0xff_43_ce_a2.toInt(), 0xff_18_5a_9d.toInt()),
            intArrayOf(0xff_B6_50_DB.toInt(), 0xff_28_73_E1.toInt()),
            intArrayOf(0xff_17_ea_d9.toInt(), 0xff_60_98_ea.toInt()),
            intArrayOf(0xFF_38_ee_7e.toInt(), 0xFF_13_9c_8e.toInt()),
            intArrayOf(0xFF_38_ce_dc.toInt(), 0xFF_5a_89_e5.toInt()),
            intArrayOf(0xFF_15_85_cb.toInt(), 0xFF_2a_36_b3.toInt()),
            intArrayOf(0xFF_99_4f_bb.toInt(), 0xFF_30_34_b3.toInt()),
            intArrayOf(0xFF_83_00_ff.toInt(), 0xFF_dd_00_ff.toInt()),
            intArrayOf(0xFF_df_26_74.toInt(), 0xFF_fe_4f_32.toInt()),
            intArrayOf(0xFF_84_04_81.toInt(), 0xFF_e2_60_92.toInt()),
            intArrayOf(0xFF_ff_60_62.toInt(), 0xFF_ff_96_66.toInt()),
            intArrayOf(0xFF_fc_4e_1b.toInt(), 0xFF_f8_b3_33.toInt()),
            intArrayOf(0xFF_f7_9f_32.toInt(), 0xFF_fc_ca_1c.toInt())
    ).shuffled()

    fun getGradient(context: Context, mediaId: MediaId): Drawable {
        return getGradient(context, mediaId.resolveId.toInt(), mediaId.resolveSource)
    }

    fun getGradient(context: Context, position: Int, source: Int = 2): Drawable {
        return get(context, position, getDrawable(source))
    }

    fun onlyGradient(context: Context, position: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, getDrawable(MediaIdCategory.SONGS.ordinal))!!.mutate() as LayerDrawable
        val gradient = drawable.getDrawable(0).mutate() as GradientDrawable

        val pos = (position) % COLORS.size
        gradient.colors = COLORS[Math.abs(pos)]
        return gradient
    }

    private fun get(context: Context, position: Int, @DrawableRes drawableRes: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, drawableRes)!!.mutate() as LayerDrawable
        val gradient = drawable.getDrawable(0) as GradientDrawable

        val icon = drawable.getDrawable(1) as Drawable

        if (AppTheme.isDarkTheme()){
            icon.tint(0xFF_88898c.toInt())
            gradient.colors = intArrayOf(0xff_282828.toInt(), 0xff_282828.toInt())
        } else {
            icon.tint(0xFF_26_26_26.toInt())
            val pos = (position) % COLORS.size
            gradient.colors = COLORS[Math.abs(pos)]
        }

        return drawable
    }

    @DrawableRes
    private fun getDrawable(source: Int): Int {
        when (source) {
            MediaIdCategory.FOLDERS.ordinal -> return R.drawable.placeholder_folder
            MediaIdCategory.PLAYLISTS.ordinal -> return R.drawable.placeholder_playlist
            MediaIdCategory.SONGS.ordinal -> return R.drawable.placeholder_musical_note
            MediaIdCategory.ALBUMS.ordinal -> return R.drawable.placeholder_album
            MediaIdCategory.ARTISTS.ordinal -> return R.drawable.placeholder_artist
            MediaIdCategory.GENRES.ordinal -> return R.drawable.placeholder_genre
        }
        throw IllegalArgumentException("invalid source $source")
    }

}