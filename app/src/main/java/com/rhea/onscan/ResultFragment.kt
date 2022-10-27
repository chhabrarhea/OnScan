package com.rhea.onscan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.rhea.onscan.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var binding: FragmentResultBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObserver()
    }

    private fun setObserver() {
        viewModel.scannedResult.observe(viewLifecycleOwner) { result ->
            binding?.apply {
                shareButton.setOnClickListener {
                    if (result)
                        shareQrData()
                    else showSnackbar("The QR code seems to be Invalid!")
                }
                validateButton.setOnClickListener {
                    val text = if (result) "The QR code is valid!" else "The QR code is invalid :("
                    showSnackbar(text)
                }
            }
        }
    }

    private fun shareQrData() {
        viewModel.scannedAddress.value?.let {
            val chooser: Intent =
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it)
                        type = "text/plain"
                    },
                    "Share QR data",
                )
            requireContext().startActivity(chooser)
        }
    }

    private fun showSnackbar(text: String) {
        binding?.root?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        binding?.apply {
            closeButton.setOnClickListener {
                requireActivity().onBackPressed()
            }
            infoHeaderTv.text = getString(R.string.result_heading, viewModel.qrType.value?.name)
            resultTv.text = viewModel.scannedAddress.value
            resultTv.setOnClickListener {
                viewModel.copyResult()
                showSnackbar("Copied!")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}