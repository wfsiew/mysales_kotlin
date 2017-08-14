package mysales.com

import mysales.com.models.CustomerItem
import mysales.com.adapters.CustomerItemDetailAdapter
import mysales.com.models.CustomerAddress
import mysales.com.tasks.CommonTask
import needle.Needle
import mysales.com.helpers.DBHelper
import mysales.com.helpers.showProgress as _showProgress
import mysales.com.helpers.isEmpty
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import mysales.com.helpers.formatDouble
import mysales.com.helpers.unlockScreenOrientation
import mysales.com.models.Result


/**
 * Created by wfsiew on 8/14/17.
 */
class CustomerItemDetailActivity : AppCompatActivity() {

    private var progress: View? = null
    private var txtmain: TextView? = null
    private var listitem: ListView? = null

    private var db: DBHelper? = null

    private var customerItemDetailTask: CustomerItemDetailTask? = null

    private var cust: String? = null
    private var custName: String? = null
    private var period: String? = null
    private var year: String? = null
    private var sort = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_item_detail)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        cust = intent.getStringExtra(ARG_CUST)
        custName = intent.getStringExtra(ARG_CUST_NAME)
        period = intent.getStringExtra(ARG_PERIOD)
        year = intent.getStringExtra(ARG_YEAR)

        db = DBHelper(this)

        load()
    }

    override fun onDestroy() {
        super.onDestroy()
        customerItemDetailTask?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customer_item_detail, menu)
        val sort_item = menu.findItem(R.id.sort_item)
        val sort_salesunit = menu.findItem(R.id.sort_salesunit)
        val sort_salesvalue = menu.findItem(R.id.sort_salesvalue)
        val sort_bonusunit = menu.findItem(R.id.sort_bonusunit)

        sort_item.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                sort = "item_name"
                load()
                return false
            }
        })

        sort_salesunit.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                sort = "salesu desc"
                load()
                return false
            }
        })

        sort_salesvalue.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                sort = "salesv desc"
                load()
                return false
            }
        })

        sort_bonusunit.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                sort = "bonusu desc"
                load()
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun load() {
        customerItemDetailTask = CustomerItemDetailTask(cust!!, custName!!, period!!, year!!, sort)
        Needle.onBackgroundThread()
                .withTaskType("customerItemDetail")
                .execute(customerItemDetailTask!!)
    }

    private fun showProgress(show: Boolean) {
        _showProgress(show, progress!!, applicationContext)
        txtmain?.setVisibility(if (show) View.GONE else View.VISIBLE)
        listitem?.setVisibility(if (show) View.GONE else View.VISIBLE)
    }

    internal inner class CustomerItemDetailTask(private val cust: String, private val custName: String, private val period: String, private val year: String, private val sort: String) : CommonTask<HashMap<String, ArrayList<CustomerItem>>>(this@CustomerItemDetailActivity) {

        private val CLASS_NAME = "CustomerItemDetailTask"

        private val la: ArrayList<String>
        private val addr: CustomerAddress

        init {
            la = ArrayList()
            addr = CustomerAddress()
            showProgress(true)
        }

        override fun doWork(): HashMap<String, ArrayList<CustomerItem>> {
            var m: HashMap<String, ArrayList<CustomerItem>> = HashMap()

            try {
                m = db!!.getItemsByCustomer(cust, custName, period, year, sort, addr, la)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                db!!.close()
            }

            return m
        }

        private fun process(m: HashMap<String, ArrayList<CustomerItem>>): Result {
            val r = Result()
            val lk = ArrayList<CustomerItem>()
            r.list = lk

            if (la.isEmpty()) {
                return r
            }

            var salesunittotal = 0
            var salesvaluetotal = 0.0
            var bonustotal = 0

            for (key in la) {
                val l = m[key]
                val h = CustomerItem()
                h.setHeader(true)
                h.setHeader(key)
                lk.add(h)

                var salesunit = 0
                var salesvalue = 0.0
                var bonus = 0

                for (o in l!!) {
                    val i = CustomerItem()
                    i.item = o.item
                    i.unit = o.unit
                    i.bonus = o.bonus
                    i.value = o.value
                    lk.add(i)

                    salesunit += o.unit
                    salesvalue += o.value
                    bonus += o.bonus

                    salesunittotal += o.unit
                    salesvaluetotal += o.value
                    bonustotal += o.bonus
                }

                val f = CustomerItem()
                f.isFooter = true
                f.sumunit = salesunit
                f.sumbonus = bonus
                f.sumvalue = salesvalue
                lk.add(f)
            }

            r.list = lk
            r.totalSalesUnit = salesunittotal
            r.totalBonusUnit = bonustotal
            r.totalSalesValue = salesvaluetotal
            return r
        }

        override fun thenDoUiRelatedWork(m: HashMap<String, ArrayList<CustomerItem>>) {
            showProgress(false)
            val r = process(m)
            val lk = r.list

            val adapter = CustomerItemDetailAdapter(this@CustomerItemDetailActivity, lk!!)
            listitem?.setAdapter(adapter)

            val sb = StringBuffer()

            if (la.size > 0) {
                val lx = m[la[0]]
                if (lx!!.size > 0) {
                    val x = lx[0]
                    sb.append(x.code + " - " + x.name + "\n")

                    if (!isEmpty(addr.addr1))
                        sb.append(addr.addr1)

                    if (!isEmpty(addr.addr2)) {
                        if (sb.toString().trim { it <= ' ' }.endsWith(",")) {
                            sb.append(" " + addr.addr2!!)
                        } else {
                            sb.append(", " + addr.addr2!!)
                        }
                    }

                    if (!isEmpty(addr.addr3)) {
                        if (sb.toString().trim { it <= ' ' }.endsWith(",")) {
                            sb.append(" " + addr.addr3!!)
                        } else {
                            sb.append(", " + addr.addr3!!)
                        }
                    }

                    sb.append("\nTotal Sales Unit: " + r.totalSalesUnit + "\n")
                            .append("Total Bonus Unit: " + r.totalBonusUnit + "\n")
                            .append("Total Sales Value: " + formatDouble(r.totalSalesValue) + "\n")
                    txtmain!!.setText(sb.toString())
                }
            }

            unlockScreenOrientation(this@CustomerItemDetailActivity)
        }
    }

    companion object {

        val ARG_CUST = "cust_code"
        val ARG_CUST_NAME = "cust_name"
        val ARG_PERIOD = "period"
        val ARG_YEAR = "year"
    }
}