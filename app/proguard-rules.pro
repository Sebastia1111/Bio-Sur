# Keep Room generated classes
-keep class cl.biosur.app.data.local.** { *; }

# Keep entities (used by Room reflection)
-keep class cl.biosur.app.data.local.entity.** { *; }

# Keep model classes for Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class cl.biosur.app.data.model.** { *; }

# iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
