package com.example.phonecleaner.features.about

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.phonecleaner.databinding.ActivityAboutPhoneCleanerBinding


class AboutPhoneCleanerActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityAboutPhoneCleanerBinding


    // =========================================================
    // onCreate
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAboutPhoneCleanerBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupShareButton()

        loadAppVersion()
    }


    // =========================================================
    // Window Insets
    // =========================================================

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.aboutRoot
        ) { _, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            binding.topAppBarContainer.setPadding(
                0,
                systemBars.top,
                0,
                0
            )

            binding.mainContent.setPadding(
                binding.mainContent.paddingLeft,
                binding.mainContent.paddingTop,
                binding.mainContent.paddingRight,
                systemBars.bottom + 8
            )

            insets
        }
    }


    // =========================================================
    // Toolbar
    // =========================================================

    private fun setupToolbar() {

        binding.btnBack.setOnClickListener {

            finish()
        }
    }


    // =========================================================
    // App Version
    // =========================================================

    private fun loadAppVersion() {

        val versionName =
            try {

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.TIRAMISU
                ) {

                    packageManager
                        .getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                        .versionName
                        ?: "Unknown"

                } else {

                    @Suppress("DEPRECATION")

                    packageManager
                        .getPackageInfo(
                            packageName,
                            0
                        )
                        .versionName
                        ?: "Unknown"
                }

            } catch (_: Exception) {

                "Unknown"
            }


        binding.tvVersion.text =
            "Version $versionName"
    }


    // =========================================================
    // Share
    // =========================================================

    private fun setupShareButton() {

        binding.btnShare.setOnClickListener {

            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {

                    type = "text/plain"

                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out Phone Cleaner!"
                    )
                }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share Phone Cleaner"
                )
            )
        }
    }
}