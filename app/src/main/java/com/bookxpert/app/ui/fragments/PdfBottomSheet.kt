package com.bookxpert.app.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bookxpert.app.databinding.BottomSheetPdfBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PdfBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPdfBinding? = null
    private val binding get() = _binding!!
    private var pdfUrl: String? = null

    companion object {
        private const val ARG_PDF_URL = "pdf_url"

        fun newInstance(pdfUrl: String): PdfBottomSheet {
            return PdfBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PDF_URL, pdfUrl)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfUrl = arguments?.getString(ARG_PDF_URL)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPdfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfUrl?.let { url ->
            lifecycleScope.launch {
                val file = downloadPdf(url)
                if (file != null) {
                    binding.pdfView.fromFile(file)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .load()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun downloadPdf(url: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("PdfBottomSheet", "Failed to download PDF")
                    return@withContext null
                }

                val inputStream: InputStream? = response.body?.byteStream()
                val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "temp.pdf")
                val outputStream = FileOutputStream(file)

                inputStream?.copyTo(outputStream)

                outputStream.flush()
                outputStream.close()
                inputStream?.close()

                return@withContext file
            } catch (e: Exception) {
                Log.e("PdfBottomSheet", "Error: ${e.message}")
                null
            }
        }
    }
}
