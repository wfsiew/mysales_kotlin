package mysales.com

import mysales.com.models.Customer
import mysales.com.adapters.CustomerItemRecyclerViewAdapter
import mysales.com.tasks.CommonTask
import needle.Needle
import mysales.com.helpers.DBHelper
import mysales.com.helpers.showProgress
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.View
import kotlinx.android.synthetic.main.activity_customer_list.*
import kotlinx.android.synthetic.main.content_customer_list.*
import mysales.com.helpers.unlockScreenOrientation

/**
 * Created by wfsiew on 8/14/17.
 */
class CustomerListActivity : AppCompatActivity() {

    private val CLASS_NAME = "CustomerListTask"

    private var db: DBHelper? = null

    private var customerListTask: CustomerListTask? = null

    private var cust: String? = null
    private var item: String? = null
    private var period: String? = null
    private var year: String? = null
    private var sort = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        cust = intent.getStringExtra(ARG_CUST)
        item = intent.getStringExtra(ARG_ITEM)
        period = intent.getStringExtra(ARG_PERIOD)
        year = intent.getStringExtra(ARG_YEAR)

        listcust.adapter = CustomerItemRecyclerViewAdapter(null, period, year)

        db = DBHelper(this)

        load()
    }

    override fun onDestroy() {
        super.onDestroy()
        customerListTask?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customer_list, menu)
        val sort_custcode = menu.findItem(R.id.sort_custcode)
        val sort_custname = menu.findItem(R.id.sort_custname)
        val sort_salesunit = menu.findItem(R.id.sort_salesunit)
        val sort_salesvalue = menu.findItem(R.id.sort_salesvalue)
        val sort_bonusunit = menu.findItem(R.id.sort_bonusunit)

        sort_custcode.setOnMenuItemClickListener {
            sort = "cust_code"
            load()
            false
        }

        sort_custname.setOnMenuItemClickListener {
            sort = "cust_name"
            load()
            false
        }

        sort_salesunit.setOnMenuItemClickListener {
            sort = "salesu desc"
            load()
            false
        }

        sort_salesvalue.setOnMenuItemClickListener {
            sort = "salesv desc"
            load()
            false
        }

        sort_bonusunit.setOnMenuItemClickListener {
            sort = "bonusu desc"
            load()
            false
        }

        return true
    }

    private fun load() {
        customerListTask = CustomerListTask(cust, item, period, year, sort)
        Needle.onBackgroundThread()
                .withTaskType("customerList")
                .execute(customerListTask!!)
    }

    private fun showProgress(show: Boolean) {
        showProgress(show, progress, applicationContext)
        listcust.visibility = View.GONE
        empty.visibility = View.GONE
    }

    internal inner class CustomerListTask(private val cust: String?,
                                          private val item: String?,
                                          private val period: String?,
                                          private val year: String?,
                                          private val sort: String?) :
            CommonTask<ArrayList<Customer>>(this@CustomerListActivity) {

        init {
            showProgress(true)
        }

        override fun doWork(): ArrayList<Customer> {
            var ls: ArrayList<Customer> = ArrayList()

            try {
                ls = db!!.filterCustomer(cust, item, period, year, sort)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                db!!.close()
            }

            return ls
        }

        override fun thenDoUiRelatedWork(ls: ArrayList<Customer>) {
            showProgress(false)
            val adapter = CustomerItemRecyclerViewAdapter(ls.toArray(arrayOfNulls<Customer>(0)), period, year)
            listcust.adapter = adapter

            listcust.visibility = if (adapter.itemCount > 0) View.VISIBLE else View.GONE
            empty.visibility = if (adapter.itemCount > 0) View.GONE else View.VISIBLE
            unlockScreenOrientation(this@CustomerListActivity)
        }
    }

    companion object {

        val ARG_CUST = "cust_name"
        val ARG_ITEM = "item_name"
        val ARG_PERIOD = "period"
        val ARG_YEAR = "year"
    }
}