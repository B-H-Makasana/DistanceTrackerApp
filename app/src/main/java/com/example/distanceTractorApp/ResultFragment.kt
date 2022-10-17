package com.example.distanceTractorApp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.distanceTractorApp.databinding.FragmentMapsBinding
import com.example.distanceTractorApp.databinding.FragmentResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
   private val args:ResultFragmentArgs by navArgs()

    private var _binding:FragmentResultBinding? =null
    private val binding get() =_binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       _binding=FragmentResultBinding.inflate(inflater,container,false)
        binding.distance.text=getString(R.string.result,args.result.distance)
        binding.timer.text=args.result.time
        binding.shareButton.setOnClickListener{
            shareResult()
        }
         return  binding.root

    }

    private fun shareResult() {
        val shareIntent=Intent().apply {
            action=Intent.ACTION_SEND
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT,"I went ${args.result.distance} in ${args.result.time}")
        }
        startActivity(shareIntent)
    }

}