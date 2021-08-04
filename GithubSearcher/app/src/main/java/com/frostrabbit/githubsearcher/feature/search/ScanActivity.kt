package com.frostrabbit.githubsearcher.feature.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import io.anyline.view.BaseScanViewConfig
import io.anyline.plugin.ocr.OcrScanViewPlugin
import io.anyline.view.ScanViewPluginConfig
import io.anyline.plugin.ocr.AnylineOcrConfig
import at.nineyards.anyline.core.LicenseException
import io.anyline.AnylineSDK
import io.anyline.view.ScanView

import android.view.WindowManager
import android.view.View
import android.content.Intent
import com.frostrabbit.githubsearcher.R


class ScanActivity : AppCompatActivity() {

    private val TAG: String = ScanActivity::class.java.getSimpleName()
    private var scanView: ScanView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_scan)
        val lic = getString(R.string.anyline_license_key)
        scanView = findViewById<View>(R.id.scan_view) as ScanView

        // This must be called before doing anything Anyline-related!
        // Try/Catch this to check whether or not your license key is valid!
        try {
            AnylineSDK.init(lic, this)
        } catch (e: LicenseException) {
            // handle exception
        }

        // see ScanScrabbleActivity for a more detailed description
        val anylineOcrConfig = AnylineOcrConfig()
        // use predefined whitelist and regular expression
        anylineOcrConfig.charWhitelist = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-" //AnylineOcrConfig.AnylineOcrRegex.ISBN.whiteList
//        anylineOcrConfig.setValidationRegex("/^[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}\$/i")//AnylineOcrConfig.AnylineOcrRegex.ISBN.regex)
        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.scanMode = AnylineOcrConfig.ScanMode.LINE

        //init the scanViewPlugin config
        val ocrScanViewPluginConfig =
            ScanViewPluginConfig(applicationContext, "scan_view_config.json")
        //init the scan view
        val scanViewPlugin =
            OcrScanViewPlugin(applicationContext, anylineOcrConfig, ocrScanViewPluginConfig, "OCR")
        //init the base config used for camera and flash
        val ocrBaseScanViewConfig = BaseScanViewConfig(applicationContext, "scan_view_config.json")
        //set the scan Base config
        scanView!!.setScanViewConfig(ocrBaseScanViewConfig)
        //set the scan view plugin to the scan view
        scanView!!.scanViewPlugin = scanViewPlugin
        //add the scan result listener
        scanViewPlugin.addScanResultListener { result ->
            if (!result.toString().isEmpty()) {
                val intent = Intent()
                intent.putExtra("DATA", result.result.toString())//.trim { it <= ' ' })
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        //start the scanning
        //internally scanView is calling the scanPlugin which is starting the scanning part
        scanView!!.start()
    }

    override fun onPause() {
        super.onPause()
        scanView!!.stop()
        scanView!!.releaseCameraInBackground()
    }

}