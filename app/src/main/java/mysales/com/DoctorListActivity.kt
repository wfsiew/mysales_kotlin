package mysales.com

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_doctor_list.*
import kotlinx.android.synthetic.main.app_bar_doctor_list.*
import kotlinx.android.synthetic.main.content_doctor_list.*
import mysales.com.adapters.CustomerAdapter
import mysales.com.adapters.DoctorAdapter
import mysales.com.helpers.WriteDBHelper
import mysales.com.helpers.isEmpty
import mysales.com.helpers.showProgress
import mysales.com.helpers.unlockScreenOrientation
import mysales.com.models.Customer
import mysales.com.models.Doctor
import mysales.com.tasks.CommonTask
import needle.Needle
import java.util.ArrayList

/**
 * Created by wingfei.siew on 8/15/2017.
 */
class DoctorListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var searchView: SearchView? = null
    private var pd: ProgressDialog? = null
    private var dlg: Dialog? = null
    private var showSelect: Boolean = false
    private var query: String? = null
    private var custCode: String? = null
    private var custName: String? = null
    private var day: String? = null

    private var db: WriteDBHelper? = null

    private var populateCustomerTask: PopulateCustomerTask? = null
    private var doctorListTask: DoctorListTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_list)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val i = Intent(this@DoctorListActivity, AddDoctorActivity::class.java)
            startActivityForResult(i, ADDDOCTOR_REQUEST_CODE)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        listdoctor.emptyView = empty
        listdoctor.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, _, _ ->
            toggleSelection()
            val x = listdoctor.adapter as DoctorAdapter
            val s = x.selectedIds
            if (s == null)
                btndel.isEnabled = false

            true
        }

        listdoctor.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            if (showSelect) {
                val x = listdoctor.adapter as DoctorAdapter
                x.select(view, position)
            } else {
                val o = listdoctor.adapter.getItem(position) as Doctor
                val i = Intent(this@DoctorListActivity, DoctorDetailActivity::class.java)
                i.putExtra(EditDoctorActivity.ARG_DOCTOR_ID, o.id)
                startActivityForResult(i, EDITDOCTOR_REQUEST_CODE)
            }
        }

        btndel.setOnClickListener {
            val x = listdoctor.adapter as DoctorAdapter
            val s = x.selectedIds ?: return@setOnClickListener

            pd = ProgressDialog.show(this@DoctorListActivity, "",
                    resources.getString(R.string.delete_wait))
            Needle.onBackgroundThread()
                    .withTaskType("deletedoctor")
                    .execute(DeleteDoctorTask(s))
        }

        btndelall.setOnClickListener {
            val x = listdoctor.adapter as DoctorAdapter
            val s = x.ids ?: return@setOnClickListener

            pd = ProgressDialog.show(this@DoctorListActivity, "",
                    resources.getString(R.string.delete_wait))
            Needle.onBackgroundThread()
                    .withTaskType("deletedoctor")
                    .execute(DeleteDoctorTask(s))
        }

        db = WriteDBHelper(this)

        dlg = Dialog(this)
        dlg!!.setContentView(R.layout.dialog_days)
        dlg!!.setTitle("Please Select")

        val spcust = dlg!!.findViewById<Spinner>(R.id.spcust)
        val spday = dlg!!.findViewById<Spinner>(R.id.spday)
        val btnok = dlg!!.findViewById<Button>(R.id.btnok)
        val btncancel = dlg!!.findViewById<Button>(R.id.btncancel)

        spcust.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val x = adapterView.adapter as CustomerAdapter
                val o = x.getItem(i)
                custCode = o.code
                custName = o.name

                if ("All" == custCode) {
                    custCode = null
                    custName = null
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        spday.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val a = resources.getStringArray(R.array.days)
                day = a[i]
                if ("All" == day) {
                    day = null
                }

                //dlg.dismiss();
                //load(query, x);
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        btnok.setOnClickListener {
            dlg!!.dismiss()
            load()
        }

        btncancel.setOnClickListener { dlg!!.dismiss() }

        init()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (!searchView!!.isIconified) {
            searchView!!.isIconified = true
        } else if (showSelect) {
            toggleSelection()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_main) {
            super.onBackPressed()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        populateCustomerTask?.cancel()
        doctorListTask?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_doctor_list, menu)
        searchView = MenuItemCompat.getActionView(menu.findItem(R.id.menu_search)) as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                this@DoctorListActivity.query = query
                load()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (isEmpty(newText)) {
                    this@DoctorListActivity.query = null
                    load()
                }

                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_add_doctor) {
            val i = Intent(this, AddDoctorActivity::class.java)
            startActivityForResult(i, ADDDOCTOR_REQUEST_CODE)
            return true
        } else if (id == R.id.menu_reload) {
            load()
            return false
        } else if (id == R.id.menu_day) {
            dlg!!.show()
            return false
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDDOCTOR_REQUEST_CODE && resultCode == AddDoctorActivity.SUBMITTED) {
            init()
        } else if (requestCode == EDITDOCTOR_REQUEST_CODE && resultCode == EditDoctorActivity.SUBMITTED) {
            init()
        }
    }

    private fun init() {
        populateCustomerTask = PopulateCustomerTask()
        Needle.onBackgroundThread()
                .withTaskType("populateCustomer")
                .execute(populateCustomerTask!!)
    }

    private fun load() {
        doctorListTask = DoctorListTask()
        Needle.onBackgroundThread()
                .withTaskType("doctorList")
                .execute(doctorListTask!!)
    }

    private fun toggleSelection() {
        val x = listdoctor.adapter as DoctorAdapter
        showSelect = x.toggleSelect()
        lybottom.visibility = if (showSelect) View.VISIBLE else View.GONE
        fab.visibility = if (showSelect) View.GONE else View.VISIBLE
        x.notifyDataSetChanged()
    }

    private fun showProgress(show: Boolean) {
        showProgress(show, progress, applicationContext)
        listdoctor.visibility = if (show) View.GONE else View.VISIBLE
        empty.visibility = if (show) View.GONE else View.VISIBLE
    }

    internal inner class DoctorListTask : CommonTask<ArrayList<Doctor>>(this@DoctorListActivity) {

        private val CLASS_NAME = "DoctorListTask"

        init {
            showProgress(true)
        }

        override fun doWork(): ArrayList<Doctor> {
            var ls = ArrayList<Doctor>()

            try {
                ls = db!!.filterDoctor(query, day, custCode, custName)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                db!!.close()
            }

            return ls
        }

        override fun thenDoUiRelatedWork(ls: ArrayList<Doctor>) {
            showProgress(false)
            val adapter = DoctorAdapter(this@DoctorListActivity, ls, btndel)
            listdoctor.adapter = adapter

            if (ls.isEmpty()) {
                showSelect = false
                lybottom.visibility = if (showSelect) View.VISIBLE else View.GONE
                fab.visibility = if (showSelect) View.GONE else View.VISIBLE
                adapter.notifyDataSetChanged()
            }

            lybottom.visibility = if (showSelect) View.VISIBLE else View.GONE
            unlockScreenOrientation(this@DoctorListActivity)
        }
    }

    internal inner class PopulateCustomerTask : CommonTask<ArrayList<Customer>>(this@DoctorListActivity) {

        private val CLASS_NAME = "PopulateCustomerTask"

        override fun doWork(): ArrayList<Customer> {
            var ls = ArrayList<Customer>()

            try {
                ls = db!!.customers
                val o = Customer()
                o.code = "All"
                o.name = "All"
                ls.add(0, o)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                db!!.close()
            }

            return ls
        }

        override fun thenDoUiRelatedWork(ls: ArrayList<Customer>) {
            val adapter = CustomerAdapter(this@DoctorListActivity, ls)
            val spcust = dlg!!.findViewById<Spinner>(R.id.spcust)
            spcust.adapter = adapter
            unlockScreenOrientation(this@DoctorListActivity)
            load()
        }
    }

    internal inner class DeleteDoctorTask(private val ids: String) : CommonTask<String>(this@DoctorListActivity) {

        private val CLASS_NAME = "DeleteDoctorTask"

        override fun doWork(): String? {
            var r: String?

            try {
                db!!.deletedoctors(ids)
                r = "success"
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
                r = null
            } finally {
                db!!.close()
            }

            return r
        }

        override fun thenDoUiRelatedWork(s: String?) {
            pd!!.dismiss()
            if (s == null) {
                Toast.makeText(this@DoctorListActivity, "Doctor(s) failed to be deleted, please retry", Toast.LENGTH_SHORT).show()
                return
            }

            if ("success" == s) {
                Toast.makeText(this@DoctorListActivity, "Doctor(s) have been successfully deleted", Toast.LENGTH_SHORT).show()
                init()
            } else {
                Toast.makeText(this@DoctorListActivity, "Doctor(s) failed to be deleted, please retry", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private val ADDDOCTOR_REQUEST_CODE = 1
        private val EDITDOCTOR_REQUEST_CODE = 2
    }
}