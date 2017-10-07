package host.serenity.serenity.api.value

import java.util.*

interface ValueChangeListener {
    fun onChanged(oldValue: Any?)
}

abstract class Value<T>(val name: String, value: T) {
    val defaultValue: T = value

    open var value: T = value
        set(_value) {
            val oldValue = value
            field = _value
            changed(oldValue)
        }

    val changeListeners: List<ValueChangeListener> = ArrayList()
    private fun changed(oldValue: T) {
        changeListeners.forEach { it.onChanged(oldValue) }
    }

    abstract fun setValueFromString(string: String)
}
abstract class BoundedValue<T : Number>(name: String, value: T, var min: T, var max: T) : Value<T>(name, value) {
    override var value: T
        get() = super.value
        set(value) {
            if (isWithinBounds(value)) {
                super.value = value
            } else {
                if (value.toDouble() < min.toDouble()) {
                    super.value = min
                }
                if (value.toDouble() > max.toDouble()) {
                    super.value = max
                }
            }
        }

    fun isWithinBounds(value: T): Boolean {
        return value.toDouble() >= min.toDouble() && value.toDouble() <= max.toDouble()
    }
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleValue
