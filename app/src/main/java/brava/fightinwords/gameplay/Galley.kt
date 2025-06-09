package brava.fightinwords.gameplay

class Galley(
    val capacity: Int
) {
    private val composingStick : ArrayDeque<Slug> = ArrayDeque(capacity)

    val isFull : Boolean get() {
        // 📎 You can do this in an "expression" style, using:
        //        isFull : Boolean get() = value;
        //    Equivalent to C#'s:
        //        boolean isFull => value;
        //    But that's confusing, because it LOOKS, but is NOT the same, as C#'s:
        //        boolean isFull = value;
        return composingStick.size == capacity
    }

    fun add(slug: Slug){
        if(isFull) {
            throw IllegalStateException("Can't add $slug because I am already at my full capacity of ${capacity}!")
        }

        if(composingStick.contains(slug)) {
            throw IllegalArgumentException("I already contain $slug!")
        }

        composingStick.add(slug);

        println("Adding $slug produced: $composingStick")
    }

    fun remove(slug: Slug) {
        if(composingStick.contains(slug) == false) {
            throw NoSuchElementException("I don't contain $slug!")
        }

        composingStick.remove(slug)
        println("Removing $slug produced: $composingStick")
    }

    fun submitAndClear(): List<Char> {
        val word = composingStick.map { it.letter }
        clear()
        return word
    }

    fun clear(){
        composingStick.clear()
    }

    fun backspace() : Slug? {
        return composingStick.removeLastOrNull()
    }

    fun contains(slug: Slug): Boolean {
        return composingStick.contains(slug)
    }

    fun elementAtOrNull(index: Int): Slug? {
        return composingStick.elementAtOrNull(index)
    }
}