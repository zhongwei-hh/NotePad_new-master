#  NotepadMaster 笔记应用

## 简介
NotepadMaster 是一款基于 Google Notepad Master 开发的安卓笔记应用，旨在帮助用户高效管理个人笔记。应用主要提供了笔记的基本管理功能，包括记录笔记时间戳、搜索功能、笔记分类和标签管理等，便于用户查看历史记录并快速定位需要的信息。

## 功能特性

### 基础功能
- **时间戳**：每次保存笔记时自动生成时间戳，方便记录笔记时间。
- **搜索功能**：快速搜索笔记内容，帮助你高效找到所需信息。

### 2. 拓展功能
 
* UI美化   
* 笔记收藏
* 导出笔记  

## 功能实现
### ⏳ 显示时间戳
**功能描述**
应用会自动在每次保存笔记时记录修改时间。时间戳格式为 yyyy-MM-dd HH:mm，例如：2024-12-01 14:30，以方便追踪笔记的修改历史。
在笔记列表中，每条笔记都会显示它的最后修改时间，帮助你快速查看最新的笔记。

实现原理：

1.找到notelist_item.xml布局文件中添加TextView 

（添加新的TextView时应添加垂直约束——android:gravity="center_vertical"）
```
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">
    >
    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/text1"
        android:layout_width="match_parent"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:singleLine="true"
        />

    <TextView
        android:id="@android:id/text2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dp"
        android:singleLine="true" />
```

2.在NoteList类的PROJECTION中添加COLUMN_NAME_MODIFICATION_DATE字段
```
private static final String[] PROJECTION = new String[] {
        NotePad.Notes._ID, // 0
        NotePad.Notes.COLUMN_NAME_TITLE, // 1
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//在这里加入了修改时间的显示
};
```

3.修改适配器内容，在NoteList类增加dataColumns中装配到ListView的内容，所以要同时增加一个ID标识来存放该时间参数
```
// The names of the cursor columns to display in the view, initialized to the title column
String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE  } ;

// The view IDs that will display the cursor columns, initialized to the TextView in
// noteslist_item.xml
int[] viewIDs = { android.R.id.text1 ,android.R.id.text2};
```

4.在NoteEditor类的updateNote方法中获取当前系统的时间，并对时间进行格式化
```
// Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        Long now = Long.valueOf(System.currentTimeMillis());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(now);
        String format = sf.format(d);
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, format);
```

