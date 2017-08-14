package mysales.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import mysales.com.R
import mysales.com.helpers.formatDouble
import mysales.com.models.CustomerItem
import java.util.ArrayList

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class CustomerItemDetailAdapter(context: Context, items: ArrayList<CustomerItem>) : ArrayAdapter<CustomerItem>(context, 0, items) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View = convertView!!
        val o = getItem(position) as CustomerItem

        if (o.isHeader()) {
            v = inflater.inflate(R.layout.section_customer_item_detail_header, null)

            v.isClickable = false

            val header = v.findViewById<TextView>(R.id.section_header)
            header.text = o.getHeader()
        } else if (o.isFooter) {
            v = inflater.inflate(R.layout.section_customer_item_detail_footer, null)

            v.isClickable = false

            val footer = v.findViewById<TextView>(R.id.section_val)

            val `val` = String.format("%d (Sales Unit) %d (Bonus Unit) %s (Sales Value)", o.sumunit,
                    o.sumbonus, formatDouble(o.sumvalue))
            footer.text = `val`
        } else {
            v = inflater.inflate(R.layout.list_customer_item_detail, null)
            val txtitem = v.findViewById<TextView>(R.id.txtitem)
            val txtval = v.findViewById<TextView>(R.id.txtval)

            txtitem.setText(o.item)
            val `val` = String.format("%d (Sales Unit) %d (Bonus Unit) %s (Sales Value)", o.unit,
                    o.bonus, formatDouble(o.value))
            txtval.text = `val`
        }

        return v
    }
}