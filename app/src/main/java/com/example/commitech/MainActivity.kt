package com.example.commitech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.commitech.ui.navigation.AppNavGraph
import com.example.commitech.ui.theme.CommitechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommitechTheme {
                Surface {
                    AppNavGraph()
                }
            }
        }
    }
}
