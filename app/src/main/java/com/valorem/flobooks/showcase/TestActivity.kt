package com.valorem.flobooks.showcase

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.valorem.flobooks.showcaselib.Showcase
import com.valorem.flobooks.showcaselib.ShowcaseTarget

class TestActivity : AppCompatActivity() {

    private val showcase by lazy { Showcase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        showcase.register(this)

        listOf(R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5)
            .map { findViewById<AppCompatButton>(it) }
            .onEach {
                it.setOnClickListener { view -> showShowcase(view) }
            }
    }

    private fun showShowcase(anchor: View) {
        showcase.add(
            ShowcaseTarget.Sync(
                id = anchor.id.toString(),
                style = ShowcaseTarget.Style(),
                target = anchor
            )
        )
    }
}