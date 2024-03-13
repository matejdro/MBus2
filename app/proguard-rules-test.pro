# Uncomment this to debug tests. Comment it back to ensure that tests also test whether obfuscation breaks anything
#-dontobfuscate

# Proguard rules needed for instrumented tests to pass
# (since instrumented tests use some code that would otherwise be optimized out)

-dontwarn com.google.errorprone.annotations.MustBeClosed

-keep class si.inova.kotlinova.core.outcome.CoroutineResourceManager {
    *;
}
-keep class si.inova.kotlinova.core.test.** {
    *;
}

-keep class kotlin.** {
    *;
}
-keep class kotlinx.coroutines.** {
    *;
}
-keep class androidx.** {
    *;
}
-keep class dispatch.** {
    *;
}
-keep class okhttp3.** {
    *;
}
-keep class okio.** {
    *;
}

-keep class dagger.** {
    *;
}
-keep @dagger.** class * {
    *;
}
-keep class * extends dagger.internal.Factory {
    *;
}
