package kr.co.bepo.geofencingapp.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.bepo.geofencingapp.util.Constants.DATABASE_TABLE_NAME

@Entity(tableName = DATABASE_TABLE_NAME)
class GeofenceEntity(
    val geoId: Long,
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val snapshot: Bitmap
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}