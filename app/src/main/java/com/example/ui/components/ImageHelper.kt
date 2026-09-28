package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageHelper {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 82

    /**
     * Redimensionne un bitmap en conservant le ratio d'aspect si l'une des dimensions
     * dépasse [maxDim] (par défaut 1024px), évitant la saturation mémoire.
     */
    fun downscaleBitmap(bitmap: Bitmap, maxDim: Int = MAX_DIMENSION): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDim && height <= maxDim) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1f) {
            newWidth = maxDim
            newHeight = (maxDim / ratio).toInt()
        } else {
            newHeight = maxDim
            newWidth = (maxDim * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compresse et enregistre un Bitmap dans l'espace de stockage interne privé
     * avec redimensionnement automatique à 1024px max.
     */
    fun saveBitmapToFile(context: Context, sourceBitmap: Bitmap, prefix: String = "client"): String? {
        return try {
            val dir = File(context.filesDir, "clients")
            if (!dir.exists()) dir.mkdirs()

            val scaled = downscaleBitmap(sourceBitmap, MAX_DIMENSION)
            val file = File(dir, "${prefix}_${UUID.randomUUID()}.jpg")

            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            if (scaled != sourceBitmap) {
                scaled.recycle()
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lit un Uri (ex: photo sélectionnée depuis la galerie ou fichier temporaire),
     * calcule le sous-échantillonnage optimal (inSampleSize) pour éviter les OOM,
     * puis enregistre une version optimisée à 1024px max dans filesDir.
     */
    fun saveUriToFile(context: Context, uri: Uri, prefix: String = "client"): String? {
        return try {
            val dir = File(context.filesDir, "clients")
            if (!dir.exists()) dir.mkdirs()

            // 1. Décodage des dimensions uniquement
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            // 2. Calcul du inSampleSize
            var sampleSize = 1
            while (options.outWidth / (sampleSize * 2) >= MAX_DIMENSION &&
                options.outHeight / (sampleSize * 2) >= MAX_DIMENSION
            ) {
                sampleSize *= 2
            }

            // 3. Décodage avec sous-échantillonnage
            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            // 4. Redimensionnement précis et compression
            val finalScaled = downscaleBitmap(decodedBitmap, MAX_DIMENSION)
            val file = File(dir, "${prefix}_${UUID.randomUUID()}.jpg")

            FileOutputStream(file).use { out ->
                finalScaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            if (finalScaled != decodedBitmap) {
                finalScaled.recycle()
            }
            decodedBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
