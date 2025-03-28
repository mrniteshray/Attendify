package xcom.niteshray.apps.attendify.helper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xcom.niteshray.apps.attendify.R
import xcom.niteshray.apps.attendify.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment() {

    private var _binding : FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

}