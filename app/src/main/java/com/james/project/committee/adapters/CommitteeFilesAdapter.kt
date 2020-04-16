package com.james.project.committee.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.james.project.committee.databinding.CommitteeFileItemBinding
import com.james.project.committee.models.CommitteeFile
import com.james.project.committee.services.*
import com.james.project.committee.utils.GeneralUtil
import com.pixplicity.easyprefs.library.Prefs
import java.io.File
import java.io.FileNotFoundException


class CommitteeFilesAdapter(
    options: FirestoreRecyclerOptions<CommitteeFile>,
    val listener: CommitteeFile.() -> Unit
) :
    FirestoreRecyclerAdapter<CommitteeFile, CommitteeFilesViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommitteeFilesViewHolder {
        return CommitteeFilesViewHolder.from(parent, listener)
    }

    override fun onBindViewHolder(
        holder: CommitteeFilesViewHolder,
        position: Int,
        model: CommitteeFile
    ) {
        holder.bind(model)
    }


}

class CommitteeFilesViewHolder(
    private val binding: CommitteeFileItemBinding,
    private val listener: CommitteeFile.() -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup, listner: (CommitteeFile) -> Unit): CommitteeFilesViewHolder {
            return CommitteeFilesViewHolder(
                CommitteeFileItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                listner
            )
        }
    }

    fun bind(committeeFile: CommitteeFile) {
        binding.file = committeeFile
        binding.root.setOnClickListener {
            when {
                fileAlreadyExists(committeeFile) -> openCommitteeFolder(committeeFile)
                isFileStillDownloading(committeeFile) -> Snackbar.make(
                    binding.root,
                    "This file is still downloading, please wait",
                    Snackbar.LENGTH_LONG
                ).show()
                else -> downloadFile(committeeFile)
            }
        }
        binding.downloadIndicator.visibility =
            if (fileAlreadyExists(committeeFile)) View.GONE else View.VISIBLE
    }

    private fun isFileStillDownloading(committeeFile: CommitteeFile) = Prefs.getBoolean(
        "${IS_DOWNLOADING}_${committeeFile.id}",
        false
    )

    private fun fileAlreadyExists(committeeFile: CommitteeFile): Boolean {
        try {
            val n = File(
                GeneralUtil.getRootDirPath(),
                committeeFile.committeeDirectory
            )
            val file =
                File(n, committeeFile.name)
            return if (file.exists()) {
                binding.downloadIndicator.visibility = View.GONE
                true
            } else {
                false
            }

        } catch (e: FileNotFoundException) {
            return false
        }
    }

    private fun downloadFile(committeeFile: CommitteeFile) {
        binding.root.context.startService(
            Intent(
                binding.root.context,
                FileDownloadService::class.java
            ).apply {
                putExtra(FILE_DIRECTORY, committeeFile.committeeDirectory)
                putExtra(FILE_NAME, committeeFile.name)
                putExtra(FILE_URL, committeeFile.url)
            })
    }

    private fun openCommitteeFolder(committeeFile: CommitteeFile) {
        listener.invoke(committeeFile)
    }
}

