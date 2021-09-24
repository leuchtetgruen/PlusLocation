package de.leuchtetgruen.pluslocation.ui.activities

import android.arch.lifecycle.Observer
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.ui.viewmodels.EnterCodeViewModel
import kotlinx.android.synthetic.main.activity_enter_code.*

class EnterCodeActivity : AppCompatActivity() {

    private val viewModel by lazy { EnterCodeViewModel.create(this)}

    private var codeObserver: Observer<String> = Observer { txtCode.text = it }

    companion object {
        fun intentTo(context : Context) : Intent {

            return Intent(context, EnterCodeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_code)
        addObservers()
    }

    override fun onStart() {
        super.onStart()
        txtCode.setOnLongClickListener(View.OnLongClickListener {
            paste()
            return@OnLongClickListener true
        })
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun paste() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            viewModel.setEnteredCode(clipboard.primaryClip?.getItemAt(0)?.text.toString())
        }
    }

    private fun addObservers() {
        viewModel.displayedCode.observe(this, codeObserver)
        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.displayedCode.removeObserver(codeObserver)
    }

    // Button listeners
    fun addNumber(v : View) {
        if (v !is Button) return

        viewModel.enteredText(v.text)
    }

    fun removeChar(v : View) {
        viewModel.removeChar()
    }

    fun save(v : View) {
        viewModel.save()
        finish()
    }
}
