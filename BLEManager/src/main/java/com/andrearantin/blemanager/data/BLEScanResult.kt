package com.andrearantin.blemanager.data

import com.andrearantin.blemanager.BLEDevice

/**
 * Created by: Andrea Rantin
 * Date: 19/08/2024
 * Time: 11:21
 */
data class BLEScanResult(
    val statusCode : BLEScanCode,
    val bleDevice : BLEDevice?
)
