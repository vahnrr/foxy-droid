package nya.kitsunyan.foxydroid.utility

import android.content.Context
import android.content.pm.Signature
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.LocaleList
import androidx.core.content.ContextCompat
import nya.kitsunyan.foxydroid.BuildConfig
import nya.kitsunyan.foxydroid.R
import nya.kitsunyan.foxydroid.utility.extension.android.*
import nya.kitsunyan.foxydroid.utility.extension.resources.*
import nya.kitsunyan.foxydroid.utility.extension.text.*
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.util.Locale

object Utils {
  private fun createDefaultApplicationIcon(context: Context, tintAttrResId: Int): Drawable {
    return ContextCompat.getDrawable(context, R.drawable.ic_application_default)!!.mutate()
      .apply { setTintList(context.getColorFromAttr(tintAttrResId)) }
  }

  fun getDefaultApplicationIcons(context: Context): Pair<Drawable, Drawable> {
    val progressIcon: Drawable = createDefaultApplicationIcon(context, android.R.attr.textColorSecondary)
    val defaultIcon: Drawable = createDefaultApplicationIcon(context, android.R.attr.colorAccent)
    return Pair(progressIcon, defaultIcon)
  }

  fun getToolbarIcon(context: Context, resId: Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, resId)!!.mutate()
    drawable.setTintList(context.getColorFromAttr(android.R.attr.textColorPrimary))
    return drawable
  }

  fun calculateHash(signature: Signature): String? {
    return MessageDigest.getInstance("MD5").digest(signature.toCharsString().toByteArray()).hex()
  }

  fun calculateFingerprint(certificate: Certificate): String {
    val encoded = try {
      certificate.encoded
    } catch (e: CertificateEncodingException) {
      null
    }
    return encoded?.let(::calculateFingerprint).orEmpty()
  }

  fun calculateFingerprint(key: ByteArray): String {
    return if (key.size >= 256) {
      try {
        val fingerprint = MessageDigest.getInstance("SHA-256").digest(key)
        val builder = StringBuilder()
        for (byte in fingerprint) {
          builder.append("%02X".format(Locale.US, byte.toInt() and 0xff))
        }
        builder.toString()
      } catch (e: Exception) {
        e.printStackTrace()
        ""
      }
    } else {
      ""
    }
  }

  fun configureLocale(context: Context): Context {
    val supportedLanguages = BuildConfig.LANGUAGES.toSet()
    val configuration = context.resources.configuration
    val currentLocales = if (Android.sdk(24)) {
      val localesList = configuration.locales
      (0 until localesList.size()).map(localesList::get)
    } else {
      @Suppress("DEPRECATION")
      listOf(configuration.locale)
    }
    val compatibleLocales = currentLocales
      .filter { it.language in supportedLanguages }
      .let { if (it.isEmpty()) listOf(Locale.US) else it }
    Locale.setDefault(compatibleLocales.first())
    val newConfiguration = Configuration(configuration)
    if (Android.sdk(24)) {
      newConfiguration.setLocales(LocaleList(*compatibleLocales.toTypedArray()))
    } else {
      @Suppress("DEPRECATION")
      newConfiguration.locale = compatibleLocales.first()
    }
    return context.createConfigurationContext(newConfiguration)
  }
}