效果图展示：
![image](https://github.com/user-attachments/assets/f98d2ab9-f224-409b-85cf-8480790ecb57)


### 🔍 搜索功能
**功能描述**
      在主界面中，点击搜索按钮，输入关键字即可检索笔记标题。支持模糊查询功能，无需输入完整标题即可找到相关笔记，适合大量笔记管理。

   **实现原理**：  
   - 在 SQLite 中使用 `LIKE` 语句进行关键字匹配查询。  
   - 搜索结果会实时更新，并通过 Cursor 加载数据到 UI 层展示。
     
**代码实现**

1.搜索组件在主页面的菜单选项中，所以选择在res—menu—list_options_menu.xml布局文件中添加搜索功能
新增menu_search
```
<item
    android:id="@+id/menu_search"
    android:icon="@android:drawable/ic_menu_search"
    android:title="@string/menu_search"
    android:showAsAction="always" />
```
2.在res—layout中新建一个查找笔记内容的布局文件note_search.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        />
    <ListView
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />
</LinearLayout>

```
3.在NoteList类中的onOptionsItemSelected方法中新增search查询的处理(跳转)
```
case R.id.menu_search:
    //查找功能
    Intent intent = new Intent(this, NoteSearch.class);
    this.startActivity(intent);
    return true;
```
4.新建一个NoteSearch类用于search功能的功能实现

package com.example.android.notepad;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteSearch extends Activity implements SearchView.OnQueryTextListener
{
    ListView listView;
    SQLiteDatabase sqLiteDatabase;
    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//时间
    };

    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "您选择的是："+query, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);
        SearchView searchView = findViewById(R.id.search_view);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        listView = findViewById(R.id.list_view);
        sqLiteDatabase = new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
        //设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);

        //设置该SearchView内默认显示的提示文本
        searchView.setQueryHint("查找");
        searchView.setOnQueryTextListener(this);

    }
    public boolean onQueryTextChange(String string) {
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?";
        String[] selection2 = {"%"+string+"%","%"+string+"%"};
        Cursor cursor = sqLiteDatabase.query(
                NotePad.Notes.TABLE_NAME,
                PROJECTION, // The columns to return from the query
                selection1, // The columns for the where clause
                selection2, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                NotePad.Notes.DEFAULT_SORT_ORDER // The sort order
        );
        // The names of the cursor columns to display in the view, initialized to the title column
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
        } ;
        // The view IDs that will display the cursor columns, initialized to the TextView in
        // noteslist_item.xml
        int[] viewIDs = {
                android.R.id.text1,
                android.R.id.text2
        };
        // Creates the backing adapter for the ListView.
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // The Context for the ListView
                R.layout.noteslist_item,         // Points to the XML for a list item
                cursor,                           // The cursor to get items from
                dataColumns,
                viewIDs
        );
        // Sets the ListView's adapter to be the cursor adapter that was just created.
        listView.setAdapter(adapter);
        return true;
    }
}

5.搜索框效果展示

![image](https://github.com/user-attachments/assets/8131f51c-554a-4056-a67f-37bed2a91ec4)

![image](https://github.com/user-attachments/assets/ecd7da88-1be3-4260-b5c2-f4ac2535e49f)


### 文件导出

**功能描述**
在笔记菜单中选择export功能，笔记将会被以.txt文件的形式导出至storage/download下

**代码实现**

1.导出功能通过 menu_export 选项在菜单中触发
java
case R.id.menu_export:
    export();
    break;
2.在NoteEditor中添加函数outputNote()：

//跳转导出笔记的activity，将uri信息传到新的activity

    private final void outputNote() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,OutputText.class);
        NoteEditor.this.startActivity(intent);
 }

3.要对选择导出文件界面进行布局，新建布局output_text.xml，垂直线性布局放置EditText和Button:

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="6dip"
    android:paddingRight="6dip"
    android:paddingBottom="3dip">
    <EditText android:id="@+id/output_name"
        android:maxLines="1"
        android:layout_marginTop="2dp"
        android:layout_marginBottom="15dp"
        android:layout_width="wrap_content"
        android:ems="25"
        android:layout_height="wrap_content"
        android:autoText="true"
        android:capitalize="sentences"
        android:scrollHorizontally="true" />
    <Button android:id="@+id/output_ok"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="right"
        android:text="@string/output_ok"
        android:onClick="OutputOk" />
</LinearLayout>
4.创建OutputText的Acitvity:

public class OutputText extends Activity {

   //要使用的数据库中笔记的信息
   
    private static final String[] PROJECTION = new String[] {
    
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_NOTE, // 2
            NotePad.Notes.COLUMN_NAME_CREATE_DATE, // 3
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 4
            
    };

    
    //读取出的值放入这些变量
    
    private String TITLE;
    private String NOTE;
    private String CREATE_DATE;
    private String MODIFICATION_DATE;
    //读取该笔记信息
    private Cursor mCursor;
    //导出文件的名字
    private EditText mName;
    //NoteEditor传入的uri，用于从数据库查出该笔记
    private Uri mUri;
    //关于返回与保存按钮的一个特殊标记，返回的话不执行导出，点击按钮才导出
    private boolean flag = false;
    
    private static final int COLUMN_INDEX_TITLE = 1;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_text);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,        // The URI for the note that is to be retrieved.
                PROJECTION,  // The columns to retrieve
                null,        // No selection criteria are used, so no where columns are needed.
                null,        // No where columns are used, so no where values are needed.
                null         // No sort order is needed.
        );
        mName = (EditText) findViewById(R.id.output_name);
    }
    
    @Override
    
    protected void onResume(){
        super.onResume();
        if (mCursor != null) {
            // The Cursor was just retrieved, so its index is set to one record *before* the first
            // record retrieved. This moves it to the first record.
            mCursor.moveToFirst();
            //编辑框默认的文件名为标题，可自行更改
            mName.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
        //从mCursor读取对应值
            TITLE = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE));
            NOTE = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE));
            CREATE_DATE = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CREATE_DATE));
            MODIFICATION_DATE = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            //flag在点击导出按钮时会设置为true，执行写文件
            if (flag == true) {
                write();
            }
            flag = false;
        }
    }
    
    public void OutputOk(View v){
        flag = true;
        finish();
    }
    
5.在AndroidManifest.xml中将这个Acitvity主题定义为对话框样式，并且加入权限：

<!--添加导出activity-->
        <activity android:name="OutputText"
            android:label="@string/output_name"
            android:theme="@android:style/Theme.Holo.Dialog"
            android:windowSoftInputMode="stateVisible">
        </activity>

效果展示：

![image](https://github.com/user-attachments/assets/4f820dc2-fe77-465d-801b-860bb28b5246)


![image](https://github.com/user-attachments/assets/661ea09d-5fa1-4373-bde5-9f7edf311e42)


### UI美化
#### 内容映射至笔记上
**功能描述**
能够在笔记首页浏览到笔记内容开头

**代码实现**
在 AndroidManifest.xml 中为 NotesList 活动添加主题属性：

<activity android:name=".NotesList"
          android:theme="@android:style/Theme.Holo.Light"
          android:label="@string/title_notes_list">
</activity>

2.修改 NotesList.java 中的 onOptionsItemSelected 方法：

@Override

public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
    
        case R.id.menu_theme:
        
             //获取当前主题设置
             
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean isDarkTheme = settings.getBoolean(THEME_KEY, false);
                
                //切换主题并保存设置
                
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(THEME_KEY, !isDarkTheme);
                editor.apply();
                
                //重新创建活动以应用新主题
                
                recreate();
                return true;
                
        //其他保持不变
    }
}


保存主题设置以便在应用重启时保持选择的主题,在 NotesList.java 中添加主题设置的保存和读取

public class NotesList extends ListActivity {
    private static final String PREFS_NAME = "NotepadPrefs";
    private static final String THEME_KEY = "theme_key";
 @Override
    protected void onCreate(Bundle savedInstanceState) {
    
        //在 super.onCreate() 之前设置主题
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkTheme = settings.getBoolean(THEME_KEY, false);
        setTheme(isDarkTheme ? android.R.style.Theme_Holo : android.R.style.Theme_Holo_Light);
        
        super.onCreate(savedInstanceState);
        
        //...其余代码保持不变
    }

效果展示：

![image](https://github.com/user-attachments/assets/e669e413-74e2-4897-b252-4efbfc72ef33)

![image](https://github.com/user-attachments/assets/ea2058dd-3a2d-4052-b18a-6baae2c6205b)

![image](https://github.com/user-attachments/assets/d6c1af75-2b8f-4c7a-9351-466e6143e8ff)


### ⏳ 笔记收藏
1.首先在 NotePad.java 中添加收藏状态列:
```
public static final String COLUMN_NAME_FAVORITE = "favorite";
```
2.修改 NotePadProvider.java 中的数据库创建语句:
```
static class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;  // 增加版本号

    private static final String DATABASE_CREATE =
        "CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
            + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
            + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
            + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
            + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
            + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
            + NotePad.Notes.COLUMN_NAME_TODO_STATUS + " INTEGER DEFAULT 0,"
            + NotePad.Notes.COLUMN_NAME_FAVORITE + " INTEGER DEFAULT 0"
            + ");";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + NotePad.Notes.TABLE_NAME + 
                    " ADD COLUMN " + NotePad.Notes.COLUMN_NAME_FAVORITE + 
                    " INTEGER DEFAULT 0");
            } catch (Exception e) {
                // 列可能已经存在，忽略错误
            }
        }
    }
}
```
3.在 list_options_menu.xml 中添加收藏菜单项:
```
<item
    android:id="@+id/menu_favorites"
    android:icon="@android:drawable/btn_star"
    android:title="收藏夹"
    android:showAsAction="always" />
