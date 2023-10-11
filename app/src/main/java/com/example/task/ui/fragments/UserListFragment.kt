package com.example.task.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task.adapter.UserAdapter
import com.example.task.databinding.FragmentUserListBinding
import com.example.task.model.Action
import com.example.task.viewmodel.UserListViewModel

class UserListFragment : Fragment() {

    private lateinit var binding: FragmentUserListBinding
    private lateinit var userAdapter: UserAdapter
    private val userListViewModel: UserListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserListBinding.inflate(inflater, container, false)

        setupRecyclerView()

        binding.viewModel = userListViewModel
        binding.lifecycleOwner = this

        // Update progress bar visibility with LiveData
        userListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        userListViewModel.userList.observe(viewLifecycleOwner) { users ->
            users?.let {
                userAdapter.setData(it)
            }
        }

        // Observe the refreshing LiveData and trigger refresh on change
        userListViewModel.refreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        // Set the listener for swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            userListViewModel.onRefresh()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        // Creating and setting adapters for RecyclerView
        userAdapter = UserAdapter { user, action, position ->
            when (action) {
                Action.CONNECT -> userListViewModel.connectUser(listOf(user), position)
                Action.DISCONNECT -> userListViewModel.disconnectUser(listOf(user), position)
                Action.FOLLOW -> checkNotificationPermission()
                Action.UNFOLLOW -> checkNotificationPermission()
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun checkNotificationPermission() {
        // Check notification permission
        val permission = Manifest.permission.RECEIVE_SMS

        // Has permission already been granted? Send follow request notification if given
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            userListViewModel.sendPushNotification()
        } else {
            // Ask for permission
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECEIVE_SMS), PERMISSION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, send follow request notification
                    userListViewModel.sendPushNotification()
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

}