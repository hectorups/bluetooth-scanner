package com.example.bluetoothtest;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        final private String TAG = "PlaceholderFragment";

        private ImageButton mSearchBtn;
        private ListView mLebResultList;
        private ProgressBar mProgress;
        private ArrayList<Map<String, String>> mList;

        private BluetoothAdapter mBluetoothAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mSearchBtn = (ImageButton) rootView.findViewById(R.id.searchBtn);
            mLebResultList = (ListView) rootView.findViewById(R.id.lebResults);
            mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);

            mList = new ArrayList<>();
            String[] from = {"a","b","c"};
            int[] to = { R.id.leb_list_item_mac, R.id.leb_list_item_uuid, R.id.leb_list_item_rssi };

            SimpleAdapter adapter = new SimpleAdapter(getActivity(), mList,
                    R.layout.list_item_leb, from, to);
            mLebResultList.setAdapter(adapter);

            mSearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchBluetooth(v);
                }
            });

            return rootView;
        }


        public void searchBluetooth(View v){

            Log.d(TAG, "LANGUAGE: " + Locale.getDefault().getLanguage());

            Log.d(TAG, "Searching for bluetooth...");

            mList.clear();
            ((SimpleAdapter) mLebResultList.getAdapter()).notifyDataSetChanged();

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            mProgress.setVisibility(View.VISIBLE);
            mLebResultList.setVisibility(View.INVISIBLE);

            scanLeDevice();

        }


        private static final long SCAN_PERIOD = 2000;
        private void scanLeDevice() {
            // Stops scanning after a pre-defined scan period.
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            endSearch();
                        }
                    });
                }
            }, SCAN_PERIOD);

            mSearchBtn.setEnabled(false);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        private BluetoothAdapter.LeScanCallback mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi,
                                         byte[] scanRecord) {
                        Log.d(TAG, "Bluetooth found: "
                                + device.getName() + " - "
                                + device.getAddress() + " - "
                                + device.getUuids() + " - "
                                + rssi
                        );


                        addDevice(device, serviceFromScanRecord(scanRecord), rssi);
                    }

                };

        public String serviceFromScanRecord(byte[] scanRecord) {

            final int serviceOffset = 9;
            final int serviceLimit = 16;

            try{

                byte[] service = Arrays.copyOfRange(scanRecord, serviceOffset, serviceOffset + serviceLimit);

                return bytesToHex(service);


            } catch (Exception e){
                return null;
            }

        }


        public String bytesToHex(byte[] bytes) {
            StringBuilder builder = new StringBuilder();
            for (byte b: bytes) {
                builder.append(String.format("%02x ", b));
            }
            return builder.toString();
        }


        private void endSearch(){
            Log.d(TAG, "search finished");

            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            mSearchBtn.setEnabled(true);

            Context context = getActivity().getApplicationContext();
            CharSequence text = "SEARCH FINISHED";
            int duration = Toast.LENGTH_SHORT;

            ((SimpleAdapter) mLebResultList.getAdapter()).notifyDataSetChanged();

            mProgress.setVisibility(View.INVISIBLE);
            mLebResultList.setVisibility(View.VISIBLE);

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        private void addDevice(BluetoothDevice device, String uuid, int rssi){
            HashMap<String, String> item = new HashMap<>();
            item.put("a", device.getAddress());
            item.put("b", "" + uuid );
            item.put("c", "" + rssi);
            mList.add(item);

        }

    }
}
