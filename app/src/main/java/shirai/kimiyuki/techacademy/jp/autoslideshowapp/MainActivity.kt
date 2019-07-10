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
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor: Cursor? = null
    private var isPlaying = false
    private var mTimer: Timer? = null
    private val mHandler = Handler()
    private var myCallback: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myCallback = {
            setupCursor()
            setImageToImageView()
        }
        my_request_permission()
        Log.d("hello", "End OnCreate")
        //end of flow on Create

        //set listeners
        backward_button.setOnClickListener {
            myCallback = { back_image(it) }
            my_request_permission()
        }

        forward_button.setOnClickListener {
            myCallback = { forward_image(it) }
            my_request_permission()
        }

        play_stop_button.setOnClickListener {
            myCallback = { play_stop(it) }
            my_request_permission()
        }

    }

    private fun my_request_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ret = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (ret != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            } else {
                myCallback()
            }
        } else {
            myCallback()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("hello", "onReqPermission:${requestCode}")
        if (requestCode != PERMISSIONS_REQUEST_CODE) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            myCallback()
        }
    }

    private fun back_image(v: View) {
        Log.d("hello", "start back_image")
        if (isPlaying) {
            val snack = Snackbar.make(v, "再生中です。停止してからです", 3000)
            snack.setAction("了解") { /*no response*/ }.show()
            return
        }
        if (cursor!!.isFirst) cursor?.moveToLast() else cursor?.moveToPrevious()
        setImageToImageView()
    }

    private fun forward_image(v: View) {
        Log.d("hello", "start forward_image")
        if (isPlaying) {
            val snack = Snackbar.make(v, "再生中です。停止を押してからです", 3000)
            snack.setAction("了解") {}
            snack.show()
            Log.d("hello", "isplaying")
            //it.animate().rotation(180.0f).alpha(1.0f).setDuration(300).start()
            return
        }
        if (cursor!!.isLast) cursor?.moveToFirst() else cursor?.moveToNext()
        setImageToImageView()
    }

    private fun play_stop(v: View) {
        play_stop_button.text = if (isPlaying) "再生" else "停止"
        isPlaying = !isPlaying
        if (isPlaying) {
            if (mTimer != null) {
                return
            }
            Snackbar.make(v, "再生します", 1000).show()
            mTimer = Timer()
            mTimer!!.schedule(
                object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (cursor!!.isLast) cursor?.moveToFirst() else cursor?.moveToNext()
                            setImageToImageView()
                        }
                    }
                },
                2000, 2000
            )
        } else {
            if (mTimer != null) {
                Snackbar.make(v, "停止します", 1000).show()
                mTimer!!.cancel()
                mTimer = null
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cursor?.close()
    }

    private fun setupCursor() {
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        cursor?.moveToFirst()
        Log.d("hello", "setupCursor:${cursor?.position}")
    }

    private fun setImageToImageView() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        Log.d("hello cursor count", "${cursor?.count}")
        if (cursor!!.count > 0) {
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imageUri)
            Log.d("hello setImage", "URI : " + imageUri.toString())
        } else {
            //TODO no image
        }
    }
}
