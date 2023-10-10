package com.example.task.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task.databinding.ItemUserBinding
import com.example.task.model.Action
import com.example.task.model.User
import com.example.task.viewmodel.UserListViewModel

class UserAdapter(private val onItemClickListener: (User, Action, Int) -> Unit) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var userList = emptyList<User>()
    private var userListViewModel = UserListViewModel()

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.user = user
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[holder.adapterPosition]
        holder.bind(user)

        holder.itemView.setOnClickListener {
            userListViewModel.getIsConnectedStatus(object : IsConnectedCallback {
                override fun onIsCurrentUserConnectedFetched(isConnected: String) {
                    showOptionsDialog(it.context, user, isConnected, holder.adapterPosition)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
    }

    private fun showOptionsDialog(context: Context, user: User, isConnected: String, position: Int) {
        if (isConnected == "true") {
            val actions = arrayOf("Disconnect", "Follow Live")
            val builder = AlertDialog.Builder(context)
            builder.setItems(actions) { dialog, which ->
                when (which) {
                    0 -> onItemClickListener(user, Action.DISCONNECT, position)
                    1 -> onItemClickListener(user, Action.FOLLOW, position)
                }
                dialog.dismiss()
            }
            builder.show()
        } else {
            val actions = arrayOf("Connect", "Follow Live")
            val builder = AlertDialog.Builder(context)
            builder.setItems(actions) { dialog, which ->
                when (which) {
                    0 -> onItemClickListener(user, Action.CONNECT, position)
                    1 -> onItemClickListener(user, Action.FOLLOW, position)
                }
                dialog.dismiss()
            }
            builder.show()
        }
    }

    interface IsConnectedCallback {
        fun onIsCurrentUserConnectedFetched(isConnected: String)
    }

}