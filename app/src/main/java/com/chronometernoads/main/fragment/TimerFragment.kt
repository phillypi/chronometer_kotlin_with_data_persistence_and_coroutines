package com.chronometernoads.main.fragment
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.com.melhortreino.chronometer.ui.fragment.ChronometerFragment
import com.chronometernoads.R
import com.chronometernoads.databinding.FragmentTimerBinding


class TimerFragment : Fragment() {
    private lateinit var binding: FragmentTimerBinding
    private var isTimerRunning = false
    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0
    private var beepIsOn: Boolean = true
    private var countDownTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        changeStatusBarColor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //keep the order
        configNumberPickers()
        readBackupTimer()
        readBackupBipSong()
        clickListeners()
    }

    private fun saveBackupBeepSong() {
        val sharedPrefs = context?.getSharedPreferences("beep", Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        editor?.putBoolean("isOn", beepIsOn)
        editor?.apply()
    }

    private fun readBackupBipSong() {
        beepIsOn = context?.getSharedPreferences(
            "beep",
            Context.MODE_PRIVATE
        )?.getBoolean("isOn", true)!!

        setIconToSoundButton()
    }

    private fun setIconToSoundButton() {
        if (beepIsOn) {
            binding.chronometerMainImgViewSoundButtom
                .setImageResource(R.drawable.ic_volum_on)
        } else {
            binding.chronometerMainImgViewSoundButtom
                .setImageResource(R.drawable.ic_volum_off)
        }
    }

    private fun saveBackupTimer() {
        val sharedPrefs = context?.getSharedPreferences("timerBackup", Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        editor?.putInt("hours", hours)
        editor?.putInt("minutes", minutes)
        editor?.putInt("seconds", seconds)
        editor?.apply()
    }

    private fun readBackupTimer() {
        hours = context?.getSharedPreferences(
            "timerBackup",
            Context.MODE_PRIVATE
        )?.getInt("hours", 0)!!

        minutes = context?.getSharedPreferences(
            "timerBackup",
            Context.MODE_PRIVATE
        )?.getInt("minutes", 0)!!

        seconds = context?.getSharedPreferences(
            "timerBackup",
            Context.MODE_PRIVATE
        )?.getInt("seconds", 0)!!

        updateNumberPicker()
        updateTimerText()
    }

    private fun updateNumberPicker() {
        binding.timerNumberpickerHour.value = hours
        binding.timerNumberpickerMinute.value = minutes
        binding.timerNumberpickerSecond.value = seconds
    }

    private fun configNumberPickers() {
        binding.timerNumberpickerHour.minValue = 0
        binding.timerNumberpickerHour.maxValue = 12
        binding.timerNumberpickerHour.value = 0
        binding.timerNumberpickerHour.setFormatter { value ->
            String.format("%02d", value)
        }

        binding.timerNumberpickerHour.setOnValueChangedListener { _, _, newVal ->
            hours = newVal
            pickerChangeValueAction()
        }

        binding.timerNumberpickerMinute.minValue = 0
        binding.timerNumberpickerMinute.maxValue = 59
        binding.timerNumberpickerMinute.value = 0
        binding.timerNumberpickerMinute.setFormatter { value ->
            String.format("%02d", value)
        }
        binding.timerNumberpickerMinute.setOnValueChangedListener { _, _, newVal ->
            minutes = newVal
            pickerChangeValueAction()
        }

        binding.timerNumberpickerSecond.minValue = 0
        binding.timerNumberpickerSecond.maxValue = 59
        binding.timerNumberpickerSecond.value = 0
        binding.timerNumberpickerSecond.setFormatter { value ->
            String.format("%02d", value)
        }
        binding.timerNumberpickerSecond.setOnValueChangedListener { _, _, newVal ->
            seconds = newVal
            pickerChangeValueAction()
        }
    }

    private fun pickerChangeValueAction() {
        isTimerRunning = false
        countDownTimer?.cancel()
        updateTimerText()
        updateNumberPicker()
        binding.timerPlayPauseButtom.setImageResource(R.drawable.ic_chronometer_main_play)
    }

    private fun changeStatusBarColor() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
    }

    private fun clickListeners() {
        binding.timerPlayPauseButtom.setOnClickListener {
            animateClick(binding.timerPlayPauseButtom)
            vibrate()
            if (!isTimerRunning) {
                saveBackupTimer()
                startCount()
                binding.timerPlayPauseButtom.setImageResource(R.drawable.ic_chronometer_main_pause)
            }
        }
        binding.timerMainResetButtom.setOnClickListener {
            animateClick(binding.timerMainResetButtom)
            vibrate()
            resetAction()
        }

        binding.timerMainExitButtom.setOnClickListener {
            vibrate()
            requireActivity().finish()
        }

        binding.chronometerMainImgViewSoundButtom.setOnClickListener {
            animateClick(binding.chronometerMainImgViewSoundButtom)
            vibrate()
            beepIsOn = !beepIsOn
            saveBackupBeepSong()
            setIconToSoundButton()
        }
        binding.timerToChronometerButtom.setOnClickListener{
            animateClick(binding.timerToChronometerButtom)
            openFragment(ChronometerFragment())
        }
    }
    private fun openFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        transaction.replace(R.id.time_counter_frame_layout, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun startCount() {
        val milliseconds = HHMMSSToMillis()
        countDownTimer = object : CountDownTimer(milliseconds, 10) {

            override fun onTick(millisUntilFinished: Long) {
                millisToHHMMSS(millisUntilFinished)
                isTimerRunning = true
                updateTimerText()
            }

            override fun onFinish() {
                cancel()
                doBeep()
                isTimerRunning = false
                millisToHHMMSS(milliseconds)
                updateTimerText()
                binding.timerPlayPauseButtom
                    .setImageResource(R.drawable.ic_chronometer_main_play)
            }
        }.start()
    }

    private fun doBeep() {
        if (beepIsOn && isTimerRunning) {
            val toneGen1 = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000)
        }
    }

    private fun HHMMSSToMillis(): Long {
        return hours * 3600000L + minutes * 60000L + seconds * 1000L
    }

    private fun millisToHHMMSS(milliseconds: Long) {
        hours = (milliseconds / 3600000L).toInt()
        minutes = ((milliseconds % 3600000L) / 60000L).toInt()
        seconds = ((milliseconds % 60000L) / 1000L).toInt()
    }

    private fun resetAction() {
        countDownTimer?.onFinish()
        isTimerRunning = false
        hours = 0
        minutes = 0
        seconds = 0
        saveBackupTimer()
        updateTimerText()
        updateNumberPicker()
    }

    private fun animateClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_card_view)
        view.startAnimation(animation)
    }

    private fun updateTimerText() {
        binding.timerMainTextViewSeconds.text = String.format("%02d", seconds)
        binding.timerMainTextViewMinute.text = String.format("%02d", minutes)
        binding.timerMainTextViewHour.text = String.format("%02d", hours)
    }


    private fun vibrate() {
        val timeVibrationMillis = 100L
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(
                timeVibrationMillis,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(timeVibrationMillis)
        }
    }
}