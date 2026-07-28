package com.linkside.app.data.api

import android.net.Uri
import android.util.Log
import com.linkside.app.BuildConfig

object CoursePhotoUtils {
    private const val TAG = "CoursePhoto"

    /**
     * Same URL shape as iOS `ApiService.coursePhotoURL`.
     * Uses [Uri.encode] (percent-encoding, spaces as %20) — not `URLEncoder`
     * (which uses + for spaces and can diverge from iOS/CFNetwork).
     */
    fun photoUrl(placeId: String?, courseName: String): String? {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val url = when {
            !placeId.isNullOrBlank() ->
                "$base/courses/photo?placeId=${Uri.encode(placeId)}"
            courseName.isNotBlank() ->
                "$base/courses/photo?name=${Uri.encode(courseName)}"
            else -> null
        }
        if (BuildConfig.DEBUG && url != null) {
            Log.d(TAG, "photoUrl placeId=$placeId name=$courseName → $url")
        }
        return url
    }
}
