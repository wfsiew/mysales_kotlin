package mysales.com

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_edit_doctor.*
import kotlinx.android.synthetic.main.content_edit_doctor.*
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
class EditDoctorActivity : AppCompatActivity() {

    private var db: WriteDBHelper? = null
    private var dbr: DBHelper? = null

    private var id: Int = 0
    private var submit = 0

    private var populateCustomerTask: PopulateCustomerTask? = null
    private var loadDoctorTask: LoadDoctorTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        id = intent.getIntExtra(ARG_DOCTOR_ID, 0)

        dbr = DBHelper(this)
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

        db = WriteDBHelper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDoctorTask?.cancel()
        populateCustomerTask?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_doctor, menu)
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
        if (isEmpty(txtname!!.text.toString())) {
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

        val i = spcust.selectedItemPosition
        val x = spcust.adapter as CustomerAdapter
        val customer = x.getItem(i)

        val o = Doctor()
        o.id = id
        o.name = getSqlStr(txtname.text.toString())
        o.phone = getSqlStr(txtphone.text.toString())
        o.hp = getSqlStr(txtmobile.text.toString())
        o.email = getSqlStr(txtemail.text.toString())
        o.custCode = customer.code
        o.custName = customer.name
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

        Needle.onBackgroundThread().execute(UpdateDoctorTask(o))
    }

    internal inner class LoadDoctorTask : CommonTask<Doctor>(this@EditDoctorActivity) {

        private val CLASS_NAME = "LoadDoctorTask"

        override fun doWork(): Doctor? {
            var o: Doctor?

            try {
                o = db!!.getDoctor(id)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
                o = null
            } finally {
                db!!.close()
            }

            return o
        }

        override fun thenDoUiRelatedWork(o: Doctor?) {
            if (o == null) {
                Toast.makeText(this@EditDoctorActivity, "Failed to load doctor deatils with id " + id, Toast.LENGTH_SHORT).show()
                return
            }

            txtname.setText(o.name)
            txtphone.setText(o.phone)
            txtmobile.setText(o.hp)
            txtemail.setText(o.email)
            txtasst1.setText(o.assistant1)
            txtasst2.setText(o.assistant2)
            txtasst3.setText(o.assistant3)

            val x = spcust.adapter as CustomerAdapter
            val i = x.getPosition(o.custCode, o.custName)
            spcust.setSelection(i)

            chkmon_morning.isChecked = o.isMonMor
            chkmon_afternoon.isChecked = o.isMonAft
            chktue_morning.isChecked = o.isTueMor
            chktue_afternoon.isChecked = o.isTueAft
            chkwed_morning.isChecked = o.isWedMor
            chkwed_afternoon.isChecked = o.isWedAft
            chkthu_morning.isChecked = o.isThuMor
            chkthu_afternoon.isChecked = o.isThuAft
            chkfri_morning.isChecked = o.isFriMor
            chkfri_afternoon.isChecked = o.isFriAft
            chksat_morning.isChecked = o.isSatMor
            chksat_afternoon.isChecked = o.isSatAft
            chksun_morning.isChecked = o.isSunMor
            chksun_afternoon.isChecked = o.isSunAft

            unlockScreenOrientation(this@EditDoctorActivity)
        }
    }

    internal inner class PopulateCustomerTask : CommonTask<ArrayList<Customer>>(this@EditDoctorActivity) {

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
            val adapter = CustomerAdapter(this@EditDoctorActivity, ls)
            spcust!!.adapter = adapter
            spcust!!.setSelection(selectedCustPosition)
            unlockScreenOrientation(this@EditDoctorActivity)

            loadDoctorTask = LoadDoctorTask()
            Needle.onBackgroundThread()
                    .withTaskType("loadDoctor")
                    .execute(loadDoctorTask!!)
        }
    }

    internal inner class UpdateDoctorTask(private val doctor: Doctor) : CommonTask<String>(this@EditDoctorActivity) {

        private val CLASS_NAME = "UpdateDoctorTask"

        init {
            showProgress(true, progresssubmit, this@EditDoctorActivity)
        }

        override fun doWork(): String? {
            var r: String?

            try {
                db!!.updateDoctor(doctor)
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
            showProgress(false, progresssubmit, this@EditDoctorActivity)

            try {
                if (s == null) {
                    Toast.makeText(this@EditDoctorActivity, R.string.update_doctor_fail, Toast.LENGTH_SHORT).show()
                    unlockScreenOrientation(this@EditDoctorActivity)
                    return
                }

                if ("success" == s) {
                    submit = SUBMITTED
                    Toast.makeText(this@EditDoctorActivity, R.string.update_doctor_ok, Toast.LENGTH_SHORT).show()
                    setResult(submit)
                    finish()
                } else {
                    Toast.makeText(this@EditDoctorActivity, R.string.update_doctor_fail, Toast.LENGTH_SHORT).show()
                }

                unlockScreenOrientation(this@EditDoctorActivity)
            } catch (e: Exception) {
                Log.e(CLASS_NAME, e.message, e)
            }
        }
    }

    companion object {
        private var selectedCustPosition = 0

        val ARG_DOCTOR_ID = "id"

        val SUBMITTED = 1
    }
}