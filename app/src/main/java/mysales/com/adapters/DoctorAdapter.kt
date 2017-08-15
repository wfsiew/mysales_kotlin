package mysales.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import mysales.com.R
import mysales.com.models.Doctor
import mysales.com.helpers.isEmpty as _isEmpty

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class DoctorAdapter(context: Context,
                    private val items: ArrayList<Doctor>,
                    private val btndel: Button) :
        ArrayAdapter<Doctor>(context, 0, items) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var showSelect: Boolean = false
    private var selected: HashMap<Int, Int>? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View = convertView!!
        v = inflater.inflate(R.layout.list_doctor, null)
        val txtname = v.findViewById<TextView>(R.id.txtname)
        val txtphone = v.findViewById<TextView>(R.id.txtphone)
        val txthp = v.findViewById<TextView>(R.id.txthp)
        val txtemail = v.findViewById<TextView>(R.id.txtemail)
        val lbphone = v.findViewById<TextView>(R.id.lbphone)
        val lbhp = v.findViewById<TextView>(R.id.lbhp)
        val lbemail = v.findViewById<TextView>(R.id.lbemail)
        val txtday = v.findViewById<TextView>(R.id.txtday)
        val txtcust = v.findViewById<TextView>(R.id.txtcust)
        val chk = v.findViewById<CheckBox>(R.id.chk)

        val o = getItem(position) as Doctor
        txtname.text = o.name
        txtphone.text = o.phone
        txthp.text = o.hp
        txtemail.text = o.email
        txtday.text = o.shortDays
        txtcust.text = String.format("%s - %s", o.custCode, o.custName)
        chk.visibility = if (showSelect) View.VISIBLE else View.GONE

        lbphone.visibility = if (_isEmpty(o.phone)) View.GONE else View.VISIBLE
        lbhp.visibility = if (_isEmpty(o.hp)) View.GONE else View.VISIBLE
        lbemail.visibility = if (_isEmpty(o.email)) View.GONE else View.VISIBLE
        txtphone.visibility = lbphone.visibility
        txthp.visibility = lbhp.visibility
        txtemail.visibility = lbemail.visibility
        txtday.visibility = if (_isEmpty(o.shortDays)) View.GONE else View.VISIBLE

        chk.tag = position
        chk.setOnClickListener { view ->
            val c = view as CheckBox
            val i = c.tag as Int
            doSelect(c, i)
        }

        return v
    }

    fun toggleSelect(): Boolean {
        showSelect = !showSelect
        return showSelect
    }

    val selectedIds: String?
        get() {
            if (selected == null)
                return null

            val sb = StringBuffer()
            for (i in selected!!.keys) {
                sb.append(i.toString() + ",")
            }

            val r = sb.substring(0, sb.length - 1)
            return r
        }

    val ids: String
        get() {
            val sb = StringBuffer()
            for (o in items) {
                sb.append(o.id.toString() + ",")
            }

            val r = sb.substring(0, sb.length - 1)
            return r
        }

    fun select(v: View?, position: Int) {
        if (v != null) {
            val chk = v.findViewById<CheckBox>(R.id.chk)
            chk.isChecked = !chk.isChecked
            doSelect(chk, position)
        }
    }

    private fun doSelect(chk: CheckBox, position: Int) {
        if (selected == null) {
            selected = HashMap<Int, Int>()
        }

        if (chk.isChecked) {
            selected!!.put(items[position].id, 1)
        } else {
            selected!!.remove(items[position].id)
        }

        btndel.isEnabled = !selected!!.isEmpty()
    }
}