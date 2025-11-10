package brava.fightinwords

@JvmInline
value class GenericValueType<T>(val value: T)

data class GenericDataType<T>(val value: T)