```
4.创建新的 FavoritesList.java Activity:
```
public class FavoritesList extends ListActivity {
    private static final String[] PROJECTION = new String[] {
        NotePad.Notes._ID,
        NotePad.Notes.COLUMN_NAME_TITLE,
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
        NotePad.Notes.COLUMN_NAME_FAVORITE
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String selection = NotePad.Notes.COLUMN_NAME_FAVORITE + "=1";
        Cursor cursor = managedQuery(
            NotePad.Notes.CONTENT_URI,
            PROJECTION,
            selection,
            null,
            NotePad.Notes.DEFAULT_SORT_ORDER
        );
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item,
            cursor,
            new String[] { NotePad.Notes.COLUMN_NAME_TITLE },
            new int[] { android.R.id.text1 }
        );
        setListAdapter(adapter);
    }
}
```
5.在 NotesList.java 的 onOptionsItemSelected 方法中添加处理:
```
case R.id.menu_favorites:
    Intent intent = new Intent(this, FavoritesList.class);
    favoritesIntent.setData(getIntent().getData());
    startActivity(intent);
    return true;
```
6.在 AndroidManifest.xml 中注册新 Activity:
```
<activity
    android:name=".FavoritesList"
    android:label="收藏夹"
    android:exported="true" />
```
7.修改 NotesList.java 中的上下文菜单,添加收藏功能:
```
case R.id.context_favorite:
    ContentValues values = new ContentValues();
    values.put(NotePad.Notes.COLUMN_NAME_FAVORITE, 1);
    getContentResolver().update(noteUri, values, null, null);
    Toast.makeText(this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
    return true;
```
8.修改 NotesList.java 中的 PROJECTION：
```
private static final String[] PROJECTION = new String[] {
    NotePad.Notes._ID,
    NotePad.Notes.COLUMN_NAME_TITLE,
    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
    NotePad.Notes.COLUMN_NAME_TODO_STATUS,
    NotePad.Notes.COLUMN_NAME_FAVORITE
};
```
9.在 NotePadProvider.java 中添加收藏列的映射：
```
static {
    sNotesProjectionMap = new HashMap<String, String>();
    sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_TITLE);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, NotePad.Notes.COLUMN_NAME_CREATE_DATE);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TODO_STATUS, NotePad.Notes.COLUMN_NAME_TODO_STATUS);
    sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_FAVORITE, NotePad.Notes.COLUMN_NAME_FAVORITE);
}
```
10.在 NotesList.java 中添加批量收藏相关的变量和方法：
```
private boolean isInBatchMode = false;
private Set<Long> selectedNotes = new HashSet<>();
private Menu mMenu;

 private void toggleBatchMode() {
        isInBatchMode = !isInBatchMode;
        MenuItem batchDeleteItem = mMenu.findItem(R.id.menu_batch_delete);
        MenuItem batchFavoriteItem = mMenu.findItem(R.id.menu_favorites);

        batchDeleteItem.setVisible(isInBatchMode);
        batchFavoriteItem.setIcon(isInBatchMode ? android.R.drawable.ic_menu_add : android.R.drawable.btn_star);
        batchFavoriteItem.setTitle(isInBatchMode ? "添加到收藏" : "收藏夹");

        if (!isInBatchMode) {
            selectedNotes.clear();
        }
        invalidateOptionsMenu();
        getListView().invalidateViews();
    }
