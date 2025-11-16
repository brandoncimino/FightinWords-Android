@file:OptIn(ExperimentalStdlibApi::class)

package brava.fightinwords

import android.util.Log
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.botlin.appendJoin
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.StringWord
import brava.fightinwords.gameplay.data.TinyLetter
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.Invokable
import com.google.common.reflect.TypeToken
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
import java.lang.reflect.WildcardType
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextUInt
import kotlin.random.nextULong
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

@ApiStatus.Experimental
class DataGenerator(
    private val simpleGenerators: ImmutableMap<TypeToken<*>, Generator<*>>,
    private val collectors: ImmutableMap<TypeToken<*>, CollectionCollector<*>>,
    private val middlemanConverters: ImmutableMap<TypeToken<*>, MiddlemanConverter<*, *>>,
    val config: Config,
) {
    data class Blogger(
        val prefix: String?,
        val tag: String?,
        val level: Int,
    ) {
        inline operator fun invoke(
            message: () -> Any,
        ) {
            when (prefix) {
                null -> blog(level, tag, message)
                else -> blog(level, tag) { "$prefix ${message()}" }
            }
        }
    }

    data class Config(
        val preferDefaultParameterValues: Boolean = false,
        val generateEmptyCollectionsIfNecessary: Boolean = false,
        val sequenceLengthRange: IntRange = 3..5,
    )

    data class Context(
        val config: Config,
        val random: Random,
        /**
         * If we are generating a [Sequence] to use with a [CollectionCollector], then this is the current index within that [Sequence].
         */
        val elementIndex: Int? = null,
    )

    class Builder {
        @PublishedApi
        internal val simpleGenerators = ImmutableMap.builder<TypeToken<*>, Generator<*>>()

        @PublishedApi
        internal val collectors = ImmutableMap.builder<TypeToken<*>, CollectionCollector<*>>()

        @PublishedApi
        internal val middlemanConverters =
            ImmutableMap.builder<TypeToken<*>, MiddlemanConverter<*, *>>()

        var builderConfig = Config()

        inline fun <reified T : Any> addGenerator(generator: Generator<T>): Builder {
            simpleGenerators.put(typeToken<T>(), generator)
            return this
        }

        inline fun <reified T : Any> addCollector(collector: CollectionCollector<T>): Builder {
            collectors.put(typeToken<T>(), collector)
            return this
        }

        inline fun <reified T : Any, reified MIDDLEMAN : Any> addMiddlemanConverter(noinline middlemanConverter: (MIDDLEMAN) -> T): Builder {
            middlemanConverters.put(
                typeToken<T>(), MiddlemanConverter(
                    typeToken<MIDDLEMAN>(),
                    middlemanConverter
                )
            )
            return this
        }

        internal fun build() = DataGenerator(
            simpleGenerators = simpleGenerators.buildKeepingLast(),
            collectors = collectors.buildKeepingLast(),
            middlemanConverters = middlemanConverters.buildKeepingLast(),
            config = builderConfig
        )
    }

    companion object {
        @Contract(pure = true)
        fun build(action: Builder.() -> Unit): DataGenerator {
            return Builder().apply(action).build()
        }

        @Contract(pure = true)
        fun DataGenerator.rebuild(action: Builder.() -> Unit): DataGenerator {
            val original = this
            return build {
                simpleGenerators.putAll(original.simpleGenerators)
                collectors.putAll(original.collectors)
                middlemanConverters.putAll(original.middlemanConverters)
                builderConfig = original.config

                apply(action)
            }
        }

        val basic: DataGenerator = build {
            addGenerator { it.random.nextInt() }
            addGenerator { it.random.nextBoolean() }
            addGenerator { it.random.nextLong() }
            addGenerator { it.random.nextFloat() }
            addGenerator { it.random.nextDouble() }
            addGenerator { it.random.nextBytes(1)[0] }
            addGenerator { it.random.nextUInt() }
            addGenerator { it.random.nextULong() }
            // addGenerator<UByte> { it.random.nextUBytes(1)[0] } // Excluded to avoid the need for an experimental opt-in
            addGenerator<Char> {
                val random = it.random
                repeat(3) {
                    val bmpChar = random.nextInt(0x0000, 0x10000).toChar()
                    if (!bmpChar.isISOControl()) {
                        return@addGenerator bmpChar
                    }
                }

                // In the astronomical chance that we failed all of our random attempts, just give me a random A-z instead
                return@addGenerator random.nextInt('A'.code..'z'.code).toChar()
            }

            addGenerator { UUID.randomUUID() }
            addGenerator { UUID.randomUUID().toString() /* TODO: Get rid of this baby-mode crap */ }

            addCollector { ImmutableList.copyOf(it.iterator()) }
            addCollector { ImmutableSet.copyOf(it.iterator()) }


            //region My stuffs

            addGenerator<TinyLetter> { TinyLetter.random(it.random) }
            addGenerator<Letter> {
                val maxAttempts: Int = 10
                for (i in 0 until maxAttempts) {
                    val codePoint = it.random.nextInt(
                        Character.MIN_CODE_POINT..Character.MAX_CODE_POINT
                    )
                    if (Character.isLetter(codePoint)) {
                        val letter = Letter.of(codePoint)
                        blog { "Generated letter: $letter" }
                        return@addGenerator letter
                    }
                }
                val fallback = Letter.of('ß')
                blog { "Unable to generate an actual letter after $maxAttempts attempts; falling back to $fallback" }
                return@addGenerator fallback
            }

            addMiddlemanConverter<TinyWord, List<TinyLetter>> { it.toWord() as TinyWord }
            addMiddlemanConverter<StringWord, List<Letter>> {
                (it + Letter.of('ß')).toWord() as StringWord
            }


            addMiddlemanConverter<Word, List<Letter>> { it.toWord() }
            addGenerator<TinyFlags> {
                var tf = TinyFlags.none
                for (i in TinyFlags.MIN_FLAG..TinyFlags.MAX_FLAG) {
                    tf = tf.set(i, it.random.nextBoolean())
                }
                tf
            }

            //endregion
        }
    }

    @Contract(pure = true)
    inline fun <reified T : Any> generate(random: Random = Random): T {
        return generate(typeToken<T>(), random)
    }

    @Contract(pure = true)
    fun <T : Any> generate(outputType: TypeToken<T>, random: Random = Random): T {
        val generator = tryCreateGenerator(outputType, null)
                        ?: throw IllegalArgumentException("I can't generate instances of $outputType!")
        return generator(Context(config, random))
    }

    private fun <T : Any> tryCreateConstructorGenerator(
        outputType: TypeToken<T>,
        constructor: KFunction<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val primaryParameters = constructor.parameters

        val parameterGenerators = buildMap {
            for (param in primaryParameters) {
//                val paramTypeToken = outputType.resolveParameterType(constructor, param.index)
                val paramTypeToken = when {
                    param.type.classifier is KTypeParameter -> {
                        // If the parameter is generic, we need to resolve it in the context of the actual type we're trying to generate.
                        // If it is NOT, then we MUST NOT resolve it - doing so would require us to call `.javaType`, which would
                        //   return the "inlined" version of a `@JvmInline` type, which is NOT COMPATIBLE with `.callBy()`.
                        // See: https://youtrack.jetbrains.com/issue/KT-64097
                        val resolved = outputType.resolveType(param.type.javaType)
                        blogger { "Resolving the parameter type ${param.type.javaType} in the context of $outputType to -> $resolved" }
                        resolved
                    }

                    param.type.jvmErasure.isValue           -> {
                        outputType.resolveType(param.type.jvmErasure.javaObjectType)
                    }

                    else                                    -> TypeToken.of(param.type.javaType)
                }

                blogger {
                    """
                    param.type:            [${param.type::class}] ${param.type}
                    param.type.jvmErasure: ${param.type.jvmErasure}
                    param.type.javaType:   ${param.type.javaType}
                    paramTypeToken:        ${paramTypeToken}
                    outputType.resolveType(param.type.jvmErasure.javaObjectType)
                        ${outputType.resolveType(param.type.jvmErasure.javaObjectType)}
                    param.type.classifier: ${param.type.classifier}
                     > ${param.type.classifier!!::class}
                """.trimIndent()
                }
                blogger {
                    "  TypeToken.of(param.type.jvmErasure.javaObjectType) = ${
                        TypeToken.of(
                            param.type.jvmErasure.javaObjectType
                        )
                    }"
                }

                param.kind

                val paramGen = tryCreateGenerator(paramTypeToken, blogger)
                if (paramGen == null) {
                    if (param.isOptional) {
                        blogger { "Couldn't create a generator for the $outputType primary constructor parameter $param, but that parameter is optional, so we'll be able to use the default value." }
                    } else {
                        blogger {
                            "Couldn't create a generator for the REQUIRED $outputType primary constructor parameter ${param.type}, so we won't be able to construct $outputType this way!"
                        }
                        return null
                    }
                } else {
                    put(param, paramGen)
                }
            }
        }

        return Generator { context ->
            val argMap = buildMap {
                for ((param, generator) in parameterGenerators) {
                    if (param.isOptional && config.preferDefaultParameterValues) {
                        blogger { "Using the default value for the $outputType primary constructor parameter $param - even though we _could_ generate it - because of the config: $config" }
                        continue
                    }

                    val generated = generator(context)
                    put(param, generated)
                }
            }

            blogger {
                """Constructing $outputType with the arguments:
                |${
                    argMap.entries.joinToString(separator = "\n") {
                        """
                      $it
                        ${it.value!!::class}
                        ${it.value}
                """.trimIndent()
                    }
                }
            """.trimMargin()
            }

            constructor.callBy(argMap)
        }
    }

    fun <T : Any> tryCreateGenerator(
        outputType: TypeToken<T>,
        outerBlogger: Blogger?,
    ): Generator<T>? {
        val blogger: Blogger = outerBlogger?.copy(
            prefix = createSubIndent(
                currentIndent = outerBlogger.prefix ?: "",
                subIndent = "[${outputType.shortName()}]",
                outdent = "↳ "
            )
        )
                               ?: Blogger(
                                   outputType.shortName(),
                                   DataGenerator::class.simpleName,
                                   Log.DEBUG
                               )

        if (outputType.type is WildcardType) {
            blogger { "Cannot generate wildcard types like `$outputType`!" }
            return null
        }
        if (outputType in forbiddenTypes) {
            blogger { "Cannot generate `$outputType` because it is one of the forbidden types: $forbiddenTypes" }
            return null
        }

        val result = sequenceOf<() -> Generator<T>?>(
            { findSimpleGenerator(outputType, blogger) },
            { tryCreateMiddlemanagedGenerator(outputType, blogger) },
            { tryCreateCollectionGenerator(outputType, blogger) },
            { tryCreateEnumGenerator(outputType, blogger) },
            { tryCreatePrimaryConstructorGenerator(outputType, blogger) },
            { tryCreateSealedTypeGenerator(outputType, blogger) },
        ).firstNotNullOfOrNull { it() }

        if (result == null) {
            blogger { "Unable to create ANY kind of generator for: $outputType" }
            return null
        }

        blogger { "Created $result to generate $outputType" }
        return result
    }

    private fun <T : Any> findSimpleGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val found = simpleGenerators.findByTypeOrSubtype(outputType.wrap())
        if (found == null) {
            blogger { "No dedicated simple generator for the type: $outputType" }
            return null
        }

        @Suppress("UNCHECKED_CAST")
        val foundCast = found as Generator<T>

        return foundCast
    }

    private fun <T : Any> tryCreatePrimaryConstructorGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val kClass: KClass<in T> = outputType.rawType.kotlin
        if (kClass.isData || kClass.isValue) {
            @Suppress("UNCHECKED_CAST")
            val constructorGenerator = tryCreateConstructorGenerator(
                outputType,
                kClass.primaryConstructor as KFunction<T>,
                blogger
            )

            return constructorGenerator
        }

        return null
    }

    private fun <T : Any> tryCreateMiddlemanagedGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        @Suppress("UNCHECKED_CAST")
        val middlemanConverter =
            middlemanConverters.findByTypeOrSubtype(outputType) as MiddlemanConverter<T, *>?
        if (middlemanConverter == null) {
            blogger { "No ${MiddlemanConverter::class.simpleName} for the type: $outputType" }
            return null
        }

        blogger { "Attempting to generate the middleman ${middlemanConverter.middlemanType} and then convert it to $outputType" }
        // Not really sure why, but I can't inline this 🤷‍♀️
        return tryCreateMiddlemanagedGenerator(middlemanConverter, blogger)
    }

    private fun <T : Any> tryCreateCollectionGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val collector =
            findCollector(outputType)
        if (collector == null) {
            blogger { "No ${CollectionCollector::class.simpleName} for the type: $outputType" }
            return null
        }

        blogger { "Attempting to use ${CollectionCollector::class.simpleName} $collector to generate $outputType" }
        return tryCreateCollectionGenerator(outputType, collector, blogger)
    }

    private fun <T : Any> tryCreateCollectionGenerator(
        outputType: TypeToken<T>,
        collector: CollectionCollector<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val outputElementType = outputType.getActualGenericTypeArgument(Collection::class)
        val elementGenerator =
            tryCreateGenerator(outputElementType.resolveType(outputElementType.rawType), blogger)

        if (elementGenerator == null) {
            if (config.generateEmptyCollectionsIfNecessary) {
                blogger { "Unable to generate elements of type $outputElementType, so we'll just generate empty $outputType collections instead." }
                return Generator { collector.createEmpty() }
            }
            blogger { "Unable to generate elements of type $outputElementType, which means we can't generate collections of $outputType, either!" }
            return null
        } else {
            blogger { "Generating elements of type $outputElementType, and then collecting them into $outputType" }
            return collector.createGenerator(elementGenerator)
        }
    }

    private fun <T : Any> findCollector(outputCollectionType: TypeToken<T>): CollectionCollector<T>? {
        @Suppress("UNCHECKED_CAST")
        return collectors.entries
            .firstOrNull {
                outputCollectionType.rawType.isAssignableFrom(it.key.rawType)
            }?.value as CollectionCollector<T>?
    }

    private fun <T : Any, M : Any> tryCreateMiddlemanagedGenerator(
        middlemanConverter: MiddlemanConverter<T, M>,
        blogger: Blogger,
    ): Generator<T>? {
        val middlemanType = middlemanConverter.middlemanType

        val middlemanGenerator = tryCreateGenerator(middlemanType, blogger)

        if (middlemanGenerator == null) {
            blogger {
                "Unable to generate the middleman type $middlemanType, so we can't use the middleman converter $middlemanConverter"
            }
            return null
        }

        return Generator {
            val middleman = middlemanGenerator.generate(it)
            middlemanConverter.convertMiddleman(middleman)
        }
    }

    private fun <T : Any> tryCreateEnumGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        if (outputType.rawType.isEnum == false) {
            blogger {
                "$outputType is NOT an enum type"
            }
            return null
        }

        val enumEntries = outputType.rawType.enumConstants as Array<T>
        return Generator { enumEntries.random(it.random) }
    }

    private fun <T : Any> tryCreateSealedTypeGenerator(
        outputType: TypeToken<T>,
        blogger: Blogger,
    ): Generator<T>? {
        val sealedSubtypes = outputType.rawType.permittedSubclasses

        if (sealedSubtypes.isNullOrEmpty()) {
            blogger {
                "$outputType is NOT a sealed type"
            }
            return null
        }

        val subtypeGenerators = sealedSubtypes.map {
            val typedSubtype = outputType.getSubtype(it)
            val subtypeGenerator = tryCreateGenerator(typedSubtype, blogger)
            if (subtypeGenerator == null) {
                blogger {
                    "Cannot generate instances of the sealed type $outputType's subtype $typedSubtype! To generate $outputType, we must be able to generate it directly, OR generate all of its permitted subtypes:\n${
                        sealedSubtypes.joinToString(
                            "\n"
                        )
                    }"
                }
                return null
            }
            return@map subtypeGenerator
        }

        return Generator { subtypeGenerators.random(it.random).generate(it) }
    }
}

