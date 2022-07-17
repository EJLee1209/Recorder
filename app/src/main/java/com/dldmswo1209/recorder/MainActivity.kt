package com.dldmswo1209.recorder

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dldmswo1209.recorder.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    var mBinding : ActivityMainBinding? = null
    val binding get() = mBinding!!
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var state = State.BEFORE_RECORDING
        set(value){
            field = value
            // reset 버튼의 활성화/비활성화 설정(현재 상태가 녹음 후 또는 재생 중인 경우 활성화)
            binding.resetButton.isEnabled = (value == State.AFTER_RECORDING) || (value == State.ON_PLAYING)
            binding.recordButton.updateIconWithState(value)
        }
    private val recordingFilePath: String by lazy{
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    private fun requestAudioPermission(){ // 권한 요청 메소드
        // requiredPermissions 의 권한을 요청, 요청 코드는 상수로 정의
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(!audioRecordPermissionGranted){
            // 권한 요청 거부 시
            finish()
            // 앱 종료
        }
    }

    private fun initViews() {
        binding.recordButton.updateIconWithState(state)
    }

    private fun bindViews(){
        binding.soundVisualizerView.onRequestCurrentAplitude = {
            recorder?.maxAmplitude ?: 0
        }
        binding.resetButton.setOnClickListener {
            stopPlaying()
            binding.soundVisualizerView.clearVisualization() // 시각화 초기화
            binding.recordTimeTextView.clearCountTime() // 녹음 시간 초기화
            state = State.BEFORE_RECORDING
        }
        binding.recordButton.setOnClickListener{
            when(state){ // 현재 상태에 따라 다른 메소드 호출
                State.BEFORE_RECORDING -> { // 녹음 전
                    startRecording() // 녹음 시작
                }
                State.ON_RECORDING -> { // 녹음 중
                    stopRecording() // 녹음 중단
                }
                State.AFTER_RECORDING -> { // 녹음 후
                    startPlaying() // 재생
                }
                State.ON_PLAYING -> { // 재생 중
                    stopPlaying() // 재생 중단
                }
            }
        }
    }

    private fun initVariables(){
        state = State.BEFORE_RECORDING // 초기 상태(녹음 전)
    }

    private fun startRecording(){
        // 녹음 시작을 위한 메소드
        recorder = MediaRecorder()
            .apply {
            // 녹음을 시작하기 위한 필수 과정들
                setAudioSource(MediaRecorder.AudioSource.MIC) // 소스 지정
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // 포맷 지정
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // 인코더 지정
                setOutputFile(recordingFilePath) // 파일을 지정
                prepare()
            }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }
        recorder?.start()
        binding.soundVisualizerView.startVisualizing(false)
        binding.recordTimeTextView.startCountUp()
        state = State.ON_RECORDING

    }

    private fun stopRecording(){
        recorder?.run{
            stop()
            release() // 메모리 해제, 비용이 큰 작업이므로 메모리 해제를 해주는게 좋음
        }
        recorder = null
        binding.soundVisualizerView.stopVisualizing()
        binding.recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun startPlaying(){ // 재생 시작 메소드
        player = MediaPlayer().apply { // 녹음된 음성을 재생시키기 위한 MediaPlayer
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.start() // 재생 시작
        binding.soundVisualizerView.startVisualizing(true) // 음성 시각화
        binding.recordTimeTextView.startCountUp()
        state = State.ON_PLAYING
    }
    private fun stopPlaying(){ // 재생 멈춤 메소드
        player?.release() // 메모리 해제
        player = null
        binding.soundVisualizerView.stopVisualizing()
        binding.recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}