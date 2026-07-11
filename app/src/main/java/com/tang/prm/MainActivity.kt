package com.tang.prm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.ui.navigation.TangNavHost
import com.tang.prm.ui.theme.FixedDensityProvider
import com.tang.prm.ui.theme.TangTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // tabletModeEnabled 使用 null 作为 initialValue，避免 DataStore 尚未加载完成时
            // 以 false 渲染手机 UI，待真实值 true 加载后又切换到平板 UI，造成视觉闪烁
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val tabletModeEnabled by settingsRepository.tabletModeEnabled.collectAsStateWithLifecycle(initialValue = null)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FixedDensityProvider(context = this) {
                TangTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // tabletModeEnabled 尚未加载完成时显示空白背景，避免错误的手机 UI 一闪而过
                        val enabled = tabletModeEnabled
                        if (enabled != null) {
                            TangNavHost(tabletModeEnabled = enabled)
                        }
                    }
                }
            }
        }
    }
}
