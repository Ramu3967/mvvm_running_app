package com.example.myapplication.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentSettingsBinding
import com.example.myapplication.util.RunConstants
import com.example.myapplication.util.RunConstants.EMPTY_STRING
import com.example.myapplication.util.RunConstants.PREF_USER_NAME
import com.example.myapplication.util.RunConstants.PREF_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(){
    lateinit var binding:FragmentSettingsBinding
    @Inject
    lateinit var appPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle? )=FragmentSettingsBinding.inflate(inflater,container,false).run { binding=this; root}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnApplyChanges.setOnClickListener {
                if(!(etName.text.isNullOrEmpty() || etWeight.text.isNullOrEmpty())){
                    appPreferences.edit()
                        .putString(PREF_USER_NAME,etName.text.toString())
                        .putString(RunConstants.PREF_WEIGHT,etWeight.text.toString())
                        .apply()
                    Snackbar.make(it,"Changes saved successfully",Snackbar.LENGTH_SHORT).show()
                }
                else Snackbar.make(it,"All fields must be filled",Snackbar.LENGTH_SHORT).show()
            }
            etName.setText(appPreferences.getString(PREF_USER_NAME, EMPTY_STRING))
            etWeight.setText(appPreferences.getString(PREF_WEIGHT, EMPTY_STRING))
        }
    }
}