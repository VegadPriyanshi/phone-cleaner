package com.example.phonecleaner.ui.apps

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.data.model.InstalledApp
import com.example.phonecleaner.data.scanner.UnusedAppsScanner
import com.example.phonecleaner.utils.UsageAccessUtils
import com.google.android.material.button.MaterialButton

class UnusedAppsFragment :
    Fragment(R.layout.fragment_unused_apps) {

    private lateinit var appsAdapter: InstalledAppAdapter

    /*
     * This flag prevents an unnecessary second scan when the
     * App Details screen is closed.
     *
     * The first time this fragment is opened, we scan.
     *
     * When AppDetailsFragment is opened, this fragment is only
     * hidden, not destroyed.
     *
     * Therefore coming back does NOT start the scan again.
     */
    private var hasPerformedInitialScan = false


    // =========================================================
    // ON VIEW CREATED
    // =========================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupBackButton(view)

        setupRecyclerView(view)

        setupUsageAccessButton(view)


        /*
         * This result is sent by AppDetailsFragment ONLY when
         * something actually changed, such as an uninstall.
         *
         * Therefore a real rescan is performed after uninstall.
         */
        parentFragmentManager.setFragmentResultListener(
            "refresh_apps",
            viewLifecycleOwner
        ) { _, _ ->

            scanUnusedApps(view)
        }


        /*
         * IMPORTANT:
         *
         * Only perform the initial scan once.
         *
         * When the user opens App Details and comes back,
         * this Fragment remains alive because we use hide/show.
         *
         * Therefore this block will NOT run again.
         */
        if (!hasPerformedInitialScan) {

            hasPerformedInitialScan = true

            scanUnusedApps(view)
        }
    }


    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        /*
         * DO NOT call scanUnusedApps() here.
         *
         * If we scanned here, every time the user comes back
         * from App Details we would see the loading animation.
         *
         * The list is already available.
         *
         * A scan is only triggered by:
         *
         * 1. Initial screen opening
         * 2. Successful app uninstall
         */
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private fun setupBackButton(
        view: View
    ) {

        view.findViewById<View>(
            R.id.btnBackUnusedApps
        ).setOnClickListener {

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private fun setupRecyclerView(
        view: View
    ) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvUnusedApps
            )


        /*
         * Reuse the SAME adapter that is already used
         * by the Apps tab.
         */
        appsAdapter =
            InstalledAppAdapter { app ->

                openAppDetails(app)
            }


        recyclerView.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                appsAdapter

            isNestedScrollingEnabled =
                false

            setHasFixedSize(false)
        }
    }


    // =========================================================
    // OPEN APP DETAILS
    // =========================================================

    private fun openAppDetails(
        app: InstalledApp
    ) {

        /*
         * IMPORTANT:
         *
         * DO NOT use replace() here.
         *
         * replace() destroys the UnusedAppsFragment view.
         *
         * When the user presses Back, the fragment gets
         * recreated and scanUnusedApps() runs again.
         *
         * Instead:
         *
         * 1. Hide UnusedAppsFragment
         * 2. Add AppDetailsFragment
         *
         * When AppDetailsFragment is popped, the original
         * UnusedAppsFragment is still there with the existing
         * list and NO loading animation.
         */
        parentFragmentManager.beginTransaction()
            .hide(this)
            .add(
                R.id.mainFragmentContainer,
                AppDetailsFragment.newInstance(
                    packageName =
                        app.packageName,

                    sizeBytes =
                        app.sizeBytes,

                    lastUsedTime =
                        app.lastUsedTime,

                    isSystemApp =
                        app.isSystemApp
                )
            )
            .addToBackStack(null)
            .commit()
    }


    // =========================================================
    // USAGE ACCESS BUTTON
    // =========================================================

    private fun setupUsageAccessButton(
        view: View
    ) {

        view.findViewById<MaterialButton>(
            R.id.btnUnusedUsageAccess
        ).setOnClickListener {

            try {

                startActivity(
                    Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                    )
                )

            } catch (_: Exception) {

                // Keep the screen usable if the Settings page
                // is not available on the device.
            }
        }
    }


    // =========================================================
    // USAGE ACCESS STATE
    // =========================================================

    private fun updateUsageAccessState(
        view: View
    ) {

        val usageCard =
            view.findViewById<View>(
                R.id.unusedUsageAccessCard
            )


        val hasAccess =
            UsageAccessUtils.hasUsageAccess(
                requireContext()
            )


        usageCard.visibility =
            if (hasAccess) {

                View.GONE

            } else {

                View.VISIBLE
            }
    }


    // =========================================================
    // SCAN UNUSED APPS
    // =========================================================

    private fun scanUnusedApps(
        view: View
    ) {

        val loading =
            view.findViewById<View>(
                R.id.unusedAppsLoading
            )


        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvUnusedApps
            )


        val emptyState =
            view.findViewById<TextView>(
                R.id.tvUnusedAppsEmpty
            )


        val countText =
            view.findViewById<TextView>(
                R.id.tvUnusedAppsCount
            )


        val subtitle =
            view.findViewById<TextView>(
                R.id.tvUnusedAppsSubtitle
            )


        /*
         * Get the same unused-days setting used by
         * the Unused Apps Reminder screen.
         */
        val unusedDays =
            AppPrefs.getUnusedAppsDays(
                requireContext()
            )


        subtitle.text =
            "Apps not used for more than $unusedDays days"


        updateUsageAccessState(
            view
        )


        // =====================================================
        // USAGE ACCESS NOT GRANTED
        // =====================================================

        if (
            !UsageAccessUtils.hasUsageAccess(
                requireContext()
            )
        ) {

            loading.visibility =
                View.GONE

            recyclerView.visibility =
                View.GONE

            emptyState.visibility =
                View.VISIBLE

            emptyState.text =
                "Allow Usage Access to find unused apps."

            countText.text =
                "Usage Access required"

            return
        }


        // =====================================================
        // SHOW LOADING
        // =====================================================

        loading.visibility =
            View.VISIBLE

        recyclerView.visibility =
            View.GONE

        emptyState.visibility =
            View.GONE

        countText.text =
            "Checking installed apps..."


        // =====================================================
        // BACKGROUND SCAN
        // =====================================================

        Thread {

            val unusedApps =
                try {

                    UnusedAppsScanner.findUnusedApps(
                        requireContext()
                            .applicationContext,

                        unusedDays
                    )

                } catch (_: Exception) {

                    emptyList()
                }


            if (!isAdded) {

                return@Thread
            }


            requireActivity().runOnUiThread {

                if (
                    !isAdded ||
                    view !== this.view
                ) {

                    return@runOnUiThread
                }


                loading.visibility =
                    View.GONE


                /*
                 * Update the existing adapter.
                 */
                appsAdapter.submitList(
                    unusedApps
                )


                // =================================================
                // NO UNUSED APPS
                // =================================================

                if (
                    unusedApps.isEmpty()
                ) {

                    recyclerView.visibility =
                        View.GONE

                    emptyState.visibility =
                        View.VISIBLE

                    emptyState.text =
                        "No unused apps found."

                    countText.text =
                        "0 apps"

                } else {

                    // =============================================
                    // UNUSED APPS FOUND
                    // =============================================

                    recyclerView.visibility =
                        View.VISIBLE

                    emptyState.visibility =
                        View.GONE


                    countText.text =
                        when (
                            unusedApps.size
                        ) {

                            1 ->
                                "1 unused app"

                            else ->
                                "${unusedApps.size} unused apps"
                        }
                }
            }

        }.start()
    }
}