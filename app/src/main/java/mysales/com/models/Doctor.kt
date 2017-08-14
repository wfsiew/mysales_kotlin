package mysales.com.models

/**
 * Created by wingfei.siew on 8/14/2017.
 */
class Doctor {

    var id: Int = 0
    var name: String? = null
    var phone: String? = null
    var hp: String? = null
    var email: String? = null
    var assistant1: String? = null
    var assistant2: String? = null
    var assistant3: String? = null
    var custCode: String? = null
    var custName: String? = null
    var isMonMor: Boolean = false
    var isMonAft: Boolean = false
    var isTueMor: Boolean = false
    var isTueAft: Boolean = false
    var isWedMor: Boolean = false
    var isWedAft: Boolean = false
    var isThuMor: Boolean = false
    var isThuAft: Boolean = false
    var isFriMor: Boolean = false
    var isFriAft: Boolean = false
    var isSatMor: Boolean = false
    var isSatAft: Boolean = false
    var isSunMor: Boolean = false
    var isSunAft: Boolean = false

    val shortDays: String
        get() {
            var s = ""
            val sb = StringBuffer()
            if (isMonMor || isMonAft) {
                sb.append("Mon | ")
            }

            if (isTueMor || isTueAft) {
                sb.append("Tue | ")
            }

            if (isWedMor || isWedAft) {
                sb.append("Wed | ")
            }

            if (isThuMor || isThuAft) {
                sb.append("Thu | ")
            }

            if (isFriMor || isFriAft) {
                sb.append("Fri | ")
            }

            if (isSatMor || isSatAft) {
                sb.append("Sat | ")
            }

            if (isSunMor || isSunAft) {
                sb.append("Sun | ")
            }

            if (sb.length > 0) {
                s = sb.substring(0, sb.length - 3)
            }

            return s
        }

    val days: String
        get() {
            var s = ""
            val sb = StringBuffer()
            if (isMonMor) {
                sb.append("Mon Morning | ")
            }

            if (isMonAft) {
                sb.append("Mon Afternoon | ")
            }

            if (isTueMor) {
                sb.append("Tue Morning | ")
            }

            if (isTueAft) {
                sb.append("Tue Afternoon | ")
            }

            if (isWedMor) {
                sb.append("Wed Morning | ")
            }

            if (isWedAft) {
                sb.append("Wed Afternoon | ")
            }

            if (isThuMor) {
                sb.append("Thu Morning | ")
            }

            if (isThuAft) {
                sb.append("Thu Afternoon | ")
            }

            if (isFriMor) {
                sb.append("Fri Morning | ")
            }

            if (isFriAft) {
                sb.append("Fri Afternoon | ")
            }

            if (isSatMor) {
                sb.append("Sat Morning | ")
            }

            if (isSatAft) {
                sb.append("Sat Afternoon | ")
            }

            if (isSunMor) {
                sb.append("Sun Morning | ")
            }

            if (isSunAft) {
                sb.append("Sun Afternoon | ")
            }

            if (sb.length > 0) {
                s = sb.substring(0, sb.length - 3)
            }

            return s
        }
}