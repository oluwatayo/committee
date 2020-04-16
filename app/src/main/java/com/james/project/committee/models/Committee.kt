package com.james.project.committee.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Committee(
    var name: String,
    var description: String,
    var createdBy: String?,
    var timestamp: Timestamp? = null,
    var id: String? = null
): Parcelable {

    constructor() : this("", "", "")

    companion object {
        const val TABLE_NAME = "committees"
    }
}