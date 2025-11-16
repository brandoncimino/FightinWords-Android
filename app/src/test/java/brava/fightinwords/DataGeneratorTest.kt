@file:OptIn(ExperimentalStdlibApi::class)

package brava.fightinwords

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.botlin.toImmutableList
import brava.fightinwords.gameplay.scoring.MultiSelectWordFilters
import com.google.common.collect.ForwardingList
import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.TypeToken
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assumptions
import org.junit.Test
import java.time.DayOfWeek
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod

data class TypedInstance<T : Any>(
    val type: TypeToken<T>,
    val instance: T,
)

class DataGeneratorTest {
    @Test
    fun cborRoundTrip() {

    }

    @Test
    fun typeTokenTest() {
        val stringListToken = typeToken<List<String>>()
        val resolveListClass = stringListToken.resolveType(List::class.java)
        println("resolveListClass = $resolveListClass")

        val resolveJavaTypeParameter =
            stringListToken.resolveType(List::class.java.typeParameters[0])
        println("resolveJavaTypeParameter = $resolveJavaTypeParameter")

        println("getListElementType<MyList>() = ${getListElementType<MyList>()}")
    }

    @Test
    fun getActualGenericTypeArgumentTest() {
        val scenarios = listOf(
            GetActualGenericTypeArgumentScenario(
                specificType = typeToken<WeirdList<Int, MyList>>(),
                genericType = Iterable::class,
                expectedTypeArgumentType = typeToken<MyList>()
            ),
            GetActualGenericTypeArgumentScenario(
                specificType = typeToken<WeirdList<*, List<List<MyList>>>>(),
                genericType = Iterable::class,
                expectedTypeArgumentType = typeToken<List<List<MyList>>>(),
            )
        )

        Assertions.assertThat(scenarios)
            .allSatisfy {
                val result = it.specificType.getActualGenericTypeArgument(
                    it.genericType,
                    it.genericTypeArgumentIndex
                )

                println(describeRelationship(result, it.expectedTypeArgumentType))

                Assertions.assertThat(result)
                    .describedAs {
                        describeRelationship(result, it.expectedTypeArgumentType)
                    }
                    .isNotEqualTo(typeToken<Any>())
                    .extracting { r -> r.isSubtypeOf(it.expectedTypeArgumentType) }
            }
    }

    fun printRelationship(a: TypeToken<*>, b: TypeToken<*>) {
        println(describeRelationship(a, b))
        printing()
    }

    fun describeRelationship(a: TypeToken<*>, b: TypeToken<*>): String {
        val aSuper = a.isSupertypeOf(b)
        val aSub = a.isSubtypeOf(b)
        val aEq = a == b

        if (aEq || aSub || aSuper) {
            return """
            $a ==      $b => ${a == b}    
            $a super   $b => ${a.isSupertypeOf(b)}
            $a extends $b => ${a.isSubtypeOf(b)}
            """.trimIndent()
        }

        val aRawSuper = a.rawType.isAssignableFrom(b.rawType)
        val aRawSub = b.rawType.isAssignableFrom(a.rawType)

        if (aRawSuper || aRawSub) {
            return """
                🥩 RAW:
                ${a.rawType} super ${b.rawType} => $aRawSuper
                ${a.rawType} sub   ${b.rawType} => $aRawSub
                """.trimIndent()
        }

        return "$a unrelated to $b"
    }

    class StringList : ArrayList<String>()

    @Test
    fun getByTypeOrChild() {
        val stringList = typeToken<StringList>()
        println("stringList.rawType = ${stringList.rawType}")
        val starList = typeToken<List<*>>()
        println("starList.rawType = ${starList.rawType}")
        val immutableList = typeToken<ImmutableList<*>>()
        val typeMap = mapOf(
//            stringList to stringList,
            starList to starList,
            immutableList to immutableList
        )

        val requestedType = typeToken<Collection<CharSequence>>()

        Assertions.assertThat(
            typeMap.values.findCompatibleType(requestedType)
        )
            .isEqualTo(starList)

        Assertions.assertThat(typeMap.values.findCompatibleType(typeToken<Collection<Int>>()))
            .isEqualTo(starList)

        Assertions.assertThat(
            typeMap.values.findCompatibleType(typeToken<ImmutableCollection<String>>())
        )
            .isEqualTo(immutableList)
    }

