package com.unitn.audioindexer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.unitn.audioindexer.ui.navigation.AppNavigation
import com.unitn.audioindexer.ui.theme.AudioIndexerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioIndexerTheme {
                AppNavigation()
            }
        }
    }

    @Preview(showBackground = true)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun HomeScreenPreview() {
        AudioIndexerTheme {
            AppNavigation()
        }
    }
}
