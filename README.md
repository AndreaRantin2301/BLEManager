This library provides an implementation of a service for BLE connectivity and data communication with a device.
You can connect to a BLE device and send and receive data from it.

This library uses dagger-hilt for dependency injection so you also need to include it in your project to use this.
It is also strongly recommended to use dagger-hilt for dependency injection in your project too since it makes
setting this up much easier

# Usage

## Manifest

First to use this library you have to declare the service in your manifest like this:

```

<service
    android:name="com.andrearantin.blemanager.BLEService"
    android:enabled="true"
    android:stopWithTask="true" />

```

## Init

The **BLEService** class works alongside a **BLEDataManager** class that is responsible for data handling and connectivity state change.
To use them you simply have to inject them with dagger-hilt like this:

```

@HiltViewModel
class YourViewModel @Inject constructor(
    private val bleService: BLEService,
    private val bleDataManager: BLEDataManager
) :ViewModel() {
    //YOUR LOGIC HERE
}

```

## Connecting to a device

To connect to a device you have to set the service UIDs in the **BLEService** and call the **connect()** function like this:

```

 bleService.serviceUuid = UUID.fromString("0000aaa0-0000-1000-8000-aabbccddeeff")
 bleService.writeUuid = UUID.fromString("0000aaa1-0000-1000-8000-aabbccddeeff")
 bleService.readUuid = UUID.fromString("0000aaa2-0000-1000-8000-aabbccddeeff")

val deviceAddress = "67:94:F3:A2:07:6F" //PUT YOUR DEVICE ADDRESS HERE
val res = bleService.connect(deviceAddress)

```
The **connect()** function returns a boolean that is true in case of a successfull gatt connection or false if the connection
could not be estabilished or the deviceAddress provided was not valid

### Listen for connectivity state changes

The **BLEDataManager** class listens to connectivity state changes from the **BLEService** and sends values trough a channel whenever it detects a connectivity state change.
The value returned are as follow:

- BLEConnectionEvent.BLE_CONNECTED --> The service has successfully connected to the device but the services have not yet been discovered. You cannot send or receive data yet
- BLEConnectionEvent.BLE_DISCONNECTED --> The device disconnected
- BLEConnectionEvent.BLE_SERVICES_DISCOVERED --> The services needed for communication have been discovered. You can start sending and receiving data from the device

To receive those updates you need to setup a channel to receive values like this: 

```

 bleDataManager.connectionEventChannel.consumeAsFlow().collect { bleConnectionEvent ->
    when(bleConnectionEvent){
        //MANAGE UI AND LOGIC AS NEEDED DEPENDING ON ACTION
        BLEConnectionEvent.BLE_CONNECTED -> {
           //YOUR DEVICE CONNECTED LOGIC HERE
        }
        BLEConnectionEvent.BLE_DISCONNECTED -> {
            //YOUR DEVICE DISCONNECTED LOGIC HERE
        }
        BLEConnectionEvent.BLE_SERVICES_DISCOVERED -> {
            //READY TO RECEIVE AND TRASMIT DATA
        }
    }
}

```

## Setting up to transmit data

### Command structure

This library has a command structure where commands who have a cmd value and a data class that holds all the command's data to be transmitted are converted in
a byte array. The class that represents a command is called **BLECommand** and you need to specify with a data class what data the command holds if any.
Here are some examples.

#### Command that has no data

A command that has no data to be transmitted is only composed of a byte that specifies the command value(i.e 0x41)

```

val commandWithNoData : BLECommand<Unit> = BLECommand(0x41,Unit)

```

#### Command that has data

A command that has data also has a data class instance that comes with it that holds all of the command's data(For example integer values that need to be sent to the BLE device)

```

val bleCommandWithData : BLECommand<CmdData> = BLECommand(0x41, CmdData(147,2192))

```

```

data class CmdData(
    val valueToSend1 : Int,
    val valueToSend2 : Int
)

```

