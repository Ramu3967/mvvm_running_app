package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.RunAdapter
import com.example.myapplication.databinding.FragmentRunBinding
import com.example.myapplication.ui.viewmodels.MainViewModel
import com.example.myapplication.util.RunConstants
import com.example.myapplication.util.RunConstants.hasLocationPerm
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks{
    @Inject
    lateinit var locationPermissions:Array<String>
    private lateinit var binding:FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()
    private val runAdapter by lazy { RunAdapter(emptyList()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentRunBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            fab.setOnClickListener {
                findNavController().navigate(RunFragmentDirections.actionRunFragmentToTrackingFragment())
            }
        }
        requestPerms()
        binding.rvRuns.apply {
            adapter=runAdapter
            layoutManager=LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        }
        observeLiveData()

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                handleSorting(pos)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun observeLiveData() {
        viewModel.runLiveData.observe(viewLifecycleOwner){runAdapter.submitList(it)}
    }

    private fun handleSorting(pos: Int) {
        viewModel.apply {
            // maintain this order
            when(pos){
                0 -> switchSortingStrategy(RunConstants.SortingOptions.DATE)
                1 -> switchSortingStrategy(RunConstants.SortingOptions.TIME)
                2 -> switchSortingStrategy(RunConstants.SortingOptions.DISTANCE)
                3 -> switchSortingStrategy(RunConstants.SortingOptions.AVG_SPEED)
                4 -> switchSortingStrategy(RunConstants.SortingOptions.CALORIES)
            }
        }
    }

    private fun requestPerms(){
        if(!requireContext().hasLocationPerm()) EasyPermissions.requestPermissions(this,"accept the permissions",
            RunConstants.LOCATION_PERMISSION_REQUEST_CODE,*locationPermissions
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
            AppSettingsDialog.Builder(this).build().show()
        else requestPerms()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}