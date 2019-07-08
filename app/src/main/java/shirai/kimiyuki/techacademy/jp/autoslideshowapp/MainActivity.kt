package shirai.kimiyuki.techacademy.jp.autoslideshowapp

import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.Snackbar
import android.os.Bundle
import android.os.Handler
import android.Manifest
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor: Cursor? = null
    private var isPlaying = false
    private var mTimer: Timer? = null
    private var mTimeSec = 0.0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
        } else {
            setupCursor()
            setImageToImageView()
        }

        backward_button.setOnClickListener {
            Log.d("hello", isPlaying.toString())
            if(isPlaying){
                Log.d("hello", "aaal")
                Snackbar.make(it, "再生中です。自動再生を停止してください", 10).show()
                return@setOnClickListener
            }
            if (cursor!!.isFirst) {
                cursor!!.moveToLast()
            } else {
                cursor!!.moveToPrevious()
            }
            setImageToImageView()
        }
        forward_button.setOnClickListener {
            Log.d("hello", isPlaying.toString())
            if(isPlaying){
                //TODO waringを出す
                Snackbar.make(it, "再生中です。自動再生を停止してください", 10).show()
                return@setOnClickListener
            }
            if (cursor!!.isLast) {
                cursor!!.moveToFirst()
            } else {
                cursor!!.moveToNext()
            }
            setImageToImageView()
        }
        toggle_button.setOnClickListener {
            toggle_button.text = if (isPlaying) "再生" else "停止"
            isPlaying = !isPlaying
            if (isPlaying) {
                if (mTimer != null) {
                    return@setOnClickListener
                }
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if(cursor!!.isLast) {
                                cursor!!.moveToFirst()
                            }else{
                                cursor!!.moveToNext()
                            }
                            setImageToImageView()
                        }
                    }
                }, 2000, 2000)
            }else{
                if(mTimer != null) {
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }

        Log.d("hello", "End OnCreate")
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("hello", "onReqPermission:${requestCode}")
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCursor()
                    setImageToImageView()
                }
        }
    }


    private fun setupCursor() {
        if (cursor != null) cursor!!.close()
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        cursor!!.moveToFirst()
        Log.d("hello", "setupCursor:${cursor!!.position}")
    }

    private fun setImageToImageView() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
        Log.d("hello setImage", "URI : " + imageUri.toString())
    }
}
