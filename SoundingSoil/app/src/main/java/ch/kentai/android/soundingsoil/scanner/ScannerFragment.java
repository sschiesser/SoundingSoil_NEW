package ch.kentai.android.soundingsoil.scanner;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import ch.kentai.android.soundingsoil.R;
import ch.kentai.android.soundingsoil.viewmodels.BlinkyViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScannerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScannerFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    private OnDeviceSelectedListener mListener;

    private DeviceListAdapter mAdapter;
    private Button mScanButton;

    private BlinkyViewModel mViewModel;
    private View mScanningView;



    public ScannerFragment() {
        // Required empty public constructor
    }

    public static ScannerFragment getInstance() {
        final ScannerFragment fragment = new ScannerFragment();

//        final Bundle args = new Bundle();
//        if (uuid != null)
//            args.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
//        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScannerFragment newInstance(String param1, String param2) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Interface required to be implemented by activity.
     */
    public interface OnDeviceSelectedListener {
        /**
         * Fired when user selected the device.
         *
         * @param name
         *            the device number
         */
        void onDeviceSelected(final SimpleBluetoothDevice device);

        /**
         * Fired when scanner dialog has been cancelled without selecting a device.
         */
        void onDialogCanceled();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mViewModel = ViewModelProviders.of(getActivity()).get(BlinkyViewModel.class);

        mViewModel.getDeviceDiscovered().observe(this, device -> {
            if (device != null) {
                mAdapter.update(device);
            }
            //Log.i("info", "updateAdapter" + text);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnDeviceSelectedListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDeviceSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clearDevices();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_scanner, null);
        final ListView listview = dialogView.findViewById(android.R.id.list);

        listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
        listview.setAdapter(mAdapter = new DeviceListAdapter(getActivity()));

        builder.setTitle(R.string.scanner_title);
        final AlertDialog dialog = builder.setView(dialogView).create();
        listview.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            final SimpleBluetoothDevice d = (SimpleBluetoothDevice) mAdapter.getItem(position);
            mListener.onDeviceSelected(d);
        });

        //mPermissionRationale = dialogView.findViewById(R.id.permission_rationale); // this is not null only on API23+

        mScanButton = dialogView.findViewById(R.id.action_cancel);
        mScanButton.setOnClickListener(v -> {
            if (v.getId() == R.id.action_cancel) {
                dialog.cancel();
            }
        });

        mScanningView = dialogView.findViewById(R.id.state_scanning);

        mViewModel.getInqState().observe(this, state -> {
           // if(!state) dialog.cancel();
            if(state) mScanningView.setVisibility(View.VISIBLE);
            else mScanningView.setVisibility(View.INVISIBLE);

        });

        mAdapter.clearDevices();


        //addBoundDevices();
        return dialog;
    }



    // receive a2dp bluetooth devices scan results and update the adapter
//    private ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(final int callbackType, final ScanResult result) {
//            // do nothing
//        }
//
//        @Override
//        public void onBatchScanResults(final List<ScanResult> results) {
//            mAdapter.update(results);
//        }
//
//        @Override
//        public void onScanFailed(final int errorCode) {
//            // should never be called
//        }
//    };


}
