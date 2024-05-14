package com.andrearantin.blemanager.data

/**
 * Class to represent BLE characteristic data elaboration result
 */
enum class BLEDataResult {
    OK,
    SOF_EOF_ERROR,
    CRC_ERROR
}