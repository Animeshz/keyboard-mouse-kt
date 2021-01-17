package com.github.animeshz.keyboard

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
// TODO: Revert back to Level.ERROR after https://youtrack.jetbrains.com/issue/KT-44007
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPEALIAS
)
public annotation class ExperimentalKeyIO
