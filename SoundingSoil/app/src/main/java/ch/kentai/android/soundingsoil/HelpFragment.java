package ch.kentai.android.soundingsoil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
//import android.support.design.widget.FloatingActionButton;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HelpFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HelpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelpFragment extends Fragment {

    private Button prePageButton;
    private Button nextPageButton;

    private Button closeButton;
    private ImageView imageViewPdf;

    private static final String FILENAME = "help.pdf";

    private Bundle savedState = null;
    private int pageIndex;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;




    public HelpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HelpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HelpFragment newInstance(String param1, String param2) {
        HelpFragment fragment = new HelpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageIndex = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_help, container, false);

        prePageButton = fragmentView.findViewById(R.id.btn_previous);
        nextPageButton = fragmentView.findViewById(R.id.btn_next);

        imageViewPdf = fragmentView.findViewById(R.id.pdf_image);


        //set buttons event
        prePageButton.setOnClickListener(onActionListener(-1)); //previous button clicked
        nextPageButton.setOnClickListener(onActionListener(1)); //next button clicked


        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle("current_state");
        }
        if(savedState != null) {
            pageIndex = savedInstanceState.getInt("current_page", 0);
        }
        savedState = null;

        try {
            openRenderer(getContext());
            openRenderer(getContext());
            showPage(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }





        closeButton = fragmentView.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> {
            if (null != currentPage) {
//                if (null != currentPage && null != mBundle) {
                //getArguments().putInt("current_page", currentPage.getIndex());
                //mBundle.putInt("current_page", currentPage.getIndex());
            }

            getActivity().onBackPressed();
        });



        return fragmentView;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }



    @Override
    public void onStop() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
    }


    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();
        state.putInt("current_page", currentPage.getIndex());
        return state;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBundle("current_state", (savedState != null) ? savedState : saveState());

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
        void onFragmentInteraction(int count);
    }


    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        //file = new File(context.getDir);
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }


    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (null != currentPage) {
            currentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        imageViewPdf.setImageBitmap(bitmap);
        updateUi();
    }

    private void updateUi() {
        int index = currentPage.getIndex();
        int pageCount = pdfRenderer.getPageCount();
        prePageButton.setEnabled(0 != index);
        nextPageButton.setEnabled(index + 1 < pageCount);
    }

    public int getPageCount() {
        return pdfRenderer.getPageCount();
    }


    private View.OnClickListener onActionListener(final int i) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (i < 0) {//go to previous page
                    showPage(currentPage.getIndex() - 1);
                } else {
                    showPage(currentPage.getIndex() + 1);
                }
            }
        };
    }



}