    val Boolean.icon
        get() = when (this) {
            true -> "✅"
            else -> "❌"
        }

    fun Iterable<TypeToken<*>>.findCompatibleType(
        desired: TypeToken<*>,
        vararg versions: Int,
    ): TypeToken<*> {
        return first {
            println("🅰 $desired vs. 🅱 $it:")

            val v1 = desired.isSupertypeOf(it.rawType)
            println("  🅰 >= 🅱.rawType -> ${v1.icon}")


            val v2 = desired.rawType.isAssignableFrom(it.rawType)
            println("  🅰.raw >= 🅱.raw -> ${v2.icon}")

            return@first (versions.isEmpty() || versions.contains(1)) && v1
                         || (versions.isEmpty() || versions.contains(2)) && v2
        }
    }

    data class GetActualGenericTypeArgumentScenario(
        val specificType: TypeToken<*>,
        val genericType: KClass<*>,
        val expectedTypeArgumentType: TypeToken<*>,
        val genericTypeArgumentIndex: Int = 0,
    )

    data class HasGeneratableStuff(
        val string: String,
        val uuid: UUID,
        val ints: ImmutableList<Int>,
        val dps: List<Dp>,
    )

    @Test
    fun dataGeneratorTestOhBoy() {
        val dataGenerator = DataGenerator.build {
            addGenerator { UUID.randomUUID() }
            addGenerator { "yolo" }
            addGenerator { 99 }
            addCollector { it.toHashSet() }
            addCollector { ArrayList(it.toList()) }
            addCollector<ImmutableList<*>> { it.toImmutableList() }
            addCollector { ImmutableSet.copyOf(it.iterator()) }
            addMiddlemanConverter { int: Int -> int.dp }
        }

        val outCharSeq = outTypeToken<CharSequence>()
        println("outTypeToken<CharSequence>() = $outCharSeq")
        println(
            "outCharSeq.isSupertypeOf(typeToken<String>()) = ${
                outCharSeq.isSupertypeOf(
                    typeToken<String>()
                )
            }"
        )


        val generatedString = dataGenerator.generate<String>()
        println("generatedString = $generatedString")
        Assertions.assertThat(generatedString)
            .isEqualTo("yolo")

//        val foundGenerator = dataGenerator.generators.findInstanceOrChild(typeToken<Generator<CharSequence>>())
//        printed("foundGenerator = ${foundGenerator}")

        val generatedCharSequence: CharSequence = dataGenerator.generate<CharSequence>()
        println("generatedCharSequence = $generatedCharSequence")
        Assertions.assertThat(generatedCharSequence)
            .isEqualTo("yolo")

        val generatedListOfCharSequences = dataGenerator.generate<List<CharSequence>>()
        println("generatedListOfCharSequences = $generatedListOfCharSequences")

        val result = dataGenerator.generate<List<String>>()

        println("result = $result")

        val generatedDp = dataGenerator.generate<Dp>()
        println("generatedDp = $generatedDp")

        Assertions.assertThat(generatedDp)
            .isEqualTo(99.dp)

