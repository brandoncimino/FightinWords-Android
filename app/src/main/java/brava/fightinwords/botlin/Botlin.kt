package brava.fightinwords.botlin

import android.os.Bundle
import android.util.Log
import kotlinx.serialization.json.Json

inline fun <T : Any> T.blog(level: Int = Log.INFO, tag: String = javaClass.simpleName, message: () -> Any?) {
    if (Log.isLoggable(tag, level)) {
        Log.println(level, tag, message().toString())
    }
}

inline fun <T> T.peekIfNull(action: () -> Unit): T {
    if (this == null) {
        action()
    }

    return this
}

inline fun <reified T : Any> Bundle.putJson(obj: T, key: String? = T::class.qualifiedName, jsonThingy: Json = Json) {
    val json = jsonThingy.encodeToString<T>(obj)
    blog {
        """Saving the ${this.javaClass.simpleName} key `$key` with the ${T::class.simpleName}-JSON:
        | ```json
        | $json
        | ```""".trimMargin()
    }
    putString(key, json)
}

inline fun <reified T : Any> Bundle.readJson(): T? = readJson<T>(T::class.qualifiedName)

inline fun <reified T : Any> Bundle.readJson(key: String? = T::class.qualifiedName, jsonThingy: Json = Json): T? {
    val json = this.getString(key)

    if (json == null) {
        blog { "The key `$key` wasn't present in the ${this.javaClass.simpleName}!" }
        return null
    }

    return jsonThingy.decodeFromString(json)
}

val IntRange.size: Int get() = endInclusive - start + 1

inline fun <I, O, O2> ((I) -> O).andThen(crossinline nextStep: (O) -> O2): (I) -> O2 {
    return {
        val firstResult = this(it)
        nextStep(firstResult)
    }
}

inline fun <O, O2> (() -> O).andThen(crossinline nextStep: (O) -> O2): () -> O2 {
    return {
        nextStep(this())
    }
}

inline fun <I, O> ((I) -> O).andAlso(crossinline sideAction: (O) -> Any): (I) -> O {
    return {
        val firstResult = this(it)
        sideAction(firstResult)
        firstResult
    }
}

inline fun <O> (() -> O).andAlso(crossinline sideAction: (O) -> Any): () -> O {
    return {
        val firstResult = this()
        sideAction(firstResult)
        firstResult
    }
}