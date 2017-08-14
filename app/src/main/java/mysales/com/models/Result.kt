package mysales.com.models

import java.util.ArrayList

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class Result {

    var list: ArrayList<CustomerItem>? = null
    var totalSalesUnit: Int = 0
    var totalBonusUnit: Int = 0
    var totalSalesValue: Double = 0.toDouble()
}