package com.rhea.onscan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rhea.onscan.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {
    private var binding: FragmentHomeBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            scanEthButton.setOnClickListener {
                viewModel.setQrType(MainViewModel.Companion.QrType.ETH)
            }
            scanBtcButton.setOnClickListener {
                viewModel.setQrType(MainViewModel.Companion.QrType.BTC)
            }
        }
    }
}