package com.example.phonecleaner.features.privacy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.phonecleaner.databinding.ActivityPrivacyPolicyBinding


class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding


    // =========================================================
    // onCreate
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPrivacyPolicyBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupManagePermissionsButton()
    }


    // =========================================================
    // Window Insets
    // =========================================================

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.topAppBarContainer
        ) { view, insets ->

            val topInset =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                ).top

            view.setPadding(
                0,
                topInset,
                0,
                0
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
    // Manage Permissions
    // =========================================================

    private fun setupManagePermissionsButton() {

        binding.btnManagePermissions
            .setOnClickListener {

                openAppSettings()
            }
    }


    // =========================================================
    // Open Android App Settings
    // =========================================================

    private fun openAppSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {

                    data =
                        Uri.parse(
                            "package:$packageName"
                        )
                }

            startActivity(intent)

        } catch (_: Exception) {

            // Ignore if the device does not
            // expose the application settings page.
        }
    }
}