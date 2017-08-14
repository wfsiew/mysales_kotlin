package mysales.com.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import mysales.com.models.Customer
import mysales.com.models.CustomerAddress
import mysales.com.models.CustomerItem

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class DBHelper : SQLiteOpenHelper {

    var db_path: String? = null
    var db: SQLiteDatabase? = null

    constructor(context: Context) : super(context, "app.db", null, 1) {
        db_path = Environment.getExternalStorageDirectory().toString() + "/mysales/app.db"
    }

    fun openDataBase() {
        db = SQLiteDatabase.openDatabase(db_path, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized
    override fun close() {
        db?.close()
        super.close()
    }

    override fun onCreate(p0: SQLiteDatabase?) {

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun getCustomers() : ArrayList<String> {
        var ls = ArrayList<String>()
        var cur = db?.rawQuery("select distinct cust_name from sales order by cust_name", null)
        cur?.moveToFirst()

        while (cur?.isAfterLast == false) {
            ls.add(cur.getString(cur.getColumnIndex("cust_name")))
            cur.moveToNext()
        }

        return ls
    }

    fun getCustomers1() : ArrayList<Customer> {
        var ls = ArrayList<Customer>()
        var cur = db?.rawQuery("select distinct cust_code, cust_name from sales order by cust_name", null)
        cur?.moveToFirst()

        while (cur?.isAfterLast == false) {
            var o = Customer()
            o.code = cur.getString(cur.getColumnIndex("cust_code"))
            o.name = cur.getString(cur.getColumnIndex("cust_name"))
            ls.add(o)
            cur.moveToNext()
        }

        return ls
    }

    fun getItems() : ArrayList<String> {
        var ls = ArrayList<String>()
        var cur = db?.rawQuery("select distinct item_name from sales order by item_name", null)
        cur?.moveToFirst()

        while (cur?.isAfterLast == false) {
            ls.add(cur.getString(cur.getColumnIndex("item_name")))
            cur.moveToNext()
        }

        return ls
    }

    fun filterCustomer(name: String, item: String, period: String, year: String, sort: String): java.util.ArrayList<Customer> {
        var sort = sort
        val ls = ArrayList<Customer>()
        var and = false
        openDataBase()
        val sb = StringBuffer()

        if (isEmpty(sort))
            sort = "cust_name"

        if ("cust_name" == sort) {
            sb.append("select distinct cust_code, cust_name from sales")
        } else {
            sb.append("select cust_code, cust_name, sum(sales_unit) salesu, sum(sales_value) salesv,")
                    .append(" sum(bonus_unit) bonusu from sales")
        }

        if (!isEmpty(name) || !isEmpty(item) || !isEmpty(period) || !isEmpty(year)) {
            sb.append(" where")

            if (!isEmpty(name)) {
                sb.append(" cust_name like '%$name%'")
                and = true
            }

            if (!isEmpty(item)) {
                if (and) {
                    sb.append(" and item_name in ($item)")
                } else {
                    sb.append(" item_name in ($item)")
                    and = true
                }
            }

            if (!isEmpty(period)) {
                if (and) {
                    sb.append(" and period in ($period)")
                } else {
                    sb.append(" period in ($period)")
                    and = true
                }
            }

            if (!isEmpty(year)) {
                if (and) {
                    sb.append(" and year in ($year)")
                } else {
                    sb.append(" year in ($year)")
                }
            }
        }

        if ("cust_name" != sort) {
            sb.append(" group by cust_code, cust_name")
        }

        sb.append(" order by " + sort)
        //System.out.println("===========" + sb.toString());

        val q = sb.toString()
        val cur = db?.rawQuery(q, null)
        cur?.moveToFirst()

        while (cur?.isAfterLast == false) {
            val o = Customer()
            o.code = cur.getString(cur.getColumnIndex("cust_code"))
            o.name = cur.getString(cur.getColumnIndex("cust_name"))
            ls.add(o)
            cur.moveToNext()
        }

        return ls
    }

    fun getCustomerAddress(code: String, name: String): CustomerAddress {
        val o = CustomerAddress()
        val q = "select cust_addr1, cust_addr2, cust_addr3, postal_code, area, territory from sales" +
                " where cust_code = '" + code + "'  and cust_name = '" + name + "'"
        val cur = db?.rawQuery(q, null)
        cur?.moveToFirst()

        val addr1 = cur?.getString(cur.getColumnIndex("cust_addr1"))
        val addr2 = cur?.getString(cur.getColumnIndex("cust_addr2"))
        val addr3 = cur?.getString(cur.getColumnIndex("cust_addr3"))
        val postalcode = cur?.getString(cur.getColumnIndex("postal_code"))
        val area = cur?.getString(cur.getColumnIndex("area"))
        val territory = cur?.getString(cur.getColumnIndex("territory"))

        o.addr1 = addr1
        o.addr2 = addr2
        o.addr3 = addr3
        o.postalCode = postalcode
        o.area = area
        o.territory = territory

        return o
    }

    fun getItemsByCustomer(code: String, name: String, period: String, year: String,
                           sort: String,
                           addr: CustomerAddress,
                           ls: java.util.ArrayList<String>): HashMap<String, java.util.ArrayList<CustomerItem>> {
        var sort = sort
        val m = HashMap<String, ArrayList<CustomerItem>>()
        openDataBase()
        val address = getCustomerAddress(code, name)
        addr.set(address)
        val sb = StringBuffer()
        val sa = StringBuffer()

        if (isEmpty(sort))
            sort = "salesv desc"

        sb.append("select period, year, item_name, sum(sales_unit) salesu, sum(sales_value) salesv, sum(bonus_unit) bonusu from sales")
                .append(" where cust_code = '$code' and cust_name = '$name'")
        sa.append("select period, year, sum(sales_unit) salesu, sum(sales_value) salesv, sum(bonus_unit) bonusu from sales")
                .append(" where cust_code = '$code' and cust_name = '$name'")

        if (!isEmpty(period)) {
            sb.append(" and period in ($period)")
            sa.append(" and period in ($period)")
        }

        if (!isEmpty(year)) {
            sb.append(" and year in ($year)")
            sa.append(" and year in ($year)")
        }

        sb.append(" group by period, year, item_name")
                .append(" order by $sort, period, year")
        sa.append(" group by period, year")
                .append(" order by $sort, period, year")
        //System.out.println("===========" + sb.toString());

        var q = sb.toString()
        var cur = db?.rawQuery(q, null)
        cur?.moveToFirst()

        while (cur?.isAfterLast == false) {
            val month = cur.getInt(cur.getColumnIndex("period"))
            val y = cur.getInt(cur.getColumnIndex("year"))
            val item = cur.getString(cur.getColumnIndex("item_name"))
            val salesq = cur.getInt(cur.getColumnIndex("salesu"))
            val salesv = cur.getDouble(cur.getColumnIndex("salesv"))
            val bonus = cur.getInt(cur.getColumnIndex("bonusu"))

            val key = String.format("%d-%d", y, month)
            val x = CustomerItem()
            x.code = code
            x.name = name
            x.item = item
            x.unit = salesq
            x.value = salesv
            x.bonus = bonus

            if (m.containsKey(key)) {
                m[key]?.add(x)
            } else {
                val l = java.util.ArrayList<CustomerItem>()
                l.add(x)
                m.put(key, l)

                if ("item_name" == sort)
                    ls.add(key)
            }

            cur.moveToNext()
        }

        if ("item_name" != sort) {
            q = sa.toString()
            cur = db?.rawQuery(q, null)
            cur?.moveToFirst()

            while (cur?.isAfterLast == false) {
                val month = cur.getInt(cur.getColumnIndex("period"))
                val y = cur.getInt(cur.getColumnIndex("year"))

                val key = String.format("%d-%d", y, month)
                ls.add(key)

                cur.moveToNext()
            }
        }

        return m
    }
}