package mysales.com.adapters

import android.content.Context
import android.widget.ArrayAdapter
import mysales.com.models.Customer

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class CustomerAdapter(context: Context, private val items: ArrayList<Customer>) : ArrayAdapter<Customer>(context, android.R.layout.simple_spinner_item, items) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    fun getPosition(code: String, name: String): Int {
        var k = 0
        for (i in items.indices) {
            if (items[i].code == code && items[i].name == name) {
                k = i
                break
            }
        }

        return k
    }
}