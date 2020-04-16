package com.james.project.committee.fragments

import android.app.Dialog
import android.os.Bundle

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.james.project.committee.R


open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }
}