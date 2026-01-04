package com.example.qryoklamajetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qryoklamajetpack.data.Prefs
import com.example.qryoklamajetpack.ui.theme.QRYoklamaJetpackTheme
import com.example.qryoklamajetpack.view.ScanScreen
import com.example.qryoklamajetpack.view.SinavWebViewScreen
import com.example.qryoklamajetpack.view.StudentAttendanceScreen
import com.example.qryoklamajetpack.view.StudentSetupScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = Prefs(this)
        val start = if (prefs.getStudentNo().isNullOrBlank()) "setup" else "scan"

        setContent {
            MaterialTheme {
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = start) {

                    composable("setup") {
                        StudentSetupScreen(
                            onSaved = {
                                nav.navigate("scan") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("scan") {
                        ScanScreen(
                            onOpenAttendance = { nav.navigate("attendance") },
                            onOpenExam = { nav.navigate("exam") }
                        )
                    }

                    composable("attendance") { StudentAttendanceScreen() }

                    composable("exam") { SinavWebViewScreen() }
                }
            }
        }
    }
}
