package com.joshua.vector_finance

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * VECTOR Finance home-screen widget: how much runway is left.
 */
class RunwayWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_runway)
            views.setTextViewText(R.id.widget_primary,
                context.getString(R.string.widget_loading))
            mgr.updateAppWidget(id, views)
            thread { refresh(context, mgr, id) }
        }
    }

    private fun refresh(context: Context, mgr: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_runway)
        try {
            val o = JSONObject(httpGet(context, "/finance"))
            val runway = if (o.isNull("runway_days")) null else o.optInt("runway_days")
            val free = if (o.isNull("free_today")) null else o.optDouble("free_today", 0.0)
            val currency = o.optString("currency", "IDR")
            views.setTextViewText(R.id.widget_primary,
                if (runway == null) "-- days" else runway.toString() + " days")
            views.setTextViewText(R.id.widget_secondary,
                if (free == null) "No spending logged yet"
                else fmt(free) + " " + currency + " free today")
            views.setTextViewText(R.id.widget_badge, "RUNWAY")
        } catch (e: Exception) {
            views.setTextViewText(R.id.widget_primary,
                context.getString(R.string.widget_offline))
            views.setTextViewText(R.id.widget_secondary,
                context.getString(R.string.widget_offline_hint))
            views.setTextViewText(R.id.widget_badge, "")
        }
        val open = Intent(context, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        views.setOnClickPendingIntent(R.id.widget_root,
            PendingIntent.getActivity(context, 0, open, flags))
        mgr.updateAppWidget(id, views)
    }


    /** Group digits so a 7-figure amount stays readable in a narrow widget. */
    private fun fmt(v: Double): String = String.format("%,.0f", v)

    private fun httpGet(context: Context, path: String): String =
        http(context, "GET", path, null)

    private fun httpPost(context: Context, path: String): String =
        http(context, "POST", path, "{}")

    /**
     * Single HTTP entry point. A widget provider runs on the main thread, so
     * every caller is responsible for being off it.
     */
    private fun http(context: Context, method: String, path: String,
                     body: String?): String {
        val base = context.getString(R.string.vector_api_base).trimEnd('/')
        val userId = context.getString(R.string.vector_user_id)
        val key = context.getString(R.string.vector_api_key)
        val conn = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("X-User-Id", userId)
            setRequestProperty("Accept", "application/json")
            if (key.isNotEmpty()) setRequestProperty("X-Api-Key", key)
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        try {
            if (body != null) {
                conn.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            return stream?.bufferedReader()?.use { it.readText() } ?: ""
        } finally {
            conn.disconnect()
        }
    }


}
