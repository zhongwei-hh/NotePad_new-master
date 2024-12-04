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
             // 获取当前主题设置
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean isDarkTheme = settings.getBoolean(THEME_KEY, false);
                
                // 切换主题并保存设置
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(THEME_KEY, !isDarkTheme);
                editor.apply();
                
                // 重新创建活动以应用新主题
                recreate();
                return true;
        // 其他保持不变
    }
}


保存主题设置以便在应用重启时保持选择的主题,在 NotesList.java 中添加主题设置的保存和读取

public class NotesList extends ListActivity {
    private static final String PREFS_NAME = "NotepadPrefs";
    private static final String THEME_KEY = "theme_key";
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 super.onCreate() 之前设置主题
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkTheme = settings.getBoolean(THEME_KEY, false);
        setTheme(isDarkTheme ? android.R.style.Theme_Holo : android.R.style.Theme_Holo_Light);
        
        super.onCreate(savedInstanceState);
        // ... 其余代码保持不变
    }

效果展示：

![image](https://github.com/user-attachments/assets/e669e413-74e2-4897-b252-4efbfc72ef33)

![image](https://github.com/user-attachments/assets/ea2058dd-3a2d-4052-b18a-6baae2c6205b)

![image](https://github.com/user-attachments/assets/d6c1af75-2b8f-4c7a-9351-466e6143e8ff)


### ⏳ 笔记收藏
// 1. 在NotePad.java中添加收藏状态列
// 定义一个表示收藏状态的常量字符串，用于在数据库表结构中标识收藏列
public static final String COLUMN_NAME_FAVORITE = "favorite";

// 2. 修改NotePadProvider.java中的数据库创建语句
static class DatabaseHelper extends SQLiteOpenHelper {
    // 定义数据库名称
    private static final String DATABASE_NAME = "notes.db";
    // 增加数据库版本号，这里设置为3，用于后续数据库结构变更时的版本控制
    private static final int DATABASE_VERSION = 3;  

    // 构建创建数据库表的SQL语句
    // 此语句用于创建存储笔记信息的表，包含了笔记的各种属性列，如ID、标题、内容、创建日期、修改日期、待办状态等，同时添加了收藏状态列，并设置默认值为0（表示未收藏）
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

    // 当数据库版本升级时的处理方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果旧版本小于3，说明可能需要添加收藏列到已存在的表中
        if (oldVersion < 3) {
            try {
                // 执行SQL语句，向表中添加收藏列，并设置默认值为0
                db.execSQL("ALTER TABLE " + NotePad.Notes.TABLE_NAME + 
                    " ADD COLUMN " + NotePad.Notes.COLUMN_NAME_FAVORITE + 
                    " INTEGER DEFAULT 0");
            } catch (Exception e) {
                // 列可能已经存在，忽略错误，因为添加重复列会抛出异常，这里捕获并忽略即可
                // 比如在多次执行升级操作时，可能会出现列已经添加过的情况
            }
        }
    }
}

// 3. 在list_options_menu.xml中添加收藏菜单项
// 定义一个菜单项，用于在界面上显示收藏夹相关操作的入口
// 设置了菜单项的ID、图标（使用安卓系统自带的星星图标）、标题（显示为“收藏夹”）以及显示方式（总是显示在界面上）
<item
    android:id="@+id/menu_favorites"
    android:icon="@android:drawable/btn_star"
    android:title="收藏夹"
    android:showAsAction="always" />

// 4. 创建新的FavoritesList.java Activity
public class FavoritesList extends ListActivity {
    // 定义查询投影，用于指定从数据库中查询哪些列的数据
    // 这里查询了笔记的ID、标题、修改日期以及收藏状态列，以便在收藏夹列表中展示相关信息
    private static final String[] PROJECTION = new String[] {
        NotePad.Notes._ID,
        NotePad.Notes.COLUMN_NAME_TITLE,
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
        NotePad.Notes.COLUMN_NAME_FAVORITE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置查询条件，只查询收藏状态为1（表示已收藏）的笔记记录
        String selection = NotePad.Notes.COLUMN_NAME_FAVORITE + "=1";
        // 执行查询操作，通过ContentResolver从指定的Content URI获取满足条件的游标数据
        Cursor cursor = managedQuery(
            NotePad.Notes.CONTENT_URI,
            PROJECTION,
            selection,
            null,
            NotePad.Notes.DEFAULT_SORT_ORDER
        );

        // 创建一个简单游标适配器，用于将游标数据绑定到列表视图的布局上
        // 指定了当前上下文、列表项布局资源、游标数据、要显示的列数据以及对应的视图ID
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item,
            cursor,
            new String[] { NotePad.Notes.COLUMN_NAME_TITLE },
            new int[] { android.R.id.text1 }
        );
        // 将适配器设置给列表视图，以便在界面上展示查询到的收藏夹笔记列表
        setListAdapter(adapter);
    }
}

// 5. 在NotesList.java的onOptionsItemSelected方法中添加处理
// 当点击了收藏夹菜单项（ID为R.id.menu_favorites）时的处理逻辑
case R.id.menu_favorites:
    // 创建一个意图，用于启动FavoritesList这个Activity
    Intent intent = new Intent(this, FavoritesList.class);
    // 设置意图的数据，这里使用了当前Activity接收到的原始数据（比如可能是某个笔记的相关数据）
    favoritesIntent.setData(getIntent().getData());
    // 启动目标Activity
    startActivity(intent);
    return true;

 6. 在AndroidManifest.xml中注册新Activity
    
 注册FavoritesList这个Activity，设置了它的名称、标签（显示在界面顶部的标题）以及是否可导出（这里设置为可导出，以便其他应用可以访问）
<activity
    android:name=".FavoritesList"
    android:label="收藏夹"
    android:exported="true" />

 7. 修改NotesList.java中的上下文菜单，添加收藏功能

 当点击了上下文菜单中的收藏菜单项（ID为R.id.context_favorite）时的处理逻辑
 
case R.id.context_favorite:
    // 创建一个ContentValues对象，用于存储要更新到数据库的值
    ContentValues values = new ContentValues();
    // 将收藏状态列的值设置为1，表示将当前笔记标记为已收藏
    values.put(NotePad.Notes.COLUMN_NAME_FAVORITE, 1);
    // 通过ContentResolver执行更新操作，将指定笔记的收藏状态更新到数据库中
    getContentResolver().update(noteUri, values, null, null);
    // 在界面上显示一个短暂的提示消息，告知用户已添加到收藏夹
    Toast.makeText(this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
    return true;

 8. 修改NotesList.java中的PROJECTION

  重新定义查询投影，添加了收藏状态列到要查询的列列表中
  这样在后续的查询操作中就可以获取到笔记的收藏状态信息，以便进行相关的业务逻辑处理，比如判断是否已收藏等
  
private static final String[] PROJECTION = new String[] {
    NotePad.Notes._ID,
    NotePad.Notes.COLUMN_NAME_TITLE,
    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
    NotePad.Notes.COLUMN_NAME_TODO_STATUS,
    NotePad.Notes.COLUMN_NAME_FAVORITE
};

  9. 在NotePadProvider.java中添加收藏列的映射

  静态代码块，用于初始化一个哈希表，将数据库表中的列名与对应的属性名进行映射
  这里添加了收藏状态列的映射，确保在进行数据查询、更新等操作时能够正确地对应到数据库中的列

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

 10. 在NotesList.java中添加批量收藏相关的变量和方法


//定义一个布尔变量，用于标识是否处于批量操作模式，初始值为false，表示未处于批量模式
private boolean isInBatchMode = false;
//创建一个集合，用于存储在批量操作模式下被选中的笔记的ID
private Set<Long> selectedNotes = new HashSet<>();
//保存当前菜单对象的引用，以便在后续操作中对菜单进行修改等处理
private Menu mMenu;

//定义一个方法，用于切换批量操作模式
//当调用此方法时，会改变isInBatchMode的值，并根据新的值来设置相关菜单项的可见性、图标和标题等属性
//同时，如果退出批量模式，会清空已选中的笔记集合，并刷新菜单和列表视图

private void toggleBatchMode() {
        isInBatchMode =!isInBatchMode;
        MenuItem batchDeleteItem = mMenu.findItem(R.id.menu_batch_delete);
        MenuItem batchFavoriteItem = mMenu.findItem(R.id.menu_favorites);

        batchDeleteItem.setVisible(isInBatchMode);
        batchFavoriteItem.setIcon(isInBatchMode? android.R.drawable.ic_menu_add : android.R.drawable.btn_star);
        batchFavoriteItem.setTitle(isInBatchMode? "添加到收藏" : "收藏夹");

        if (!isInBatchMode) {
            selectedNotes.clear();
        }
        invalidateOptionsMenu();
        getListView().invalidateViews();
}

  11. 修改onOptionsItemSelected中的收藏处理
//当点击了收藏夹菜单项（ID为R.id.menu_favorites）时的处理逻辑，这里区分了是否处于批量操作模式

case R.id.menu_favorites:
                if (isInBatchMode) {
                    // 如果处于批量模式，首先判断是否有选中的笔记
                    if (selectedNotes.isEmpty()) {
                        // 如果没有选中任何笔记，显示提示消息告知用户先选择要收藏的笔记
                        Toast.makeText(this, "请先选择要收藏的笔记", Toast.LENGTH_SHORT).show();
                    } else {
                        // 如果有选中的笔记，构建一个用于更新数据库的查询条件字符串
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

                        // 创建一个ContentValues对象，用于存储要更新到数据库的值，这里将收藏状态设置为1
                        ContentValues values = new ContentValues();
                        values.put(NotePad.Notes.COLUMN_NAME_FAVORITE, 1);
                        // 通过ContentResolver执行更新操作，根据构建的查询条件和参数，将选中笔记的收藏状态更新到数据库中
                        getContentResolver().update(
                                NotePad.Notes.CONTENT_URI,
                                values,
                                selection.toString(),
                                selectionArgs
                        );

                        // 在界面上显示一个短暂的提示消息，告知用户已添加到收藏夹
                        Toast.makeText(this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
                        // 清空已选中的笔记集合，以便下次进行新的批量操作
                        selectedNotes.clear();
                    }
                    // 切换回非批量操作模式，更新菜单和列表视图的显示状态
                    toggleBatchMode();
                } else {
                    // 如果不处于批量模式，创建一个意图，用于启动FavoritesList这个Activity
                    Intent favoritesIntent = new Intent(this, FavoritesList.class);
                    // 设置意图的数据，这里使用了当前Activity接收到的原始数据（比如可能是某个笔记的相关数据）
                    favoritesIntent.setData(getIntent().getData());
                    // 启动目标Activity
                    startActivity(favoritesIntent);
                }
                return true;

 12. 修改SimpleCursorAdapter的bindView方法
 重写SimpleCursorAdapter的bindView方法，用于在将游标数据绑定到列表视图的每个项时进行额外的处理

@Override
public void bindView(View view, Context context, Cursor cursor) {
    super.bindView(view, context, cursor);

    //在列表视图的每个项中找到对应的复选框视图

    CheckBox selectCheckBox = view.findViewById(R.id.select_checkbox);
    
    //获取当前笔记的ID
    long noteId = cursor.getLong(cursor.getColumnIndex(NotePad.Notes._ID));

    //根据是否处于批量操作模式，设置复选框的可见性
    selectCheckBox.setVisibility(isInBatchMode? View.VISIBLE : View.GONE);
    //判断当前笔记的ID是否在已选中的笔记集合中，如果是，则将复选框设置为选中状态
    selectCheckBox.setChecked(selectedNotes.contains(noteId));

    //设置复选框的选中状态改变监听器
    //当复选框的选中状态发生改变时，根据新的选中状态，在已选中的笔记集合中添加或移除对应的笔记ID
    selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
            selectedNotes.add(noteId);
        } else {
            selectedNotes.remove(noteId);
        }
    });
}

13. 在实现收藏夹的基础上，实现在收藏夹里，可以继续点击进行修改，需要在FavoritesList.java中，添加点击事件处理。在onCreate方法中添加以下代码：

@Override
protected void onListItemClick(ListView l, View v, int position, long id) {
    //构建笔记的URI，通过ContentUris工具类，将原始数据的URI和当前点击的笔记ID组合起来
    Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
    //启动编辑活动，创建一个意图，指定动作为ACTION_EDIT，并传入构建好的笔记URI
    startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
}

效果展示：

![image](https://github.com/user-attachments/assets/b9bba0f5-cfd3-465b-96b1-eff7688bc7c3)

![image](https://github.com/user-attachments/assets/b0bb6fdd-fb50-413a-8d78-83318152a476)


### 作者：张鸿伟
