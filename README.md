# BeeLe
BeeLe is an Android Library project for Bluetooth Low Energy communication.We have addressed concurent write charasteristic failure issue
and added work around for it.

Note: Read the below description of Android's BluetoothGattCallback class Key methods for better understanding before use this library.

onDescriptorRead and onDescriptorWrite

This is used to write/read the configuration settings for the BLE device, some manufactures might require to send some data to the BLE device and acknowledge it by reading, before you can connect to the BLE device

onCharacteristicWrite

This is used to send data to the BLE device, usually in data mode for the BLE device. This callback is called when you type

gatt.writeCharacteristic(characteristics);
onCharacteristicRead

This is used to read data from the BLE device The callback is called when you write this code

gatt.readCharacteristic(characteristics);
onCharacteristicChanged

This callback is called when you are trying to send data using writeCharacteristic(characteristics) and the BLE device responds with some value.

BLE device has few characteristics like

1. WRITE - write Characteristics : When you send data, you will need to use WRITE characteristics.
2. READ - read Characteristics : To read the data from the BLE device we have call the corresponding READ characteristics.

Refer the following link for more detail: https://developer.android.com/intl/ko/reference/android/bluetooth/BluetoothGattCallback.html
