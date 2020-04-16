package com.james.project.committee.models

import com.google.firebase.Timestamp

data class Message(
    var message: String,
    val senderId: String,
    val senderName: String,
    val senderImage: String,
    val notification: Boolean = false,
    var timestamp: Timestamp = Timestamp.now(),
    var messageType: MessageType = MessageType.DEFAULT,
    val time: String = timestamp.toString()
) {
    companion object {
        const val TABLE_NAME = "messages"
    }

    constructor() : this("", "", "", "")
}


enum class MessageType(val viewType: Int) {
    MESSAGE_ME(1),
    MESSAGE_OTHERS(2),
    MESSAGE_NOTIFICATIONS(3),
    DEFAULT(-1)
}