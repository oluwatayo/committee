package com.james.project.committee.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.james.project.committee.R
import com.james.project.committee.databinding.LayoutComDetBinding
import com.james.project.committee.databinding.UploadDialogLayoutBinding
import com.james.project.committee.utils.GeneralUtil
import com.james.project.committee.viewmodels.CommitteeViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.math.roundToInt

const val PERMISSIONS_REQUEST_CODE = 10011

class CommitteeFilesFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    companion object {
        private const val FILE_REQUEST_CODE = 1001
        fun newInstance(): CommitteeFilesFragment {
            return CommitteeFilesFragment()
        }
    }

    private lateinit var binding: LayoutComDetBinding
    private val viewmodel: CommitteeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            requestPermissions()
        }
        binding = LayoutComDetBinding.inflate(inflater, container, false)
        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.fab.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_attachment))
        binding.fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            val i = Intent.createChooser(intent, "File")
            startActivityForResult(
                i,
                FILE_REQUEST_CODE
            )
        }
        val progressDialog =
            AlertDialog.Builder(context)
        val uploadBinding = UploadDialogLayoutBinding.inflate(inflater, null, false)
        uploadBinding.viewmodel = viewmodel
        uploadBinding.lifecycleOwner = viewLifecycleOwner
        progressDialog.setView(uploadBinding.root)
        progressDialog.setCancelable(false)
        val pDialog = progressDialog.create()
        viewmodel.progressLiveData.observe(viewLifecycleOwner, Observer {
            when {
                it.isComplete && pDialog.isShowing -> pDialog.cancel()
                it.justStarted -> pDialog.show()
            }
        })
        viewmodel.info.observe(viewLifecycleOwner, Observer {
            it?.run {
                showSnackbar(this)
                viewmodel.endInfoBroadcast()
            }
        })
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        viewmodel.committeeFilesAdapter.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.rv.adapter = it
                binding.emptyRepo.visibility = if (it.itemCount < 1) View.VISIBLE else View.GONE
            }
        })
        viewmodel.directoryToOpen.observe(viewLifecycleOwner, Observer {
            it?.let {
                try {
                    val file =
                        File("${GeneralUtil.getRootDirPath()}/${it.committeeDirectory}/${it.name}")
                    val uri = FileProvider.getUriForFile(
                        context!!,
                        context?.applicationContext?.packageName + ".provider", file
                    )
                    Timber.e(uri.toString())
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, it.contentType)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                }
                viewmodel.closeDirectory()
            }
        })
        return binding.root
    }

    override fun onPermissionsGranted(
        requestCode: Int,
        perms: List<String?>
    ) {
        showSnackbar("Storage permissions granted")
    }

    override fun onPermissionsDenied(
        requestCode: Int,
        perms: List<String?>
    ) {
        Toast.makeText(
            context,
            "You must accept all permissions to enable all app features",
            Toast.LENGTH_LONG
        ).show()
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        requestPermissions()
    }

    private fun requestPermissions() =
        EasyPermissions.requestPermissions(
            this,
            "This app needs to write data to your Storage. Please accept all permissions else app will misbehave",
            PERMISSIONS_REQUEST_CODE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FILE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                try {
                    prepareUploadFile(data?.data!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                    showSnackbar("The selected file was not found on this device")
                }
            }
        }
    }

    private fun showSnackbar(s: String) =
        Snackbar.make(binding.mainCoordinator, s, Snackbar.LENGTH_LONG).show()


    @Throws(IOException::class)
    private fun prepareUploadFile(fileUri: Uri) {
        val afd: AssetFileDescriptor =
            context!!.contentResolver.openAssetFileDescriptor(fileUri, "r")!!
        val fileSize = afd.length
        afd.close()
        val sizeInMegaBytes =
            (fileSize / (1024 * 1024) * 10.toFloat()).roundToInt().toFloat() / 10
        if (sizeInMegaBytes > 15.1) {
            showSnackbar("The file size should not be larger than 15.0 mb")
            return
        }
        viewmodel.uploadFileToStorage(fileUri, GeneralUtil.getFileName(fileUri, context!!))
    }
}