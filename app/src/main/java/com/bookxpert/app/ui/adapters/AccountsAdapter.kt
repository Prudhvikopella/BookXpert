package com.bookxpert.app.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bookxpert.app.databinding.ItemAccountBinding
import com.bookxpert.app.helper.SwipeToDeleteCallback
import com.bookxpert.app.ui.fragments.dialogs.AlternateNameDialogFragment
import com.bookxpert.app.utils.Account

class AccountAdapter(
    private val onAccountUpdated: (Account) -> Unit,
    private val onAccountDeleted: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var accountList: MutableList<Account> = mutableListOf()
    private var filteredList: MutableList<Account> = mutableListOf()

    fun updateList(newList: List<Account>) {
        accountList.clear()
        accountList.addAll(newList)
        filteredList = accountList.toMutableList() // ✅ Keep filteredList in sync
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(filteredList[position], onAccountUpdated, onAccountDeleted, { updatedAccount ->
            updateItem(updatedAccount)
        }, { deletedAccount ->
            removeItem(deletedAccount)
        })
    }

    override fun getItemCount(): Int = filteredList.size

    private fun updateItem(updatedAccount: Account) {
        val index = filteredList.indexOfFirst { it.accountId == updatedAccount.accountId }
        if (index != -1) {
            filteredList[index] = updatedAccount
            notifyItemChanged(index) // ✅ Notify RecyclerView to refresh the updated item
        }
    }

     fun removeItem(account: Account) {
        val index = filteredList.indexOfFirst { it.accountId == account.accountId }
        if (index != -1) {
            filteredList.removeAt(index)
            notifyItemRemoved(index) // ✅ Notify RecyclerView to remove the item
        }
    }

    class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            account: Account,
            onAccountUpdated: (Account) -> Unit,
            onAccountDeleted: (Account) -> Unit,
            updateItem: (Account) -> Unit,
            removeItem: (Account) -> Unit
        ) {
            binding.apply {
                tvAccountName.text = account.accountName
                tvAccountAltName.text = account.accountAltName
                tvAddAltName.text =
                    if (account.accountAltName.isEmpty()) "Add alternate name" else "Edit alternate name"
                tvAccountId.text = "ID: ${account.accountId}"
                tvSerialNumber.text = "S/N: ${adapterPosition + 1}"

                tvAddAltName.setOnClickListener {
                    val context = binding.root.context
                    if (context is FragmentActivity && !context.supportFragmentManager.isStateSaved) {
                        AlternateNameDialogFragment.newInstance(
                            account,
                            onAccountUpdated = { updatedAccount ->
                                tvAccountAltName.text = updatedAccount.accountAltName
                                tvAddAltName.text = if (updatedAccount.accountAltName.isEmpty()) "Add alternate name" else "Edit alternate name"
                                updateItem(updatedAccount) // ✅ Update the list in adapter
                                onAccountUpdated(updatedAccount) // ✅ Notify parent
                            },
                            onAccountDeleted = { deletedAccount ->
                                removeItem(deletedAccount) // ✅ Update UI if needed
                                onAccountDeleted(deletedAccount) // ✅ Notify parent
                            }
                        ).show((binding.root.context as AppCompatActivity).supportFragmentManager, "AltNameDialog")
                    }


                }
            }
        }
    }



    // ✅ Optimized filtering
    fun filter(query: String) {
        filteredList = if (query.isBlank()) {
            accountList.toMutableList()
        } else {
            accountList.filter {
                it.accountName.contains(query, ignoreCase = true) ||
                        it.accountAltName.contains(query, ignoreCase = true) ||
                        it.accountId.toString().contains(query)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
    // ✅ Optimized sorting
    fun sortList(sortType: String) {
        filteredList = when (sortType) {
            "ID" -> filteredList.sortedBy { it.accountId }
            "Name_AZ" -> filteredList.sortedBy { it.accountName }
            "Name_ZA" -> filteredList.sortedByDescending { it.accountName }
            "AltName" -> filteredList.sortedBy { it.accountAltName }
            else -> accountList
        }.toMutableList()

        notifyDataSetChanged()
    }

    fun resetListToInitial() {
        filteredList.clear()
        filteredList.addAll(accountList)  // Restore original data
        notifyDataSetChanged()
    }



    fun setupSwipeToDelete(recyclerView: RecyclerView , onAccountDeleted: (Account) -> Unit) {
        val itemTouchHelper = ItemTouchHelper(object : SwipeToDeleteCallback(recyclerView.context ,
            {pos ->

            }) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val account = accountList[position]

                AlertDialog.Builder(recyclerView.context)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Delete") { _, _ ->
                        removeItem(account)
                        onAccountDeleted(account)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
