package com.andrearantin.blemanager.data.command

enum class BytesResult {
    ERROR_HEADER,
    ERROR_SOF_EOF,
    ERROR_CRC,
    ERROR_CMD_DATA,
    OK
}