package com.james.project.committee.viewmodels

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.james.project.committee.models.Users
import com.james.project.committee.models.getUserTableName
import com.pixplicity.easyprefs.library.Prefs

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SIGN_UP_COMPLETE = "sign_up_done"
        const val USER_FIRST_NAME = "firstname"
        const val USER_LAST_NAME = "lastname"
        const val USER_EMAIL = "email"
        const val USER_DISPLAY_IMAGE = "display_image"

        fun getCurrentLoggedinUser(): Users {
            val firstname = Prefs.getString(USER_FIRST_NAME, "")
            val lastname = Prefs.getString(USER_LAST_NAME, "")
            val displayImage = Prefs.getString(USER_DISPLAY_IMAGE, "")
            val email = Prefs.getString(USER_EMAIL, "")
            return Users(firstname, lastname, "", displayImage, email)
        }
    }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _progressVisibilityLiveData = MutableLiveData<Int>()
    private val _googleSignInLiveData = MutableLiveData<Boolean>()
    private val _showCompleteProfileLayout = MutableLiveData<Boolean>()
    val userLiveData = MutableLiveData<Users>()

    val showCompleteProfileLayoutLiveData: LiveData<Boolean>
        get() = _showCompleteProfileLayout

    val progressVisibilityLiveData: LiveData<Int>
        get() = _progressVisibilityLiveData

    val googleSignInLiveData: LiveData<Boolean>
        get() = _googleSignInLiveData
    private val _signInDone = MutableLiveData<Boolean>()

    val signInDone: LiveData<Boolean>
        get() = _signInDone

    init {
        _progressVisibilityLiveData.value = View.GONE
        auth.addAuthStateListener {
            if (!Prefs.getBoolean(SIGN_UP_COMPLETE, false))
                it.currentUser?.checkIfUserAddedToDatabase()
            else _signInDone.value = true
        }
    }

    private fun FirebaseUser.checkIfUserAddedToDatabase() {
        _progressVisibilityLiveData.value = View.VISIBLE
        firebaseFireStore.collection(getUserTableName()).document(uid).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val snapshot = it.result!!
                    if (snapshot.exists()) {
                        val u = snapshot.toObject(Users::class.java)
                        saveUserToPref(u!!)
                    } else {
                        userLiveData.value = Users("", "", "", "", "")
                        _showCompleteProfileLayout.value = true
                        _progressVisibilityLiveData.value = View.GONE
                    }
                }
            }
    }


    fun addUserToDatabse() {
        _progressVisibilityLiveData.value = View.VISIBLE
        val users = userLiveData.value
        if (TextUtils.isEmpty(users?.firstName) || TextUtils.isEmpty(users?.lastName))
            return
        users?.email = auth.currentUser?.email!!
        users?.displayImage = auth.currentUser?.photoUrl.toString()
        firebaseFireStore.collection(getUserTableName()).document(auth.uid!!).set(users!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToPref(users)
                }
            }

    }

    private fun saveUserToPref(users: Users) {
        Prefs.putBoolean(SIGN_UP_COMPLETE, true)
        Prefs.putString(USER_FIRST_NAME, users.firstName)
        Prefs.putString(USER_LAST_NAME, users.lastName)
        Prefs.putString(USER_EMAIL, users.email)
        Prefs.putString(USER_DISPLAY_IMAGE, users.displayImage)
        _signInDone.value = true
    }

    fun endAuth() {
        _signInDone.value = false
    }

    fun signInWithGoogle() {
        _googleSignInLiveData.value = true
    }

    fun endGoogleSignIn() {
        _googleSignInLiveData.value = false
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        _progressVisibilityLiveData.value = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    //Log.d(TAG, "signInWithCredential:success")
//                    //updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    //Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    //Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
//                    //updateUI(null)
//                }
            }
    }
}