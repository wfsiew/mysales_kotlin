package mysales.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import mysales.com.helpers.*
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import android.widget.ArrayAdapter
import mysales.com.tasks.CommonTask
import android.content.pm.PackageManager
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Build
import android.annotation.TargetApi
import android.support.design.widget.Snackbar
import needle.Needle
import android.content.Intent
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 0

    private var db: DBHelper? = null

    private var populateCustomerTask: PopulateCustomerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        btnsubmit.isEnabled = false

        val periodlist = resources.getStringArray(R.array.period)
        val la = ArrayList<KeyPairBoolData>()

        for (i in periodlist.indices) {
            val h = KeyPairBoolData()
            val v = periodlist[i]
            h.id = i.toLong()
            h.name = v.toString()
            h.isSelected = false
            la.add(h)
        }

        spperiod.setLimit(-1, null)
        spperiod.setItems(la, -1) { }

        val yearlist = resources.getStringArray(R.array.year)
        val lb = ArrayList<KeyPairBoolData>()

        for (i in yearlist.indices) {
            val h = KeyPairBoolData()
            val v = yearlist[i]
            h.id = (i + 1).toLong()
            h.name = v.toString()
            h.isSelected = false

            lb.add(h)
        }

        spyear.setLimit(-1, null)
        spyear.setItems(lb, -1) { }
        spyear.setSelectedIds(arrayOf(1))

        btnsubmit.setOnClickListener { searchData() }

        db = DBHelper(this)
        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        populateCustomerTask?.cancel()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }

        super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_doctor) {
            val i = Intent(this, DoctorListActivity::class.java)
            startActivity(i)
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun searchData() {
        val period = getSelected(spperiod!!.selectedItems)
        val year = getSelected(spyear!!.selectedItems)
        val item = getSelected(spitem!!.selectedItems, true)

        val i = Intent(this, CustomerListActivity::class.java)
        i.putExtra(CustomerListActivity.ARG_CUST, txtcust!!.text.toString())
        i.putExtra(CustomerListActivity.ARG_ITEM, item)
        i.putExtra(CustomerListActivity.ARG_PERIOD, period)
        i.putExtra(CustomerListActivity.ARG_YEAR, year)

        startActivity(i)
    }

    private fun checkPermission() {
        if (!mayRequestReadExternalStorage()) {
            return
        }

        populateAutoComplete()
    }

    private fun populateAutoComplete() {
        populateCustomerTask = PopulateCustomerTask()
        Needle.onBackgroundThread()
                .withTaskType("populateCustomer")
                .execute(populateCustomerTask!!)
    }

    private fun mayRequestReadExternalStorage(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(txtcust!!, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, object : View.OnClickListener {
                        @TargetApi(Build.VERSION_CODES.M)
                        override fun onClick(v: View) {
                            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
                        }
                    })
        } else {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission()
            }
        }
    }

    private fun getSelected(li: List<KeyPairBoolData>, usequote: Boolean = false): String {
        val sb = StringBuffer()
        var r = ""

        if (li.isEmpty()) {
            return r
        }

        for (i in li.indices) {
            val v = escapeStr(li[i].name)
            if (usequote) {
                sb.append(String.format("'%s'", v))
            } else {
                sb.append(v)
            }

            if (i < li.size - 1) {
                sb.append(",")
            }
        }

        r = sb.toString()
        return r
    }

    internal inner class PopulateCustomerTask : CommonTask<HashMap<String, ArrayList<String>>>(this@MainActivity) {

        private val CLASS_NAME = "PopulateCustomerTask"

        override fun doWork(): HashMap<String, ArrayList<String>> {
            val m = HashMap<String, ArrayList<String>>()
            val ls: ArrayList<String>
            val li: ArrayList<String>

            try {
                db!!.openDataBase()
                ls = db!!.customers
                li = db!!.items
                m.put("customer", ls)
                m.put("item", li)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                db!!.close()
            }

            return m
        }

        override fun thenDoUiRelatedWork(m: HashMap<String, ArrayList<String>>) {
            val ls = m["customer"]
            val li = m["item"]
            val adapter = ArrayAdapter(this@MainActivity,
                    android.R.layout.simple_dropdown_item_1line, ls)

            val la = ArrayList<KeyPairBoolData>()

            for (i in 0..li!!.size - 1) {
                val h = KeyPairBoolData()
                val v = li[i]
                h.id = (i + 1).toLong()
                h.name = v
                h.isSelected = false
                la.add(h)
            }

            spitem.setLimit(-1, null)
            spitem.setItems(la, -1) { }

            btnsubmit.isEnabled = true
            txtcust.setAdapter(adapter)
            unlockScreenOrientation(this@MainActivity)
        }
    }
}
