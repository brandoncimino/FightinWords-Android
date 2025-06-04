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
    }

    fun remove(slug: Slug) {
        if(composingStick.contains(slug) == false) {
            throw NoSuchElementException("I don't contain $slug!")
        }
    }

    fun clear(){
        composingStick.clear()
    }

    fun backspace() : Slug? {
        return composingStick.removeLastOrNull()
    }
}