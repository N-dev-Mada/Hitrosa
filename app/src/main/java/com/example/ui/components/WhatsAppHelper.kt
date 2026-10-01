package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {

    /**
     * Ouvre le sélecteur Android natif (Intent.createChooser) pour laisser à l'utilisateur
     * le choix total de l'application sur laquelle partager (WhatsApp, SMS, Mail, Notes, etc.)
     */
    fun shareViaWhatsApp(context: Context, phoneNumber: String?, message: String) {
        shareGeneral(context, message, "Partager via...")
    }

    fun shareGeneral(context: Context, message: String, title: String = "Partager le reçu") {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooser = Intent.createChooser(sendIntent, title)
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
