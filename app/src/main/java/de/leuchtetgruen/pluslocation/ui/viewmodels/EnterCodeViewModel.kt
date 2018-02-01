package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.persistence.SavedCode
import de.leuchtetgruen.pluslocation.ui.activities.EnterCodeActivity

class EnterCodeViewModel(app: Application?) : AndroidViewModel(app!!), LifecycleObserver {

    companion object {
        fun create(activity: EnterCodeActivity): EnterCodeViewModel {
            return ViewModelProviders.of(activity).get(EnterCodeViewModel::class.java)
        }
    }

    private var enteredCode: String = ""

    val displayedCode: MutableLiveData<String> = MutableLiveData()

    private fun updateDisplayCode() {
        val complete = "........+.."

        if (enteredCode.length < 11) {
            displayedCode.value = enteredCode + complete.substring(enteredCode.length, complete.length)
        } else {
            displayedCode.value = enteredCode
        }
    }

    // Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        enteredCode = SavedCode.savedCode(getApplication())
        updateDisplayCode()
    }


    // Button listener
    fun enteredText(text: CharSequence) {
        if (text.equals("+")) {
            if (enteredCode.indexOf("+") > 0) {
                return
            }

            if (enteredCode.length != 8) {
                return
            }
        }

        if ((enteredCode.length == 8) && (!text.equals("+"))) {
            return
        }


        enteredCode = enteredCode + text
        updateDisplayCode()
    }

    fun removeChar() {
        if (enteredCode.length == 0) {
            return
        } else {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
        }


        updateDisplayCode()
    }


    fun save() {
        if (!OpenLocationCode.isValidCode(enteredCode)) {
            //TODO show alert
            return
        }



        SavedCode.changedCode(enteredCode, "Manually entered destination", getApplication())
    }
}