package com.james.project.committee.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude


data class CommitteeFile(
    val name: String,
    val contentType: String,
    val size: String,
    val url: String,
    var id: String = "",
    @Exclude var committeeDirectory: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val resolvedContentType: FileType = FileType.TYPE_DEFAULT
) {
    constructor() : this("", "", "", "")
}

enum class FileType() {
    TYPE_AUDIO,
    TYPE_IMAGE,
    TYPE_PDF,
    TYPE_VIDEO,
    TYPE_DOC,
    TYPE_SPREADSHEET,
    TYPE_PPT,
    TYPE_ZIP,
    TYPE_DEFAULT
}

data class UploadProgress(
    var progress: Int,
    var p: String,
    var progressString: String,
    var justStarted: Boolean = false,
    var isComplete: Boolean = false
)