        Assertions.assertThatCode {
            val generatedHasGeneratableStuff = dataGenerator.generate<HasGeneratableStuff>()
            println("generatedHasGeneratableStuff = $generatedHasGeneratableStuff")
        }.doesNotThrowAnyException()
    }

    @Test
    fun listAnySuper() {
        val listAny = typeToken<List<Any>>()
        val listString = typeToken<List<String>>()
        Assertions.assertThat(listAny.isSupertypeOf(listString))
        val immutableListString = typeToken<ImmutableList<String>>()
        Assertions.assertThat(listAny.isSupertypeOf(immutableListString))
    }

    @Test
    fun basicGeneratorTest() {
        val types = listOf(
            typeToken<Int>().wrap(),
            typeToken<Int>().unwrap(),
            typeToken<DayOfWeek>(),
            typeToken<TinyFlags>(),
            typeToken<HasInt>(),
            typeToken<HasIntValue>(),
            typeToken<HasStringValue>(),
            typeToken<HasGeneric<HasStringValue>>(),
            typeToken<HasTinyFlags>(),
            typeToken<MultiSelectWordFilters.MultiSelectSerializableState>(),
            typeToken<SaveGameState>()
        )

        Assertions.assertThat(types)
            .allSatisfy {
                Assertions.assertThatCode {
                    DataGenerator.basic.generate(it)
                }
                    .describedAs { "Generate: $it" }.doesNotThrowAnyException()
            }
    }

    @Test
    fun genericSealedSubtypeParameter() {
        val genericSealedParent = typeToken<GenericSealedParent<String>>()

        val sealedSubtypes = genericSealedParent.rawType.permittedSubclasses!!

        val sealedSubtypeToken_1 = TypeToken.of(sealedSubtypes[0])
        println("sealedSubtypeToken_1 = $sealedSubtypeToken_1")
    }

    @Test
    fun getValueTypeBoxer() {
        val realType = typeToken<ValueListType<String>>()
        val primaryConstructor = realType.rawType.kotlin.primaryConstructor!!

        val primaryConstructorMethod = primaryConstructor.javaMethod


        val hasStringValue = HasStringValue::class
        println("hasStringValue = ${hasStringValue}")
    }

    @JvmInline
    value class SuperString(val string: String)

    data class PrimaryConstructorHasValueTypeParameter(val superString: SuperString)

    @Test
    fun primaryConstructorHasValueTypeParameter() {
        val dataGenerator = DataGenerator.build {
            addGenerator { "yolo" }
        }

        Assertions.assertThat(dataGenerator.generate<PrimaryConstructorHasValueTypeParameter>())
            .isEqualTo(PrimaryConstructorHasValueTypeParameter(SuperString("yolo")))
    }

    data class PrimaryConstructorHasSpecifiedGenericValueTypeParameter(val genericValueType: GenericValueType<String>)

    @Test
    fun primaryConstructorHasSpecifiedGenericValueTypeParameter() {
        Assumptions.assumeThat(true)
            .describedAs { "not yet supported" }.isFalse
        val expectedString = UUID.randomUUID().toString()
        val dataGenerator = DataGenerator.build {
            addGenerator { expectedString }
        }

        Assertions.assertThat(dataGenerator.generate<PrimaryConstructorHasSpecifiedGenericValueTypeParameter>())
            .isEqualTo(
                PrimaryConstructorHasSpecifiedGenericValueTypeParameter(
                    GenericValueType(expectedString)
                )
            )
    }

    @Test
    fun genericValueType() {
        Assumptions.assumeThat(false)
            .describedAs { "this isn't supported right now" }
            .isTrue

        val expectedString = UUID.randomUUID().toString()
        val dataGenerator = DataGenerator.build {
            addGenerator { expectedString }
        }

        Assertions.assertThat(dataGenerator.generate<GenericValueType<String>>())
            .extracting { it.value }
            .isEqualTo(expectedString)
    }

    @Test
    fun genericDataType() {
        val expectedString = UUID.randomUUID().toString()
        val dataGenerator = DataGenerator.build {
            addGenerator { expectedString }
        }

        Assertions.assertThat(dataGenerator.generate<GenericDataType<String>>())
            .extracting { it.value }
            .isEqualTo(expectedString)
    }
}

class MyList : ArrayList<UUID>()

inline fun <reified T : Any> getListElementType(): TypeToken<*> {
    return typeToken<T>().resolveType(List::class.java.typeParameters[0])
}

class WeirdList<A, B>(val aList: List<A>, val bList: List<B>) : ForwardingList<B>() {
    override fun delegate(): List<B?> {
        return bList
    }
}

inline fun <reified T : Any> T.typedInstance() = TypedInstance(
    typeToken<T>(),
    this
);

sealed interface GenericSealedParent<T>

class GenericSealedChild_1<T> : GenericSealedParent<T>
class GenericSealedChild_2<T> : GenericSealedParent<T>

data class HasTinyFlags(val tinyFlags: TinyFlags)

data class HasInt(val intValue: Int)

@JvmInline
value class HasIntValue(val intValue: Int)

@JvmInline
value class HasStringValue(val stringValue: String)

data class HasHasStringValue(val hasStringValue: HasStringValue)

data class HasGeneric<T>(val genericValue: T)

@JvmInline
value class ValueListType<T>(val stuff: List<T>)

