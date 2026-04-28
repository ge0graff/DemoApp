package com.kochetkov.demoapp.presentation.movies.banner

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kochetkov.demoapp.R

class PromoBannerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.promo_banner_title)
            .setMessage(R.string.promo_banner_message)
            .setPositiveButton(R.string.promo_banner_action_text) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.promo_banner_close_text) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "promo_banner_dialog"
    }
}
