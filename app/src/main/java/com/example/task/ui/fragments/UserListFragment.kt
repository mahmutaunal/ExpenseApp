package com.example.task.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        userListViewModel.userList.observe(viewLifecycleOwner) { users ->
            users?.let {
                userAdapter.setData(it)
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        // Creating and setting adapters for RecyclerView
        userAdapter = UserAdapter { user, action ->
            when (action) {
                Action.CONNECT -> userListViewModel.connectUser(user)
                Action.FOLLOW -> userListViewModel.followUser(user)
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

}