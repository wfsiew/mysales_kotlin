package mysales.com.models

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class CustomerItem {

    var code: String? = null
    var name: String? = null
    var item: String? = null
    var unit: Int = 0
    var value: Double = 0.toDouble()
    var bonus: Int = 0

    private var header: String? = null
    private var isHeader: Boolean = false
    var isFooter: Boolean = false

    var sumunit: Int = 0
    var sumbonus: Int = 0
    var sumvalue: Double = 0.toDouble()

    fun isHeader(): Boolean {
        return isHeader
    }

    fun setHeader(header: Boolean) {
        isHeader = header
    }

    fun getHeader(): String {
        return header!!
    }

    fun setHeader(header: String) {
        this.header = header
    }
}