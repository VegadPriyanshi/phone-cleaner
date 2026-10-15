package com.example.phonecleaner.ui.apps

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.InstalledApp
import com.example.phonecleaner.utils.UsageAccessUtils

class AppsFragment : Fragment(R.layout.fragment_apps) {

    private val appsViewModel: AppsViewModel by activityViewModels()
    private lateinit var appsAdapter: InstalledAppAdapter

    private var currentSortOption =
        AppsViewModel.AppSortOption.NAME

    private var currentFilterOption =
        AppsViewModel.AppFilterOption.ALL_APPS


    private var hasLoadedApps = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupSortButton(view)
        setupFilterButton(view)

        // Disabled until the initial app scan finishes so the
        // user can't open Filter/Sort against an incomplete list.
        setFilterSortEnabled(view, enabled = false)

        parentFragmentManager.setFragmentResultListener(
            "refresh_apps",
            viewLifecycleOwner
        ) { _, _ ->

            refreshInstalledApps(view)
        }
        scanInstalledApps(view)
    }

    override fun onResume() {
        super.onResume()


    }



    // ---------------------------------------------------------
    // RecyclerView
    // ---------------------------------------------------------

    private fun setupRecyclerView(view: View) {

        val recyclerView =
            view.findViewById<RecyclerView>(R.id.rvInstalledApps)

        appsAdapter = InstalledAppAdapter { app ->

            openAppDetails(app)
        }

        recyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

            adapter = appsAdapter

            isNestedScrollingEnabled = false
        }
    }

    // ---------------------------------------------------------
    // App Details
    // ---------------------------------------------------------

    private fun openAppDetails(app: InstalledApp) {

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.mainFragmentContainer,
                AppDetailsFragment.newInstance(
                    packageName = app.packageName,
                    sizeBytes = app.sizeBytes,
                    lastUsedTime = app.lastUsedTime,
                    isSystemApp = app.isSystemApp
                )
            )
            .addToBackStack(null)
            .commit()
    }

    // ---------------------------------------------------------
    // Usage Access
    // ---------------------------------------------------------

 /*   private fun setupUsageAccessCard(view: View) {

        val statusText =
            view.findViewById<TextView>(
                R.id.tvUsageAccessStatus
            )

        val usageAccessButton =
            view.findViewById<MaterialButton>(
                R.id.btnUsageAccess
            )

        val hasUsageAccess =
            UsageAccessUtils.hasUsageAccess(
                requireContext()
            )

        if (hasUsageAccess) {

            statusText.text =
                "Usage Access is enabled"

            usageAccessButton.text =
                "Access Granted"

            usageAccessButton.isEnabled = false

        } else {

            statusText.text =
                "Allow Usage Access to see when apps were last used."

            usageAccessButton.text =
                "Allow Usage Access"

            usageAccessButton.isEnabled = true

            usageAccessButton.setOnClickListener {

                val intent =
                    Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                    )

                startActivity(intent)
            }
        }
    }*/

    // ---------------------------------------------------------
    // Sort
    // ---------------------------------------------------------

    private fun setupSortButton(view: View) {

        val sortButton =
            view.findViewById<MaterialButton>(
                R.id.btnSortApps
            )

        sortButton.setOnClickListener {

            showSortDialog()
        }
    }

    private fun showSortDialog() {

        val options = arrayOf(
            "App Name",
            "Largest Size",
            "Recently Used",
            "Least Recently Used"
        )

        val selectedIndex =
            when (currentSortOption) {

                AppsViewModel.AppSortOption.NAME ->
                    0

                AppsViewModel.AppSortOption.LARGEST_SIZE ->
                    1

                AppsViewModel.AppSortOption.RECENTLY_USED ->
                    2

                AppsViewModel.AppSortOption.LEAST_RECENTLY_USED ->
                    3
            }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Apps")
            .setSingleChoiceItems(
                options,
                selectedIndex
            ) { dialog, which ->

                currentSortOption =
                    when (which) {

                        0 ->
                            AppsViewModel.AppSortOption.NAME

                        1 ->
                            AppsViewModel.AppSortOption.LARGEST_SIZE

                        2 ->
                            AppsViewModel.AppSortOption.RECENTLY_USED

                        3 ->
                            AppsViewModel.AppSortOption.LEAST_RECENTLY_USED

                        else ->
                            AppsViewModel.AppSortOption.NAME
                    }

                applyCurrentFilterAndSort()

                dialog.dismiss()
            }
            .show()
    }

    // ---------------------------------------------------------
    // Filter
    // ---------------------------------------------------------

    private fun setupFilterButton(view: View) {

        val filterButton =
            view.findViewById<MaterialButton>(
                R.id.btnFilterApps
            )

        filterButton.setOnClickListener {

            showFilterDialog()
        }
    }

    private fun showFilterDialog() {

        val options = arrayOf(
            "All Apps",
            "User Apps",
            "System Apps"
        )

        val selectedIndex =
            when (currentFilterOption) {

                AppsViewModel.AppFilterOption.ALL_APPS ->
                    0

                AppsViewModel.AppFilterOption.USER_APPS ->
                    1

                AppsViewModel.AppFilterOption.SYSTEM_APPS ->
                    2
            }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter Apps")
            .setSingleChoiceItems(
                options,
                selectedIndex
            ) { dialog, which ->

                currentFilterOption =
                    when (which) {

                        0 ->
                            AppsViewModel.AppFilterOption.ALL_APPS

                        1 ->
                            AppsViewModel.AppFilterOption.USER_APPS

                        2 ->
                            AppsViewModel.AppFilterOption.SYSTEM_APPS

                        else ->
                            AppsViewModel.AppFilterOption.ALL_APPS
                    }

                applyCurrentFilterAndSort()

                dialog.dismiss()
            }
            .show()
    }

    // ---------------------------------------------------------
    // Enable/Disable Filter + Sort buttons
    // ---------------------------------------------------------

    private fun setFilterSortEnabled(
        view: View,
        enabled: Boolean
    ) {

        val filterButton =
            view.findViewById<MaterialButton>(
                R.id.btnFilterApps
            )

        val sortButton =
            view.findViewById<MaterialButton>(
                R.id.btnSortApps
            )

        filterButton.isEnabled = enabled
        sortButton.isEnabled = enabled

        val alpha =
            if (enabled) 1.0f else 0.5f

        filterButton.alpha = alpha
        sortButton.alpha = alpha
    }

    // ---------------------------------------------------------
    // Filter + Sort
    // ---------------------------------------------------------

    private fun applyCurrentFilterAndSort() {

        val filteredAndSortedApps =
            appsViewModel.getFilteredAndSortedApps(
                filterOption = currentFilterOption,
                sortOption = currentSortOption
            )

        appsAdapter.submitList(
            filteredAndSortedApps
        )

        updateAppsCount(
            filteredAndSortedApps.size
        )
    }

    // ---------------------------------------------------------
    // Apps Count
    // ---------------------------------------------------------

    private fun updateAppsCount(count: Int) {

        val appsCountTextView =
            view?.findViewById<TextView>(
                R.id.tvInstalledAppsCount
            )

        appsCountTextView?.text =
            "$count apps"
    }

    // ---------------------------------------------------------
    // Scan Installed Apps
    // ---------------------------------------------------------

    private fun scanInstalledApps(view: View) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvInstalledApps
            )

        val loadingContainer =
            view.findViewById<View>(
                R.id.appsLoadingContainer
            )

        val statusText =
            view.findViewById<TextView>(
                R.id.tvInstalledAppsStatus
            )

        val appsCountTextView =
            view.findViewById<TextView>(
                R.id.tvInstalledAppsCount
            )

        /*
         * If apps were already scanned, reuse the existing
         * list from AppsViewModel instead of scanning again.
         */
        val existingApps =
            appsViewModel.getInstalledApps()

        if (existingApps.isNotEmpty()) {

            loadingContainer.visibility =
                View.GONE

            recyclerView.visibility =
                View.VISIBLE

            statusText.text =
                "Apps loaded"

            applyCurrentFilterAndSort()

            setFilterSortEnabled(view, enabled = true)

            return
        }

        /*
         * No previous scan exists.
         * Start a new scan.
         */
        loadingContainer.visibility =
            View.VISIBLE

        recyclerView.visibility =
            View.GONE

        statusText.text =
            "Scanning installed apps..."

        appsCountTextView.text =
            "Scanning..."

        // Keep Filter/Sort disabled while the scan is running.
        setFilterSortEnabled(view, enabled = false)

        appsViewModel.scanApps(
            requireContext()
        ) { apps ->

            if (!isAdded) {
                return@scanApps
            }

            appsViewModel.setInstalledApps(apps)

            hasLoadedApps = true

            loadingContainer.visibility =
                View.GONE

            recyclerView.visibility =
                View.VISIBLE

            applyCurrentFilterAndSort()

            // Scan complete - allow Filter/Sort again.
            setFilterSortEnabled(view, enabled = true)
        }
    }



    private fun refreshInstalledApps(view: View) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvInstalledApps
            )

        val loadingContainer =
            view.findViewById<View>(
                R.id.appsLoadingContainer
            )

        val statusText =
            view.findViewById<TextView>(
                R.id.tvInstalledAppsStatus
            )

        loadingContainer.visibility =
            View.VISIBLE

        recyclerView.visibility =
            View.GONE

        statusText.text =
            "Refreshing installed apps..."

        // Keep Filter/Sort disabled while refreshing.
        setFilterSortEnabled(view, enabled = false)

        appsViewModel.scanApps(
            requireContext()
        ) { apps ->

            if (!isAdded) {
                return@scanApps
            }

            appsViewModel.setInstalledApps(apps)

            loadingContainer.visibility =
                View.GONE

            recyclerView.visibility =
                View.VISIBLE

            applyCurrentFilterAndSort()

            // Refresh complete - allow Filter/Sort again.
            setFilterSortEnabled(view, enabled = true)
        }
    }
}