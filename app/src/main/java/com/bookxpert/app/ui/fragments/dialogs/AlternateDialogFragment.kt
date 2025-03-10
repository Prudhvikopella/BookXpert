package com.bookxpert.app.ui.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.ImageView
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.bookxpert.app.R
import com.bookxpert.app.utils.Account
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class AlternateNameDialogFragment : DialogFragment() {

    private var account: Account? = null
    private var onAccountUpdated: ((Account) -> Unit)? = null
    private var onAccountDeleted: ((Account) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            account = BundleCompat.getParcelable(it, "account", Account::class.java)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_alternate_name, null)
        val etAlternateName = view.findViewById<TextInputEditText>(R.id.alternateNameInput)
        val btnMic = view.findViewById<ImageView>(R.id.btnMic)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        etAlternateName.setText(account?.accountAltName)

        // Speech-to-text functionality
        btnMic.setOnClickListener {
            startSpeechToText { spokenText ->
                etAlternateName.setText(spokenText)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        // Save Button Click
        btnSave.setOnClickListener {
            val newAltName = etAlternateName.text.toString().trim()
            if (newAltName.isNotEmpty() && newAltName != account?.accountAltName) {
                val updatedAccount = account?.copy(accountAltName = newAltName)
                updatedAccount?.let { onAccountUpdated?.invoke(it) }
            }
            dialog.dismiss()
        }

        // Delete Button Click
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        return dialog
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account?")
            .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                account?.let { onAccountDeleted?.invoke(it) }
                dialog?.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startSpeechToText(onResult: (String) -> Unit) {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        startActivityForResult(speechIntent, SPEECH_REQUEST_CODE)
        speechResultCallback = onResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            speechResultCallback?.invoke(spokenText)
        }
    }

    companion object {
        private const val SPEECH_REQUEST_CODE = 101
        private var speechResultCallback: ((String) -> Unit)? = null

        fun newInstance(
            account: Account,
            onAccountUpdated: (Account) -> Unit,
            onAccountDeleted: (Account) -> Unit
        ): AlternateNameDialogFragment {
            return AlternateNameDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("account", account) // Ensure Account implements Parcelable
                }
                this.onAccountUpdated = onAccountUpdated
                this.onAccountDeleted = onAccountDeleted
            }
        }
    }
}
