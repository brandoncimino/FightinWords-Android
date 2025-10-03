package brava.fightinwords.botlin

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.reflect.KProperty

inline fun <T : Any> T.blog(level: Int = Log.INFO, tag: String? = javaClass.simpleName, message: () -> Any?) {
    if (Log.isLoggable(tag, level)) {
        Log.println(level, tag, message().toString())
    }
}

inline fun <reified T> T.andBlog(
    level: Int = Log.INFO,
    message: (T) -> Any? = { it },
): T {
    val self = this
    Unit.blog(level = level, tag = T::class.simpleName) {
        message(self)
    }
    return self
}

class BlogTimeline<PROP>(
    var value: PROP,
    val blogLevel: Int = Log.INFO,
) {
    operator fun getValue(owner: Any?, kProperty1: KProperty<*>) = value

    operator fun setValue(owner: Any?, kProperty1: KProperty<*>, newValue: PROP) {
        blog { "Setting ${this.value} -> $newValue" }
        val oldValue = this.value
        if (oldValue != newValue) {
            this.value = newValue
            blog(blogLevel) { "Set $oldValue -> ${this.value}" }
        } else {
            blog { "No change from $oldValue" }
        }
    }

    companion object {
        fun <PROP> Any.blogged(initialValue: PROP): BlogTimeline<PROP> {
            return BlogTimeline(initialValue)
        }

    }
}


inline fun <T> T.peekIfNull(action: () -> Unit): T {
    if (this == null) {
        action()
    }

    return this
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


fun <T : Comparable<T>> T.constrain(
    min: T?,
    max: T?,
): T {
    if (min != null && min > this) {
        return min
    }

    if (max != null && max < this) {
        return max
    }

    return this
}

fun File.getMemoryMappedBuffer(): MappedByteBuffer {
    val randomAccessFile = RandomAccessFile(this, "r")
    val channel = randomAccessFile.channel
    return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
}

fun <T> sequenceOfNotNull(element: T?): Sequence<T> {
    return when (element) {
        null -> sequenceOf()
        else -> sequenceOf(element)
    }
}

fun <T> sequenceOfNotNull(vararg elements: T?): Sequence<T> {
    return sequenceOf(*elements).filterNotNull()
}