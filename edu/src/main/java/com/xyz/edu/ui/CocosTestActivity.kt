package com.xyz.edu.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import com.xyz.base.utils.L
import org.cocos2dx.javascript.SDKWrapper
import org.cocos2dx.lib.Cocos2dxActivity
import org.cocos2dx.lib.Cocos2dxGLSurfaceView

class CocosTestActivity : Cocos2dxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.i("onCreate")
        SDKWrapper.getInstance().init(this)
    }

    override fun onCreateView(): Cocos2dxGLSurfaceView {
        L.i("onCreateView")
        val glSurfaceView = Cocos2dxGLSurfaceView(this)
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8)
        SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView, this)

        return glSurfaceView
    }

    override fun onResume() {
        super.onResume()
        SDKWrapper.getInstance().onResume()
    }

    override fun onPause() {
        super.onPause()
        SDKWrapper.getInstance().onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot) {
            return
        }
        SDKWrapper.getInstance().onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SDKWrapper.getInstance().onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        SDKWrapper.getInstance().onNewIntent(intent)
    }

    override fun onRestart() {
        super.onRestart()
        SDKWrapper.getInstance().onRestart()
    }

    override fun onStop() {
        super.onStop()
        SDKWrapper.getInstance().onStop()
    }

    override fun onBackPressed() {
        SDKWrapper.getInstance().onBackPressed()
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        SDKWrapper.getInstance().onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig!!)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        SDKWrapper.getInstance().onSaveInstanceState(outState)
        super.onSaveInstanceState(outState!!)
    }

    override fun onStart() {
        SDKWrapper.getInstance().onStart()
        super.onStart()
    }
}