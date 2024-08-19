package com.andrearantin.blemanagerexample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.andrearantin.blemanagerexample.domain.TestViewsNoHiltViewModel

class TestViewsActivityNoHilt : AppCompatActivity() {

    private val viewModel: TestViewsNoHiltViewModel by viewModels<TestViewsNoHiltViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}