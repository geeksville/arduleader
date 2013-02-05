package com.geeksville.aspeech

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech

trait TTSClient extends Activity with TextToSpeech.OnInitListener {
  private val MY_DATA_CHECK_CODE = 0x4403

  private var tts: Option[TextToSpeech] = None

  def startTTSCheck() {
    val checkIntent = new Intent();
    checkIntent.setAction("android.speech.tts.engine.CHECK_TTS_DATA")
    startActivityForResult(checkIntent, MY_DATA_CHECK_CODE)
  }

  /**
   * Notify us that TTS is ready to role
   * @param errorCode 0 means okay
   */
  override def onInit(errorCode: Int) {

  }

  override protected def onActivityResult(
    requestCode: Int, resultCode: Int, data: Intent) {
    val CHECK_VOICE_DATA_PASS = 0x00000001

    if (requestCode == MY_DATA_CHECK_CODE) {
      if (resultCode == CHECK_VOICE_DATA_PASS) {
        // success, create the TTS instance
        tts = Some(new TextToSpeech(this, this))
      } else {
        // missing data, install it
        val installIntent = new Intent()
        installIntent.setAction("android.speech.tts.engine.INSTALL_TTS_DATA")
        startActivity(installIntent)
      }
    } else
      super.onActivityResult(requestCode, resultCode, data)
  }
}