package com.linkside.app.data.api

import com.linkside.app.BuildConfig

object CoursePhotoUtils {
    fun photoUrl(placeId: String?, courseName: String): String? {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        if (!placeId.isNullOrBlank()) {
            return "$base/courses/photo?placeId=${java.net.URLEncoder.encode(placeId, Charsets.UTF_8.name())}"
        }
        if (courseName.isNotBlank()) {
            return "$base/courses/photo?name=${java.net.URLEncoder.encode(courseName, Charsets.UTF_8.name())}"
        }
        return null
    }
}
