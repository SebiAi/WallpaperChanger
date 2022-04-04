package com.sebiai.wallpaperchanger;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.app.WallpaperManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MyFileHandler {

    public static ArrayList<Uri> getFiles(Context context, Uri treePath) {
        final ArrayList<Uri> uris = new ArrayList<>();

        // Get all files as Cursor
        DocumentFile dir = DocumentFile.fromTreeUri(context, treePath);
        Uri dirUri = Objects.requireNonNull(dir).getUri();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(dirUri,
                DocumentsContract.getDocumentId(dirUri));
        final Cursor c = context.getContentResolver().query(childrenUri, new String[] {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        }, null, null, null);

        if (c == null)
            return uris;

        // Get files
        while (c.moveToNext()) {
            // Only images
            if (c.getString(2).matches("image/.*")) {
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(dirUri,
                        c.getString(0));
                uris.add(documentUri);
            }
        }
        c.close();

        return uris;
    }

    public static DocumentFile getRandomFile(Context context, ArrayList<Uri> uris) {
        if (uris.size() != 0) {
            Random random = new Random();
            Uri randomUri = uris.get(random.nextInt(uris.size()));
            return DocumentFile.fromSingleUri(context, randomUri);
        }
        return null;
    }

    public static DocumentFile setRandomFileAsWallpaper(Context context) {
        // Get random
        DocumentFile file = getRandomFile(context, getFiles(context, getMyApplication(context).wallpaperDir));

        if (file != null) {
            // Set as Wallpaper
            try {
                MyFileHandler.setFileAsWallpaper(context, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void setFileAsWallpaper(Context context, DocumentFile file) throws IOException {
        InputStream stream = context.getContentResolver().openInputStream(file.getUri());
        WallpaperManager.getInstance(context).setStream(stream);
        stream.close();
    }

    public static boolean isWallpaperDirValid(Context context) {
        if (getMyApplication(context).wallpaperDir == null)
            return false;
        ArrayList<Uri> uris = getFiles(context, getMyApplication(context).wallpaperDir);
        return uris.size() != 0;
    }
}