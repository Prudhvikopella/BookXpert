package com.bookxpert.app.ui.fragments.dialogs

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bookxpert.app.databinding.DialogImagePickerBinding
import com.bumptech.glide.Glide
import androidx.activity.result.contract.ActivityResultContracts

class ImagePickerDialogFragment : DialogFragment() {

    private var _binding: DialogImagePickerBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var onImageSelected: ((Uri?, Bitmap?) -> Unit)? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            loadImage(it)
            onImageSelected?.invoke(it, null)
        }
    }

    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            binding.imageView.setImageBitmap(it)  // Keeping bitmap loading for camera
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogImagePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGallery.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            captureImage.launch(null)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
