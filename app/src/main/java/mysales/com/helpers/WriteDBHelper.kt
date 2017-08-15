package mysales.com.helpers

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import mysales.com.models.Customer
import mysales.com.models.Doctor
import java.util.ArrayList

/**
 * Created by wingfei.siew on 8/15/2017.
 */
class WriteDBHelper(context: Context) : SQLiteOpenHelper(context, "data.db", null, 1) {

    private var db_path: String? = Environment.getExternalStorageDirectory().toString() + "/mysales/data.db"
    private var db: SQLiteDatabase? = null

    @Throws(SQLException::class)
    fun openDataBase(read: Boolean) {
        val r = if (read) SQLiteDatabase.OPEN_READONLY else SQLiteDatabase.OPEN_READWRITE
        db = SQLiteDatabase.openDatabase(db_path, null, r)
    }

    @Synchronized override fun close() {
        db?.close()
        super.close()
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

    }

    val customers: ArrayList<Customer>
        get() {
            var cur: Cursor? = null
            val ls = ArrayList<Customer>()

            try {
                openDataBase(true)
                cur = db?.rawQuery("select distinct cust_code, cust_name from doctor order by cust_name", null)
                cur?.moveToFirst()

                while (cur?.isAfterLast == false) {
                    val o = Customer()
                    o.code = cur.getString(cur.getColumnIndex("cust_code"))
                    o.name = cur.getString(cur.getColumnIndex("cust_name"))
                    ls.add(o)
                    cur.moveToNext()
                }
            }

            finally {
                cur?.close()
            }

            return ls
        }

    fun getDoctor(id: Int): Doctor {
        var cur: Cursor? = null
        val o = Doctor()

        try {
            openDataBase(true)
            val sb = StringBuffer()

            sb.append("select id, name, phone, hp, email, cust_code, cust_name, ")
                    .append("asst1, asst2, asst3, ")
                    .append("mon_mor, mon_aft, tue_mor, tue_aft, ")
                    .append("wed_mor, wed_aft, thu_mor, thu_aft, ")
                    .append("fri_mor, fri_aft, sat_mor, sat_aft, sun_mor, sun_aft ")
                    .append("from doctor ")
                    .append("where id = " + id)
            val q = sb.toString()
            cur = db!!.rawQuery(q, null)
            cur.moveToFirst()

            if (cur.isAfterLast)
                return o

            o.id = cur.getInt(cur.getColumnIndex("id"))
            o.name = cur.getString(cur.getColumnIndex("name"))
            o.phone = cur.getString(cur.getColumnIndex("phone"))
            o.hp = cur.getString(cur.getColumnIndex("hp"))
            o.email = cur.getString(cur.getColumnIndex("email"))
            o.custCode = cur.getString(cur.getColumnIndex("cust_code"))
            o.custName = cur.getString(cur.getColumnIndex("cust_name"))
            o.assistant1 = cur.getString(cur.getColumnIndex("asst1"))
            o.assistant2 = cur.getString(cur.getColumnIndex("asst2"))
            o.assistant3 = cur.getString(cur.getColumnIndex("asst3"))
            o.isMonMor = getBoolean(cur.getInt(cur.getColumnIndex("mon_mor")))
            o.isMonAft = getBoolean(cur.getInt(cur.getColumnIndex("mon_aft")))
            o.isTueMor = getBoolean(cur.getInt(cur.getColumnIndex("tue_mor")))
            o.isTueAft = getBoolean(cur.getInt(cur.getColumnIndex("tue_aft")))
            o.isWedMor = getBoolean(cur.getInt(cur.getColumnIndex("wed_mor")))
            o.isWedAft = getBoolean(cur.getInt(cur.getColumnIndex("wed_aft")))
            o.isThuMor = getBoolean(cur.getInt(cur.getColumnIndex("thu_mor")))
            o.isThuAft = getBoolean(cur.getInt(cur.getColumnIndex("thu_aft")))
            o.isFriMor = getBoolean(cur.getInt(cur.getColumnIndex("fri_mor")))
            o.isFriAft = getBoolean(cur.getInt(cur.getColumnIndex("fri_aft")))
            o.isSatMor = getBoolean(cur.getInt(cur.getColumnIndex("sat_mor")))
            o.isSatAft = getBoolean(cur.getInt(cur.getColumnIndex("sat_aft")))
            o.isSunMor = getBoolean(cur.getInt(cur.getColumnIndex("sun_mor")))
            o.isSunAft = getBoolean(cur.getInt(cur.getColumnIndex("sun_aft")))
        }

        finally {
            cur?.close()
        }

        return o
    }

