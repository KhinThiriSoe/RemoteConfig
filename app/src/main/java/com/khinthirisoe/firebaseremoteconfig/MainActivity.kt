package com.khinthirisoe.firebaseremoteconfig

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private var firebaseDefaultMap: HashMap<String, Any> = HashMap()
    private val VERSION_CODE_KEY = "latest_app_version"
    private val TAG = "MainActivity"

    private val APP_BACKGROUND_COLOR_KEY = "app_background_color"
    private val APP_MAIN_TEXT_VIEW = "app_main_text_view"
    private val APP_SUB_TEXT_VIEW = "app_sub_text_view"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainLayout = layoutInflater.inflate(R.layout.activity_main, null)
        setContentView(mainLayout)

        title = "Firebase Remote Config Demo"

        //This is default Map
        //Setting the Default Map Value with the current version code
        firebaseDefaultMap = HashMap()
        firebaseDefaultMap[VERSION_CODE_KEY] = getCurrentVersionCode()
//        firebaseRemoteConfig.setDefaultsAsync(firebaseDefaultMap)

        firebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_defaults)

        firebaseRemoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600L)
                .build())

//getting the Remote values from Remote Config
        getRemoteConfigValues()

//        //Fetching the values here
//        firebaseRemoteConfig.fetch().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                firebaseRemoteConfig.activateFetched()
//                Log.d(TAG, "Fetched value: " + firebaseRemoteConfig.getString(VERSION_CODE_KEY))
//                //calling function to check if new version is available or not
//                checkForUpdate()
//            } else {
//                Toast.makeText(
//                    this@MainActivity, "Something went wrong please try again",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//        Log.d(TAG, "Default value: " + firebaseRemoteConfig.getString(VERSION_CODE_KEY))
    }

    private fun getRemoteConfigValues() {
        remote_value_main_tv.text = firebaseRemoteConfig.getString(APP_MAIN_TEXT_VIEW)
        remote_value_tv.text = firebaseRemoteConfig.getString(APP_SUB_TEXT_VIEW)
        //here we have set the cache expiration to 2 hrs
        //by setting the cacheExpiration, you app will refresh after every 2hour to check
        // if some changes are there in remote config
        val cacheExpiration: Long = 0

        firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Fetch Succeeded", Toast.LENGTH_SHORT).show()
                firebaseRemoteConfig.activateFetched()
            } else {
                Toast.makeText(applicationContext, "Fetch Failed", Toast.LENGTH_SHORT).show()
            }
            //changing the textview and backgorund color
            setRemoteConfigValues()
        }
    }

    private fun setRemoteConfigValues() {
        val remoteValueMainText = firebaseRemoteConfig.getString(APP_MAIN_TEXT_VIEW)
        val remoteValueSubText = firebaseRemoteConfig.getString(APP_SUB_TEXT_VIEW)
        val remoteValueBackground = firebaseRemoteConfig.getString(APP_BACKGROUND_COLOR_KEY)
        Log.d("message","remoteValueBackground " + remoteValueBackground)
        if (!remoteValueBackground.isNullOrEmpty()) {
            remote_value_main_tv.text = remoteValueMainText
            remote_value_tv.text = remoteValueSubText
            main_layout?.setBackgroundColor(Color.parseColor(remoteValueBackground))
        } else
            remote_value_main_tv.text = "Failed to load values"
    }

    private fun checkForUpdate() {
        val latestAppVersion = firebaseRemoteConfig.getDouble(VERSION_CODE_KEY).toInt()
        if (latestAppVersion > getCurrentVersionCode()) {
            AlertDialog.Builder(this).setTitle("Please Update the App")
                .setMessage("A new version of this app is available. Please update it")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Take user to Google Play Store",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }.setCancelable(false).show()
        } else {
            Toast.makeText(this, "This app is already up to date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentVersionCode(): Int {
        try {
            return packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return -1
    }
}
