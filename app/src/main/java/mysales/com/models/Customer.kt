package mysales.com.models

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class Customer {

    var name: String? = null
    var code: String? = null

    override fun toString(): String {
        if ("All" == code) return code!!
        return String.format("%s - %s", code, name)
    }
}