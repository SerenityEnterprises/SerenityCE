package host.serenity.serenity.api.help

@Target(AnnotationTarget.CLASS)
annotation class GenericDescription(val value: String)

@Target(AnnotationTarget.CLASS)
annotation class ModuleDescription(val value: String)

@Target(AnnotationTarget.FIELD)
annotation class ValueDescription(val value: String)