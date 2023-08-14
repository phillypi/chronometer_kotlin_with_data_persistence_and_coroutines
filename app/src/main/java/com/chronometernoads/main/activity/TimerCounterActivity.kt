package com.chronometernoads.main.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import br.com.melhortreino.chronometer.ui.fragment.ChronometerFragment
import com.chronometernoads.R
import com.chronometernoads.databinding.ActivityTimeCounterBinding

class TimerCounterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeCounterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val chronometerFragment = ChronometerFragment()
            openFragment(chronometerFragment)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.timeCounterFrameLayout.id, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
}