    fun createDoctor(doctor: Doctor) {
        openDataBase(false)
        val sb = StringBuffer()

        sb.append("insert into doctor (name, phone, hp, email, cust_code, cust_name, ")
                .append("asst1, asst2, asst3, ")
                .append("mon_mor, mon_aft, tue_mor, tue_aft, ")
                .append("wed_mor, wed_aft, thu_mor, thu_aft, ")
                .append("fri_mor, fri_aft, sat_mor, sat_aft, sun_mor, sun_aft) ")
                .append("values(?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
        val q = sb.toString()
        val p = arrayOf<Any?>(doctor.name, doctor.phone, doctor.hp, doctor.email, doctor.custCode,
                doctor.custName, doctor.assistant1, doctor.assistant2, doctor.assistant3,
                getInt(doctor.isMonMor), getInt(doctor.isMonAft), getInt(doctor.isTueMor), getInt(doctor.isTueAft),
                getInt(doctor.isWedMor), getInt(doctor.isWedAft), getInt(doctor.isThuMor), getInt(doctor.isThuAft),
                getInt(doctor.isFriMor), getInt(doctor.isFriAft), getInt(doctor.isSatMor), getInt(doctor.isSatAft),
                getInt(doctor.isSunMor), getInt(doctor.isSunAft))
        db?.execSQL(q, p)
    }

    fun updateDoctor(doctor: Doctor) {
        openDataBase(false)
        val sb = StringBuffer()

        sb.append("update doctor set name = ?, phone = ?, hp = ?, email = ?, cust_code = ?, cust_name = ?, ")
                .append("asst1 = ?, asst2 = ?, asst3 = ?, ")
                .append("mon_mor = ?, mon_aft = ?, tue_mor = ?, tue_aft = ?, ")
                .append("wed_mor = ?, wed_aft = ?, thu_mor = ?, thu_aft = ?, ")
                .append("fri_mor = ?, fri_aft = ?, sat_mor = ?, sat_aft = ?, sun_mor = ?, sun_aft = ? ")
                .append("where id = ?")
        val q = sb.toString()
        val p = arrayOf<Any?>(doctor.name, doctor.phone, doctor.hp, doctor.email, doctor.custCode,
                doctor.custName, doctor.assistant1, doctor.assistant2, doctor.assistant3,
                getInt(doctor.isMonMor), getInt(doctor.isMonAft), getInt(doctor.isTueMor), getInt(doctor.isTueAft),
                getInt(doctor.isWedMor), getInt(doctor.isWedAft), getInt(doctor.isThuMor), getInt(doctor.isThuAft),
                getInt(doctor.isFriMor), getInt(doctor.isFriAft), getInt(doctor.isSatMor), getInt(doctor.isSatAft),
                getInt(doctor.isSunMor), getInt(doctor.isSunAft), doctor.id)
        db?.execSQL(q, p)
    }

    fun deletedoctors(s: String) {
        openDataBase(false)
        val q = "delete from doctor where id in ($s)"
        db?.execSQL(q)
    }

    fun filterDoctor(search: String?, day: String?, custCode: String?, custName: String?): ArrayList<Doctor> {
        var cur: Cursor? = null
        val ls = ArrayList<Doctor>()
        var where = false

        try {
            openDataBase(true)
            val sb = StringBuffer()

            sb.append("select id, name, phone, hp, email, cust_code, cust_name, ")
                    .append("asst1, asst2, asst3, ")
                    .append("mon_mor, mon_aft, tue_mor, tue_aft, ")
                    .append("wed_mor, wed_aft, thu_mor, thu_aft, ")
                    .append("fri_mor, fri_aft, sat_mor, sat_aft, sun_mor, sun_aft ")
                    .append("from doctor")

            if (!isEmpty(search)) {
                sb.append(" where (name like '%$search%' or")
                        .append(" phone like '%$search%' or")
                        .append(" hp like '%$search%' or")
                        .append(" email like '%$search%' or")
                        .append(" asst1 like '%$search%' or")
                        .append(" asst2 like '%$search%' or")
                        .append(" asst3 like '%$search%')")

                if (!isEmpty(day)) {
                    sb.append(" and " + getDay(day) + " = 1")
                }

                if (!isEmpty(custCode) && !isEmpty(custName)) {
                    sb.append(" and cust_code = '$custCode'")
                            .append(" and cust_name = '$custName'")
                }
            } else {
                if (!isEmpty(day)) {
                    where = true
                    sb.append(" where " + getDay(day) + " = 1")
                }

                if (!isEmpty(custCode) && !isEmpty(custName)) {
                    if (!where)
                        sb.append(" where ")

                    sb.append("cust_code = '$custCode'")
                            .append(" and cust_name = '$custName'")
                }
            }

            sb.append(" order by name")

            val q = sb.toString()
            //System.out.println("===========" + q);
            cur = db?.rawQuery(q, null)
            cur?.moveToFirst()

            while (cur?.isAfterLast == false) {
                val o = Doctor()
                o.id = cur.getInt(cur.getColumnIndex("id"))
                o.name = cur.getString(cur.getColumnIndex("name"))
                o.phone = cur.getString(cur.getColumnIndex("phone"))
                o.hp = cur.getString(cur.getColumnIndex("hp"))
                o.email = cur.getString(cur.getColumnIndex("email"))
                o.custCode = cur.getString(cur.getColumnIndex("cust_code"))
                o.custName = cur.getString(cur.getColumnIndex("cust_name"))
                o.assistant1 = cur.getString(cur.getColumnIndex("asst1"))
                o.assistant2 = cur.getString(cur.getColumnIndex("asst2"))
                o.assistant3 = cur.getString(cur.getColumnIndex("asst3"))
                o.isMonMor = getBoolean(cur.getInt(cur.getColumnIndex("mon_mor")))
                o.isMonAft = getBoolean(cur.getInt(cur.getColumnIndex("mon_aft")))
                o.isTueMor = getBoolean(cur.getInt(cur.getColumnIndex("tue_mor")))
                o.isTueAft = getBoolean(cur.getInt(cur.getColumnIndex("tue_aft")))
                o.isWedMor = getBoolean(cur.getInt(cur.getColumnIndex("wed_mor")))
                o.isWedAft = getBoolean(cur.getInt(cur.getColumnIndex("wed_aft")))
                o.isThuMor = getBoolean(cur.getInt(cur.getColumnIndex("thu_mor")))
                o.isThuAft = getBoolean(cur.getInt(cur.getColumnIndex("thu_aft")))
                o.isFriMor = getBoolean(cur.getInt(cur.getColumnIndex("fri_mor")))
                o.isFriAft = getBoolean(cur.getInt(cur.getColumnIndex("fri_aft")))
                o.isSatMor = getBoolean(cur.getInt(cur.getColumnIndex("sat_mor")))
                o.isSatAft = getBoolean(cur.getInt(cur.getColumnIndex("sat_aft")))
                o.isSunAft = getBoolean(cur.getInt(cur.getColumnIndex("sun_mor")))
                o.isSunAft = getBoolean(cur.getInt(cur.getColumnIndex("sun_aft")))
                ls.add(o)
                cur.moveToNext()
            }
        }

        finally {
            cur?.close()
        }

        return ls
    }

    private fun getDay(s: String?): String? {
        var a: String? = null

        if ("Mon Morning" == s) {
            a = "mon_mor"
        } else if ("Mon Afternoon" == s) {
            a = "mon_aft"
        } else if ("Tue Morning" == s) {
            a = "tue_mor"
        } else if ("Tue Afternoon" == s) {
            a = "tue_aft"
        } else if ("Wed Morning" == s) {
            a = "wed_mor"
        } else if ("Wed Afternoon" == s) {
            a = "wed_aft"
        } else if ("Thu Morning" == s) {
            a = "thu_mor"
        } else if ("Thu Afternoon" == s) {
            a = "thu_aft"
        } else if ("Fri Morning" == s) {
            a = "fri_mor"
        } else if ("Fri Afternoon" == s) {
            a = "fri_aft"
        } else if ("Sat Morning" == s) {
            a = "sat_mor"
        } else if ("Sat Afternoon" == s) {
            a = "sat_aft"
        } else if ("Sun Morning" == s) {
            a = "sun_mor"
        } else if ("Sun Afternoon" == s) {
            a = "sun_aft"
        }

        return a
    }
}