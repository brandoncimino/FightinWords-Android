package brava.fightinwords.botlin

import kotlin.contracts.ExperimentalContracts

enum class BiTruth {
    A,
    B,
    Both,
    Neither;

    companion object {
        fun of(a: Boolean, b: Boolean) = when {
            a && b  -> Both
            a xor b -> when {
                a -> A; else -> B
            }

            else    -> Neither
        }

        @OptIn(ExperimentalContracts::class)
        fun <T : Any?> ofNullable(a: T?, b: T?): BiTruth {
            return of(a == null, b == null)
        }

        inline fun <A : Any, B : Any, OUT> handleNullable(
            a: A?,
            b: B?,
            ifA: (a: A) -> OUT,
            ifB: (b: B) -> OUT,
            ifBoth: (a: A, b: B) -> OUT,
            ifNeither: () -> OUT,
        ): OUT {
            return when (ofNullable(a, b)) {
                A       -> ifA(a!!)
                B       -> ifB(b!!)
                Both    -> ifBoth(a!!, b!!)
                Neither -> ifNeither()
            }
        }

        inline fun <T> of(a: T, b: T, condition: (T) -> Boolean) = of(condition(a), condition(b))
    }
}

sealed interface Duo<A, B> {
    companion object {
        fun <A : Any, B : Any> of(a: A?, b: B?): Duo<A, B> {
            return when {
                a == null && b == null -> Neither.of()
                a != null && b != null -> Both(a, b)
                else                   -> Either.of(a, b)
            }
        }

        fun <A : Any, B : Any> ofNullable(a: A?, b: B?): Duo<A, B> {
            return when (a) {
                null -> when (b) {
                    null -> Neither.of()
                    else -> JustB(b)
                }

                else -> when (b) {
                    null -> JustA(a)
                    else -> Both(a, b)
                }
            }
        }

        fun <A : Any, B : Any> Duo<A, B>.getValue(): Any {
            return when (this) {
                is JustA   -> this.a
                is JustB   -> this.b
                is Both    -> this.a to this.b
                is Neither -> Unit
            }
        }
    }
}

sealed interface Either<A, B> : Duo<A, B> {
    companion object {
        fun <A : Any, B : Any> of(a: A?, b: B?): Either<A, B> {
            val aNull = a == null
            val bNull = b == null

            if (aNull == bNull) {
                throw IllegalArgumentException(
                    """You must provided exactly 1 non-null value of 🅰️ OR 🅱️:
                        |🅰️ $a
                        |🅱️ $b
                    """.trimMargin()
                )
            }

            return when (a) {
                null -> JustB(b!!)
                else -> JustA(a)
            }
        }
    }
}

data class JustA<A, B>(val a: A) : Either<A, B>
data class JustB<A, B>(val b: B) : Either<A, B>

data class Both<A, B>(val a: A, val b: B) : Duo<A, B>

class Neither<A, B> private constructor() : Duo<A, B> {
    companion object {
        private val _instance = Neither<Any, Any>()

        @Suppress("UNCHECKED_CAST")
        fun <A : Any, B : Any> of(): Neither<A, B> = _instance as Neither<A, B>
    }
}