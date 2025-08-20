package brava.fightinwords

import android.content.Context
import java.io.File

internal fun Context.getCachedAssetFile(assetName: String): File {
    val cacheFile = File(this.cacheDir, assetName)

    // Copy only if not already cached
    if (!cacheFile.exists()) {
        this.assets.open(assetName).use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    return cacheFile
}