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

class UserAdapter(private val onItemClickListener: (User, Action) -> Unit) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var userList = emptyList<User>()
    private val userListViewModel = UserListViewModel()

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
        val user = userList[position]
        holder.bind(user)

        holder.itemView.setOnClickListener {
            showOptionsDialog(it.context, user)
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

    private fun showOptionsDialog(context: Context, user: User) {
        val actions = arrayOf("Connect", "Follow Live")
        val builder = AlertDialog.Builder(context)
        builder.setItems(actions) { dialog, which ->
            when (which) {
                0 -> onItemClickListener(user, Action.CONNECT)
                1 -> onItemClickListener(user, Action.FOLLOW)
            }
            dialog.dismiss()
        }
        builder.show()
    }

}