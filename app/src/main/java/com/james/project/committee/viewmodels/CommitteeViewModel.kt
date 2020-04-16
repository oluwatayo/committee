package com.james.project.committee.viewmodels

import android.net.Uri
import android.util.Patterns
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.james.project.committee.adapters.CommitteeFilesAdapter
import com.james.project.committee.adapters.CommitteeUsersAdapter
import com.james.project.committee.adapters.MessageAdapter
import com.james.project.committee.models.*
import kotlin.math.roundToInt

class CommitteeViewModel : ViewModel() {
    private lateinit var query: CollectionReference
    private lateinit var usersQuery: CollectionReference
    private lateinit var filesQuery: CollectionReference
    private val storageRef: StorageReference by lazy {
        Firebase.storage.reference
    }
    private var usersRef = FirebaseFirestore.getInstance().collection(getUserTableName())
    private var _committee: MutableLiveData<Committee> = MutableLiveData()
    private val _closeCreateUserDialog: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }

    private val _showAddNewUserButton: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = _committee.value?.createdBy == Users.FIREBASE_UID
        }
    }

    val showAddNewUserButton: LiveData<Boolean>
        get() = _showAddNewUserButton

    val closeCreateUserDialog: LiveData<Boolean>
        get() = _closeCreateUserDialog
    private val _messageAdapterLiveData: MutableLiveData<MessageAdapter> by lazy {
        MutableLiveData<MessageAdapter>().also {
            it.value = getAdapter()
        }
    }

    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.value = ""
        }
    }

    private val _committeeUsersAdapter: MutableLiveData<CommitteeUsersAdapter> by lazy {
        MutableLiveData<CommitteeUsersAdapter>().also {
            it.value = getCommitteeUsers()
        }
    }

    private val _committeeFilesAdapter: MutableLiveData<CommitteeFilesAdapter> by lazy {
        MutableLiveData<CommitteeFilesAdapter>().also {
            it.value = getCommitteeFiles()
        }
    }

    val committeeFilesAdapter: LiveData<CommitteeFilesAdapter>
        get() = _committeeFilesAdapter

    val committeeUsersAdapter: LiveData<CommitteeUsersAdapter>
        get() = _committeeUsersAdapter

    private val _showCommitteeDetails: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }


    val createUserProgressStateLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = View.GONE
        }
    }

    val showCommitteeDetails: LiveData<Boolean>
        get() = _showCommitteeDetails

    private fun getAdapter(): MessageAdapter {
        val options = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query.orderBy("timestamp", Query.Direction.DESCENDING)) { snapshot ->
                val message: Message = snapshot.toObject(Message::class.java)!!
                message.messageType = when {
                    message.notification -> MessageType.MESSAGE_NOTIFICATIONS
                    message.senderId != Users.FIREBASE_UID -> MessageType.MESSAGE_OTHERS
                    else -> MessageType.MESSAGE_ME
                }
                message
            }
            .build()
        val adapter = MessageAdapter(options)
        adapter.startListening()
        return adapter
    }

    val message: MutableLiveData<Message> by lazy {
        MutableLiveData<Message>().also {
            val users = AuthViewModel.getCurrentLoggedinUser()
            it.value = Message(
                "",
                Users.FIREBASE_UID!!,
                "${users.firstName} ${users.lastName}",
                users.displayImage
            )
        }
    }

    private lateinit var committeeDir: String
    fun setSelectedCommittee(committee: Committee) {
        _committee.value = committee
        committeeDir = "Committee/${committee.name.replace(" ", "-")}"
        val baseRef = FirebaseFirestore.getInstance().collection(Committee.TABLE_NAME)
            .document(_committee.value?.id!!)
        query = baseRef
            .collection(Message.TABLE_NAME)
        _messageAdapterLiveData.value = getAdapter()
        usersQuery = baseRef
            .collection("users")
        _committeeUsersAdapter.value = getCommitteeUsers()
        filesQuery = baseRef
            .collection("files")
        _committeeFilesAdapter.value = getCommitteeFiles()
    }

    private fun getCommitteeUsers(): CommitteeUsersAdapter? {
        val options = FirestoreRecyclerOptions.Builder<CommitteeUser>()
            .setQuery(
                usersQuery.orderBy("dateAdded", Query.Direction.ASCENDING),
                CommitteeUser::class.java
            )
            .build()
        val adapter = CommitteeUsersAdapter(options, _committee.value!!)
        adapter.startListening()
        return adapter
    }

    private val _directoryToOpen: MutableLiveData<CommitteeFile?> by lazy {
        MutableLiveData<CommitteeFile?>().also { it.value = null }
    }

    val directoryToOpen: LiveData<CommitteeFile?>
        get() = _directoryToOpen

    fun closeDirectory() {
        _directoryToOpen.value = null
    }

    private fun getCommitteeFiles(): CommitteeFilesAdapter? {
        val options = FirestoreRecyclerOptions.Builder<CommitteeFile>()
            .setQuery(
                filesQuery.orderBy("timestamp", Query.Direction.DESCENDING)
            ) {
                val committeeFile = it.toObject(CommitteeFile::class.java)
                committeeFile?.apply {
                    id = it.id
                    committeeDirectory = committeeDir
                }!!
            }
            .build()
        val adapter = CommitteeFilesAdapter(options) {
            _directoryToOpen.value = this
        }
        adapter.startListening()
        return adapter
    }

    val messageAdapterObserver: LiveData<MessageAdapter>
        get() = _messageAdapterLiveData

    val currentCommittee: LiveData<Committee>
        get() = _committee

    fun sendMessage() {
        val messageToSend = message.value!!
        if (messageToSend.message.isEmpty())
            return
        messageToSend.timestamp = Timestamp.now()
        query.add(messageToSend)
            .addOnCompleteListener {
            }
        messageToSend.message = ""
        message.value = messageToSend
    }

    fun showCommitteeDetails(bool: Boolean) {
        _showCommitteeDetails.value = bool
    }

    fun addNewUserToCommittee(email: String, role: String) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error.value = "$email is not a valid email address"
            return
        }
        if (role.isEmpty()) {
            error.value = "please enter a role"
            return
        }
        error.value = ""
        createUserProgressStateLiveData.value = View.VISIBLE
        usersRef.whereEqualTo("email", email)
            .get()
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    createUserProgressStateLiveData.value = View.GONE
                    error.value = "An error occurred, try again"
                    return@addOnCompleteListener
                }
                if (it.result!!.size() > 0) {
                    val user = it.result!!.documents[0].toObject(Users::class.java)
                    user?.id = it.result!!.documents[0].id
                    val committeeUser = CommitteeUser(user, role, Timestamp.now())
                    usersQuery.document(it.result!!.documents[0].id)
                        .set(committeeUser)
                        .addOnCompleteListener { task ->
                            createUserProgressStateLiveData.value = View.GONE
                            if (task.isSuccessful) {
                                _closeCreateUserDialog.value = true
                            } else {
                                error.value = task.exception?.localizedMessage
                            }
                        }
                } else {
                    createUserProgressStateLiveData.value = View.GONE
                    error.value = "user with email $email not found"
                }
            }

    }

    private var uploadTask: UploadTask? = null
    private val _progressLiveData: MutableLiveData<UploadProgress> by lazy {
        MutableLiveData<UploadProgress>().also {
            it.value = getFreshUploadProgress()
        }
    }
    val progressLiveData: LiveData<UploadProgress>
        get() = _progressLiveData

    private val _info: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>().also {
            it.value = null
        }
    }

    val info: LiveData<String?>
        get() = _info

    fun endInfoBroadcast() {
        _info.value = null
    }

    private fun getFreshUploadProgress() = UploadProgress(0, "0 %", "")

    fun uploadFileToStorage(fileUri: Uri, fileName: String) {
        uploadTask =
            storageRef.child("committee:{${_committee.value?.id!!}}/$fileName").putFile(fileUri)
        var uploadProgress = getFreshUploadProgress()
        uploadProgress.justStarted = true
        _progressLiveData.value = uploadProgress
        uploadTask?.addOnProgressListener {
            uploadProgress = _progressLiveData.value!!
            val progress = ((100.0 * it.bytesTransferred) / it.totalByteCount).roundToInt()
            uploadProgress.apply {
                justStarted = false
                this.progress = progress
                p = "$progress %"
                progressString =
                    "${it.bytesTransferred} / ${it.totalByteCount} bytes transferred"
            }
            _progressLiveData.value = uploadProgress
        }
        uploadTask?.addOnCompleteListener {
            uploadProgress.isComplete = true
            _progressLiveData.value = uploadProgress
            if (it.isSuccessful)
                _info.value = "finishing $fileName upload, Please wait!!!"
            else
                _info.value = it.exception?.message
        }
    }

    fun cancelUploadTask() {
        uploadTask?.cancel()
        val up = _progressLiveData.value
        up?.isComplete = true
        _progressLiveData.value = up
    }


    override fun onCleared() {
        super.onCleared()
        _messageAdapterLiveData.value?.stopListening()
        _committeeUsersAdapter.value?.stopListening()
        _committeeFilesAdapter.value?.stopListening()
    }
}