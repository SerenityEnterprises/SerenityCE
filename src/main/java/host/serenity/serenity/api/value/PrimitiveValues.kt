package host.serenity.serenity.api.value

class DoubleValue : BoundedValue<Double> {
    constructor(name: String, value: Double, min: Double, max: Double) : super(name, value, min, max)
    constructor(name: String, value: Double) : super(name, value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

    override fun setValueFromString(string: String) {
        try {
            value = string.toDouble()
        } catch (ignored: Exception) {}
    }
}

class FloatValue : BoundedValue<Float> {
    constructor(name: String, value: Float, min: Float, max: Float) : super(name, value, min, max)
    constructor(name: String, value: Float) : super(name, value, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)

    override fun setValueFromString(string: String) {
        try {
            value = string.toFloat()
        } catch (ignored: Exception) {}
    }
}

class IntValue : BoundedValue<Int> {
    constructor(name: String, value: Int, min: Int, max: Int) : super(name, value, min, max)
    constructor(name: String, value: Int) : super(name, value, Int.MIN_VALUE, Int.MAX_VALUE)

    override fun setValueFromString(string: String) {
        try {
            value = string.toInt()
        } catch (ignored: Exception) {}
    }
}

class BooleanValue(name: String, value: Boolean) : Value<Boolean>(name, value) {
    override fun setValueFromString(string: String) {
        try {
            value = string.toBoolean()
        } catch (ignored: Exception) {}
    }
}

class StringValue(name: String, value: String) : Value<String>(name, value) {
    override fun setValueFromString(string: String) {
        value = string
    }
}