fun <T : Any, V> Map<TypeToken<*>, V>.findByTypeOrSubtype(outputType: TypeToken<T>): V? {
    val exactMatch = get(outputType)
    if (exactMatch != null) {
        return exactMatch
    }

    return entries.find { (generatedType, _) ->
        outputType.isSupertypeOf(generatedType)
    }?.value
}

inline fun <reified T : Any> typeToken() = object : TypeToken<T>() {}

inline fun <reified T : Any> outTypeToken() =
    (object : TypeToken<List<T>>() {}).getActualGenericTypeArgument(List::class)

fun interface Generator<T : Any> {
    fun generate(context: DataGenerator.Context): T
}

operator fun <T : Any> Generator<T>.invoke(context: DataGenerator.Context): T {
    return generate(context)
}

fun interface CollectionCollector<SELF> where SELF : Any {
    fun collect(sequence: Sequence<Any>): SELF

    fun createEmpty(): SELF = collect(sequenceOf())
}

private fun <T : Any> CollectionCollector<T>.createGenerator(
    elementGenerator: Generator<*>,
): Generator<T> {
    return Generator { context ->
        val sequenceLength = context.config.sequenceLengthRange.random(context.random)
        val sequenceIndices = 0 until sequenceLength
        val sequence = sequenceIndices.asSequence().map {
            elementGenerator.generate(
                context.copy(elementIndex = it)
            )
        }

        collect(sequence)
    }
}

fun TypeToken<*>.getActualGenericTypeArgument(
    genericSupertype: KClass<*>,
    genericParameterIndex: Int = 0,
): TypeToken<*> {
    val typeParam = genericSupertype.java.typeParameters[genericParameterIndex]
    return this.resolveType(typeParam)
}

class MiddlemanConverter<T : Any, MIDDLEMAN : Any>(
    val middlemanType: TypeToken<MIDDLEMAN>,
    val convertMiddleman: (MIDDLEMAN) -> T,
)

private val forbiddenTypes = ImmutableSet.of(
    typeToken<Any>(),
    typeToken<Object>(),
)

fun TypeToken<*>.resolveParameterType(function: KFunction<*>, parameterIndex: Int): TypeToken<*> {
    val javaMethodParameter =
        (function.javaMethod ?: function.javaConstructor)!!.parameters[parameterIndex]
    val javaParameterType = javaMethodParameter.type
    val resolvedParameterType = this.resolveType(javaParameterType)
    return resolvedParameterType
}

fun <T : Any> TypeToken<T>.getValueTypeBoxer(): Invokable<T, T> {
    require(isValue) { "$this is not a value type!" }

    val boxMethod = this.rawType.declaredMethods.first { it.name == "box-impl" }
    return method(boxMethod).returning(this)
}

private fun createSubIndent(
    currentIndent: CharSequence,
    subIndent: Any,
    outdent: CharSequence = "",
): String {
    return buildString {
        val spaceBeforeOutdent = currentIndent.length - outdent.length

        for (i in currentIndent.indices) {
            if (i >= spaceBeforeOutdent) {
                append(outdent[i - spaceBeforeOutdent])
            } else {
                append(' ')
            }
        }

        append(subIndent)
    }
}

private fun <T : Any> TypeToken<T>.shortName(depth: Int = 2): String {
    val type = this
    return buildString {
        val outerType = type.rawType.declaringClass
        if (outerType != null) {
            append(TypeToken.of(outerType).shortName(depth - 1))
            append('.')
        }

        append(type.rawType.simpleName)

        appendJoin(
            stuff = type.rawType.typeParameters.asIterable(),
            element = {
                append(
                    type.resolveType(it).shortName(depth - 1)
                )
            },
            separator = { append(',') },
            prefix = { append('<') },
            suffix = { append('>') }
        )
    }
}