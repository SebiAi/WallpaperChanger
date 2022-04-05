package com.sebiai.wallpaperchanger;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Button buttonChooseDir;
    private Button buttonSetRandomWallpaper;
    private TextView textViewCurrentWallpaper;
    private ActivityResultLauncher<Uri> uriActivityResultLauncher;

    private SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Make persistent
            requireActivity().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Save globally and in preferences
            getMyApplication(requireContext()).wallpaperDir = result;
            sharedPreferences.edit().putString(getString(R.string.key_wallpaper_dir), result.toString()).apply();

            // Enable button
            buttonSetRandomWallpaper.setEnabled(true);

//                final ArrayList<Uri> uris = MyFileHandler.getFiles(requireContext(), result);
//
//                DocumentFile file = MyFileHandler.getRandomFile(requireContext(), uris);
//                if (file != null) {
//                    // Log
//                    Log.d("Result", file.getName() + "\t" + file.getUri().getPath());
//                    // Set as Wallpaper
//                    try {
//                        InputStream stream = requireContext().getContentResolver().openInputStream(file.getUri());
//                        Drawable drawable = Drawable.createFromStream(stream, file.getUri().getPath());
//                        stream.close();
//                        frameLayout.setBackground(drawable);
//
//                        MyFileHandler.setFileAsWallpaper(requireContext(), file);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        // Load Uri
        if (sharedPreferences.contains(getString(R.string.key_wallpaper_dir))) {
            getMyApplication(requireContext()).wallpaperDir = Uri.parse(sharedPreferences.getString(getString(R.string.key_wallpaper_dir), null));
            buttonSetRandomWallpaper.setEnabled(true);
        }
        // Load last set wallpaper name
        if (sharedPreferences.contains(getString(R.string.key_current_picture))) {
            textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string),
                    sharedPreferences.getString(getString(R.string.key_current_picture), "-")));
        }
    }

    private void setup() {
        buttonChooseDir = requireView().findViewById(R.id.button_choose_dir);
        buttonChooseDir.setOnClickListener(v -> uriActivityResultLauncher.launch(Uri.parse("image/*")));

        buttonSetRandomWallpaper = requireView().findViewById(R.id.button_set_random_wallpaper);
        buttonSetRandomWallpaper.setOnClickListener(v -> {
            DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(requireContext());
            if (file != null) {
                String fileName = file.getName();
                textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), fileName));
                int amountChangesManual = sharedPreferences.getInt(getString(R.string.key_amount_changes_manual), 0);
                sharedPreferences.edit().
                        putString(getString(R.string.key_current_picture), fileName).
                        putInt(getString(R.string.key_amount_changes_manual), ++amountChangesManual).
                        apply();
            }
        });

        textViewCurrentWallpaper = requireView().findViewById(R.id.textview_current_wallpaper);
        textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), "-"));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if uri is still valid
        if (!MyFileHandler.isWallpaperDirValid(requireContext())) {
            buttonSetRandomWallpaper.setEnabled(false);
        }
    }
}