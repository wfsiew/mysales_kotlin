package mysales.com.adapters

import mysales.com.models.Customer
import android.support.v7.widget.RecyclerView
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import mysales.com.AddDoctorActivity
import mysales.com.CustomerItemDetailActivity
import mysales.com.R

/**
 * Created by wfsiew on 8/14/17.
 */
class CustomerItemRecyclerViewAdapter(private val values: Array<Customer>?,
                                      private val period: String?,
                                      private val year: String?) :
        RecyclerView.Adapter<CustomerItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (values == null)
            return

        holder.item = values[position]
        val customer = values[position]
        holder.txtcustname.text = customer.name
        holder.txtcustcode.text = customer.code

        holder.view.setOnClickListener { view ->
            val context = view.context
            val intent = Intent(context, CustomerItemDetailActivity::class.java)
            intent.putExtra(CustomerItemDetailActivity.ARG_CUST, customer.code)
            intent.putExtra(CustomerItemDetailActivity.ARG_CUST_NAME, customer.name)
            intent.putExtra(CustomerItemDetailActivity.ARG_PERIOD, period)
            intent.putExtra(CustomerItemDetailActivity.ARG_YEAR, year)

            context.startActivity(intent)
        }

        holder.view.setOnLongClickListener { view ->
            val context = view.context
            val intent = Intent(context, AddDoctorActivity::class.java)
            intent.putExtra(AddDoctorActivity.ARG_CUST, customer.code)
            intent.putExtra(AddDoctorActivity.ARG_CUST_NAME, customer.name)

            context.startActivity(intent)
            false
        }
    }

    override fun getItemCount(): Int {
        return values?.size ?: 0
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val txtcustname: TextView = view.findViewById<TextView>(R.id.txtcustname)
        val txtcustcode: TextView = view.findViewById<TextView>(R.id.txtcustcode)
        var item: Customer? = null
    }
}