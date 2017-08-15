package mysales.com.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.View
import java.text.DecimalFormat

/**
 * Created by wingfei.siew on 8/14/2017.
 */
fun isEmpty(s: String?) : Boolean {
    val b = s.isNullOrEmpty()
    return b
}

fun getSqlStr(s: String?) : String? {
    return s
}

fun getMessages(ls: ArrayList<String>) : String {
    val s: String?

    val sb = StringBuffer()
    for (i in 0..ls.size - 1)
        sb.append(String.format("%d. %s\n", i + 1, ls[i]))

    s = sb.toString()
    return s
}

fun formatDouble(x: Double) : String {
    val formatter = DecimalFormat("#0.00")
    return formatter.format(x)
}

fun escapeStr(s: String) : String {
    var r = s
    if (isEmpty(s)) return s

    r = r.replace("'", "''")
    return r
}

fun getInt(x: Boolean) : Int {
    val a = if (x) 1 else 0
    return a
}

fun getBoolean(x: Int) : Boolean {
    val a = x == 1
    return a
}

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
fun showProgress(show: Boolean, progress: View, context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            progress.visibility = if (show) View.VISIBLE else View.GONE
            progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        }

        else {
            progress.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    catch (e: Exception) {

    }
}

fun lockScreenOrientation(a: Activity) {
    try {
        val res = a.resources
        val currentOrientation = res.configuration.orientation
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT)
            a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        else
            a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    catch (e: Exception) {

    }
}

fun unlockScreenOrientation(a: Activity?) {
    a?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
}