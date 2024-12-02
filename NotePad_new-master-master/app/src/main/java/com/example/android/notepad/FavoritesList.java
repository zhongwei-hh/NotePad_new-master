package com.example.android.notepad;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.app.ListActivity;

public class FavoritesList extends ListActivity {
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
    };
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // 构建笔记的 URI
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);

        // 启动编辑活动
        startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }


        String selection = NotePad.Notes.COLUMN_NAME_FAVORITE + "=1";
        Cursor cursor = managedQuery(
                getIntent().getData(),    // URI
                PROJECTION,              // 投影
                selection,               // 选择条件
                null,                    // 选择参数
                NotePad.Notes.DEFAULT_SORT_ORDER  // 排序
        );

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                new String[] {
                        NotePad.Notes.COLUMN_NAME_TITLE,
                        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
                },
                new int[] {
                        android.R.id.text1,
                        android.R.id.text2
                }
        );
        setListAdapter(adapter);
    }


}