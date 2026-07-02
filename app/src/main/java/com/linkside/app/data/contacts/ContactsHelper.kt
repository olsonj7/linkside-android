package com.linkside.app.data.contacts

import android.content.Context
import android.provider.ContactsContract
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.model.Friend

object ContactsHelper {
    fun loadContacts(context: Context): List<Friend> {
        val results = linkedMapOf<String, Friend>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ) ?: return emptyList()

        cursor.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val rawPhone = it.getString(phoneIndex)?.trim().orEmpty()
                if (rawPhone.isEmpty() || !PhoneUtils.isValidPhone(rawPhone)) continue
                val normalized = PhoneUtils.normalizePhone(rawPhone)
                if (results.containsKey(normalized)) continue
                val displayName = it.getString(nameIndex)?.trim().orEmpty()
                val parts = displayName.split(" ", limit = 2)
                val firstName = parts.getOrElse(0) { "Golfer" }
                val lastName = parts.getOrElse(1) { "" }
                results[normalized] = Friend(
                    phone = normalized,
                    firstName = firstName,
                    lastName = lastName,
                )
            }
        }
        return results.values.sortedBy { it.fullName.lowercase() }
    }
}