```
11.修改 onOptionsItemSelected 中的收藏处理：
```
case R.id.menu_favorites:
                if (isInBatchMode) {
                    if (selectedNotes.isEmpty()) {
                        Toast.makeText(this, "请先选择要收藏的笔记", Toast.LENGTH_SHORT).show();
                    } else {
                        StringBuilder selection = new StringBuilder();
                        selection.append(NotePad.Notes._ID).append(" IN (");
                        String[] selectionArgs = new String[selectedNotes.size()];

                        int i = 0;
                        for (Long noteId : selectedNotes) {
                            if (i > 0) {
                                selection.append(",");
                            }
                            selection.append("?");
                            selectionArgs[i] = String.valueOf(noteId);
                            i++;
                        }
                        selection.append(")");

                        ContentValues values = new ContentValues();
                        values.put(NotePad.Notes.COLUMN_NAME_FAVORITE, 1);
                        getContentResolver().update(
                                NotePad.Notes.CONTENT_URI,
                                values,
                                selection.toString(),
                                selectionArgs
                        );

                        Toast.makeText(this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
                        selectedNotes.clear();
                    }
                    toggleBatchMode();
                } else {
                    Intent favoritesIntent = new Intent(this, FavoritesList.class);
                    favoritesIntent.setData(getIntent().getData());
                    startActivity(favoritesIntent);
                }
                return true;
```
12.修改 SimpleCursorAdapter 的 bindView 方法:
```
@Override
public void bindView(View view, Context context, Cursor cursor) {
    super.bindView(view, context, cursor);
    
    CheckBox selectCheckBox = view.findViewById(R.id.select_checkbox);
    long noteId = cursor.getLong(cursor.getColumnIndex(NotePad.Notes._ID));
    
    selectCheckBox.setVisibility(isInBatchMode ? View.VISIBLE : View.GONE);
    selectCheckBox.setChecked(selectedNotes.contains(noteId));
    
    selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
            selectedNotes.add(noteId);
        } else {
            selectedNotes.remove(noteId);
        }
    });
}
```
13.在实现收藏夹的基础上，实现在收藏夹里，可以继续点击进行修改，需要在FavoritesList.java 中，添加点击事件处理。在 onCreate 方法中添加以下代码：
```
@Override
protected void onListItemClick(ListView l, View v, int position, long id) {
    // 构建笔记的 URI
    Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
    // 启动编辑活动
    startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
}
```

效果展示：

![image](https://github.com/user-attachments/assets/b9bba0f5-cfd3-465b-96b1-eff7688bc7c3)

![image](https://github.com/user-attachments/assets/b0bb6fdd-fb50-413a-8d78-83318152a476)


### 作者：张鸿伟
