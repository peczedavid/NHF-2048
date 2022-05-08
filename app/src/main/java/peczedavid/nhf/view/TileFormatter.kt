package peczedavid.nhf.view

import android.content.Context
import android.content.res.Resources
import android.widget.TextView
import androidx.core.content.ContextCompat
import peczedavid.nhf.R

class TileFormatter(
    private var packageName: String,
    private var context: Context,
    private var resources: Resources
) {

    fun formatTile(tile: TextView, value: Int) {
        if(value!=0)
            tile.text = value.toString()
        else
            tile.text = ""

        val uri = "@color/tile_${value}_color"
        val resource = resources.getIdentifier(uri, null, packageName)
        tile.setBackgroundColor(ContextCompat.getColor(context, resource))

        if(value <= 4) {
            tile.setTextColor(ContextCompat.getColor(context, R.color.tile_dark_text))
        } else {
            tile.setTextColor(ContextCompat.getColor(context, R.color.tile_light_text))
        }
    }
}