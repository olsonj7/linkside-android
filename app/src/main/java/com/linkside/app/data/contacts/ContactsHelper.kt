package com.linkside.app.data.contacts

import android.content.Context
import android.provider.ContactsContract
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.model.Friend

object ContactsHelper {
    private data class RawContact(
        val contactId: String,
        val name: String,
        val normalized: String,
        val label: String?,
    )

    fun loadContacts(context: Context): List<Friend> {
        val resolver = context.contentResolver
        val resources = context.resources
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ) ?: return emptyList()

        val raw = mutableListOf<RawContact>()
        // Track numbers already seen per contact so the same number listed twice
        // (rare, but happens) doesn't get counted as two distinct lines.
        val seenPerContact = mutableSetOf<String>()

        cursor.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            while (it.moveToNext()) {
                val rawPhone = it.getString(phoneIndex)?.trim().orEmpty()
                if (rawPhone.isEmpty() || !PhoneUtils.isValidPhone(rawPhone)) continue
                val normalized = PhoneUtils.normalizePhone(rawPhone)
                val contactId = it.getString(contactIdIndex).orEmpty()
                if (!seenPerContact.add("$contactId|$normalized")) continue

                val displayName = it.getString(nameIndex)?.trim().orEmpty()
                val type = if (typeIndex >= 0) it.getInt(typeIndex) else 0
                val customLabel = if (labelIndex >= 0) it.getString(labelIndex) else null
                val typeLabel = ContactsContract.CommonDataKinds.Phone
                    .getTypeLabel(resources, type, customLabel)
                    ?.toString()
                    ?.takeIf { l -> l.isNotBlank() }

                raw.add(
                    RawContact(
                        contactId = contactId,
                        name = displayName,
                        normalized = normalized,
                        label = typeLabel,
                    ),
                )
            }
        }

        // A label only helps when a single contact has more than one number.
        val numbersPerContact = raw.groupingBy { it.contactId }.eachCount()

        val results = linkedMapOf<String, Friend>()
        raw.forEach { entry ->
            // Global de-dup by number (first wins) — matches prior behavior.
            if (results.containsKey(entry.normalized)) return@forEach
            val parts = entry.name.split(" ", limit = 2)
            val firstName = parts.getOrElse(0) { "Golfer" }.ifEmpty { "Golfer" }
            val lastName = parts.getOrElse(1) { "" }
            val hasMultiple = (numbersPerContact[entry.contactId] ?: 0) > 1
            results[entry.normalized] = Friend(
                phone = entry.normalized,
                firstName = firstName,
                lastName = lastName,
                phoneLabel = if (hasMultiple) entry.label else null,
            )
        }

        return results.values.sortedBy { it.fullName.lowercase() }
    }
}
