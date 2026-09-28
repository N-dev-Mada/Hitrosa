package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun shareViaWhatsApp(context: Context, phoneNumber: String?, message: String) {
        try {
            val cleanPhone = phoneNumber?.replace(Regex("[^0-9+]"), "")
            if (!cleanPhone.isNullOrBlank()) {
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                // If phone is missing, open generic WhatsApp share
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sendIntent)
            }
        } catch (e: Exception) {
            // WhatsApp is not installed or error, fall back to Android Chooser
            shareGeneral(context, message)
        }
    }

    fun shareGeneral(context: Context, message: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooser = Intent.createChooser(sendIntent, "Partager le reçu")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible de partager", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String?) {
        val cleanPhone = phoneNumber?.replace(Regex("[^0-9+]"), "")
        if (!cleanPhone.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Numéro de téléphone non renseigné", Toast.LENGTH_SHORT).show()
        }
    }
}
