package com.james.project.committee.viewmodels

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.james.project.committee.adapters.HomeAdapter
import com.james.project.committee.models.Committee
import com.james.project.committee.models.Users
import com.james.project.committee.models.getUserTableName
import timber.log.Timber

class HomeViewModel : ViewModel() {

    private val _addNewCommittee = MutableLiveData<Boolean>()
    private var adapter: HomeAdapter? = null

    val committeeMutableLiveData: MutableLiveData<Committee> by lazy {
        MutableLiveData<Committee>().also {
            it.value = Committee("", "", Users.FIREBASE_UID)
        }
    }

    private val _createDone: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }

    val createDone: LiveData<Boolean>
        get() = _createDone

    val selectedCommittee: LiveData<Committee>
        get() = _homeAdapter.value!!.selectedItem

    private val _homeAdapter: MutableLiveData<HomeAdapter> by lazy {
        MutableLiveData<HomeAdapter>().also {
            val query: Query = FirebaseFirestore.getInstance().collection(getUserTableName())
                .document(Users.FIREBASE_UID!!)
                .collection(Committee.TABLE_NAME)
            val recyclerOptions: FirestoreRecyclerOptions<Committee> =
                FirestoreRecyclerOptions.Builder<Committee>()
                    .setQuery(query) { snapshot ->
                        val committee: Committee = snapshot.toObject(Committee::class.java)!!
                        committee.id = snapshot.id
                        committee
                    }
                    .build()
            adapter = HomeAdapter(recyclerOptions)
            adapter?.startListening()
            it.value = adapter
        }
    }

    val homeAdapter: LiveData<HomeAdapter>
        get() = _homeAdapter

    override fun onCleared() {
        super.onCleared()
        adapter?.stopListening()
    }

    val createCommitteeProgressStateLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = View.GONE
        }
    }

    private val _message: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.value = ""
        }
    }

    val message: LiveData<String>
        get() = _message

    val addNewCommittee: LiveData<Boolean>
        get() = _addNewCommittee


    fun showAddNewCommitteeLayout() {
        _addNewCommittee.value = true
    }

    fun createNewCommittee() {
        val committee = committeeMutableLiveData.value
        committee?.let {
            if (TextUtils.isEmpty(it.name) || TextUtils.isEmpty(it.description)) {
                _message.value = "Fill up all fields!!!"
                return
            }
            _message.value = ""
            createCommitteeProgressStateLiveData.value = View.VISIBLE
            committee.timestamp = Timestamp.now()
            FirebaseFirestore.getInstance().collection(Committee.TABLE_NAME)
                .add(committee)
                .addOnCompleteListener { task ->
                    createCommitteeProgressStateLiveData.value = View.GONE
                    if (task.isSuccessful) {
                        _createDone.value = true
                    } else {
                        Timber.e(task.exception)
                        _message.value = task.exception?.message
                    }
                }
        }
    }

    fun endAddNewCommittee(){
        _addNewCommittee.value = false
    }

    fun resetCreate() {
        _createDone.value = false
    }

    fun endNavigation() {
        _homeAdapter.value!!.resetSelected()
    }
}