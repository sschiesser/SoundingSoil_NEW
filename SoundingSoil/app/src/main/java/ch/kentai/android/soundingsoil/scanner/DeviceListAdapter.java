package ch.kentai.android.soundingsoil.scanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.kentai.android.soundingsoil.R;

import static java.lang.Integer.min;

public class DeviceListAdapter extends BaseAdapter {

    private static final int TYPE_TITLE = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_EMPTY = 2;

    //private final ArrayList<SimpleBluetoothDevice> mListBondedValues = new ArrayList<>();
    private final ArrayList<SimpleBluetoothDevice> mListValues = new ArrayList<>();
    private final Context mContext;

    public DeviceListAdapter(final Context context) {
        mContext = context;
    }



    /**
     * Updates the list of devices.
     */

        public void update(final SimpleBluetoothDevice _device) {

            final SimpleBluetoothDevice device = findDevice(_device);
            if (device == null) {
                mListValues.add(_device);
            } else {
                device.rssi = _device.rssi;
            }

        notifyDataSetChanged();
    }


    private SimpleBluetoothDevice findDevice(final SimpleBluetoothDevice _device) {
        for (final SimpleBluetoothDevice device : mListValues)
            if (device.matches(_device))
                return device;
        return null;
    }

    public void clearDevices() {
        mListValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        final int availableCount = mListValues.isEmpty() ? 1 : mListValues.size() ; // 1 for title, 1 for empty text
//        final int availableCount = mListValues.isEmpty() ? 2 : mListValues.size() + 1; // 1 for title, 1 for empty text
        return availableCount;
    }


    @Override
    public Object getItem(int position) {

//        if (position == 0)
//            return R.string.scanner_subtitle_not_bonded;
//        else
//            return mListValues.get(position - 1);

            return mListValues.get(position);
    }


    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    @Override
    public int getItemViewType(int position) {
        //if (position == 0)
          //  return TYPE_TITLE;

        if (position == getCount() - 1 && mListValues.isEmpty())
            return TYPE_EMPTY;

        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View oldView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final int type = getItemViewType(position);

        View view = oldView;
        switch (type) {
            case TYPE_EMPTY:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_empty, parent, false);
                }
                break;
            case TYPE_TITLE:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_title, parent, false);
                }
                final TextView title = (TextView) view;
                //title.setText((Integer) getItem(position));
                break;
            default:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_row, parent, false);
                    final ViewHolder holder = new ViewHolder();
                    holder.name = view.findViewById(R.id.name);
                    holder.address = view.findViewById(R.id.address);
                    holder.rssi = view.findViewById(R.id.rssi);
                    view.setTag(holder);
                }

                final SimpleBluetoothDevice device = (SimpleBluetoothDevice) getItem(position);
                final ViewHolder holder = (ViewHolder) view.getTag();
                final String name = device.name;
                holder.name.setText(name != null ? name : mContext.getString(R.string.not_available));
                final int rssiPercent = min(100, (int) (100.0f * (127.0f - Integer.parseInt(device.rssi)) / (127.0f + 20.0f)));
                holder.rssi.setImageLevel(rssiPercent);
                holder.rssi.setVisibility(View.VISIBLE);
                break;
        }

        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private ImageView rssi;
    }
}


