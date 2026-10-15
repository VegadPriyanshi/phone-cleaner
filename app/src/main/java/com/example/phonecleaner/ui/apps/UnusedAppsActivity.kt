package com.example.phonecleaner.ui.apps

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.phonecleaner.R

class UnusedAppsActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        /*
         * Allow the app to draw edge-to-edge.
         */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(
            R.layout.activity_unused_apps
        )

        setupSystemBarInsets()

        if (savedInstanceState == null) {

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    UnusedAppsFragment()
                )
                .commit()
        }
    }


    // =========================================================
    // SYSTEM BAR INSETS
    // =========================================================

    private fun setupSystemBarInsets() {

        val root =
            findViewById<View>(
                R.id.unusedAppsRoot
            )

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            /*
             * Keep the content below the status bar
             * and above the navigation bar.
             */
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }
}