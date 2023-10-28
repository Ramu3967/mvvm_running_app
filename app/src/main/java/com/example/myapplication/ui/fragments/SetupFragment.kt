package com.example.myapplication.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSetupBinding
import com.example.myapplication.util.RunConstants.EMPTY_STRING
import com.example.myapplication.util.RunConstants.PREF_FIRST_TIME
import com.example.myapplication.util.RunConstants.PREF_USER_NAME
import com.example.myapplication.util.RunConstants.PREF_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(){
    private lateinit var binding:FragmentSetupBinding

    @Inject
    lateinit var appPref: SharedPreferences

    @set:Inject
    var isFirstRun : Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentSetupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editor = appPref.edit()
        binding.apply {
            if(!isFirstRun)
            {
                findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment(),
                    NavOptions.Builder()
                        .setPopUpTo(R.id.setupFragment,true)
                        .build())
            }
            saveInPref()
            etWeight.setText(appPref.getString(PREF_WEIGHT,EMPTY_STRING))
            etName.setText(appPref.getString(PREF_USER_NAME,EMPTY_STRING))

            tvContinue.setOnClickListener{
//            findNavController().navigate(R.id.action_setupFragment_to_runFragment)
                findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
                if(!etWeight.text.isNullOrEmpty()){
                    editor.putString(PREF_WEIGHT,etWeight.text.toString())
                }
                if(!etName.text.isNullOrEmpty()){
                    editor.putString(PREF_USER_NAME,etName.text.toString())
                }
                editor.apply()
            }
        }
    }

    private fun saveInPref() = appPref.edit().putBoolean(PREF_FIRST_TIME,false).apply()
}