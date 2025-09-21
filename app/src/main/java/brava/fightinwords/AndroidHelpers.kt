package brava.fightinwords

import android.content.Context
import android.os.Bundle
import brava.fightinwords.botlin.blog
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
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

/**
 * Saves an instance of [T] to this [Bundle] using your favorite [SerialFormat].
 *
 * @param obj The thing you want to save, which must be [kotlinx.serialization.Serializable].
 * @param key Uniquely identifies the instance within the [Bundle].
 * @param serialFormat Reads & writes things using a certain format, like [kotlinx.serialization.json.Json].
 */
internal inline fun <reified T : Any> Bundle.writeObject(
    key: String,
    obj: T,
    serialFormat: SerialFormat,
) {
    return when (serialFormat) {
        is BinaryFormat -> putByteArray(
            key,
            serialFormat.encodeToByteArray(obj)
        )

        is StringFormat -> putString(
            key,
            serialFormat.encodeToString(obj)
        )

        else            -> throw IllegalArgumentException("I don't know how to save $serialFormat!")
    }
}

/**
 * Similar to [writeObject], but assumes that there is only ever going to be at most 1 instance of [T] in the [Bundle].
 */
internal inline fun <reified T : Any> Bundle.putSingleton(
    obj: T,
    serialFormat: SerialFormat,
) {
    return writeObject(
        getSingletonName<T>(),
        obj,
        serialFormat
    )
}

private inline fun <reified T> getSingletonName() = T::class.qualifiedName
                                                    ?: throw IllegalArgumentException("The type ${T::class} cannot be used as a serialized 'singleton' because it doesn't have a qualified name!")

inline fun <reified T : Any> Bundle.getObject(key: String, serialFormat: SerialFormat): T? {
    return when (serialFormat) {
        is StringFormat -> {
            val str = this.getString(key)
            if (str == null) {
                blog { "The key `$key` wasn't present in the ${this.javaClass.simpleName}!" }
                return null
            }

            serialFormat.decodeFromString<T>(str)
        }

        is BinaryFormat -> {
            val bytes = this.getByteArray(key)
            if (bytes == null) {
                blog { "The key `$key` wasn't present in the ${this.javaClass.simpleName}!" }
                return null
            }

            serialFormat.decodeFromByteArray(bytes)
        }

        else            -> {
            throw IllegalArgumentException("I don't know how to handle $serialFormat!")
        }
    }
}

internal inline fun <reified T : Any> Bundle.getSingleton(serialFormat: SerialFormat): T? {
    return getObject(
        getSingletonName<T>(),
        serialFormat
    )
}