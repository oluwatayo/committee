package com.james.project.committee.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.james.project.committee.models.Committee

class GlobalViewModelFactory : ViewModelProvider.Factory {
    private var application: Application? = null
    private var committee: Committee? = null

    constructor(application: Application) {
        this.application = application
    }

    constructor(committee: Committee) {
        this.committee = committee
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(application!!) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel() as T
            modelClass.isAssignableFrom(CommitteeViewModel::class.java) -> CommitteeViewModel() as T
            else -> throw IllegalAccessException("error")
        }
    }
}