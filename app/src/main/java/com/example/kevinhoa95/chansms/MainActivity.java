package com.example.kevinhoa95.chansms;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity {

    SQLiteDatabase sqlite;
    Button btnSave;
    EditText etNum;
    ListView lvPhoneBlock;

    ArrayList<String> arrPreNum;
    ArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost.TabSpec spec = getTabHost().newTabSpec("Tag1");
        spec.setContent(R.id.main);
        spec.setIndicator("Main");
        getTabHost().addTab(spec);

        spec = getTabHost().newTabSpec("tag2");
        spec.setContent(R.id.lvDanhSach);
        spec.setIndicator("Các số đã chặn");
        getTabHost().addTab(spec);
        getTabHost().setCurrentTab(0);

        createDatabase();
        getControl();
        addEvent();
        loaddata();
    }


    //Cập nhật dữ liệu mới
    void loaddata() {
        try {
            arrPreNum = new ArrayList<>();
            sqlite = openOrCreateDatabase("danhsach.db", MODE_PRIVATE, null);
            final String[] columns = {"id", "prefixNum"};
            Cursor cusor = sqlite.query("ListPhoneBlock", columns, null, null, null, null, null);
            cusor.moveToFirst();
            String data;
            while (!cusor.isAfterLast()) {
                data = cusor.getString(1);
                arrPreNum.add(data);
                cusor.moveToNext();
            }
            adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, arrPreNum);
            lvPhoneBlock.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public void cleartext() {
        etNum.setText("");
    }

    //Khai báo các control
    public void getControl() {
        btnSave = (Button) findViewById(R.id.btnLuuSo);
        etNum = (EditText) findViewById(R.id.etPhoneBlock);
        lvPhoneBlock = (ListView) findViewById(R.id.lvDanhSach);
    }

    //Bắt các sự kiện
    public void addEvent() {
        btnSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etNum.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Lỗi!");
                    builder.setMessage("Hãy nhập vào số điện thoại cần chặn");
                    builder.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                } else {
                    insertData(etNum.getText().toString());
                    loaddata();
                }
            }
        });

        lvPhoneBlock.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNum.setText(arrPreNum.get(position));
            }
        });

        //Xóa số khi không cần chặn nữa
        lvPhoneBlock.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder bui = new AlertDialog.Builder(MainActivity.this);
                bui.setTitle("Cảnh báo!");
                bui.setMessage("Bạn có muốn xóa số này không?");
                final String vitri = arrPreNum.get(position).trim();
                bui.setPositiveButton("Có", new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqlite = openOrCreateDatabase("danhsach.db", MODE_PRIVATE, null);
                        if (sqlite.delete("ListPhoneBlock", "prefixNum=?", new String[]{vitri}) != -1) {
                            loaddata();
                            cleartext();
                            Toast.makeText(MainActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Xóa thất bại !", Toast.LENGTH_SHORT).show();
                    }
                });
                bui.setNegativeButton("không", new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                bui.create().show();

                return false;
            }
        });
    }

    // Khởi tạo cơ sỏ dữ liệu
    public void createDatabase() {
        sqlite = openOrCreateDatabase("danhsach.db", MODE_PRIVATE, null);
        try {
            String query = "Create table ListPhoneBlock(id text primary key, prefixNum text)";
            sqlite.execSQL(query);
        } catch (Exception e) {
        }
    }

    //Thêm dữ liệu vào database
    public void insertData(String PrefixNum) {
        try {
            ContentValues _values = new ContentValues();
            _values.put("id", PrefixNum);
            _values.put("prefixNum", PrefixNum);
            if (sqlite.insert("ListPhoneBlock", null, _values) != -1) {
                Toast.makeText(MainActivity.this, "Thêm thành công !", Toast.LENGTH_SHORT).show();
                cleartext();
            } else
                Toast.makeText(MainActivity.this, "Số đã bị chặn!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Thêm thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:  return true;
            case R.id.action_huongdan: {
                Intent intent = new Intent(MainActivity.this, Help.class);
                startActivity(intent);
            }
            case R.id.action_exits: finish();
        }
        return super.onOptionsItemSelected(item);
    }
}