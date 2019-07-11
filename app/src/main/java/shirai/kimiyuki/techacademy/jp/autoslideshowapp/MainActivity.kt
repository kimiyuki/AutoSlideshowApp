package shirai.kimiyuki.techacademy.jp.autoslideshowapp

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.Snackbar
import android.os.Bundle
import android.os.Handler
import android.Manifest
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.content.DialogInterface
import android.database.Cursor
import android.os.Build
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor: Cursor? = null
    private var mTimer: Timer? = null
    private val mHandler = Handler()
    private var myCallback: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myRequestPermission()
        Log.d("hello", "End OnCreate")
        //end of flow on Create

        //set listeners
        backward_button.setOnClickListener { backImage(it) }

        forward_button.setOnClickListener { forwardImage(it) }

        play_stop_button.setOnClickListener { playStop(it) }

    }

    private fun myRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ret = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (ret != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            } else {
                setupCursor()
            }
        } else {
            setupCursor()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("hello", "onReqPermission:${requestCode}")
        if (requestCode != PERMISSIONS_REQUEST_CODE) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupCursor()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setNeutralButton("終了します") { dialog, which ->
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    finish()
                }
            }
            builder.setTitle("権限がない場合は終了します").show()
        }
    }

    private fun backImage(v: View) {
        Log.d("hello", "start backImage")
        if (mTimer != null) {
            showSnack(v, "再生中です。停止を押してからです", 3000)
            return
        }
        if (cursor!!.isFirst) cursor?.moveToLast() else cursor?.moveToPrevious()
        setImageToImageView()
    }

    private fun forwardImage(v: View) {
        Log.d("hello", "start forward_image")
        if (mTimer != null) {
            showSnack(v, "再生中です。停止を押してからです", 3000)
            return
        }
        if (cursor!!.isLast) cursor?.moveToFirst() else cursor?.moveToNext()
        setImageToImageView()
    }

    private fun showSnack(v: View, text: String, duration: Int) {
        val snack: Snackbar = Snackbar.make(v, text, duration)
        snack.setAction("了解") {}
        snack.show()
    }

    private fun playStop(v: View) {
        if (mTimer == null) {
            play_stop_button.text = "停止"
            backward_button.isEnabled = false
            forward_button.isEnabled = false
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
            play_stop_button.text = "停止"
            backward_button.isEnabled = true
            forward_button.isEnabled = true
            Snackbar.make(v, "停止します", 1000).show()
            mTimer!!.cancel()
            mTimer = null
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
        setImageToImageView()
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
            Toast.makeText(this, "画像がありません", Toast.LENGTH_LONG)
        }
    }
}
