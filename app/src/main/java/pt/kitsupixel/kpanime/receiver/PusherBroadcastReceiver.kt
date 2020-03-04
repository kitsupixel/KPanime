package pt.kitsupixel.kpanime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pt.kitsupixel.kpanime.services.PusherService

class PusherBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, PusherService::class.java))
    }

}