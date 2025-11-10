package brava.fightinwords

import com.google.common.reflect.TypeToken
import kotlin.reflect.KClass

/**
 * @see KClass.isValue
 */
val TypeToken<*>.isValue get() = this.rawType.kotlin.isValue

/**
 * @see KClass.isData
 */
val TypeToken<*>.isData get() = this.rawType.kotlin.isData