package com.example.phonecleaner.ui.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.phonecleaner.data.model.InstalledApp
import com.example.phonecleaner.data.scanner.InstalledAppScanner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppsViewModel : ViewModel() {

    private var installedApps: List<InstalledApp> = emptyList()

    private val scanExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()

    enum class AppSortOption {
        NAME,
        LARGEST_SIZE,
        RECENTLY_USED,
        LEAST_RECENTLY_USED
    }

    enum class AppFilterOption {
        ALL_APPS,
        USER_APPS,
        SYSTEM_APPS
    }

    fun scanApps(
        context: Context,
        onComplete: (List<InstalledApp>) -> Unit
    ) {
        val appContext = context.applicationContext

        scanExecutor.execute {

            val apps =
                InstalledAppScanner.scan(appContext)

            installedApps = apps

            android.os.Handler(
                android.os.Looper.getMainLooper()
            ).post {
                onComplete(apps)
            }
        }
    }

    fun setInstalledApps(apps: List<InstalledApp>) {
        installedApps = apps.toList()
    }

    fun getInstalledApps(): List<InstalledApp> {
        return installedApps
    }

    fun getFilteredApps(
        filterOption: AppFilterOption
    ): List<InstalledApp> {

        return when (filterOption) {

            AppFilterOption.ALL_APPS ->
                installedApps

            AppFilterOption.USER_APPS ->
                installedApps.filter {
                    !it.isSystemApp
                }

            AppFilterOption.SYSTEM_APPS ->
                installedApps.filter {
                    it.isSystemApp
                }
        }
    }

    fun getSortedApps(
        sortOption: AppSortOption
    ): List<InstalledApp> {

        return when (sortOption) {

            AppSortOption.NAME ->
                installedApps.sortedBy {
                    it.appName.lowercase()
                }

            AppSortOption.LARGEST_SIZE ->
                installedApps.sortedByDescending {
                    it.sizeBytes
                }

            AppSortOption.RECENTLY_USED ->
                installedApps.sortedByDescending {
                    it.lastUsedTime
                }

            AppSortOption.LEAST_RECENTLY_USED ->
                installedApps.sortedBy {
                    it.lastUsedTime
                }
        }
    }

    fun getFilteredAndSortedApps(
        filterOption: AppFilterOption,
        sortOption: AppSortOption
    ): List<InstalledApp> {

        val filteredApps =
            getFilteredApps(filterOption)

        return when (sortOption) {

            AppSortOption.NAME ->
                filteredApps.sortedBy {
                    it.appName.lowercase()
                }

            AppSortOption.LARGEST_SIZE ->
                filteredApps.sortedByDescending {
                    it.sizeBytes
                }

            AppSortOption.RECENTLY_USED ->
                filteredApps.sortedByDescending {
                    it.lastUsedTime
                }

            AppSortOption.LEAST_RECENTLY_USED ->
                filteredApps.sortedBy {
                    it.lastUsedTime
                }
        }
    }

    fun getAppCount(): Int {
        return installedApps.size
    }

    fun getTotalAppSize(): Long {
        return installedApps.sumOf {
            it.sizeBytes
        }
    }

    fun getUserAppCount(): Int {
        return installedApps.count {
            !it.isSystemApp
        }
    }

    fun getSystemAppCount(): Int {
        return installedApps.count {
            it.isSystemApp
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanExecutor.shutdown()
    }
}