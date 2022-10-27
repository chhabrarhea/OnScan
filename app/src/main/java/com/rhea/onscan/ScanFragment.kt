package com.rhea.onscan

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.BarcodeUtils
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import com.rhea.onscan.databinding.FragmentScanBinding

class ScanFragment : Fragment() {

    private var binding: FragmentScanBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    private val codeScanner: CodeScanner by lazy {
        CodeScanner(requireContext(), binding!!.scannerView)
    }

    private var isFlashOn = false

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>

    private var isFromGallery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isPermissionGranted ->
                if (isPermissionGranted) {
                    codeScanner.startPreview()
                    toggleStatusView(true, shouldReset = true)
                }
            }

        imageResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                // this code should run after onResume()
                lifecycleScope.launchWhenResumed {
                    val resultCode = result.resultCode
                    val data = result.data
                    if (resultCode == Activity.RESULT_OK) {
                        val fileUri = data?.data!!
                        processImageFromGallery(uri = fileUri)
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        toggleStatusView(false)
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupScanner()
        checkPermission()
    }

    private fun checkPermission() {
        if (!isHavingCameraPermissions())
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun initViews() {
        binding?.apply {
            backImageView.setOnClickListener {
                activity?.finish()
            }
            flashImageView.setOnClickListener {
                isFlashOn = !isFlashOn
                toggleFlash(isFlashOn)
            }
            galleryImageView.setOnClickListener {
                pickFromGallery()
            }
        }
    }

    private fun toggleFlash(isFlashOn: Boolean) {
        val drawable =
            if (isFlashOn) com.budiyev.android.codescanner.R.drawable.ic_code_scanner_flash_on
            else com.budiyev.android.codescanner.R.drawable.ic_code_scanner_flash_off
        binding?.flashImageView?.setImageResource(drawable)
        codeScanner.isFlashEnabled = isFlashOn
    }

    private fun setupScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS
        codeScanner.isAutoFocusEnabled = true
        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS
        codeScanner.scanMode = ScanMode.CONTINUOUS
        codeScanner.decodeCallback = DecodeCallback { result ->
            readQr(result, false)
        }
        codeScanner.errorCallback = ErrorCallback {
            activity?.runOnUiThread {
                toggleStatusView(false)
                it.message?.let { exceptionMessage ->
                    Snackbar.make(
                        requireView(),
                        exceptionMessage,
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
        }
        toggleStatusView(true, shouldReset = true)
    }

    private fun processImageFromGallery(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        requireContext().contentResolver,
                        uri
                    )
                ).copy(
                    Bitmap.Config.ARGB_8888, false
                )
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            val result = BarcodeUtils.decodeBitmap(bitmap)
            if (result != null) readQr(result, true)
            else toggleStatusView(false)
        } catch (e: Exception) {
            toggleStatusView(false)
        }
    }

    private fun readQr(result: Result, isUploaded: Boolean) {
        isFromGallery = isUploaded
        activity?.runOnUiThread {
        }
    }

    private fun isHavingCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun toggleStatusView(isSuccess: Boolean, shouldReset: Boolean = false) {
        binding?.apply {
            scannerView.frameColor = ContextCompat.getColor(
                requireContext(),
                if (shouldReset) R.color.white else if (isSuccess) R.color.secondary else R.color.error
            )
            scanStatusImageView.isVisible = !shouldReset
            scanStatusImageView.setImageResource(
                if (isSuccess) R.drawable.ic_success else R.drawable.ic_failed
            )
            scannerView.invalidate()
            scannerView.requestLayout()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }

    override fun onStop() {
        if (isHavingCameraPermissions())
            codeScanner.stopPreview()
        super.onStop()
    }

    override fun onPause() {
        if (isHavingCameraPermissions())
            codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroy() {
        if (isHavingCameraPermissions())
            codeScanner.releaseResources()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater)
        return binding!!.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
