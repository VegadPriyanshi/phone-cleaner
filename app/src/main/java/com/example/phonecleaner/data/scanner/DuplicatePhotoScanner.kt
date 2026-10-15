package com.example.phonecleaner.data.scanner

import android.content.Context
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.data.model.DuplicatePhotoGroup
import java.security.MessageDigest

object DuplicatePhotoScanner {

    fun findDuplicateGroups(
        context: Context,
        photos: List<CleanerFile>
    ): List<DuplicatePhotoGroup> {

        if (photos.size < 2) {
            return emptyList()
        }

        /*
         * Step 1:
         * Group photos by file size.
         *
         * Photos with different sizes cannot be
         * exact duplicates, so there is no need
         * to calculate their hashes.
         */
        val sameSizeGroups =
            photos.groupBy { it.sizeBytes }
                .values
                .filter { it.size >= 2 }

        if (sameSizeGroups.isEmpty()) {
            return emptyList()
        }

        val duplicateGroups =
            mutableListOf<DuplicatePhotoGroup>()

        /*
         * Step 2:
         * Calculate SHA-256 only for photos
         * that have the same file size.
         */
        for (sameSizeGroup in sameSizeGroups) {

            val hashGroups =
                sameSizeGroup
                    .mapNotNull { photo ->

                        val hash =
                            calculateSha256(
                                context,
                                photo
                            )

                        if (hash != null) {
                            hash to photo
                        } else {
                            null
                        }
                    }
                    .groupBy { it.first }

            /*
             * Step 3:
             * A hash group containing 2 or more
             * photos means they are exact duplicates.
             */
            hashGroups.values
                .filter { it.size >= 2 }
                .forEach { duplicateItems ->

                    duplicateGroups.add(
                        DuplicatePhotoGroup(
                            files =
                                duplicateItems.map {
                                    it.second
                                }
                        )
                    )
                }
        }

        return duplicateGroups
    }

    private fun calculateSha256(
        context: Context,
        photo: CleanerFile
    ): String? {

        return try {

            val digest =
                MessageDigest.getInstance(
                    "SHA-256"
                )

            context.contentResolver
                .openInputStream(
                    android.net.Uri.parse(photo.uri)
                )
                ?.use { inputStream ->

                    val buffer =
                        ByteArray(
                            8 * 1024
                        )

                    while (true) {

                        val bytesRead =
                            inputStream.read(
                                buffer
                            )

                        if (bytesRead == -1) {
                            break
                        }

                        digest.update(
                            buffer,
                            0,
                            bytesRead
                        )
                    }
                }
                ?: return null

            digest.digest()
                .joinToString("") {
                    "%02x".format(it)
                }

        } catch (exception: Exception) {

            null
        }
    }
}