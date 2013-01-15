echo displaying the debug build cert
keytool -list -v -keystore $ANDROID_SDK_HOME/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
