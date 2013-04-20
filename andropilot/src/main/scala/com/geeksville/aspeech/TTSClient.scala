package com.geeksville.aspeech

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech
import com.ridemission.scandroid._
import android.content.Context
import java.util.Locale
import com.geeksville.andropilot.FlurryContext
import com.geeksville.andropilot.R

/**
 * Speak using the Android TTS library
 */
trait TTSClient extends Activity with UsesPreferences 
with AndroidLogger with FlurryContext with UsesResources {
  private val MY_DATA_CHECK_CODE = 0x4403

  private var tts: Option[TextToSpeech] = None

  private val listener = new TextToSpeech.OnInitListener {
    /**
     * Notify us that TTS is ready to roll
     * @param errorCode 0 means okay
     */
    override def onInit(errorCode: Int) {
      if (errorCode != 0)
        error("Can't open TTS")
      else {
        info("Opened TTS")
        tts.foreach { t =>
          // val langName = Locale.getDefault
          val langName = new Locale(S(R.string.tts_language))
          warn("Using TTS lang: " + langName)
          val langCheck = t.setLanguage(langName)
          langCheck match {
            case TextToSpeech.LANG_MISSING_DATA =>
              error("Missing lang data")
              askInstall()
            case TextToSpeech.LANG_NOT_SUPPORTED =>
              error("Lang not supported")
            case _ =>
            // speak("Speech enabled")
          }
        }
      }
    }
  }

  def isSpeechEnabled = boolPreference("speech_enabled", true)
  def isSpeechEnabled_=(b: Boolean) {
    preferences.edit.putBoolean("speech_enabled", b).commit()
  }

  /**
   * Say a phrase
   *
   * @param urgent if true then previously queued text will be abandoned
   */
  def speak(str: String, urgent: Boolean = false) {
    if (isSpeechEnabled) {
      usageEvent("speech_on", "msg" -> str)

      val cleaned = str.replace('_', ' ') // Don't say 'underscore'
      tts.foreach(_.speak(cleaned, if (urgent) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null))
    } else
      usageEvent("speech_off", "msg" -> str)
  }

  /**
   * Begin asking if TTS is available, result will come in onActivityResult
   */
  /*
  def startTTSCheck() {
    if (isSpeechEnabled) {
      val checkIntent = new Intent();
      checkIntent.setAction("android.speech.tts.engine.CHECK_TTS_DATA")
      startActivityForResult(checkIntent, MY_DATA_CHECK_CODE)
    }
  }
  */

  def initSpeech() {
    val t = new TextToSpeech(this, listener)
    tts = Some(t)
  }

  /**
   * FIXME - we really should just override onDestroy
   */
  def destroySpeech() {
    tts.foreach(_.shutdown())
    tts = None
  }

  /*
  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    val CHECK_VOICE_DATA_PASS = 0x00000001

    // Ugh - this seems busted on Jellybean
    // http://code.google.com/p/android/issues/detail?id=36430
    val CHECK_VOICE_DATA_MISSING_DATA = -2

    if (requestCode == MY_DATA_CHECK_CODE) {
      debug("Voice activity result: " + resultCode)

      if (resultCode == CHECK_VOICE_DATA_PASS || resultCode == CHECK_VOICE_DATA_MISSING_DATA) {
        // success, create the TTS instance
        val t = new TextToSpeech(this, listener)

        val langCheck = t.isLanguageAvailable(Locale.getDefault())
        debug("langCheck: " + langCheck)
        langCheck match {
          case TextToSpeech.LANG_MISSING_DATA =>
            askInstall()
          case TextToSpeech.LANG_NOT_SUPPORTED =>
            ;
          case _ =>
            tts = Some(t)
        }
      } else
        askInstall
    } else
      super.onActivityResult(requestCode, resultCode, data)
  }
  */

  private def askInstall() {
    debug("Asking to install TTS")

    // missing data, install it
    val installIntent = new Intent()
    installIntent.setAction("android.speech.tts.engine.INSTALL_TTS_DATA")
    startActivity(installIntent)
  }
}