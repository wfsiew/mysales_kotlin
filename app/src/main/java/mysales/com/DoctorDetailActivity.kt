package mysales.com

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_doctor_detail.*
import kotlinx.android.synthetic.main.content_doctor_detail.*
import mysales.com.helpers.WriteDBHelper
import mysales.com.helpers.isEmpty
import mysales.com.helpers.unlockScreenOrientation
import mysales.com.models.Doctor
import mysales.com.tasks.CommonTask
import needle.Needle

import android.Manifest.permission.CALL_PHONE

/**
 * Created by wingfei.siew on 8/15/2017.
 */
class DoctorDetailActivity : AppCompatActivity() {

    private var db: WriteDBHelper? = null

    private var id: Int = 0
    private var submit = 0
    private var callNo: String? = null

    private var loadDoctorTask: LoadDoctorTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_detail)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        btnedit.setOnClickListener {
            val i = Intent(this@DoctorDetailActivity, EditDoctorActivity::class.java)
            i.putExtra(EditDoctorActivity.ARG_DOCTOR_ID, id)
            startActivityForResult(i, EDITDOCTOR_REQUEST_CODE)
        }

        btndel.setOnClickListener {
            AlertDialog.Builder(this@DoctorDetailActivity)
                    .setTitle("Delete doctor")
                    .setMessage("Do you want to delete the selected doctor?")
                    .setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, i ->
                        Needle.onBackgroundThread()
                                .withTaskType("deletedoctor")
                                .execute(DeleteDoctorTask(id.toString()))
                    }
                    .setNegativeButton(resources.getString(R.string.cancel), null)
                    .create()
                    .show()
        }

        btnphone.setOnClickListener { call(txtphone!!.text.toString()) }

        btnmobile.setOnClickListener { call(txtmobile!!.text.toString()) }

        btnmobilesms.setOnClickListener { sms(txtmobile!!.text.toString()) }

        id = intent.getIntExtra(ARG_DOCTOR_ID, 0)

        db = WriteDBHelper(this)

        load()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDoctorTask?.cancel()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDITDOCTOR_REQUEST_CODE && resultCode == EditDoctorActivity.SUBMITTED) {
            submit = SUBMITTED
            setResult(submit)
            load()
        }
    }

    private fun load() {
        loadDoctorTask = LoadDoctorTask()
        Needle.onBackgroundThread()
                .withTaskType("loadDoctor")
                .execute(loadDoctorTask!!)
    }

    private fun sms(s: String) {
        callNo = s
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("sms:" + callNo!!)
        startActivity(i)
    }

    private fun call(s: String) {
        callNo = s
        checkCallPermission()
    }

    private fun checkCallPermission() {
        if (!mayRequestCallPhone()) {
            return
        }

        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:" + callNo!!)
        startActivity(i)
    }

    private fun mayRequestCallPhone(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (checkSelfPermission(CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
            Snackbar.make(txtname, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, { requestPermissions(arrayOf(CALL_PHONE), REQUEST_CALL_PHONE) })
        } else {
            requestPermissions(arrayOf(CALL_PHONE), REQUEST_CALL_PHONE)
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCallPermission()
            }
        }
    }

    internal inner class LoadDoctorTask : CommonTask<Doctor>(this@DoctorDetailActivity) {

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
                Toast.makeText(this@DoctorDetailActivity, "Failed to load doctor deatils with id " + id, Toast.LENGTH_SHORT).show()
                return
            }

            txtname.text = o.name
            txtcust.text = String.format("%s - %s", o.custCode, o.custName)
            txtphone.text = o.phone
            txtmobile.text = o.hp
            txtemail.text = o.email
            txtasst1.text = o.assistant1
            txtasst2.text = o.assistant2
            txtasst3.text = o.assistant3

            val v1 = if (isEmpty(o.phone)) View.GONE else View.VISIBLE
            txtphone.visibility = v1
            lbphone.visibility = v1
            vphone.visibility = v1
            lyphone.visibility = v1

            val v2 = if (isEmpty(o.hp)) View.GONE else View.VISIBLE
            txtmobile.visibility = v2
            lbmobile.visibility = v2
            vmobile.visibility = v2
            lymobile.visibility = v2

            val v3 = if (isEmpty(o.email)) View.GONE else View.VISIBLE
            txtemail.visibility = v3
            lbemail.visibility = v3
            vemail.visibility = v3

            val v4 = if (isEmpty(o.assistant1)) View.GONE else View.VISIBLE
            txtasst1.visibility = v4
            lbasst1.visibility = v4
            vasst1.visibility = v4

            val v5 = if (isEmpty(o.assistant2)) View.GONE else View.VISIBLE
            txtasst2.visibility = v5
            lbasst2.visibility = v5
            vasst2.visibility = v5

            val v6 = if (isEmpty(o.assistant3)) View.GONE else View.VISIBLE
            txtasst3.visibility = v6
            lbasst3.visibility = v6
            vasst3.visibility = v6

            txtday.text = o.days

            unlockScreenOrientation(this@DoctorDetailActivity)
        }
    }

    internal inner class DeleteDoctorTask(private val ids: String) : CommonTask<String>(this@DoctorDetailActivity) {

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
            if (s == null) {
                Toast.makeText(this@DoctorDetailActivity, "Doctor failed to be deleted, please retry", Toast.LENGTH_SHORT).show()
                return
            }

            if ("success" == s) {
                submit = SUBMITTED
                Toast.makeText(this@DoctorDetailActivity, "Doctor have been successfully deleted", Toast.LENGTH_SHORT).show()
                setResult(SUBMITTED)
                finish()
            } else {
                Toast.makeText(this@DoctorDetailActivity, "Doctor failed to be deleted, please retry", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private val REQUEST_CALL_PHONE = 0

        val ARG_CUST = "cust_code"
        val ARG_CUST_NAME = "cust_name"

        val ARG_DOCTOR_ID = "id"

        val SUBMITTED = 1
        private val EDITDOCTOR_REQUEST_CODE = 2
    }
}