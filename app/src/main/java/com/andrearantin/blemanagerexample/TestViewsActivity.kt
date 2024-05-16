package com.andrearantin.blemanagerexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import com.andrearantin.blemanager.BLEDataManager
import com.andrearantin.blemanagerexample.domain.TestViewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestViewsActivity : AppCompatActivity() {

    companion object {
        private val TAG = TestViewsActivity::class.qualifiedName

        init {
            System.loadLibrary("blemanagerexample")
        }
    }

    private val testViewsViewModel by viewModels<TestViewsViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_views)
        val btn : Button = findViewById(R.id.testBtn)
        btn.setOnClickListener {
            Log.w(TAG, "TEST CONNECTION")
            testViewsViewModel.testConnection()
        }
        val getCmdBtn : Button = findViewById(R.id.testGetCmd)
        getCmdBtn.setOnClickListener {
            Log.w(TAG, "TEST GET CMD")
            testViewsViewModel.testGetCmd()
        }

    }

    override fun onResume() {
        super.onResume()
    }
}