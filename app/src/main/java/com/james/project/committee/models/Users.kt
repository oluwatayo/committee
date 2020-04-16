package com.james.project.committee.models

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

data class Users(
    var firstName: String,
    var lastName: String,
    var bio: String,
    var displayImage: String,
    var email: String,
    var id: String? = FIREBASE_UID
) {
    companion object {
        var FIREBASE_UID: String? = FirebaseAuth.getInstance().uid
    }

    constructor() : this("", "", "", "", "")
}

data class CommitteeUser(
    val users: Users?,
    val role: String,
    val dateAdded: Timestamp
) {

    constructor() : this(null, "", Timestamp.now())
}

fun getUserTableName() = "Users"