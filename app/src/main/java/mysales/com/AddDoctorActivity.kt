package mysales.com

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_add_doctor.*
import kotlinx.android.synthetic.main.content_add_doctor.*
import mysales.com.adapters.CustomerAdapter
import mysales.com.helpers.*
import mysales.com.models.Customer
import mysales.com.models.Doctor
import mysales.com.tasks.CommonTask
import needle.Needle
import java.util.ArrayList

/**
 * Created by wingfei.siew on 8/15/2017.
 */
class AddDoctorActivity : AppCompatActivity() {

    private var db: WriteDBHelper? = null
    private var dbr: DBHelper? = null

    private var cust: String? = null
    private var custName: String? = null
    private var submit = 0

    private var populateCustomerTask: PopulateCustomerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        cust = intent.getStringExtra(ARG_CUST)
        custName = intent.getStringExtra(ARG_CUST_NAME)

        if (isEmpty(cust) || isEmpty(custName)) {
            txttitle.visibility = View.GONE
            dbr = DBHelper(this)
            txtcust.visibility = View.VISIBLE
            spcust.visibility = View.VISIBLE
            spcust.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    selectedCustPosition = i
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {

                }
            }

            populateCustomerTask = PopulateCustomerTask()
            Needle.onBackgroundThread()
                    .withTaskType("populateCustomer")
                    .execute(populateCustomerTask!!)
        } else {
            txttitle.text = String.format("%s - %s", cust, custName)
        }

        db = WriteDBHelper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        populateCustomerTask?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_doctor, menu)
        val menu_save = menu.findItem(R.id.menu_save)

        menu_save.setOnMenuItemClickListener {
            submit()
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            setResult(submit)
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun validateSubmit(): ArrayList<String> {
        val ls = ArrayList<String>()
        if (isEmpty(txtname.text.toString())) {
            ls.add("Name is required")
        }

        return ls
    }

    private fun submit() {
        val ls = validateSubmit()
        val m = getMessages(ls)
        if (!isEmpty(m)) {
            AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.mandatory))
                    .setMessage(m)
                    .show()
            return
        }

        var ccode = cust
        var cname = custName

        if (isEmpty(ccode) || isEmpty(cname)) {
            val i = spcust.selectedItemPosition
            val x = spcust.adapter as CustomerAdapter
            val customer = x.getItem(i)
            ccode = customer.code
            cname = customer.name
        }

        val o = Doctor()
        o.name = getSqlStr(txtname.text.toString())
        o.phone = getSqlStr(txtphone.text.toString())
        o.hp = getSqlStr(txtmobile.text.toString())
        o.email = getSqlStr(txtemail.text.toString())
        o.custCode = ccode
        o.custName = cname
        o.assistant1 = getSqlStr(txtasst1.text.toString())
        o.assistant2 = getSqlStr(txtasst2.text.toString())
        o.assistant3 = getSqlStr(txtasst3.text.toString())
        o.isMonMor = chkmon_morning.isChecked
        o.isMonAft = chkmon_afternoon.isChecked
        o.isTueMor = chktue_morning.isChecked
        o.isTueAft = chktue_afternoon.isChecked
        o.isWedMor = chkwed_morning.isChecked
        o.isWedAft = chkwed_afternoon.isChecked
        o.isThuMor = chkthu_morning.isChecked
        o.isThuAft = chkthu_afternoon.isChecked
        o.isFriMor = chkfri_morning.isChecked
        o.isFriAft = chkfri_afternoon.isChecked
        o.isSatMor = chksat_morning.isChecked
        o.isSatAft = chksat_afternoon.isChecked
        o.isSunMor = chksun_morning.isChecked
        o.isSunAft = chksun_afternoon.isChecked

        Needle.onBackgroundThread().execute(AddDoctorTask(o))
    }

    private fun reset() {
        txtname.text = null
        txtphone.text = null
        txtmobile.text = null
        txtemail.text = null
        txtasst1.text = null
        txtasst2.text = null
        txtasst3.text = null
        chkmon_morning.isChecked = false
        chkmon_afternoon.isChecked = false
        chktue_morning.isChecked = false
        chktue_afternoon.isChecked = false
        chkwed_morning.isChecked = false
        chkwed_afternoon.isChecked = false
        chkthu_morning.isChecked = false
        chkthu_afternoon.isChecked = false
        chkfri_morning.isChecked = false
        chkfri_afternoon.isChecked = false
        chksat_morning.isChecked = false
        chksat_afternoon.isChecked = false
        chksun_morning.isChecked = false
        chksun_afternoon.isChecked = false
    }

    internal inner class PopulateCustomerTask : CommonTask<ArrayList<Customer>>(this@AddDoctorActivity) {

        private val CLASS_NAME = "PopulateCustomerTask"

        override fun doWork(): ArrayList<Customer> {
            var ls = ArrayList<Customer>()

            try {
                dbr!!.openDataBase()
                ls = dbr!!.customers1
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            } finally {
                dbr!!.close()
            }

            return ls
        }

        override fun thenDoUiRelatedWork(ls: ArrayList<Customer>) {
            val adapter = CustomerAdapter(this@AddDoctorActivity, ls)
            spcust.adapter = adapter
            spcust.setSelection(selectedCustPosition)
            unlockScreenOrientation(this@AddDoctorActivity)
        }
    }

    internal inner class AddDoctorTask(private val doctor: Doctor) : CommonTask<String>(this@AddDoctorActivity) {

        private val CLASS_NAME = "AddDoctorTask"

        init {
            showProgress(true, progresssubmit, this@AddDoctorActivity)
        }

        override fun doWork(): String? {
            var r: String?

            try {
                db!!.createDoctor(doctor)
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
            showProgress(false, progresssubmit, this@AddDoctorActivity)

            try {
                if (s == null) {
                    Toast.makeText(this@AddDoctorActivity, R.string.add_doctor_fail, Toast.LENGTH_SHORT).show()
                    unlockScreenOrientation(this@AddDoctorActivity)
                    return
                }

                if ("success" == s) {
                    reset()
                    submit = SUBMITTED
                    Toast.makeText(this@AddDoctorActivity, R.string.add_doctor_ok, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddDoctorActivity, R.string.add_doctor_fail, Toast.LENGTH_SHORT).show()
                }

                unlockScreenOrientation(this@AddDoctorActivity)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            }
        }
    }

    companion object {
        private var selectedCustPosition = 0

        val ARG_CUST = "cust_code"
        val ARG_CUST_NAME = "cust_name"

        val SUBMITTED = 1
    }
}