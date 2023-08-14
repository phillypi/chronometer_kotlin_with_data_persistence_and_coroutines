package br.com.melhortreino.chronometer.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chronometernoads.R
import com.chronometernoads.databinding.FragmentCronometerBinding
import com.chronometernoads.main.adapter.StringAdapter
import com.chronometernoads.main.fragment.TimerFragment
import kotlinx.coroutines.*

class ChronometerFragment : Fragment() {
    private lateinit var binding: FragmentCronometerBinding
    private lateinit var adapter: StringAdapter
    private var isTimerRunning = false
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var pausedTimeBackup: Long = 0L
    private val historyTimerItems = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCronometerBinding.inflate(inflater, container, false)
        changeStatusBarColor()
        return binding.root
    }

    private fun changeStatusBarColor() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        createAdapterLapFunction()
    }

    private fun clickListeners() {
        binding.chronometerPlayPauseButtom.setOnClickListener {

            animateClick(binding.chronometerPlayPauseButtom)

            vibrate()
            if (!isTimerRunning) {
                startRoutine()
            } else {
                pauseRoutine()
            }
        }
        binding.chronometerMainResetButtom.setOnClickListener {
            animateClick(binding.chronometerMainResetButtom)
            vibrate()
            clearAll()
        }

        binding.chronometerMainImgViewLapButtom.setOnClickListener {
            animateClick(binding.chronometerMainImgViewLapButtom)
            vibrate()
            lapClick()
        }

        binding.chronometerMainExitButtom.setOnClickListener{
            vibrate()
            requireActivity().finish()
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.chronometerToTimerButtom.setOnClickListener{
            animateClick(binding.chronometerToTimerButtom)
            openFragment(TimerFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        transaction.replace(R.id.time_counter_frame_layout, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun animateClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_card_view)
        view.startAnimation(animation)
    }

    private fun lapClick() {
        if(!isTimerRunning){
            Toast.makeText(requireContext(), "Aperte play!", Toast.LENGTH_SHORT).show()
            return
        }

        historyTimerItems.add(formatMinutes()+":"+formatSeconds()+":"+formatMillis())
        adapter.notifyItemInserted(historyTimerItems.size)
        binding.fragmentChronometerRecyclerView.smoothScrollToPosition(historyTimerItems.size)
        restartTimer()
        startTimer()
    }

    private fun createAdapterLapFunction() {
        adapter = StringAdapter(historyTimerItems)
        binding.fragmentChronometerRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.fragmentChronometerRecyclerView.adapter = adapter
    }

    private fun pauseRoutine() {
        isTimerRunning = false
        pauseTimer()
        pausedTimeBackup = elapsedTime
        binding.chronometerPlayPauseButtom.setImageResource(R.drawable.ic_chronometer_main_play)
    }

    private fun startRoutine() {
        isTimerRunning = true

        if (pausedTimeBackup == 0L) {
            startTimer()
        } else {
            startTimer()
        }
        binding.chronometerPlayPauseButtom.setImageResource(R.drawable.ic_chronometer_main_pause)
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    private val runnable = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime
            updateTimerText()
            scope.launch {
                delay(65)
                run()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAll() {
        binding.chronometerPlayPauseButtom.setImageResource(R.drawable.ic_chronometer_main_play)
        scope.coroutineContext.cancelChildren()
        elapsedTime = 0
        pausedTimeBackup = 0
        isTimerRunning = false
        updateTimerText()
        historyTimerItems.clear()
        adapter.notifyDataSetChanged()
    }

    private fun restartTimer(){
        elapsedTime = 0
        pausedTimeBackup = 0
        pauseTimer()
        startTimer()
    }

    private fun pauseTimer() {
        scope.coroutineContext.cancelChildren()
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis() - pausedTimeBackup
        scope.launch {
            runnable.run()
        }
    }

    private fun updateTimerText() {
        binding.chronometerMainTextViewSeconds.text = formatSeconds()
        binding.chronometerMainTextViewMillis.text = formatMillis()
        binding.chronometerMainTextViewMinutes.text =  formatMinutes()
    }

    private fun formatMinutes(): String {
        val minutes = (elapsedTime / 1000) / 60
        return String.format("%02d", minutes)
    }

    private fun formatSeconds(): String {
        val seconds = (elapsedTime / 1000) % 60
        return String.format("%02d", seconds)
    }

    private fun formatMillis(): String {
        val milliseconds = elapsedTime % 1000
        return String.format("%02d", milliseconds / 10)
    }

    private fun vibrate(){
        val timeVibrationMillis = 100L
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(timeVibrationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(timeVibrationMillis)
        }
    }
}