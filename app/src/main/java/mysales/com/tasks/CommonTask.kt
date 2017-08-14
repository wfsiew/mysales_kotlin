package mysales.com.tasks

import android.app.Activity
import mysales.com.helpers.lockScreenOrientation
import needle.UiRelatedTask



/**
 * Created by wfsiew on 8/14/17.
 */
abstract class CommonTask<T> : UiRelatedTask<T> {

    private var activity: Activity

    protected constructor(activity: Activity) {
        this.activity = activity
    }

    protected constructor(a: Activity, activity: Activity) {
        this.activity = a
        init()
        this.activity = activity
    }

    protected fun init() {
        lockScreenOrientation(activity)
    }
}