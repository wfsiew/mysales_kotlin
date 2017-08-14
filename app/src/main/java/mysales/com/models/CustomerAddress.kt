package mysales.com.models

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class CustomerAddress {

    var addr1: String? = null
        set(addr1) {
            field = addr1
            if ("0" == this.addr1) {
                field = ""
            }
        }
    var addr2: String? = null
        set(addr2) {
            field = addr2
            if ("0" == this.addr2) {
                field = ""
            }
        }
    var addr3: String? = null
        set(addr3) {
            field = addr3
            if ("0" == this.addr3) {
                field = ""
            }
        }
    var postalCode: String? = null
        set(postalCode) {
            field = postalCode
            if ("0" == this.postalCode) {
                field = ""
            }
        }
    var area: String? = null
        set(area) {
            field = area
            if ("0" == this.area) {
                field = ""
            }
        }
    var territory: String? = null
        set(territory) {
            field = territory
            if ("0" == this.territory) {
                field = ""
            }
        }

    fun set(address: CustomerAddress) {
        addr1 = address.addr1
        addr2 = address.addr2
        addr3 = address.addr3
        postalCode = address.postalCode
        area = address.area
        territory = address.territory
    }
}