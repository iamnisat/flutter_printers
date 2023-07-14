// ignore_for_file: library_private_types_in_public_api

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:pos_printer/blue_pos_printer.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  BlueThermalPrinter bluetooth = BlueThermalPrinter.instance;

  List<BluetoothDevice> _devices = [];
  BluetoothDevice? _device;
  bool _connected = false;
  bool? isOn = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    // TODO here add a permission request using permission_handler
    // if permission is not granted, kzaki's thermal print plugin will ask for location permission
    // which will invariably crash the app even if user agrees so we'd better ask it upfront

    // var statusLocation = Permission.location;
    // if (await statusLocation.isGranted != true) {
    //   await Permission.location.request();
    // }
    // if (await statusLocation.isGranted) {
    // ...
    // } else {
    // showDialogSayingThatThisPermissionIsRequired());
    // }
    isOn = await bluetooth.turnOn;
    debugPrint("initState isOn: $isOn");
    bool? isConnected = await bluetooth.isConnected;
    List<BluetoothDevice> devices = [];
    try {
      devices = await bluetooth.getBondedDevices();
    } on PlatformException {
      debugPrint("Error");
    }

    bluetooth.onStateChanged().listen((state) {
      switch (state) {
        case BlueThermalPrinter.CONNECTED:
          setState(() {
            _connected = true;
            debugPrint("bluetooth device state: connected");
          });
          break;
        case BlueThermalPrinter.DISCONNECTED:
          setState(() {
            _connected = false;
            debugPrint("bluetooth device state: disconnected");
          });
          break;
        case BlueThermalPrinter.DISCONNECT_REQUESTED:
          setState(() {
            _connected = false;
            debugPrint("bluetooth device state: disconnect requested");
          });
          break;
        case BlueThermalPrinter.STATE_TURNING_OFF:
          setState(() {
            _connected = false;
            debugPrint("bluetooth device state: bluetooth turning off");
          });
          break;
        case BlueThermalPrinter.STATE_OFF:
          setState(() {
            _connected = false;
            debugPrint("bluetooth device state: bluetooth off");
          });
          break;
        case BlueThermalPrinter.STATE_ON:
          setState(() {
            _connected = false;
            print("bluetooth device state: bluetooth on");
          });
          break;
        case BlueThermalPrinter.STATE_TURNING_ON:
          setState(() {
            _connected = false;
            debugPrint("bluetooth device state: bluetooth turning on");
          });
          break;
        case BlueThermalPrinter.ERROR:
          setState(() {
            _connected = false;
            print("bluetooth device state: error");
          });
          break;
        default:
          print(state);
          break;
      }
    });

    if (!mounted) return;
    setState(() {
      _devices = devices;
    });

    if (isConnected == true) {
      setState(() {
        _connected = true;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Pos Printer Example'),
          actions: [
            Switch(
                value: isOn ?? false,
                onChanged: (value) async {
                  debugPrint("switch value: $isOn");
                  if (isOn??false) {
                    isOn = await bluetooth.turnOff;
                  } else {
                    isOn = await bluetooth.turnOn;
                  }
                  setState(() {});
                },
                activeColor: Colors.white),
          ],
        ),
        body: ListView.builder(
          shrinkWrap: true,
          itemCount: _devices.length,
          itemBuilder: (context, index) {
            return ListTile(
              title: Text(_devices[index].name ?? ''),
              subtitle: Text(_devices[index].address ?? ''),
              onTap: () async {
                try {
                  bool? isOn = await bluetooth.turnOn;
                  if (isOn == true) {
                    bluetooth.isConnected.then((isConnected) {
                      if (!isConnected!) {
                        bluetooth.connect(_devices[index]).catchError((error) {
                          setState(() {
                            _connected = false;
                          });
                        });
                      }
                    });
                  } else {
                    showDialog(
                        context: context,
                        builder: (BuildContext context) {
                          return AlertDialog(
                            title: const Text("Error"),
                            content: const Text("Bluetooth is not on"),
                            actions: <Widget>[
                              TextButton(
                                child: const Text("Ok"),
                                onPressed: () async {
                                  isOn = await bluetooth.turnOn;
                                },
                              ),
                            ],
                          );
                        });
                  }
                } catch (e) {
                  debugPrint(e.toString());
                }
              },
            );
          },
          // body: Padding(
          //   padding: const EdgeInsets.all(8.0),
          //   child: ListView(
          //     children: <Widget>[
          //       Row(
          //         crossAxisAlignment: CrossAxisAlignment.center,
          //         mainAxisAlignment: MainAxisAlignment.start,
          //         children: <Widget>[
          //           const SizedBox(width: 10),
          //           const Text(
          //             'Device:',
          //             style: TextStyle(
          //               fontWeight: FontWeight.bold,
          //             ),
          //           ),
          //           const SizedBox(width: 30),
          //           Expanded(
          //             child: DropdownButton(
          //               items: _getDeviceItems(),
          //               onChanged: (BluetoothDevice? value) =>
          //                   setState(() => _device = value),
          //               value: _device,
          //             ),
          //           ),
          //         ],
          //       ),
          //       const SizedBox(height: 10),
          //       Row(
          //         crossAxisAlignment: CrossAxisAlignment.center,
          //         mainAxisAlignment: MainAxisAlignment.end,
          //         children: <Widget>[
          //           ElevatedButton(
          //             style: ElevatedButton.styleFrom(primary: Colors.brown),
          //             onPressed: () {
          //               initPlatformState();
          //             },
          //             child: const Text(
          //               'Refresh',
          //               style: TextStyle(color: Colors.white),
          //             ),
          //           ),
          //           const SizedBox(width: 20),
          //           ElevatedButton(
          //             style: ElevatedButton.styleFrom(
          //                 primary: _connected ? Colors.red : Colors.green),
          //             onPressed: _connected ? _disconnect : _connect,
          //             child: Text(
          //               _connected ? 'Disconnect' : 'Connect',
          //               style: const TextStyle(color: Colors.white),
          //             ),
          //           ),
          //         ],
          //       ),
          //       Padding(
          //         padding:
          //             const EdgeInsets.only(left: 10.0, right: 10.0, top: 50),
          //         child: ElevatedButton(
          //           style: ElevatedButton.styleFrom(primary: Colors.brown),
          //           onPressed: () {},
          //           child: const Text('PRINT TEST',
          //               style: TextStyle(color: Colors.white)),
          //         ),
          //       ),
          //     ],
          //   ),
        ),
      ),
    );
  }

  List<DropdownMenuItem<BluetoothDevice>> _getDeviceItems() {
    List<DropdownMenuItem<BluetoothDevice>> items = [];
    if (_devices.isEmpty) {
      items.add(const DropdownMenuItem(
        child: Text('NONE'),
      ));
    } else {
      _devices.forEach((device) {
        items.add(DropdownMenuItem(
          child: Text(device.name ?? ""),
          value: device,
        ));
      });
    }
    return items;
  }

  void _connect() {
    if (_device != null) {
      bluetooth.isConnected.then((isConnected) {
        if (isConnected == false) {
          bluetooth.connect(_device!).catchError((error) {
            setState(() => _connected = false);
          });
          setState(() => _connected = true);
        }
      });
    } else {
      show('No device selected.');
    }
  }

  void _disconnect() {
    bluetooth.disconnect();
    setState(() => _connected = false);
  }

  Future show(
    String message, {
    Duration duration = const Duration(seconds: 3),
  }) async {
    await Future.delayed(Duration(milliseconds: 100));
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          message,
          style: const TextStyle(color: Colors.white),
        ),
        duration: duration,
      ),
    );
  }
}
