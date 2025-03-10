package com.bookxpert.app.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookxpert.app.R
import com.bookxpert.app.databinding.ActivityHomeBinding
import com.bookxpert.app.ui.adapters.AccountAdapter
import com.bookxpert.app.ui.fragments.PdfBottomSheet
import com.bookxpert.app.ui.fragments.dialogs.ImagePickerDialogFragment
import com.bookxpert.app.ui.viewmodel.CommonViewModel
import com.bookxpert.app.utils.Account
import com.bookxpert.app.utils.Status
import com.bookxpert.app.utils.ViewExtensionFunction.setLayoutEdgeBottom
import com.bookxpert.app.utils.ViewExtensionFunction.setLayoutEdgeTop
import com.bookxpert.app.utils.observeOnce
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val commonViewModel: CommonViewModel by viewModel { parametersOf(application) }
    private lateinit var accountAdapter: AccountAdapter
    private var accountList: List<Account> = emptyList()
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setLayoutEdgeTop(binding.vRefernce)
        setLayoutEdgeTop(binding.navigationView)
        setLayoutEdgeBottom(binding.recyclerView)
        overrideBackAction()
        fetchAccountsFromApi()
        binding.drawerLayout.closeDrawer(GravityCompat.START, false)
        binding.customToolbar.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_pdf -> {showPdf()}
                R.id.nav_image -> {showImageDialog()}
                R.id.nav_logout -> {finishAffinity()}
            }
            binding.drawerLayout.closeDrawers()
            true
        }
        setUpRecyclerView()
        setListeners()
        observeDatabase()

    }

    private fun overrideBackAction(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }


    private fun setListeners() {
        binding.customToolbar.btnSearch.setOnClickListener(this)
        binding.customToolbar.btnFilter.setOnClickListener(this)
        binding.customToolbar.btnCloseSearch.setOnClickListener(this)
    }

    private fun addTextWatcherToSearch(){
        binding.customToolbar.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) } // Cancel previous calls

                searchRunnable = Runnable {
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        accountAdapter.filter(query)  // Execute search only if query is not empty
                    } else {
                        accountAdapter.resetListToInitial() // Clear results when input is empty
                    }
                }

                searchHandler.postDelayed(searchRunnable!!, 300) // 300ms debounce
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun observeDatabase() {
        commonViewModel.getAllAccountsFromDb().observe(this) { localAccounts ->
            if (localAccounts.isNotEmpty()) {
                accountList = localAccounts
                accountAdapter.updateList(accountList)
            } else {
                commonViewModel.getAllAccounts() // Only fetch if DB is empty
            }
        }
    }

    private fun fetchAccountsFromApi() {
        commonViewModel.allAccountsResponse().observe(this) { resource ->
            when(resource.status){
                Status.LOADING -> {
                    binding.tvLoading.visibility = View.VISIBLE
                    Log.e("CHECKINGLOADINGCALL" , "CALLED")
                }
                Status.ERROR -> binding.tvLoading.visibility = View.GONE
                Status.SUCCESS -> {
                    binding.tvLoading.visibility = View.GONE
                    resource?.data?.let { apiAccounts ->
                        commonViewModel.getAllAccountsFromDb().observeOnce(this) { localAccounts ->
                            val localIds = localAccounts.map { it.accountId }.toSet()
                            val newAccounts = apiAccounts.filter { it.actid !in localIds }

                            if (newAccounts.isNotEmpty()) {
                                val accountEntities = newAccounts.map {
                                    Account(it.actid, it.ActName, "", "")
                                }
                                commonViewModel.insertAllAccounts(accountEntities) {
                                    loadAccountsFromDb()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadAccountsFromDb() {
        commonViewModel.getAllAccountsFromDb().observeOnce(this) { accounts ->
            accountList = accounts
            accountAdapter.updateList(accountList)
        }
    }

    private fun setUpRecyclerView() {
        accountAdapter = AccountAdapter(
            onAccountUpdated = { updatedAccount->
                commonViewModel.updateAccountDetails(updatedAccount.accountId , updatedAccount.accountAltName , updatedAccount.accountPicture)
                accountAdapter.notifyDataSetChanged()
            },
            onAccountDeleted = { deletedAccount ->
                commonViewModel.deleteAccountById(deletedAccount.accountId)
                accountAdapter.notifyDataSetChanged()
            }
        )
        accountAdapter.setupSwipeToDelete(binding.recyclerView) { deletedAccount ->
            commonViewModel.deleteAccountById(deletedAccount.accountId)
            accountAdapter.notifyDataSetChanged()
        }
        binding.recyclerView.apply {
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade)
            scheduleLayoutAnimation()
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = accountAdapter
            setHasFixedSize(true)  // 🚀 Performance boost
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSearch -> showSearchField()
            R.id.btnFilter -> showFilterDialog()
            R.id.btnCloseSearch -> closeSearchField()
        }
    }

    private fun showSearchField(){
        addTextWatcherToSearch()
        binding.customToolbar.etSearch.visibility = View.VISIBLE
        binding.customToolbar.btnCloseSearch.visibility = View.VISIBLE
        binding.customToolbar.btnSearch.visibility = View.GONE
        binding.customToolbar.btnFilter.visibility = View.GONE
        binding.customToolbar.txtTitle.visibility = View.GONE
        binding.customToolbar.btnMenu.visibility = View.GONE
    }

    private fun closeSearchField(){
        accountAdapter.resetListToInitial()
        binding.customToolbar.etSearch.visibility = View.GONE
        binding.customToolbar.btnCloseSearch.visibility = View.GONE
        binding.customToolbar.btnSearch.visibility = View.VISIBLE
        binding.customToolbar.btnFilter.visibility = View.VISIBLE
        binding.customToolbar.txtTitle.visibility = View.VISIBLE
        binding.customToolbar.btnMenu.visibility = View.VISIBLE
    }

    private fun showFilterDialog() {
        val options = arrayOf( "ID", "Name (A-Z)", "Name (Z-A)", "Alternate Name")

        AlertDialog.Builder(this)
            .setTitle("Sort by:")
            .setItems(options) { _, which ->
                val sortType = when (which) {
                    0 -> "ID"
                    1 -> "Name_AZ"
                    2 -> "Name_ZA"
                    3 -> "AltName"
                    else -> null
                }
                sortType?.let { accountAdapter.sortList(it) }
            }
            .show()
    }

    private fun showPdf(){
        val pdfUrl = "https://fssservices.bookxpert.co/GeneratedPDF/Companies/nadc/2024-2025/BalanceSheet.pdf"
        val pdfBottomSheet = PdfBottomSheet.newInstance(pdfUrl)
        pdfBottomSheet.show(supportFragmentManager, "PdfBottomSheet")

    }

    private fun showImageDialog() {
        val dialog = ImagePickerDialogFragment()
        dialog.show(supportFragmentManager, "ImagePickerDialog")
    }



}
