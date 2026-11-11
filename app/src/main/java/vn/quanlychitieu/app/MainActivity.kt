package vn.quanlychitieu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import vn.quanlychitieu.app.ui.AppRoot

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !vm.loaded.value }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AppRoot(vm) }
    }
}
