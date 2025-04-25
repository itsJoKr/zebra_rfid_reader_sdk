
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:zebra_rfid_reader_sdk/zebra_rfid_reader_sdk.dart';

class HomeScreen extends StatelessWidget {
  HomeScreen({super.key});

  final _zebraRfidReaderSdkPlugin = ZebraRfidReaderSdk();

  @override
  Widget build(BuildContext context) {
    // ref.watch(zebraRfidSdkPluginNotifierProvider);
    // _setUpListeners(ref, context);

    // ref.read(zebraRfidSdkPluginNotifierProvider.notifier).startReader();

    return CupertinoPageScaffold(
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Theme.of(context).colorScheme.inversePrimary,
          title: Text("PLUGIN RFID Example"),
        ),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              MaterialButton(
                color: Theme.of(context).colorScheme.primary,
                child: Text(
                  "Connect to Zebra",
                  style: TextStyle(color: Theme.of(context).colorScheme.onPrimary, fontSize: 14),
                ),
                onPressed: () async {
                  debugPrint("Connecting to reader: ");
                  final name = 'RFIDEM45';
                  await _zebraRfidReaderSdkPlugin.connect(name);
                  // await ref.read(zebraRfidSdkPluginNotifierProvider.notifier).connectToZebra(ReaderDevice.initial());

                  // snackbar
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Done connecting'),
                    ),
                  );
                },
              ),
              MaterialButton(
                color: Theme.of(context).colorScheme.primary,
                child: Text(
                  "Disconnect",
                  style: TextStyle(color: Theme.of(context).colorScheme.onPrimary, fontSize: 14),
                ),
                onPressed: () {
                  _zebraRfidReaderSdkPlugin.disconnect();
                  // ref.read(zebraRfidSdkPluginNotifierProvider.notifier).disconnectFromZebra();
                },
              ),
              MaterialButton(
                color: Theme.of(context).colorScheme.primary,
                child: Text(
                  "Stop/start Scan",
                  style: TextStyle(color: Theme.of(context).colorScheme.onPrimary, fontSize: 14),
                ),
                onPressed: () {
                  // ref.read(zebraRfidSdkPluginNotifierProvider.notifier).triggerInventoryRead();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
